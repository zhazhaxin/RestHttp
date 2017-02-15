package cn.alien95.resthttp.request;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import cn.alien95.resthttp.image.cache.DiskCache;
import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.request.callback.HttpsCallback;
import cn.alien95.resthttp.request.http.HttpRequest;
import cn.alien95.resthttp.util.HttpLog;
import cn.alien95.resthttp.util.Util;


/**
 * Created by linlongxin on 2015/12/26.
 */
public abstract class RestHttp {

    private static Map<String,HttpRequest> mInstanceMap = new HashMap<>();

    //这样去写单例模式虽然可以省去很多代码，不过因为newInstance方法有限制：构造函数必须public,必须有一个构造函数没有参数
    public static<T extends HttpRequest> T getInstance(Class<T> clazz) {
        if(!mInstanceMap.containsKey(clazz.getSimpleName())){
            synchronized (clazz){
                if(!mInstanceMap.containsKey(clazz.getSimpleName())){
                    try {
                        T instance = clazz.newInstance();
                        mInstanceMap.put(clazz.getSimpleName(), instance);
                        return instance;
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                        return null;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }

        return (T) mInstanceMap.get(clazz.getSimpleName());
    }
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
        HttpLog.setDebug(isDebug, tag);
    }


}
