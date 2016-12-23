package com.xinzhihui.r2update;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xinzhihui.r2update.Service.DownLoadService;
import com.xinzhihui.r2update.Utils.CommonUtils;
import com.xinzhihui.r2update.Utils.Md5Utils;
import com.xinzhihui.r2update.bean.CheckUpdateBean;
import com.xinzhihui.r2update.bean.CheckUpdateCallBack;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static Context mContext;

    private TextView mNetNoticeTv;
    private TextView mCurVersionTv;  //TODO: 最好有一个接口，能够获取到当前的版本号！
    private TextView mCheckStatusTv;
    private Button mCheckBtn;
    public Button mUpdateBtn;

    public ProgressDialog pd;

    private CheckUpdateBean mCheckUpdateBean;

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.get() == null || mActivity.get().pd == null) {
                return;
            }
            if (mActivity.get() != mContext || mActivity.get().isFinishing()) {
                return;
            }
            switch (msg.what) {
                case AppConfig.DOWN_LOAD_START:
                    if (!mActivity.get().isFinishing()) {
                        mActivity.get().pd.show();
                        mActivity.get().mUpdateBtn.setClickable(false);
                    }
                    break;

                case AppConfig.DOWN_LOAD_MSG:
                    if (!mActivity.get().isFinishing()) {
                        mActivity.get().pd.show();
                        int progress = msg.arg1;
                        mActivity.get().pd.setProgress(progress);
                        mActivity.get().mUpdateBtn.setClickable(false);
                    }
                    break;

                case AppConfig.DOWN_LOAD_FINISH:
                    if (!mActivity.get().isFinishing()) {
                        mActivity.get().pd.dismiss();
                    }
                    break;

                case AppConfig.DOWN_LOAD_ERROR:
                    if (!mActivity.get().isFinishing()) {
                        mActivity.get().pd.dismiss();
                        mActivity.get().mUpdateBtn.setClickable(true);
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private Handler mHandler = new MyHandler(this);

    Messenger mSender = null;
    Messenger mReceive = new Messenger(mHandler);

    private class MyConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            mSender = new Messenger(service);

            Message msg = new Message();
            msg.what = AppConfig.MSG_REGISTER_CLIENT;
            msg.replyTo = mReceive;

            try {
                mSender.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    private MyConnection mServiceConnection = new MyConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);

        initView();
//        checkUpdate();
//        downLoad();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pd.dismiss();
        try {
            unbindService(mServiceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        mNetNoticeTv = (TextView) findViewById(R.id.tv_main_net_notice);
        mCurVersionTv = (TextView) findViewById(R.id.tv_main_version);
        mCheckStatusTv = (TextView) findViewById(R.id.tv_main_check_status);
        mCheckStatusTv.setVisibility(View.INVISIBLE);

        mCheckBtn = (Button) findViewById(R.id.btn_main_check);
        mCheckBtn.setOnClickListener(this);
        mUpdateBtn = (Button) findViewById(R.id.btn_main_update);
        mUpdateBtn.setOnClickListener(this);
        mUpdateBtn.setClickable(false);

        pd = new ProgressDialog(MainActivity.this);
        pd.setTitle("正在下载...");
        pd.setCancelable(false);
        pd.setProgress(0);
        pd.setMax(100);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_main_check:
                checkUpdate();
                break;

            case R.id.btn_main_update:
//                test();
//                downLoad();
                File file = new File("/sdcard/test.png");
                if (file.exists()) {
                    String md5OldStr = Md5Utils.getFileMD5(file);
                    if (md5OldStr != null && mCheckUpdateBean != null && mCheckUpdateBean.data != null && md5OldStr.equals(mCheckUpdateBean.data.packMD5)) {
                        //直接升级，不必下载

                        break;
                    } else {
                    }
                } else {

                }
                //下载服务
                Toast.makeText(MainActivity.this, "正在下载...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, DownLoadService.class);
                if(mCheckUpdateBean != null && mCheckUpdateBean.data != null){
                    intent.putExtra("MD5STR", mCheckUpdateBean.data.packMD5);
                }
                startService(intent);
                bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
                break;

            default:
                break;
        }
    }

    private void checkUpdate() {
        String timeStr = String.valueOf(System.currentTimeMillis());
        String signatureStr = CommonUtils.getSignature(AppConfig.APP_ID, timeStr, AppConfig.APP_SECRET);
        OkHttpUtils.get()
                .url(AppConfig.URL_UPGRADE)
                .addHeader("User-Agent", "allwinnertech")
                .addHeader("Auth-Signature", signatureStr)
                .addHeader("Auth-AppId", AppConfig.APP_ID)
                .addHeader("Auth-Timestamp", timeStr)
                .addHeader("Content-Type", "application/json;charset=UTF-8")
                .addParams("deviceId", "54110c0c5190410c0000341000000000") //TODO: 没有
                .addParams("versionCode", "010100") //TODO: 没有
                .addParams("appVersion", "1.1")
                .addParams("appName", "TestApp")
                .addParams("language", "Eng")
                .addParams("pkg", "pkg")
                .build()
                .execute(new CheckUpdateCallBack() {
                    @Override
                    public void onError(Call call, Exception e, int i) {

                    }

                    @Override
                    public void onResponse(CheckUpdateBean checkUpdateBean, int i) {
                        mCheckUpdateBean = checkUpdateBean;
                        Log.d("qiansheng", checkUpdateBean.message);
                        Log.d("qiansheng", checkUpdateBean.status);
                        if (checkUpdateBean.status.equals("success")) {
                            mCheckStatusTv.setText("可升级");
                            mCheckStatusTv.setVisibility(View.VISIBLE);
                            mUpdateBtn.setClickable(true);
                        }
                    }
                });

    }

    private void downLoad() {
        OkHttpUtils.get()
                .url("http://download.easyicon.net/png/1182979/128/")
                .build()
                .execute(new FileCallBack(Environment.getExternalStorageDirectory().getAbsolutePath(), "test.png") {
                    @Override
                    public void inProgress(float progress, long total, int id) {
                        //TODO: 进度
                        Log.e("qiansheng", "progress" + (int) (progress / total) * 100);
                        super.inProgress(progress, total, id);
                        pd.setProgress((int) (progress / total) * 100);

                    }

                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        pd.show();
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        Log.e("qiansheng", "download onError!!!");
                    }

                    @Override
                    public void onResponse(File file, int i) {
                        Log.e("qiansheng", "download onResponse :" + file.getAbsolutePath());
                        pd.dismiss();
                    }
                });
    }

    public void test() {
        String url = "http://www.csdn.net/";
        OkHttpUtils
                .get()
                .url(url)
                .addParams("username", "hyman")
                .addParams("password", "123")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {

                    }

                    @Override
                    public void onResponse(String s, int i) {
                        mCurVersionTv.setText("qiansheng");
                        Log.d("qiansheng", "Response:" + s);
                    }

                });
    }
}
