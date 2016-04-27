package cn.alien95.resthttp.request;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.LinkedBlockingDeque;

import cn.alien95.resthttp.request.callback.HttpCallback;

/**
 * Created by linlongxin on 2016/4/27.
 */
public class NetworkCacheDispatcher {

    private boolean isEmptyQueue = true;
    private LinkedBlockingDeque<Request> cacheQueue;
    private Handler handler;
    private static NetworkCacheDispatcher instance;

    private NetworkCacheDispatcher() {
        cacheQueue = new LinkedBlockingDeque<>();
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
            if (entry.isExpired() || entry.refreshNeeded()) {
                RequestQueue.getInstance().addRequest(request.httpUrl, request.method, request.callback);
            } else {
                getCacheAsyn(request.httpUrl, request.callback);
            }
            isEmptyQueue = false;
        }
        isEmptyQueue = true;
    }

    public void addCacheRequest(String url, HttpCallback callback) {
        cacheQueue.add(new Request(url, callback));
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

}
