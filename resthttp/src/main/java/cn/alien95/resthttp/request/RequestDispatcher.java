package cn.alien95.resthttp.request;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import cn.alien95.resthttp.image.ImageConnection;
import cn.alien95.resthttp.image.cache.ImageRequest;
import cn.alien95.resthttp.request.http.HttpRequestClient;
import cn.alien95.resthttp.request.https.HttpsRequestClient;
import cn.alien95.resthttp.request.https.SelfSignRequestClient;
import cn.alien95.resthttp.request.rest.RestRequestClient;
import cn.alien95.resthttp.util.Util;


/**
 * Created by linlongxin on 2015/12/27.
 */
public class RequestDispatcher {

    private boolean isEmptyNetQueue = true;
    private boolean isEmptyImageQueue = true;
    private LinkedBlockingDeque<Request> mNetQueue;
    private LinkedBlockingDeque<ImageRequest> mImageQueue;
    private ExecutorService mThreadPool; //线程池
    private Handler mHandler;

    private static RequestDispatcher instance;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private RequestDispatcher() {
        mNetQueue = new LinkedBlockingDeque<>();
        mImageQueue = new LinkedBlockingDeque<>();
        if (Util.getNumberOfCPUCores() != 0) {
            mThreadPool = Executors.newFixedThreadPool(Util.getNumberOfCPUCores());
        } else
            mThreadPool = Executors.newFixedThreadPool(4);
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static RequestDispatcher getInstance() {
        if (instance == null) {
            synchronized (RequestDispatcher.class) {
                if (instance == null) {
                    instance = new RequestDispatcher();
                }
            }
        }
        return instance;
    }

    public void executeRunnable(Runnable runnable) {
        mThreadPool.execute(runnable);
    }

    /**
     * 异步读取服务器缓存文件
     */
    public Future submitCallable(Callable callable) {
        return mThreadPool.submit(callable);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void addHttpRequest(Request request) {
        addRequest(request);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void addRestRequest(Request request) {
        addRequest(request);
    }

    public void addHttpsRequest(Request request) {
        addRequest(request);
    }

    private void addRequest(Request request) {
        mNetQueue.push(request);
        if (isEmptyNetQueue) {
            startDealNetRequest();
        }
    }

    public void addImageRequest(ImageRequest request) {
        mImageQueue.push(request);
        if (isEmptyImageQueue) {
            startDealImageRequest();
        }
    }

    /**
     * 网络请求轮询
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void startDealNetRequest() {

        while (!mNetQueue.isEmpty()) {
            final Request request = mNetQueue.poll();
            if (request.callback != null) {
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        HttpRequestClient.getInstance().request(request);
                    }
                });
            } else if (request.restCallback != null) {
                //面向接口
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        final Object result = RestRequestClient.getInstance().request(request);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                request.restCallback.callback(result);
                            }
                        });

                    }
                });
            } else if (request.isSelfSign) {
                //自签名Https证书
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        SelfSignRequestClient.getInstance().request(request);
                    }
                });
            } else if (request.httpsCallback != null) {
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        HttpsRequestClient.getInstance().request(request);
                    }
                });
            }
            isEmptyNetQueue = false;
        }
        isEmptyNetQueue = true;
    }

    /**
     * 网络请求图片轮询
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void startDealImageRequest() {
        while (!mImageQueue.isEmpty()) {
            final ImageRequest request = mImageQueue.poll();
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    ImageConnection.getInstance().request(request);
                }
            });
            isEmptyImageQueue = false;
        }
        isEmptyImageQueue = true;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void cancelAllNetRequest() {
        mNetQueue.clear();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void cancelAllImageRequest() {
        mImageQueue.clear();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void cancelRequest(String url) {
        for (Request r : mNetQueue) {
            if (r.url.equals(url)) {
                mNetQueue.remove(r);
                return;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void cancelRequest(String url, Map<String, String> params) {
        for (Request r : mNetQueue) {
            if (r.url.equals(url) && params.equals(r.params)) {
                mNetQueue.remove(r);
                return;
            }
        }
    }

}
