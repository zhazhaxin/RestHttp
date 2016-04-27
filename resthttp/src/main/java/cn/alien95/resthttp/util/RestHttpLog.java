package cn.alien95.resthttp.util;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by linlongxin on 2016/4/27.
 */
public class RestHttpLog {

    private static final String TAG = "RestHttp";

    public static void i(String... log){
        Log.i(TAG, Arrays.toString(log));
    }

    public static void e(String... log){
        Log.e(TAG,Arrays.toString(log));
    }

    public static void d(String... log){
        Log.d(TAG,Arrays.toString(log));
    }
    public static void v(String... log){
        Log.v(TAG,Arrays.toString(log));
    }
}
