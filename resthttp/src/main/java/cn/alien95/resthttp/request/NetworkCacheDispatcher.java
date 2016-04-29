package cn.alien95.resthttp.request;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.request.rest.RestHttpConnection;
import cn.alien95.resthttp.request.rest.callback.RestCallback;
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
        handler = new Handler(Looper.myLooper());
    }

    public static NetworkCacheDispatcher getInstance() {
        if (instance == null) {
            instance = new NetworkCacheDispatcher();
        }
        return instance;
    }

    public void start() {
        Request request;
        Cache.Entry entry;

        while (!cacheQueue.isEmpty()) {
            request = cacheQueue.poll();
            entry = NetworkCache.getInstance().get(request.httpUrl);
            if (entry != null) {
                if (entry.isExpired() || entry.refreshNeeded()) { //过期了
                    RequestQueue.getInstance().addRequest(request.httpUrl, request.method, request.params, request.callback);
                    RestHttpLog.i("network cache is out of date");
                } else {
                    getCacheAsyn(request.httpUrl, request.callback);
                }
            }

            isEmptyQueue = false;
        }

        /**
         * Restful接口缓存处理方式
         */
        while (!restCacheQueue.isEmpty()) {
            request = restCacheQueue.poll();
            entry = NetworkCache.getInstance().get(request.httpUrl);
            if (entry != null) {
                if (entry.isExpired() || entry.refreshNeeded()) { //过期了
                    RestHttpConnection.getInstance().quest(request.httpUrl,
                            Method.POST, request.params, request.restRestCallback.getActualClass());
                    RestHttpLog.i("network cache is out of date");
                } else {
                    getRestCacheAysn(getCacheKey(request.httpUrl, request.params), request.restRestCallback);
                }
            }

            isRestEmptyQueue = false;
        }

        isEmptyQueue = true;
        isRestEmptyQueue = true;
    }

    public void addCacheRequest(String url, int method, Map<String, String> params, HttpCallback callback) {
        cacheQueue.add(new Request(url, method, params, callback));
        if (isEmptyQueue) {
            start();
        }
    }

    public void addRestCacheRequest(String url, int method, Map<String, String> params, RestCallback<?> restCallback) {
        restCacheQueue.add(new Request(url, method, params, restCallback));
        if (isEmptyQueue) {
            start();
        }
    }

    /**
     * 异步读取文件并转化为对象
     *
     * @param url
     * @param callback
     */
    public void getCacheAsyn(final String url, final HttpCallback callback) {
        RequestQueue.getInstance().addReadNetworkCacheAsyn(new Runnable() {
            @Override
            public void run() {
                final Cache.Entry entry = NetworkCache.getInstance().get(url);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.success(entry.data);
                    }
                });
            }
        });
    }

    public <T> void getRestCacheAysn(final String url, final RestCallback<T> callback) {
        RestHttpLog.i("get network data from cache");
        RequestQueue.getInstance().addReadNetworkCacheAsyn(new Runnable() {
            @Override
            public void run() {
                final Cache.Entry entry = NetworkCache.getInstance().get(url);
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

    private String getCacheKey(String url, Map<String, String> params) {
        /**
         * 只有POST才会有参数
         */
        StringBuilder paramStrBuilder = new StringBuilder();
        if (params != null) {
            for (Map.Entry<String, String> map : params.entrySet()) {
                try {
                    paramStrBuilder = paramStrBuilder.append("&").append(URLEncoder.encode(map.getKey(), "UTF-8")).append("=")
                            .append(URLEncoder.encode(map.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            paramStrBuilder.deleteCharAt(0);
            url = url + "?" + paramStrBuilder;
        }
        RestHttpLog.i("cache-key :　" + url);
        return url;
    }

}
