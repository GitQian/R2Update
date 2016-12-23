package com.xinzhihui.r2update.Service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import com.xinzhihui.r2update.AppConfig;
import com.xinzhihui.r2update.MainActivity;
import com.xinzhihui.r2update.R;
import com.xinzhihui.r2update.Utils.LogUtil;
import com.xinzhihui.r2update.Utils.Md5Utils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;

import okhttp3.Call;
import okhttp3.Request;

public class DownLoadService extends Service {
    private final String TAG = this.getClass().getName();

    private Notification mNotification;
    Notification.Builder mBuilder;

    Messenger mClient;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AppConfig.DOWN_LOAD_START:
                    Message msgStart = new Message();
                    msgStart.what = AppConfig.DOWN_LOAD_START;
                    if (mClient != null) {
                        try {
                            mClient.send(msgStart);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case AppConfig.DOWN_LOAD_MSG:
                    Message msgProgress = new Message();
                    msgProgress.what = AppConfig.DOWN_LOAD_MSG;
                    msgProgress.arg1 = msg.arg1;
                    if (mClient != null) {
                        try {
                            mClient.send(msgProgress);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case AppConfig.DOWN_LOAD_FINISH:
                    Message msgFinish = new Message();
                    msgFinish.what = AppConfig.DOWN_LOAD_FINISH;
                    if (mClient != null) {
                        try {
                            mClient.send(msgFinish);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case AppConfig.DOWN_LOAD_ERROR:
                    Message msgError = new Message();
                    msgError.what = AppConfig.DOWN_LOAD_ERROR;
                    if (mClient != null) {
                        try {
                            mClient.send(msgError);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case AppConfig.MSG_REGISTER_CLIENT:
                    mClient = msg.replyTo;
                    break;

                default:
                    break;
            }
        }
    }

    private Handler mHandler = new IncomingHandler();
    Messenger mMessenger = new Messenger(mHandler);

    public DownLoadService() {
    }

    @Override
    public void onCreate() {
        LogUtil.d(TAG, "DownLoadService onCreate()");
        // 在API11之后构建Notification的方式
        mBuilder = new Notification.Builder(this); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, MainActivity.class);
        nfIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mBuilder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher)) // 设置下拉列表中的图标(大图标)
                .setContentTitle("正在升级...") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText("触摸可显示录制界面") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

        mNotification = mBuilder.build(); // 获取构建好的Notification
        mNotification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.d(TAG, "DownLoadService onStartCommand()");
        //TODO: MD5校验
        String md5Str = intent.getStringExtra("MD5STR");
        String md5OldStr = null;
        File file = new File("/sdcard/test.png");
        if (file.exists()) {
            md5OldStr = Md5Utils.getFileMD5(file);
        }
        if (md5OldStr != null && md5OldStr.equals(md5Str)) {
            //直接升级，不必下载
        }

        downLoad();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.d(TAG, "DownLoadService onBind()");
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.d(TAG, "DownLoadService onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "DownLoadService onDestroy()");
        super.onDestroy();
    }


    private void downLoad() {
        OkHttpUtils.get()
                .url("http://download.easyicon.net/png/1182979/128/")
                .build()
                .execute(new FileCallBack(Environment.getExternalStorageDirectory().getAbsolutePath(), "test.png") {
                    @Override
                    public void inProgress(float progress, long total, int id) {
                        //TODO: 进度
                        super.inProgress(progress, total, id);
                        Log.e("qiansheng", "progress" + (int) (progress / total) * 100);

                        Message message = new Message();
                        message.what = AppConfig.DOWN_LOAD_MSG;
                        message.arg1 = (int) (progress / total) * 100;
                        mHandler.sendMessage(message);
                    }

                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        startForeground(111, mNotification);

                        Message message = new Message();
                        message.what = AppConfig.DOWN_LOAD_START;
                        mHandler.sendMessage(message);
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        Log.e("qiansheng", "download onError!!!");
                        stopForeground(true);
                        Toast.makeText(DownLoadService.this, "下载失败！", Toast.LENGTH_LONG).show();

                        Message message = new Message();
                        message.what = AppConfig.DOWN_LOAD_ERROR;
                        mHandler.sendMessage(message);

                        stopSelf();
                    }

                    @Override
                    public void onResponse(File file, int i) {
                        Log.e("qiansheng", "download onResponse :" + file.getAbsolutePath());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    //TODO: 升级
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                stopForeground(true);
                            }
                        }).start();

                        Toast.makeText(DownLoadService.this, "下载完成！正在升级...", Toast.LENGTH_LONG).show();

                        Message message = new Message();
                        message.what = AppConfig.DOWN_LOAD_FINISH;
                        mHandler.sendMessage(message);

//                        stopSelf();
                    }
                });
    }
}