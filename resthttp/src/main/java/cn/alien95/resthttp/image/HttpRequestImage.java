package cn.alien95.resthttp.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.alien95.resthttp.image.cache.ImageCacheDispatcher;
import cn.alien95.resthttp.image.cache.DiskCache;
import cn.alien95.resthttp.image.cache.MemoryCache;
import cn.alien95.resthttp.image.callback.ImageCallback;
import cn.alien95.resthttp.request.RequestQueue;
import cn.alien95.resthttp.util.DebugUtils;


/**
 * Created by linlongxin on 2015/12/26.
 */
public class HttpRequestImage {

    private final String TAG = "HttpRequestImage";
    private ImageCacheDispatcher cacheDispatcher;
    private NetworkDispatcher networkDispatcher;
    private static HttpRequestImage instance;
    private Handler handler;

    private HttpRequestImage() {
        cacheDispatcher = new ImageCacheDispatcher();
        networkDispatcher = new NetworkDispatcher();
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
    public void requestImage(final String url, final ImageCallback callBack) {
        if (MemoryCache.getInstance().isCache(url)) {
            cacheDispatcher.addCacheQueue(url, callBack);
        } else if (DiskCache.getInstance().isCache(url)) {
            cacheDispatcher.addCacheQueue(url, callBack);
        } else {
            networkDispatcher.addNetwork(url, callBack);
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
    public synchronized void requestImageWithCompress(final String url, final int inSampleSize, final ImageCallback callBack) {
        /**
         * 判断是否真的压缩了
         */
        if(inSampleSize <= 1){
            if (MemoryCache.getInstance().isCache(url)|| DiskCache.getInstance().isCache(url)) {
                cacheDispatcher.addCacheQueue(url, callBack);
            } else {
                networkDispatcher.addNetwork(url, callBack);
            }
        }else if (inSampleSize > 1){
            if (MemoryCache.getInstance().isCache(url + inSampleSize) || DiskCache.getInstance().isCache(url + inSampleSize)) {
                cacheDispatcher.addCacheQueue(url, inSampleSize, callBack);
            } else {
                networkDispatcher.addNetworkWithCompress(url, inSampleSize, callBack);
            }
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
    public synchronized void requestImageWithCompress(final String url, final int reqWidth, final int reqHeight, final ImageCallback callBack) {
        if(MemoryCache.getInstance().isCache(url + reqWidth + "/" + reqHeight) || DiskCache.getInstance().isCache(url + reqWidth + "/" + reqHeight)){
            cacheDispatcher.addCacheQueue(url,reqWidth,reqHeight,callBack);
        }else {
            networkDispatcher.addNetworkWithCompress(url,reqWidth,reqHeight,callBack);
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


    public synchronized void loadImageFromNetWithCompress(final String url, final int reqWidth, final int reqHeight, final ImageCallback callBack) {
        RequestQueue.getInstance().addRequestForImage(new Runnable() {
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
                                    MemoryCache.getInstance().putBitmapToCache(url + reqWidth + reqHeight, compressBitmap);
                                    DiskCache.getInstance().putBitmapToCache(url + reqWidth + reqHeight, compressBitmap);
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
