package cn.alien95.resthttp.util;

import android.util.Log;

import cn.alien95.resthttp.request.Method;


/**
 * Created by alien on 2015/8/6.
 */
public class DebugLog {

    private static String DEBUG_TAG = "";

    public static int requestTimes = 0;

    public static boolean isDebug = false;  //是否开启debug模式，默认关闭

    public static void setDebug(boolean debug, String tag) {
        isDebug = debug;
        DEBUG_TAG = tag;
    }

    public static synchronized void Log(String info) {
        if (isDebug) {
            Log.i(DEBUG_TAG, info);
        }
    }

    public static synchronized int requestLog(int method,String info) {
        if (isDebug) {
            if(method == Method.GET){
                Log.i(DEBUG_TAG, requestTimes + " times GET Request:" + info);
            }else {
                Log.i(DEBUG_TAG, requestTimes + " times POST Request:" + info);
            }
        }
        return requestTimes++;
    }

    public static synchronized void responseLog(String info, int requestNum) {
        if (isDebug) {
            Log.i(DEBUG_TAG, requestNum + " times Response:" + info);
        }
    }

    public static synchronized void requestImageLog(String info) {
        if (isDebug) {
            Log.i(DEBUG_TAG, requestTimes + " times RequestImage:" + info);
            requestTimes++;
        }
    }

}
