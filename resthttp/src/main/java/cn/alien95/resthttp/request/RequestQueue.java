package cn.alien95.resthttp.request;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.util.Utils;


/**
 * Created by linlongxin on 2015/12/27.
 */
public class RequestQueue {

    private boolean isEmptyRequestQueue = true;
    private boolean isEmptyImgQueue = true;
    private boolean isEmptyNetworkCacheQueue = true;
    private LinkedBlockingDeque<Request> requestQueue;
    private LinkedBlockingDeque<Runnable> imgCacheQueue;
    private LinkedBlockingDeque<Runnable> networkCacheQueue;
    private ExecutorService threadPool; //线程池

    private RequestQueue() {
        requestQueue = new LinkedBlockingDeque<>();
        imgCacheQueue = new LinkedBlockingDeque<>();
        networkCacheQueue = new LinkedBlockingDeque<>();
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

    public void addRequest(String httpUrl, int method, Map<String,String> params,HttpCallback callback){
        requestQueue.push(new Request(httpUrl,method,params,callback));
        if(isEmptyRequestQueue){
            start();
        }
    }

    public void addReadImgCacheAsyn(Runnable runnable){
        imgCacheQueue.push(runnable);
        if(isEmptyImgQueue){
            start();
        }
    }

    public void addReadNetworkCacheAsyn(Runnable runnable){
        networkCacheQueue.push(runnable);
        if(isEmptyNetworkCacheQueue){
            start();
        }
    }

    private void start() {
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

        //图片缓存
        while (!imgCacheQueue.isEmpty()){
            threadPool.execute(imgCacheQueue.poll());
            isEmptyImgQueue = false;
        }
        isEmptyImgQueue = true;

        //服务器缓存处理
        while (!networkCacheQueue.isEmpty()){
            threadPool.execute(networkCacheQueue.poll());
            isEmptyNetworkCacheQueue = false;
        }
        isEmptyNetworkCacheQueue = true;
    }

}
