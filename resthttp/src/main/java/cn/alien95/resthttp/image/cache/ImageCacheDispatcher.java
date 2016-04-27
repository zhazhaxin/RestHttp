package cn.alien95.resthttp.image.cache;

import android.graphics.Bitmap;

import java.util.concurrent.LinkedBlockingDeque;

import cn.alien95.resthttp.image.callback.DiskCallback;
import cn.alien95.resthttp.image.callback.ImageCallback;
import cn.alien95.resthttp.util.RestHttpLog;

/**
 * Created by linlongxin on 2016/3/27.
 */
public class ImageCacheDispatcher {

    private LinkedBlockingDeque<RequestImage> cacheQueue;
    private boolean isCacheQueueEmpty = true;

    public ImageCacheDispatcher() {
        cacheQueue = new LinkedBlockingDeque<>();
    }

    /**
     * 缓存过得图片请求加入缓存队列
     *
     * @param url
     * @param callback
     */
    public void addCacheQueue(String url, ImageCallback callback) {
        cacheQueue.add(new RequestImage(url, callback));
        if (isCacheQueueEmpty) {
            start();
        }
    }

    public void addCacheQueue(String url, int inSimpleSize, ImageCallback callback) {
        cacheQueue.add(new RequestImage(url, inSimpleSize, callback));
        if (isCacheQueueEmpty) {
            start();
        }
    }

    public void addCacheQueue(String url, int reqWidth, int reqHeight,ImageCallback callback) {
        cacheQueue.add(new RequestImage(url, reqWidth, reqHeight,callback));
        if (isCacheQueueEmpty) {
            start();
        }
    }

    public void start() {
        RequestImage requestImage;
        while (!cacheQueue.isEmpty()) {
            requestImage = cacheQueue.poll();

            /**
             * 通过制定图片的宽和高的方式
             */
            if (requestImage.isControlWidthAndHeight) {
                if (MemoryCache.getInstance().getBitmapFromCache(requestImage.url + requestImage.reqWidth + "/" + requestImage.reqHeight) != null) {
                    RestHttpLog.i("Get compress picture from memoryCache");
                    requestImage.callback.success(MemoryCache.getInstance().getBitmapFromCache(requestImage.url + requestImage.reqWidth + "/" + requestImage.reqHeight));
                } else {
                    RestHttpLog.i("Get compress picture from diskCache");
                    final ImageCallback finalCallback = requestImage.callback;
                    DiskCache.getInstance().getBitmapFromCacheAsync(requestImage.url + requestImage.reqWidth + "/" + requestImage.reqHeight, new DiskCallback() {
                        @Override
                        public void callback(Bitmap bitmap) {
                            finalCallback.success(bitmap);
                        }
                    });
                }
                /**
                 * 不进行图片压缩
                 */
            } else if (requestImage.inSimpleSize <= 1) {
                if (MemoryCache.getInstance().getBitmapFromCache(requestImage.url) != null) {
                    RestHttpLog.i("Get picture from memoryCache");
                    requestImage.callback.success(MemoryCache.getInstance().getBitmapFromCache(requestImage.url));
                } else {
                    RestHttpLog.i("Get picture from diskCache");
                    final ImageCallback finalCallback = requestImage.callback;
                    DiskCache.getInstance().getBitmapFromCacheAsync(requestImage.url, new DiskCallback() {
                        @Override
                        public void callback(Bitmap bitmap) {
                            finalCallback.success(bitmap);
                        }
                    });
                }
                /**
                 * 通过inSimpleSize参数进行图片压缩
                 */
            } else if (requestImage.inSimpleSize > 1) {
                /**
                 * 压缩图片缓存读取
                 */
                if (MemoryCache.getInstance().getBitmapFromCache(requestImage.url + requestImage.inSimpleSize) != null) {
                    RestHttpLog.i("Get compress picture from memoryCache");
                    requestImage.callback.success(MemoryCache.getInstance().getBitmapFromCache(requestImage.url + requestImage.inSimpleSize));
                } else {
                    RestHttpLog.i("Get compress picture from diskCache");
                    final ImageCallback finalCallback = requestImage.callback;
                    DiskCache.getInstance().getBitmapFromCacheAsync(requestImage.url + requestImage.inSimpleSize, new DiskCallback() {
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
