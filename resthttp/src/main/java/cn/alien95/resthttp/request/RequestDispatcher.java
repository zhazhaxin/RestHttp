package cn.alien95.resthttp.request;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import cn.alien95.resthttp.image.ImageUtils;
import cn.alien95.resthttp.image.cache.DiskCache;
import cn.alien95.resthttp.image.cache.ImgRequest;
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

    private boolean isEmptyRequestQueue = true;
    private boolean isEmptyRequestImgQueue = true;
    private LinkedBlockingDeque<Request> requestQueue;
    private LinkedBlockingDeque<ImgRequest> imgRequestQueue;
    private ExecutorService threadPool; //线程池
    private Handler handler;

    private static RequestDispatcher instance;

    private RequestDispatcher() {
        requestQueue = new LinkedBlockingDeque<>();
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

    /**
     * 异步读取服务器缓存文件
     *
     * @param callable
     * @return
     */
    public Future submitCallable(Callable callable) {
        return threadPool.submit(callable);
    }

    public void addRequest(String httpUrl, int method, Map<String, String> params, HttpCallback callback) {
        requestQueue.push(new Request(httpUrl, method, params, callback));
        if (isEmptyRequestQueue) {
            startRequest();
        }
    }

    public void addRequest(String httpUrl, int method, Map<String, String> params, Class returnType, RestCallback callback) {
        requestQueue.push(new Request(httpUrl, method, params, returnType, callback));
        if (isEmptyRequestQueue) {
            startRequest();
        }
    }

    public void addRequestImg(String url, int inSimpleSize, ImageCallback callback) {
        imgRequestQueue.push(new ImgRequest(url, inSimpleSize, callback));
        if (isEmptyRequestImgQueue) {
            startRequestImg();
        }
    }

    public void addRequestImg(String url, int reqWidth, int reqHeight, ImageCallback callback) {
        imgRequestQueue.push(new ImgRequest(url, reqWidth, reqHeight, callback));
        if (isEmptyRequestImgQueue) {
            startRequestImg();
        }
    }

    /**
     * 网络请求轮询
     */
    private void startRequest() {

        while (!requestQueue.isEmpty()) {
            final Request request = requestQueue.poll();
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
                        final Object reuslt = RestConnection.getInstance().quest(request.httpUrl, request.method,
                                request.params, request.resultType);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                request.restCallback.callback(reuslt);
                            }
                        });

                    }
                });
            }
            isEmptyRequestQueue = false;
        }
        isEmptyRequestQueue = true;
    }

    /**
     * 网络请求图片轮询
     */
    public void startRequestImg() {
        while (!imgRequestQueue.isEmpty()) {
            final ImgRequest imgRequest = imgRequestQueue.poll();
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
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                            if (imgRequest.inSampleSize == 0) {
                                options.inSampleSize = ImageUtils.calculateInSampleSize(options, imgRequest.reqWidth, imgRequest.reqHeight);
                            } else {
                                options.inSampleSize = imgRequest.inSampleSize;
                            }
                            options.inJustDecodeBounds = false;

                            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    imgRequest.callback.callback(bitmap);
                                    if (bitmap != null) {
                                        if (imgRequest.inSampleSize == 0) {
                                            MemoryCache.getInstance().put(Util.getCacheKey(imgRequest.url + imgRequest.reqWidth + "/" + imgRequest.reqHeight),
                                                    bitmap);
                                            DiskCache.getInstance().put(Util.getCacheKey(imgRequest.url + imgRequest.reqWidth + "/" + imgRequest.reqHeight),
                                                    bitmap);
                                        } else if (imgRequest.inSampleSize == 1) {
                                            MemoryCache.getInstance().put(Util.getCacheKey(imgRequest.url),
                                                    bitmap);
                                            DiskCache.getInstance().put(Util.getCacheKey(imgRequest.url),
                                                    bitmap);
                                        } else {
                                            MemoryCache.getInstance().put(Util.getCacheKey(imgRequest.url + imgRequest.inSampleSize),
                                                    bitmap);
                                            DiskCache.getInstance().put(Util.getCacheKey(imgRequest.url + imgRequest.inSampleSize),
                                                    bitmap);
                                        }

                                    }
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            isEmptyRequestImgQueue = false;
        }
        isEmptyRequestImgQueue = true;
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

}
