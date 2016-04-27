package cn.alien95.resthttp.image.cache;

import android.graphics.Bitmap;

import java.util.concurrent.LinkedBlockingDeque;

import cn.alien95.resthttp.image.callback.DiskCallback;
import cn.alien95.resthttp.image.callback.ImageCallback;
import cn.alien95.resthttp.util.RestHttpLog;

/**
 * Created by linlongxin on 2016/3/27.
 */
public class CacheDispatcher {

    private LinkedBlockingDeque<ImgRequest> cacheQueue;
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
        cacheQueue.add(new ImgRequest(url, callback));
        if (isCacheQueueEmpty) {
            start();
        }
    }

    public void addCacheQueue(String url, int inSimpleSize, ImageCallback callback) {
        cacheQueue.add(new ImgRequest(url, inSimpleSize, callback));
        if (isCacheQueueEmpty) {
            start();
        }
    }

    public void addCacheQueue(String url, int reqWidth, int reqHeight,ImageCallback callback) {
        cacheQueue.add(new ImgRequest(url, reqWidth, reqHeight,callback));
        if (isCacheQueueEmpty) {
            start();
        }
    }

    public void start() {
        ImgRequest imgRequest;
        while (!cacheQueue.isEmpty()) {
            imgRequest = cacheQueue.poll();

            /**
             * 通过制定图片的宽和高的方式
             */
            if (imgRequest.isControlWidthAndHeight) {
                if (MemoryCache.getInstance().getBitmapFromCache(imgRequest.url + imgRequest.reqWidth + "/" + imgRequest.reqHeight) != null) {
                    RestHttpLog.i("Get compress picture from memoryCache");
                    imgRequest.callback.success(MemoryCache.getInstance().getBitmapFromCache(imgRequest.url + imgRequest.reqWidth + "/" + imgRequest.reqHeight));
                } else {
                    RestHttpLog.i("Get compress picture from diskCache");
                    final ImageCallback finalCallback = imgRequest.callback;
                    DiskCache.getInstance().getBitmapFromCacheAsync(imgRequest.url + imgRequest.reqWidth + "/" + imgRequest.reqHeight, new DiskCallback() {
                        @Override
                        public void callback(Bitmap bitmap) {
                            finalCallback.success(bitmap);
                        }
                    });
                }
                /**
                 * 不进行图片压缩
                 */
            } else if (imgRequest.inSimpleSize <= 1) {
                if (MemoryCache.getInstance().getBitmapFromCache(imgRequest.url) != null) {
                    RestHttpLog.i("Get picture from memoryCache");
                    imgRequest.callback.success(MemoryCache.getInstance().getBitmapFromCache(imgRequest.url));
                } else {
                    RestHttpLog.i("Get picture from diskCache");
                    final ImageCallback finalCallback = imgRequest.callback;
                    DiskCache.getInstance().getBitmapFromCacheAsync(imgRequest.url, new DiskCallback() {
                        @Override
                        public void callback(Bitmap bitmap) {
                            finalCallback.success(bitmap);
                        }
                    });
                }
                /**
                 * 通过inSimpleSize参数进行图片压缩
                 */
            } else if (imgRequest.inSimpleSize > 1) {
                /**
                 * 压缩图片缓存读取
                 */
                if (MemoryCache.getInstance().getBitmapFromCache(imgRequest.url + imgRequest.inSimpleSize) != null) {
                    RestHttpLog.i("Get compress picture from memoryCache");
                    imgRequest.callback.success(MemoryCache.getInstance().getBitmapFromCache(imgRequest.url + imgRequest.inSimpleSize));
                } else {
                    RestHttpLog.i("Get compress picture from diskCache");
                    final ImageCallback finalCallback = imgRequest.callback;
                    DiskCache.getInstance().getBitmapFromCacheAsync(imgRequest.url + imgRequest.inSimpleSize, new DiskCallback() {
                        @Override
                        public void callback(Bitmap bitmap) {
                            finalCallback.success(bitmap);
                        }
                    });
                }
            }

            isCacheQueueEmpty = false;
        }
        isCacheQueueEmpty = true;
    }


}
