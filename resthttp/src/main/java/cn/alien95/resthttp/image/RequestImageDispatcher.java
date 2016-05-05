package cn.alien95.resthttp.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.alien95.resthttp.image.cache.DiskCache;
import cn.alien95.resthttp.image.cache.MemoryCache;
import cn.alien95.resthttp.image.callback.ImageCallback;
import cn.alien95.resthttp.request.ThreadPool;
import cn.alien95.resthttp.util.DebugUtils;
import cn.alien95.resthttp.util.RestHttpLog;
import cn.alien95.resthttp.util.Util;

/**
 * Created by linlongxin on 2016/4/26.
 */
public class RequestImageDispatcher {

    private Handler handler;

    public RequestImageDispatcher() {
        handler = new Handler(Looper.getMainLooper());
    }

    public void addRequestImg(String url, ImageCallback callback) {
        loadImg(url, 1, callback);
    }

    public void addRequestImgWithCompress(String url, int inSimpleSize, ImageCallback callback) {
        loadImg(url, inSimpleSize, callback);
    }

    public void addRequestImgWithCompress(String url, int reqWidth, int reqHeight, ImageCallback callback) {
        loadImgWithCompress(url, reqWidth, reqHeight, callback);
    }

    public void loadImg(final String url, final int inSampleSize, final ImageCallback callback) {

        RestHttpLog.i("Get picture from network");
        DebugUtils.requestImageLog(url);

        ThreadPool.getInstance().addRequestImg(new Runnable() {
            @Override
            public void run() {
                int respondCode;
                try {
                    HttpURLConnection urlConnection = null;
                    try {
                        urlConnection = (HttpURLConnection) new URL(url).openConnection();
                        urlConnection.setRequestMethod("GET");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                urlConnection.setDoOutput(true);   //为毛请求图片不能添加这句
                    urlConnection.setDoInput(true);
                    urlConnection.setConnectTimeout(10 * 1000);
                    urlConnection.setReadTimeout(10 * 1000);
                    final InputStream inputStream = urlConnection.getInputStream();
                    respondCode = urlConnection.getResponseCode();
                    if (respondCode == HttpURLConnection.HTTP_OK) {
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
                        options.inSampleSize = inSampleSize;
                        options.inJustDecodeBounds = false;

                        final Bitmap compressBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.callback(compressBitmap);
                                if (compressBitmap != null) {
                                    MemoryCache.getInstance().put(Util.getCacheKey(url + inSampleSize), compressBitmap);
                                    DiskCache.getInstance().put(Util.getCacheKey(url + inSampleSize), compressBitmap);
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public synchronized void loadImgWithCompress(final String url, final int reqWidth, final int reqHeight, final ImageCallback callBack) {
        RestHttpLog.i("Get compress picture from network");
        DebugUtils.requestImageLog(url);

        ThreadPool.getInstance().addRequestImg(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    urlConnection = (HttpURLConnection) new URL(url).openConnection();
                    urlConnection.setRequestMethod("GET");
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                urlConnection.setDoOutput(true);   //为毛请求图片不能添加这句
                urlConnection.setDoInput(true);
                urlConnection.setConnectTimeout(10 * 1000);
                urlConnection.setReadTimeout(10 * 1000);
                int respondCode;
                try {
                    final InputStream inputStream = urlConnection.getInputStream();
                    respondCode = urlConnection.getResponseCode();
                    if (respondCode == HttpURLConnection.HTTP_OK) {
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
                        options.inSampleSize = ImageUtils.calculateInSampleSize(options, reqWidth, reqHeight);
                        options.inJustDecodeBounds = false;

                        final Bitmap compressBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callBack.callback(compressBitmap);
                                if (compressBitmap != null) {
                                    MemoryCache.getInstance().put(Util.getCacheKey(url + reqWidth + "/" + reqHeight), compressBitmap);
                                    DiskCache.getInstance().put(Util.getCacheKey(url + reqWidth + "/" + reqHeight), compressBitmap);
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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
