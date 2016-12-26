package com.xinzhihui.r2update.Utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;

import static android.content.ContentValues.TAG;

/**
 * Created by Administrator on 2016/12/22.
 */

public class CommonUtils {

    public static String getSignature(String appId, String timestamp, String appSecret) {
        String str = new String();
        String requestStr = appId + timestamp + appSecret;
        str = getSha1(requestStr);
        return str;
    }


    //SHA1加密
    public static String getSha1(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(str.getBytes("UTF-8"));

            byte[] md = mdTemp.digest();
            int j = md.length;
            char buf[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf);
        } catch (Exception e) {
            // TODO: handle exception
            return null;
        }
    }


    public static String getSystemInfoLine() {
        BufferedReader reader = null;
        String line;
        String info = null;
        try {
            reader = new BufferedReader(new FileReader("/sys/class/sunxi_info/sys_info"));
            reader.readLine();
            reader.readLine();
            line = reader.readLine();
            if (line != null) {
                line = line.trim();
                String[] tokens = line.split(": ");
                info = tokens[1];
                Log.d(TAG, "info=" + info);
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "/sys/class/sunxi_info/sys_info");
        } catch (IOException e) {
            Log.e(TAG, "/sys/class/sunxi_info/sys_info");
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "could not close /sys/class/sunxi_info/sys_info");
            }
        }
        return info;

    }
}
