package cn.alien95.resthttp.request;

import com.google.gson.Gson;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;

import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.request.rest.RestConnection;
import cn.alien95.resthttp.request.rest.callback.RestCallback;
import cn.alien95.resthttp.util.RestHttpLog;
import cn.alien95.resthttp.util.Util;

/**
 * Created by linlongxin on 2016/4/27.
 */
public class ServerCacheDispatcher {

    private boolean isEmptyQueue = true;
    private LinkedBlockingDeque<Request> cacheQueue;
    private static ServerCacheDispatcher instance;

    private ServerCacheDispatcher() {
        cacheQueue = new LinkedBlockingDeque<>();
    }

    public static ServerCacheDispatcher getInstance() {
        if (instance == null) {
            instance = new ServerCacheDispatcher();
        }
        return instance;
    }

    public void addCacheRequest(String url, int method, Map<String, String> params, HttpCallback callback) {
        cacheQueue.add(new Request(url, method, params, callback));
        if (isEmptyQueue) {
            start();
        }
    }

    public void addCacheRequest(String url, int method, Map<String, String> params, Class returnType, RestCallback callback) {
        cacheQueue.add(new Request(url, method, params, returnType, callback));
        if (isEmptyQueue) {
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
    public Object getRestCacheSync(String url, int method, Map<String, String> params, Class tClass) {
        final Cache.Entry entry = ServerCache.getInstance().get(Util.getCacheKey(url, params));

        if (entry != null) {
            if (entry.isExpired() || entry.refreshNeeded()) { //过期了
                RestHttpLog.i("缓存过期");
                return RestConnection.getInstance().quest(url,
                        method, params, tClass);
            } else {
                RestHttpLog.i("get network sync data from cache");
                if (tClass != null && tClass != void.class) {
                    return new Gson().fromJson(entry.data, tClass);
                }
                return null;
            }
        }
        return null;
    }


    public Object start() {
        Request request;
        Cache.Entry entry;
        /**
         * 普通请求方式，只有异步
         */
        while (!cacheQueue.isEmpty()) {
            request = cacheQueue.poll();
            entry = getCacheAsyn(Util.getCacheKey(request.httpUrl, request.params));
            if (request.restCallback == null) {
                if (entry != null) {

                    if (entry.isExpired() || entry.refreshNeeded()) { //过期了
                        RestHttpLog.i("缓存过期");
                        RequestDispatcher.getInstance().addRequest(request.httpUrl, request.method, request.params, request.callback);
                    } else {
                        request.callback.success(entry.data);
                    }
                }
            } else {
                if (entry != null) {
                    if (entry.isExpired() || entry.refreshNeeded()) { //过期了
                        RestHttpLog.i("缓存过期");
                        /**
                         * 这里只有异步
                         */
                        RequestDispatcher.getInstance().addRequest(request.httpUrl, request.method, request.params,
                                request.resultType, request.restCallback);
                    } else {
                        Class returnType = request.restCallback.getActualClass();
                        if (returnType != null && returnType != void.class) {
                            request.restCallback.callback(new Gson().fromJson(entry.data, returnType));
                        } else {
                            request.restCallback.callback(null);
                        }
                    }
                }
            }
            isEmptyQueue = false;
        }
        isEmptyQueue = true;
        return null;
    }


    /**
     * 异步读取文件并转化为对象
     *
     * @param key
     */
    public Cache.Entry getCacheAsyn(final String key) {
        RestHttpLog.i("get network async data from cache");
        try {
            return (Cache.Entry) RequestDispatcher.getInstance().submitCallable(new Callable<Cache.Entry>() {
                @Override
                public Cache.Entry call() throws Exception {
                    return ServerCache.getInstance().get(key);
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
