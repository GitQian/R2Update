package com.xinzhihui.r2update.bean;

import com.google.gson.Gson;
import com.zhy.http.okhttp.callback.Callback;

import okhttp3.Response;

/**
 * Created by Administrator on 2016/12/22.
 */

public abstract class CheckUpdateCallBack extends Callback<CheckUpdateBean> {
    @Override
    public CheckUpdateBean parseNetworkResponse(Response response, int i) throws Exception {
        String string = response.body().string();
        CheckUpdateBean user = new Gson().fromJson(string, CheckUpdateBean.class);
        return user;
    }
}
