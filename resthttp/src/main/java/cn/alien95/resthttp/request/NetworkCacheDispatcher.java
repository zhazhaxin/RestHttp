package cn.alien95.resthttp.request;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;

import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.request.rest.RestHttpConnection;
import cn.alien95.resthttp.request.rest.RestThreadPool;
import cn.alien95.resthttp.request.rest.callback.RestCallback;
import cn.alien95.resthttp.util.CacheKeyUtils;
import cn.alien95.resthttp.util.RestHttpLog;

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
        final Cache.Entry entry = NetworkCache.getInstance().get(CacheKeyUtils.getCacheKey(url, params));

        if (entry != null) {
            if (entry.isExpired() || entry.refreshNeeded()) { //过期了
                RestHttpLog.i("缓存过期");
                return RestHttpConnection.getInstance().quest(url,
                        method, params, tClass);
            } else {
                return getRestCacheSync(CacheKeyUtils.getCacheKey(url, params), tClass);
            }
        }
        return null;
    }


    public Object start() {
        Request request;
        Cache.Entry entry;

        /**
         * 普通请求方式
         */
        while (!cacheQueue.isEmpty()) {
            request = cacheQueue.poll();
            entry = NetworkCache.getInstance().get(CacheKeyUtils.getCacheKey(request.httpUrl, request.params));
            if (entry != null) {
                if (entry.isExpired() || entry.refreshNeeded()) { //过期了
                    RestHttpLog.i("缓存过期");
                    RequestQueue.getInstance().addRequest(request.httpUrl, request.method, request.params, request.callback);
                } else {
                    getCacheAsyn(CacheKeyUtils.getCacheKey(request.httpUrl, request.params), request.callback);
                }
            }

            isEmptyQueue = false;
        }

        /**
         * Restful接口缓存处理,只要异步
         */
        while (!restCacheQueue.isEmpty()) {
            request = restCacheQueue.poll();
            entry = NetworkCache.getInstance().get(CacheKeyUtils.getCacheKey(request.httpUrl, request.params));
            if (entry != null) {
                if (entry.isExpired() || entry.refreshNeeded()) { //过期了
                    RestHttpLog.i("缓存过期");
                    final Request finalRequest = request;
                    /**
                     * 这里只有异步
                     */
                    RestThreadPool.getInstance().putThreadPool(new Runnable() {
                        @Override
                        public void run() {
                            final Object result = RestHttpConnection.getInstance().quest(finalRequest.httpUrl,
                                    finalRequest.method, finalRequest.params, finalRequest.resultType);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    finalRequest.restRestCallback.callback(result);
                                }
                            });
                        }
                    });

                } else {
                    getRestCacheAysn(CacheKeyUtils.getCacheKey(request.httpUrl, request.params), request.restRestCallback);
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
     * @param callback
     */
    public void getCacheAsyn(final String key, final HttpCallback callback) {
        RequestQueue.getInstance().addReadNetworkCacheAsyn(new Runnable() {
            @Override
            public void run() {
                final Cache.Entry entry = NetworkCache.getInstance().get(key);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.success(entry.data);
                    }
                });
            }
        });
    }

    public <T> T getRestCacheSync(String key, Class<T> tClass) {
        RestHttpLog.i("get network data from sync cache");
        final Cache.Entry entry = NetworkCache.getInstance().get(key);
        if (tClass != null && tClass != void.class) {
            return new Gson().fromJson(entry.data, tClass);
        }
        return null;
    }

    public <T> void getRestCacheAysn(final String key, final RestCallback<T> callback) {
        RestHttpLog.i("get network data from aysn cache");
        RequestQueue.getInstance().addReadNetworkCacheAsyn(new Runnable() {
            @Override
            public void run() {
                final Cache.Entry entry = NetworkCache.getInstance().get(key);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Class returnType = callback.getActualClass();
                        if (returnType != null && returnType != void.class) {
                            Gson gson = new Gson();
                            T result = (T) gson.fromJson(entry.data, returnType);
                            callback.callback(result);
                        }
                    }
                });
            }
        });
    }

}
