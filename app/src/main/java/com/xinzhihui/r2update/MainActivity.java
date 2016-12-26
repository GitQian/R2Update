package com.xinzhihui.r2update;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xinzhihui.r2update.Service.DownLoadService;
import com.xinzhihui.r2update.Utils.CommonUtils;
import com.xinzhihui.r2update.Utils.NetWorkUtils;
import com.xinzhihui.r2update.bean.CheckUpdateBean;
import com.xinzhihui.r2update.bean.CheckUpdateCallBack;
import com.zhy.http.okhttp.OkHttpUtils;

import java.lang.ref.WeakReference;

import okhttp3.Call;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static Context mContext;

    private TextView mNetNoticeTv;
    private TextView mCurVersionTv;  //TODO: 最好有一个接口，能够获取到当前的版本号！
    private Button mCheckBtn;

    public ProgressDialog mDownLoadProgress;
    public ProgressDialog mUpdateProgress;

    private CheckUpdateBean mCheckUpdateBean;
    private String mDeviceId;

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.get() == null || mActivity.get().mDownLoadProgress == null) {
                return;
            }
            if (mActivity.get() != mContext || mActivity.get().isFinishing()) {
                return;
            }
            switch (msg.what) {
                case AppConfig.DOWN_LOAD_START:
                    if (!mActivity.get().isFinishing()) {
                        mActivity.get().mDownLoadProgress.show();
                    }
                    break;

                case AppConfig.DOWN_LOAD_MSG:
                    if (!mActivity.get().isFinishing()) {
                        mActivity.get().mDownLoadProgress.show();
                        int progress = msg.arg1;
                        mActivity.get().mDownLoadProgress.setProgress(progress);
                    }
                    break;

                case AppConfig.DOWN_LOAD_FINISH:
                    if (!mActivity.get().isFinishing()) {
                        mActivity.get().mDownLoadProgress.dismiss();

                        mActivity.get().mUpdateProgress.show();
                    }
                    break;

                case AppConfig.DOWN_LOAD_ERROR:
                    if (!mActivity.get().isFinishing()) {
                        mActivity.get().mDownLoadProgress.dismiss();
                    }
                    break;

                case AppConfig.UPDATE_START:
                    if (!mActivity.get().isFinishing()) {
                        mActivity.get().mUpdateProgress.show();
                    }
                    break;

                case AppConfig.UPDATE_FINISH:
                    if (!mActivity.get().isFinishing()) {
                        mActivity.get().mUpdateProgress.dismiss();
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private Handler mHandler = new MyHandler(this);

    public DownLoadService mService;

    private class MyConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            mService = ((DownLoadService.MyBinder) service).getMyService(); //获取Myservice对象
            mService.setHandler(mHandler);
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

        mDeviceId = CommonUtils.getSystemInfoLine();
        Log.d("qiansheng", "DeviceId:" + mDeviceId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDownLoadProgress.dismiss();
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

        mCheckBtn = (Button) findViewById(R.id.btn_main_check);
        mCheckBtn.setOnClickListener(this);

        mDownLoadProgress = new ProgressDialog(MainActivity.this);
        mDownLoadProgress.setTitle("正在下载...");
        mDownLoadProgress.setCancelable(false);
        mDownLoadProgress.setProgress(0);
        mDownLoadProgress.setMax(100);
        mDownLoadProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        mUpdateProgress = new ProgressDialog(MainActivity.this);
        mUpdateProgress.setMessage("正在升级中...");
        mUpdateProgress.setCancelable(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_main_check:
                if (!NetWorkUtils.isNetworkAvailable(this)) {
                    Toast.makeText(this, "请检查网络", Toast.LENGTH_SHORT).show();
                } else {
                    checkUpdate();
                }
                break;

            default:
                break;
        }
    }

    private void checkUpdate() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在查找最新版本...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String timeStr = String.valueOf(System.currentTimeMillis());
        String signatureStr = CommonUtils.getSignature(AppConfig.APP_ID, timeStr, AppConfig.APP_SECRET);
        OkHttpUtils.get()
                .url(AppConfig.URL_UPGRADE)
                .addHeader("User-Agent", "allwinnertech")
                .addHeader("Auth-Signature", signatureStr)
                .addHeader("Auth-AppId", AppConfig.APP_ID)
                .addHeader("Auth-Timestamp", timeStr)
                .addHeader("Content-Type", "application/json;charset=UTF-8")
                .addParams("deviceId", mDeviceId) //TODO: 没有
                .addParams("versionCode", "010100") //TODO: 没有
                .addParams("appVersion", "1.1")
                .addParams("appName", "TestApp")
                .addParams("language", "Eng")
                .addParams("pkg", "pkg")
                .build()
                .execute(new CheckUpdateCallBack() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        Toast.makeText(getApplicationContext(), "请求失败", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onResponse(CheckUpdateBean checkUpdateBean, int i) {
                        progressDialog.dismiss();

                        mCheckUpdateBean = checkUpdateBean;
                        Log.d("qiansheng", checkUpdateBean.message);
                        Log.d("qiansheng", checkUpdateBean.status);
                        if (checkUpdateBean.status.equals("success")) {
                            Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("下载提示")
                                    .setMessage("最新版本为V_1.3，是否下载？")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //下载服务
                                            Toast.makeText(MainActivity.this, "正在下载...", Toast.LENGTH_SHORT).show();
                                            mDownLoadProgress.show();

                                            Intent intent = new Intent(MainActivity.this, DownLoadService.class);
                                            if (mCheckUpdateBean != null && mCheckUpdateBean.data != null) {
                                                intent.putExtra("MD5STR", mCheckUpdateBean.data.packMD5);
                                            }
                                            startService(intent);
                                            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
                                        }
                                    })
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .create();
                            alertDialog.show();
                            //TODO: 强制升级
                        } else {
                            //TODO: data == null 已是最新版本..
                        }
                    }
                });

    }

}
