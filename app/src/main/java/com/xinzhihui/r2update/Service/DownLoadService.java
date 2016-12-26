package com.xinzhihui.r2update.Service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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

    public Handler mHandler;

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
                .setContentTitle("正在下载升级...") // 设置下拉列表里的标题
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
        File file = new File(AppConfig.FILE_PATH);
        if (file.exists()) {
            md5OldStr = Md5Utils.getFileMD5(file);
        }
        if (md5OldStr != null && md5OldStr.equals(md5Str)) {
            //直接升级，不必下载
            Toast.makeText(this, "正在升级...", Toast.LENGTH_LONG).show();

        }

        downLoad();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.d(TAG, "DownLoadService onBind()");
        return new MyBinder();
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
                .url("http://fileservice.365car.com.cn:88/fileService/downloads/appstore/365car_android.apk")
                .build()
                .execute(new FileCallBack(AppConfig.DIR_PATH, AppConfig.FILE_NAME) {
                    @Override
                    public void inProgress(float progress, long total, int id) {
                        //TODO: 进度
                        super.inProgress(progress, total, id);
                        sendMsg(AppConfig.DOWN_LOAD_MSG, (int) (progress * 100));
                    }

                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        startForeground(111, mNotification);

                        sendMsg(AppConfig.DOWN_LOAD_START);
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        Log.e("qiansheng", "download onError!!!");
                        stopForeground(true);
                        Toast.makeText(DownLoadService.this, "下载失败！", Toast.LENGTH_LONG).show();

                        sendMsg(AppConfig.DOWN_LOAD_ERROR);
                        stopSelf();
                    }

                    @Override
                    public void onResponse(File file, int i) {
                        Log.e("qiansheng", "download onResponse :" + file.getAbsolutePath());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    sendMsg(AppConfig.UPDATE_START);
                                    //TODO: 升级
                                    Thread.sleep(5000);
                                    sendMsg(AppConfig.UPDATE_FINISH);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                stopForeground(true);
                                stopSelf();
                            }
                        }).start();

                        Toast.makeText(DownLoadService.this, "下载完成！正在升级...", Toast.LENGTH_LONG).show();

                        sendMsg(AppConfig.DOWN_LOAD_FINISH);
//                        stopSelf();
                    }
                });
    }

    public void sendMsg(int what) {
        if (mHandler != null) {
            Message message = new Message();
            message.what = what;
            mHandler.sendMessage(message);
        }
    }

    public void sendMsg(int what, int arg1) {
        if (mHandler != null) {
            Message message = new Message();
            message.what = what;
            message.arg1 = arg1;
            mHandler.sendMessage(message);
        }
    }

    public class MyBinder extends Binder {
        public DownLoadService getMyService() {
            return DownLoadService.this;
        }
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }
}
