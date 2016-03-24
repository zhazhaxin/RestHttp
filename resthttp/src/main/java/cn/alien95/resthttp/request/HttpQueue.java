package cn.alien95.resthttp.request;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import cn.alien95.resthttp.util.Utils;


/**
 * Created by linlongxin on 2015/12/27.
 */
public class HttpQueue {
    private LinkedBlockingDeque<Runnable> requestQueue;
    private ExecutorService threadPool; //线程池

    private HttpQueue() {
        requestQueue = new LinkedBlockingDeque<>();
        if(Utils.getNumberOfCPUCores() != 0){
            threadPool = Executors.newFixedThreadPool(Utils.getNumberOfCPUCores());
        }else
            threadPool = Executors.newFixedThreadPool(4);

    }

    private static class HttpQueueHolder{
        private static final HttpQueue instance= new HttpQueue();
    }

    public static HttpQueue getInstance() {
        return HttpQueueHolder.instance;
    }

    public void addQuest(Runnable runnable) {
        requestQueue.push(runnable);
        start();
    }

    private void start() {
        while (requestQueue.peek() != null) {
            threadPool.execute(requestQueue.poll());
        }
    }


}
