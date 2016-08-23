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
import cn.alien95.resthttp.request.rest.RestConnection;
import cn.alien95.resthttp.request.rest.callback.RestCallback;
import cn.alien95.resthttp.util.Util;


/**
 * Created by linlongxin on 2015/12/27.
 */
public class RequestDispatcher {

    private boolean isEmptyNetRequestQueue = true;
    private boolean isEmptyImgRequestQueue = true;
    private LinkedBlockingDeque<Request> netRequestQueue;
    private LinkedBlockingDeque<ImageRequest> imgRequestQueue;
    private ExecutorService threadPool; //线程池
    private Handler handler;

    private static RequestDispatcher instance;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private RequestDispatcher() {
        netRequestQueue = new LinkedBlockingDeque<>();
        imgRequestQueue = new LinkedBlockingDeque<>();
        if (Util.getNumberOfCPUCores() != 0) {
            threadPool = Executors.newFixedThreadPool(Util.getNumberOfCPUCores());
        } else
            threadPool = Executors.newFixedThreadPool(4);
        handler = new Handler(Looper.getMainLooper());
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

    public void executeRunable(Runnable runnable){
        threadPool.execute(runnable);
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
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void addRequest(String httpUrl, int method, Map<String, String> params, HttpCallback callback) {
        netRequestQueue.push(new Request(httpUrl, method, params, callback));
        if (isEmptyNetRequestQueue) {
            startRequest();
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void addRequest(String httpUrl, int method, Map<String, String> params, Class returnType, RestCallback callback) {
        netRequestQueue.push(new Request(httpUrl, method, params, returnType, callback));
        if (isEmptyNetRequestQueue) {
            startRequest();
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void addImgRequest(String url, int inSimpleSize, ImageCallback callback) {
        imgRequestQueue.push(new ImageRequest(url, inSimpleSize, callback));
        if (isEmptyImgRequestQueue) {
            startRequestImg();
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void addImgRequest(String url, int reqWidth, int reqHeight, ImageCallback callback) {
        imgRequestQueue.push(new ImageRequest(url, reqWidth, reqHeight, callback));
        if (isEmptyImgRequestQueue) {
            startRequestImg();
        }
    }

    /**
     * 网络请求轮询
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void startRequest() {

        while (!netRequestQueue.isEmpty()) {
            final Request request = netRequestQueue.poll();
            if (request.restCallback == null) {
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        RequestConnection.getInstance().quest(request.httpUrl, request.method
                                , request.params, request.callback);
                    }
                });
            } else {
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        final Object result = RestConnection.getInstance().quest(request.httpUrl, request.method,
                                request.params, request.resultType);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                request.restCallback.callback(result);
                            }
                        });

                    }
                });
            }
            isEmptyNetRequestQueue = false;
        }
        isEmptyNetRequestQueue = true;
    }

    /**
     * 网络请求图片轮询
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void startRequestImg() {
        while (!imgRequestQueue.isEmpty()) {
            final ImageRequest imgRequest = imgRequestQueue.poll();
            threadPool.execute(new Runnable() {
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
                            handler.post(new Runnable() {
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
            isEmptyImgRequestQueue = false;
        }
        isEmptyImgRequestQueue = true;
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
        netRequestQueue.clear();
    }
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void cancelAllImageRequest() {
        imgRequestQueue.clear();
    }
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void cancelRequest(String url) {
        for (Request r : netRequestQueue) {
            if (r.httpUrl.equals(url)) {
                netRequestQueue.remove(r);
                return;
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void cancelRequest(String url, Map<String, String> params) {
        for (Request r : netRequestQueue) {
            if (r.httpUrl.equals(url) && params.equals(r.params)) {
                netRequestQueue.remove(r);
                return;
            }
        }
    }

}
