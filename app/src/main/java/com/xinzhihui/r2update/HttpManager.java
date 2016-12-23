package com.xinzhihui.r2update;

import android.util.Log;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;

/**
 * Created by Administrator on 2016/12/22.
 */

public class HttpManager {

    public static void checkUpdate() {
        String url = "";
//        OkHttpUtils.get()

    }


    public static void test() {
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
                        Log.d("qiansheng", "Response:" + s);
                    }

                });
    }

}
