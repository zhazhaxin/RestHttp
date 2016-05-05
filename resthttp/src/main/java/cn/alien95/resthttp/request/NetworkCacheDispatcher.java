package cn.alien95.resthttp.request;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;

import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.request.rest.RestHttpConnection;
import cn.alien95.resthttp.request.rest.callback.RestCallback;
import cn.alien95.resthttp.util.RestHttpLog;
import cn.alien95.resthttp.util.Util;

/**
 * Created by linlongxin on 2016/4/27.
 */
public class NetworkCacheDispatcher {

    private boolean isEmptyQueue = true;
    private boolean isRestEmptyQueue = true;
    private LinkedBlockingDeque<Request> cacheQueue;
    private LinkedBlockingDeque<Request> restCacheQueue;
    private Handler handler;
    private static NetworkCacheDispatcher instance;

    private NetworkCacheDispatcher() {
        cacheQueue = new LinkedBlockingDeque<>();
        restCacheQueue = new LinkedBlockingDeque<>();
        handler = new Handler(Looper.getMainLooper());
    }

    public static NetworkCacheDispatcher getInstance() {
        if (instance == null) {
            instance = new NetworkCacheDispatcher();
        }
        return instance;
    }

    public void addCacheRequest(String url, int method, Map<String, String> params, HttpCallback callback) {
        cacheQueue.add(new Request(url, method, params, callback));
        if (isEmptyQueue) {
            start();
        }
    }

    public void addAsynRestCacheRequest(String url, int method, Map<String, String> params, Class resultType, RestCallback<Object> restCallback) {
        restCacheQueue.add(new Request(url, method, params, resultType, restCallback));
        if (isRestEmptyQueue) {
            start();
        }
    }

    /**
     * 同步直接处理
     *
     * @param url
     * @param method
     * @param params
     * @param tClass
     * @return
     */
    public Object addSyncRestCacheRequest(String url, int method, Map<String, String> params, Class tClass) {
        final Cache.Entry entry = NetworkCache.getInstance().get(Util.getCacheKey(url, params));

        if (entry != null) {
            if (entry.isExpired() || entry.refreshNeeded()) { //过期了
                RestHttpLog.i("缓存过期");
                return RestHttpConnection.getInstance().quest(url,
                        method, params, tClass);
            } else {
                return getRestCacheSync(entry, tClass);
            }
        }
        return null;
    }


    public Object start() {
        Request request;
        Cache.Entry entry;

        /**
         * 普通请求方式，都是异步
         */
        while (!cacheQueue.isEmpty()) {
            request = cacheQueue.poll();
            entry = getCacheAsyn(Util.getCacheKey(request.httpUrl, request.params));
            if (entry != null) {
                if (entry.isExpired() || entry.refreshNeeded()) { //过期了
                    RestHttpLog.i("缓存过期");
                    ThreadPool.getInstance().addRequest(request.httpUrl, request.method, request.params, request.callback);
                } else {
                    request.callback.success(entry.data);
                }
            }

            isEmptyQueue = false;
        }

        /**
         * Restful接口缓存处理,只要异步
         */
        while (!restCacheQueue.isEmpty()) {
            request = restCacheQueue.poll();
            entry = getRestCacheAysn(Util.getCacheKey(request.httpUrl, request.params));
            if (entry != null) {
                if (entry.isExpired() || entry.refreshNeeded()) { //过期了
                    RestHttpLog.i("缓存过期");
                    final Request finalRequest = request;
                    /**
                     * 这里只有异步
                     */
                    ThreadPool.getInstance().addRestRequest(new Runnable() {
                        @Override
                        public void run() {
                            final Object result = RestHttpConnection.getInstance().quest(finalRequest.httpUrl,
                                    finalRequest.method, finalRequest.params, finalRequest.resultType);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    finalRequest.restCallback.callback(result);
                                }
                            });
                        }
                    });

                } else {
                    Class returnType = request.restCallback.getActualClass();
                    if (returnType != null && returnType != void.class) {
                        request.restCallback.callback(new Gson().fromJson(entry.data, returnType));
                    } else {
                        request.restCallback.callback(null);
                    }
                }
            }

            isRestEmptyQueue = false;
        }

        isEmptyQueue = true;
        isRestEmptyQueue = true;
        return null;
    }


    /**
     * 异步读取文件并转化为对象
     *
     * @param key
     */
    public Cache.Entry getCacheAsyn(final String key) {
        try {
            return (Cache.Entry) ThreadPool.getInstance().submitCallable(new Callable<Cache.Entry>() {
                @Override
                public Cache.Entry call() throws Exception {
                    RestHttpLog.i("get network async data from cache");
                    return NetworkCache.getInstance().get(key);
                }
            }).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T getRestCacheSync(Cache.Entry entry, Class<T> tClass) {
        RestHttpLog.i("get network sync data from cache");
        if (tClass != null && tClass != void.class) {
            return new Gson().fromJson(entry.data, tClass);
        }
        return null;
    }

    public <T> Cache.Entry getRestCacheAysn(final String key) {
        RestHttpLog.i("get network aysn data from cache");
        try {
            return (Cache.Entry) ThreadPool.getInstance().submitCallable(new Callable<Cache.Entry>() {
                @Override
                public Cache.Entry call() throws Exception {

                    return NetworkCache.getInstance().get(key);
                }
            }).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

}
