package cn.alien95.resthttp.request;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

import cn.alien95.resthttp.image.cache.DiskCache;
import cn.alien95.resthttp.image.cache.ImageRequest;
import cn.alien95.resthttp.image.cache.MemoryCache;
import cn.alien95.resthttp.image.callback.ImageCallback;
import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.request.http.HttpConnection;
import cn.alien95.resthttp.request.rest.RestHttpConnection;
import cn.alien95.resthttp.request.callback.RestCallback;
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
    public void addNetRequest(String httpUrl, int method, Map<String, String> params, HttpCallback callback) {
        addNetRequest(new Request(httpUrl, method, params, callback));
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void addNetRequest(String httpUrl, int method, Map<String, String> params, Class returnType, RestCallback callback) {
        addNetRequest(new Request(httpUrl, method, params, returnType, callback));
    }

    private void addNetRequest(Request request) {
        mNetQueue.push(request);
        if (isEmptyNetQueue) {
            startDealNetRequest();
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void addImageRequest(String url, int inSimpleSize, ImageCallback callback) {
        addImageRequest(new ImageRequest(url, inSimpleSize, callback));
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void addImageRequest(String url, int reqWidth, int reqHeight, ImageCallback callback) {
        addImageRequest(new ImageRequest(url, reqWidth, reqHeight, callback));
    }

    private void addImageRequest(ImageRequest request) {
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
            if (request.restCallback == null) {
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        HttpConnection.getInstance().request(request);
                    }
                });
            } else {
                //通过接口方式请求
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        final Object result = RestHttpConnection.getInstance().request(request);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                request.restCallback.callback(result);
                            }
                        });

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
            final ImageRequest imgRequest = mImageQueue.poll();
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpURLConnection urlConnection = (HttpURLConnection) new URL(imgRequest.url).openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setDoInput(true);
                        urlConnection.setConnectTimeout(10 * 1000);
                        urlConnection.setReadTimeout(10 * 1000);
                        final InputStream inputStream = urlConnection.getInputStream();
                        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            final BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;

                            //如果两次都调用BitmapFactory.decodeStream,由于输入流失有序的输入流，第二次会得到null
                            byte[] bytes = new byte[0];
                            try {
                                bytes = readStream(inputStream);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (imgRequest.inSampleSize == 0) {
                                options.inSampleSize = Util.calculateInSampleSize(options, imgRequest.reqWidth, imgRequest.reqHeight);
                            } else {
                                options.inSampleSize = imgRequest.inSampleSize;
                            }
                            options.inJustDecodeBounds = false;

                            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                            String key;
                            if (bitmap != null) {
                                if (imgRequest.isControlWidthAndHeight) {
                                    key = Util.getCacheKey(imgRequest.url + imgRequest.reqWidth + "/" + imgRequest.reqHeight);
                                } else if (imgRequest.inSampleSize <= 1) {
                                    key = Util.getCacheKey(imgRequest.url);
                                } else {
                                    key = Util.getCacheKey(imgRequest.url + imgRequest.inSampleSize);
                                }
                                MemoryCache.getInstance().put(key, bitmap);
                                DiskCache.getInstance().put(key, bitmap);

                            }
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    imgRequest.callback.callback(bitmap);
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            isEmptyImageQueue = false;
        }
        isEmptyImageQueue = true;
    }


    /**
     * 从inputStream中获取字节流 数组大小
     *
     * @param inStream
     * @return
     * @throws Exception
     */
    public byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
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
