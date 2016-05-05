package cn.alien95.resthttp.request;

import android.os.Handler;
import android.os.Looper;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.request.rest.RestConnection;
import cn.alien95.resthttp.request.rest.callback.RestCallback;
import cn.alien95.resthttp.util.Util;


/**
 * Created by linlongxin on 2015/12/27.
 */
public class ThreadPool {

    private boolean isEmptyRequestQueue = true;
    private boolean isEmptyRequestImgQueue = true;
    private boolean isEmptyRestQueue = true;
    private LinkedBlockingDeque<Request> requestQueue;
    private LinkedBlockingDeque<Runnable> imgRequestQueue;
    private ExecutorService threadPool; //线程池
    private Handler handler;

    private static ThreadPool instance;

    private ThreadPool() {
        requestQueue = new LinkedBlockingDeque<>();
        imgRequestQueue = new LinkedBlockingDeque<>();
        if (Util.getNumberOfCPUCores() != 0) {
            threadPool = Executors.newFixedThreadPool(Util.getNumberOfCPUCores());
        } else
            threadPool = Executors.newFixedThreadPool(4);
        handler = new Handler(Looper.getMainLooper());
    }

    public static ThreadPool getInstance() {
        if (instance == null) {
            synchronized (ThreadPool.class) {
                if (instance == null) {
                    instance = new ThreadPool();
                }
            }
        }
        return instance;
    }

    /**
     * 异步读取服务器缓存文件
     *
     * @param callable
     * @return
     */
    public Future submitCallable(Callable callable) {
        return threadPool.submit(callable);
    }

    public void addRequest(String httpUrl, int method, Map<String, String> params, HttpCallback callback) {
        requestQueue.push(new Request(httpUrl, method, params, callback));
        if (isEmptyRequestQueue) {
            startRequest();
        }
    }

    public void addRequest(String httpUrl, int method, Map<String, String> params, Class returnType, RestCallback callback) {
        requestQueue.push(new Request(httpUrl, method, params, returnType, callback));
        if (isEmptyRequestQueue) {
            startRequest();
        }
    }

    public void addRequestImg(Runnable runnable) {
        imgRequestQueue.push(runnable);
        if (isEmptyRequestImgQueue) {
            startRequestImg();
        }
    }

    /**
     * 网络请求轮询
     */
    private void startRequest() {

        while (!requestQueue.isEmpty()) {
            final Request request = requestQueue.poll();
            if (request.restCallback == null) {
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestConnection.getInstance().quest(request.httpUrl, request.method
                                , request.params, request.callback);
                    }
                });
            } else {
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        final Object reuslt = RestConnection.getInstance().quest(request.httpUrl, request.method, request.params, request.resultType);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                request.restCallback.callback(reuslt);
                            }
                        });

                    }
                });
            }
            isEmptyRequestQueue = false;
        }
        isEmptyRequestQueue = true;
    }

    /**
     * 网络请求图片轮询
     */
    public void startRequestImg() {
        while (!imgRequestQueue.isEmpty()) {
            threadPool.execute(imgRequestQueue.poll());
            isEmptyRequestImgQueue = false;
        }
        isEmptyRequestImgQueue = true;
    }

}
