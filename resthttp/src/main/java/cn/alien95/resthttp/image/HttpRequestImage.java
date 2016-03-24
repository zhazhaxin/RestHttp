package cn.alien95.resthttp.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.alien95.resthttp.image.callback.DiskCallback;
import cn.alien95.resthttp.image.callback.ImageCallBack;
import cn.alien95.resthttp.request.HttpQueue;
import cn.alien95.resthttp.util.DebugUtils;


/**
 * Created by linlongxin on 2015/12/26.
 */
public class HttpRequestImage {

    private final String TAG = "HttpRequestImage";

    private MemoryCache memoryCache;
    private DiskCache diskCache;
    private static HttpRequestImage instance;
    private Handler handler;

    private HttpRequestImage() {
        memoryCache = new MemoryCache();
        diskCache = new DiskCache();
        handler = new Handler();
    }

    /**
     * 获取一个HttpRequestImage实例，这里是单例模式
     *
     * @return
     */
    public static HttpRequestImage getInstance() {
        if (instance == null) {
            synchronized (HttpRequestImage.class) {
                if (instance == null) {
                    instance = new HttpRequestImage();
                }
            }
        }
        return instance;
    }

    /**
     * 从网络请求图片
     *
     * @param url      图片的网络地址
     * @param callBack 回调接口
     */
    public void requestImage(final String url, final ImageCallBack callBack) {
        if (memoryCache.getBitmapFromCache(url) != null) {
            Log.i(TAG, "Get Picture from memoryCache");
            callBack.success(memoryCache.getBitmapFromCache(url));
        } else {
            diskCache.getBitmapFromCacheAsync(url, new DiskCallback() {
                @Override
                public void callback(Bitmap bitmap) {
                    if (bitmap != null) {
                        Log.i(TAG, "Get Picture from diskCache");
                        callBack.success(bitmap);
                    } else {
                        Log.i(TAG, "Get Picture from the network");
                        loadImageFromNet(url, callBack);
                    }
                }
            });
        }
    }

    /**
     * 图片网络请求压缩处理
     * 图片压缩处理的时候内存缓存和硬盘缓存的key是通过url+inSampleSize 通过MD5加密的
     *
     * @param url
     * @param inSampleSize
     * @param callBack
     */
    public synchronized void requestImageWithCompress(final String url, final int inSampleSize, final ImageCallBack callBack) {
        if (inSampleSize <= 1) {
            requestImage(url, callBack);
            return;
        }
        if (memoryCache.getBitmapFromCache(url + inSampleSize) != null) {
            Log.i(TAG, "Compress Get Picture from memoryCache");
            callBack.success(memoryCache.getBitmapFromCache(url + inSampleSize));
        } else {
            diskCache.getBitmapFromCacheAsync(url + inSampleSize, new DiskCallback() {
                @Override
                public void callback(Bitmap bitmap) {
                    if (bitmap != null) {
                        Log.i(TAG, "Compress Get Picture from diskCache");
                        callBack.success(bitmap);
                    } else {
                        Log.i(TAG, "Compress Get Picture from the network");
                        loadImageFromNetWithCompress(url, inSampleSize, callBack);
                    }
                }
            });
        }

    }

    /**
     * 压缩加载图片
     *
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @param callBack
     */
    public synchronized void requestImageWithCompress(final String url, final int reqWidth, final int reqHeight, final ImageCallBack callBack) {
        String key = url + reqWidth + reqHeight;
        if (memoryCache.getBitmapFromCache(key) != null) {
            Log.i(TAG, "Compress Get Picture from memoryCache");
            callBack.success(memoryCache.getBitmapFromCache(key));
        } else {
            diskCache.getBitmapFromCacheAsync(key, new DiskCallback() {
                @Override
                public void callback(Bitmap bitmap) {
                    if (bitmap != null) {
                        Log.i(TAG, "Compress Get Picture from diskCache");
                        callBack.success(bitmap);
                    } else {
                        Log.i(TAG, "Compress Get Picture from the network");
                        loadImageFromNetWithCompress(url, reqWidth, reqHeight, callBack);
                    }
                }
            });
        }

    }

    public HttpURLConnection getHttpUrlConnection(String url) {
        DebugUtils.requestImageLog(url);
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod("GET");
        } catch (IOException e) {
            e.printStackTrace();
        }
//                urlConnection.setDoOutput(true);   //沃日，为毛请求图片不能添加这句
        urlConnection.setDoInput(true);
        urlConnection.setConnectTimeout(10 * 1000);
        urlConnection.setReadTimeout(10 * 1000);
        //对HttpURLConnection对象的一切配置都必须要在connect()函数执行之前完成。
        return urlConnection;
    }

    /**
     * 从网络加载图片
     *
     * @param url
     * @param callBack
     */
    private synchronized void loadImageFromNet(final String url, final ImageCallBack callBack) {
        HttpQueue.getInstance().addQuest(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = getHttpUrlConnection(url);
                int respondCode;
                try {
                    urlConnection.connect();
                    final InputStream inputStream = urlConnection.getInputStream();
                    respondCode = urlConnection.getResponseCode();
                    if (respondCode == HttpURLConnection.HTTP_OK) {
                        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callBack.success(bitmap);
                                if (bitmap != null) {
                                    memoryCache.putBitmapToCache(url, bitmap);
                                    diskCache.putBitmapToCache(url, bitmap);
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
     * 从网络加载并压缩图片
     *
     * @param url
     * @param inSampleSize
     * @param callBack
     */
    public synchronized void loadImageFromNetWithCompress(final String url, final int inSampleSize, final ImageCallBack callBack) {
        HttpQueue.getInstance().addQuest(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = getHttpUrlConnection(url);
                int respondCode;
                try {
                    final InputStream inputStream = urlConnection.getInputStream();
                    respondCode = urlConnection.getResponseCode();
                    if (respondCode == HttpURLConnection.HTTP_OK) {
                        final Bitmap compressBitmap = ImageUtils.compressBitmapFromInputStream(inputStream, inSampleSize);
                        inputStream.close();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callBack.success(compressBitmap);
                                if (compressBitmap != null) {
                                    memoryCache.putBitmapToCache(url + inSampleSize, compressBitmap);
                                    diskCache.putBitmapToCache(url + inSampleSize, compressBitmap);
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

    public synchronized void loadImageFromNetWithCompress(final String url, final int reqWidth, final int reqHeight, final ImageCallBack callBack) {
        HttpQueue.getInstance().addQuest(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = getHttpUrlConnection(url);
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
                                callBack.success(compressBitmap);
                                if (compressBitmap != null) {
                                    memoryCache.putBitmapToCache(url + reqWidth + reqHeight, compressBitmap);
                                    diskCache.putBitmapToCache(url + reqWidth + reqHeight, compressBitmap);
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
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

}
