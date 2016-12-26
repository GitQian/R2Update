package com.xinzhihui.r2update;

import android.os.Environment;

/**
 * Created by Administrator on 2016/12/22.
 */

public class AppConfig {
    public static final String URL_UPGRADE = "http://112.74.165.90/dc/devinfo/v1/upgrade";
//    public final static String URL = ""

    public static final String APP_ID = "10678706636";
    public static final String APP_SECRET = "9c725bfaa675f53617a80baef0dea84d";

    public static final int DOWN_LOAD_START = 0;
    public static final int DOWN_LOAD_MSG = 1;
    public static final int DOWN_LOAD_FINISH = 2;
    public static final int DOWN_LOAD_ERROR = 3;

    public static final int MSG_REGISTER_CLIENT = 4;

    public static final int UPDATE_START = 5;
    public static final int UPDATE_FINISH = 6;


    public static final String DIR_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String FILE_NAME = "test.apk";
    public static final String FILE_PATH = DIR_PATH + FILE_NAME;

}
