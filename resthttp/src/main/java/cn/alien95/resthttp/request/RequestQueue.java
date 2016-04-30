package cn.alien95.resthttp.request;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.util.Utils;


/**
 * Created by linlongxin on 2015/12/27.
 */
public class RequestQueue {

    private boolean isEmptyRequestQueue = true;
    private boolean isEmptyImgQueue = true;
    private boolean isEmptyRestQueue = true;
    private LinkedBlockingDeque<Request> requestQueue;
    private LinkedBlockingDeque<Runnable> imgCacheQueue;
    private LinkedBlockingDeque<Runnable> restQueue;
    private ExecutorService threadPool; //线程池

    private RequestQueue() {
        requestQueue = new LinkedBlockingDeque<>();
        imgCacheQueue = new LinkedBlockingDeque<>();
        restQueue = new LinkedBlockingDeque<>();
        if (Utils.getNumberOfCPUCores() != 0) {
            threadPool = Executors.newFixedThreadPool(Utils.getNumberOfCPUCores());
        } else
            threadPool = Executors.newFixedThreadPool(4);

    }

    private static class HttpQueueHolder {
        private static final RequestQueue instance = new RequestQueue();
    }

    public static RequestQueue getInstance() {
        return HttpQueueHolder.instance;
    }

    /**
     * 异步读取服务器缓存文件
     *
     * @param callable
     * @return
     */
    public Future putThreadPool(Callable callable) {
        return threadPool.submit(callable);
    }

    public void addRequest(String httpUrl, int method, Map<String, String> params, HttpCallback callback) {
        requestQueue.push(new Request(httpUrl, method, params, callback));
        if (isEmptyRequestQueue) {
            startRequest();
        }
    }

    public void addRestRequest(Runnable runnable) {
        restQueue.add(runnable);
        if (isEmptyRestQueue) {
            startRestRequest();
        }
    }

    public void addReadImgCacheAsyn(Runnable runnable) {
        imgCacheQueue.push(runnable);
        if (isEmptyImgQueue) {
            startExecuteImageCache();
        }
    }

    private void startRequest() {
        Request request;

        //网络请求队列
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

    public void startExecuteImageCache() {
        //图片缓存
        while (!imgCacheQueue.isEmpty()) {
            threadPool.execute(imgCacheQueue.poll());
            isEmptyImgQueue = false;
        }
        isEmptyImgQueue = true;
    }

    public void startRestRequest() {
        //服务器缓存处理
        while (!restQueue.isEmpty()) {
            threadPool.execute(restQueue.poll());
            isEmptyRestQueue = false;
        }
        isEmptyRestQueue = true;
    }

}
