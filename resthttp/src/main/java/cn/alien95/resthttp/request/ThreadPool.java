package cn.alien95.resthttp.request;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.util.Util;


/**
 * Created by linlongxin on 2015/12/27.
 */
public class ThreadPool {

    private boolean isEmptyRequestQueue = true;
    private boolean isEmptyRequestImgQueue = true;
    private boolean isEmptyRestQueue = true;
    private LinkedBlockingDeque<Request> requestQueue;
    private LinkedBlockingDeque<Runnable> restRequestQueue;
    private LinkedBlockingDeque<Runnable> imgRequestQueue;
    private ExecutorService threadPool; //线程池

    private static ThreadPool instance;

    private ThreadPool() {
        requestQueue = new LinkedBlockingDeque<>();
        imgRequestQueue = new LinkedBlockingDeque<>();
        restRequestQueue = new LinkedBlockingDeque<>();
        if (Util.getNumberOfCPUCores() != 0) {
            threadPool = Executors.newFixedThreadPool(Util.getNumberOfCPUCores());
        } else
            threadPool = Executors.newFixedThreadPool(4);
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

    public void addRestRequest(Runnable runnable) {
        restRequestQueue.add(runnable);
        if (isEmptyRestQueue) {
            startRestRequest();
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
        Request request;
        while (!requestQueue.isEmpty()) {
            request = requestQueue.poll();
            final Request finalRequest = request;
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    RequestConnection.getInstance().quest(finalRequest.httpUrl, finalRequest.method
                            , finalRequest.params, finalRequest.callback);
                }
            });
            isEmptyRequestQueue = false;
        }
        isEmptyRequestQueue = true;
    }

    /**
     * Rest请求轮询读取
     */
    public void startRestRequest() {
        while (!restRequestQueue.isEmpty()) {
            threadPool.execute(restRequestQueue.poll());
            isEmptyRestQueue = false;
        }
        isEmptyRestQueue = true;
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
