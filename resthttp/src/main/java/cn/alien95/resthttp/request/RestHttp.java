package cn.alien95.resthttp.request;

import android.content.Context;

import java.util.Map;

import cn.alien95.resthttp.image.cache.DiskCache;
import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.request.callback.HttpsCallback;
import cn.alien95.resthttp.util.DebugLog;
import cn.alien95.resthttp.util.Util;


/**
 * Created by linlongxin on 2015/12/26.
 */
public abstract class RestHttp {

    /**
     * 初始化RestHttp框架
     */
    public static void initialize(Context context) {
        Util.init(context);
        ServerCache.checkCacheRoot();
    }

    public static void setDiskCacheSize(long maxStoreSize) {
        DiskCache.setMaxStoreSize(maxStoreSize);
    }

    /**
     * GET
     */
    public void get(String url, HttpCallback callBack){}
    public void get(String url, HttpsCallback callBack){}
    /**
     * POST
     */
    public void post(String url, Map<String, String> params, HttpCallback callBack){}
    public void post(String url, Map<String, String> params, HttpsCallback callBack){}
    /**
     * 是否开启调试模式，默认是关闭
     */
    public static void setDebug(boolean isDebug, String tag) {
        DebugLog.setDebug(isDebug, tag);
    }


}
