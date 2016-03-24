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
    private LinkedBlockingDeque<Callable> requestQueue;
    private ExecutorService threadPool; //线程池

    private RestThreadPool() {
        requestQueue = new LinkedBlockingDeque<>();
        if(Utils.getNumberOfCPUCores() != 0){
            threadPool = Executors.newFixedThreadPool(Utils.getNumberOfCPUCores());
        }else
            threadPool = Executors.newFixedThreadPool(4);
    }

    private static class HttpQueueHolder{
        private static final RestThreadPool instance= new RestThreadPool();
    }

    public static RestThreadPool getInstance() {
        return HttpQueueHolder.instance;
    }

    public synchronized <T> Future<T> addQuest(Callable<T> callable) {
        Future<T> result = threadPool.submit(callable);
        return result;
    }

}
