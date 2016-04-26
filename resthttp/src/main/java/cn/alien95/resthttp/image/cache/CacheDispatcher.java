package cn.alien95.resthttp.image.cache;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.concurrent.LinkedBlockingDeque;

import cn.alien95.resthttp.image.callback.DiskCallback;
import cn.alien95.resthttp.image.callback.ImageCallback;

/**
 * Created by linlongxin on 2016/3/27.
 */
public class CacheDispatcher {

    private final String TAG = "CacheDispatcher";

    private LinkedBlockingDeque<Requst> cacheQueue;
    private boolean isCacheQueueEmpty = true;

    public CacheDispatcher() {
        cacheQueue = new LinkedBlockingDeque<>();
    }

    /**
     * 缓存过得图片请求加入缓存队列
     *
     * @param url
     * @param callback
     */
    public void addCacheQueue(String url, ImageCallback callback) {
        cacheQueue.add(new Requst(url, callback));
        if (isCacheQueueEmpty) {
            start();
        }
    }

    public void addCacheQueue(String url, int inSimpleSize, ImageCallback callback) {
        cacheQueue.add(new Requst(url, inSimpleSize, callback));
        if (isCacheQueueEmpty) {
            start();
        }
    }

    public void addCacheQueue(String url, int reqWidth, int reqHeight) {
        cacheQueue.add(new Requst(url, reqWidth, reqHeight));
        if (isCacheQueueEmpty) {
            start();
        }
    }

    public void start() {
        Requst requst;
        while (!cacheQueue.isEmpty()) {
            requst = cacheQueue.poll();

            /**
             * 通过制定图片的宽和高的方式
             */
            if (requst.isControlWidthAndHeight) {
                if (MemoryCache.getInstance().getBitmapFromCache(requst.url + requst.reqWidth + "/" + requst.reqHeight) != null) {
                    Log.i(TAG, "Get compress picture from memoryCache");
                    requst.callback.success(MemoryCache.getInstance().getBitmapFromCache(requst.url + requst.reqWidth + "/" + requst.reqHeight));
                } else {
                    Log.i(TAG, "Get compress picture from diskCache");
                    final ImageCallback finalCallback = requst.callback;
                    DiskCache.getInstance().getBitmapFromCacheAsync(requst.url + requst.reqWidth + "/" + requst.reqHeight, new DiskCallback() {
                        @Override
                        public void callback(Bitmap bitmap) {
                            finalCallback.success(bitmap);
                        }
                    });
                }
                /**
                 * 不进行图片压缩
                 */
            } else if (requst.inSimpleSize <= 1) {
                if (MemoryCache.getInstance().getBitmapFromCache(requst.url) != null) {
                    Log.i(TAG, "Get picture from memoryCache");
                    requst.callback.success(MemoryCache.getInstance().getBitmapFromCache(requst.url));
                } else {
                    Log.i(TAG, "Get picture from diskCache");
                    final ImageCallback finalCallback = requst.callback;
                    DiskCache.getInstance().getBitmapFromCacheAsync(requst.url, new DiskCallback() {
                        @Override
                        public void callback(Bitmap bitmap) {
                            finalCallback.success(bitmap);
                        }
                    });
                }
                /**
                 * 通过inSimpleSize参数进行图片压缩
                 */
            } else if (requst.inSimpleSize > 1) {
                /**
                 * 压缩图片缓存读取
                 */
                if (MemoryCache.getInstance().getBitmapFromCache(requst.url + requst.inSimpleSize) != null) {
                    Log.i(TAG, "Get compress picture from memoryCache");
                    requst.callback.success(MemoryCache.getInstance().getBitmapFromCache(requst.url + requst.inSimpleSize));
                } else {
                    Log.i(TAG, "Get compress picture from diskCache");
                    final ImageCallback finalCallback = requst.callback;
                    DiskCache.getInstance().getBitmapFromCacheAsync(requst.url + requst.inSimpleSize, new DiskCallback() {
                        @Override
                        public void callback(Bitmap bitmap) {
                            finalCallback.success(bitmap);
                        }
                    });
                }
            }

        }
        isCacheQueueEmpty = true;
    }


}
