package cn.alien95.resthttp.request;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.request.rest.NetworkRequest;
import cn.alien95.resthttp.util.Utils;


/**
 * Created by linlongxin on 2015/12/27.
 */
public class RequestQueue {

    private boolean isEmptyQueue = true;
    private boolean isEmptyImgQueue = true;
    private LinkedBlockingDeque<NetworkRequest> requestQueue;
    private LinkedBlockingDeque<Runnable> imgRequest;
    private ExecutorService threadPool; //线程池

    private RequestQueue() {
        requestQueue = new LinkedBlockingDeque<>();
        imgRequest = new LinkedBlockingDeque<>();
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

    public void addRequest(String httpUrl, int method, HttpCallback callback){
        requestQueue.push(new NetworkRequest(httpUrl,method,callback));
        if(isEmptyQueue){
            start();
        }
    }

    public void addRequestForImage(Runnable runnable){
        imgRequest.push(runnable);
        start();
    }

    private void start() {
        NetworkRequest request;
        while (!requestQueue.isEmpty()) {
            request = requestQueue.poll();
            final NetworkRequest finalRequest = request;
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    HttpConnection.getInstance().quest(finalRequest.httpUrl, finalRequest.method
                            , null, finalRequest.callback);
                }
            });
            isEmptyQueue = false;
        }
        isEmptyQueue = true;

        while (!imgRequest.isEmpty()){
            threadPool.execute(imgRequest.poll());
            isEmptyImgQueue = false;
        }
        isEmptyImgQueue = true;
    }

}
