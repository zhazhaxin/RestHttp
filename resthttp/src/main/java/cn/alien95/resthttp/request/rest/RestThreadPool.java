package cn.alien95.resthttp.request.rest;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import cn.alien95.resthttp.util.Utils;


/**
 * Created by linlongxin on 2015/12/27.
 */
public class RestThreadPool {

    private final String TAG = "RestThreadPool";
    private LinkedBlockingDeque<Callable> requestQueue;
    private ExecutorService threadPool;

    private RestThreadPool() {
        requestQueue = new LinkedBlockingDeque<>();
        if (Utils.getNumberOfCPUCores() != 0) {
            threadPool = Executors.newFixedThreadPool(Utils.getNumberOfCPUCores());
        } else
            threadPool = Executors.newFixedThreadPool(4);
    }

    private static class HttpQueueHolder {
        private static final RestThreadPool instance = new RestThreadPool();
    }

    public static RestThreadPool getInstance() {
        return HttpQueueHolder.instance;
    }

    public<T> Future<T> putThreadPool(Callable callable) {
        return threadPool.submit(callable);
    }

    public void putThreadPool(Runnable runnable){
        threadPool.execute(runnable);
    }

}
