package cn.lemon.resthttp.request;

import com.google.gson.Gson;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;

import cn.lemon.resthttp.request.rest.RestRequestClient;
import cn.lemon.resthttp.util.RestHttpLog;
import cn.lemon.resthttp.util.Util;

/**
 * Created by linlongxin on 2016/4/27.
 */
public class ServerCacheDispatcher {

    private boolean isEmptyQueue = true;
    private LinkedBlockingDeque<Request> cacheQueue;
    private static ServerCacheDispatcher instance;

    private ServerCacheDispatcher() {
        cacheQueue = new LinkedBlockingDeque<>();
    }

    public static ServerCacheDispatcher getInstance() {
        if (instance == null) {
            instance = new ServerCacheDispatcher();
        }
        return instance;
    }

    public void addCacheRequest(Request request) {
        cacheQueue.add(request);
        if (isEmptyQueue) {
            start();
        }
    }

    /**
     * 同步直接处理
     */
    public Object getRestCacheSync(Request request) {

        final Cache.Entry entry = ServerCache.getInstance().get(Util.getCacheKey(request.url));

        if (entry != null) {
            if (entry.isExpired() || entry.refreshNeeded()) { //过期了
                RestHttpLog.i("缓存过期");
                return RestRequestClient.getInstance().request(request);
            } else {
                RestHttpLog.i("Sync Request data from cache");
                if (request.resultType != null && request.resultType != void.class) {
                    return new Gson().fromJson(entry.data, request.resultType);
                }
                return null;
            }
        }
        return null;
    }


    public void start() {
        Request request;
        Cache.Entry entry;
        /**
         * 普通请求方式，只有异步
         */
        while (!cacheQueue.isEmpty()) {
            request = cacheQueue.poll();
            entry = getCacheAsyn(Util.getCacheKey(request.url));
            if(entry != null){
                if(entry.isExpired() || entry.refreshNeeded()){
                    RestHttpLog.i("缓存过期");
                    //向服务器拉取数据
                    if(request.isHttps){
                        RequestDispatcher.getInstance().addHttpsRequest(request);
                    }else if(request.callback != null){
                        RequestDispatcher.getInstance().addHttpRequest(request);
                    }else if(request.restCallback != null){
                        RequestDispatcher.getInstance().addRestRequest(request);
                    }
                }else {
                    //本地读取缓存
                    if(request.isHttps){
                        request.httpsCallback.success(entry.data);
                    }else if(request.callback != null){
                        request.callback.success(entry.data);
                    }else if(request.restCallback != null){
                        Class returnType = request.restCallback.getActualClass();
                        if (returnType != null && returnType != void.class) {
                            request.restCallback.callback(new Gson().fromJson(entry.data, returnType));
                        } else {
                            request.restCallback.callback(null);
                        }
                    }
                }
            }
            isEmptyQueue = false;
        }
        isEmptyQueue = true;
    }


    /**
     * 异步读取文件并转化为对象
     */
    public Cache.Entry getCacheAsyn(final String key) {

        RestHttpLog.i("Asyn Request data from cache");

        try {
            return (Cache.Entry) RequestDispatcher.getInstance().submitCallable(new Callable<Cache.Entry>() {
                @Override
                public Cache.Entry call() throws Exception {
                    return ServerCache.getInstance().get(key);
                }
            }).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }


}
