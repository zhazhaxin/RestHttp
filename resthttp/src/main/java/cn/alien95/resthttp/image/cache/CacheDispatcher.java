package cn.alien95.resthttp.image.cache;

import android.graphics.Bitmap;

import java.util.concurrent.LinkedBlockingDeque;

import cn.alien95.resthttp.image.callback.ImageCallback;
import cn.alien95.resthttp.util.RestHttpLog;
import cn.alien95.resthttp.util.Util;

/**
 * Created by linlongxin on 2016/3/27.
 */
public class CacheDispatcher {

    private LinkedBlockingDeque<ImageRequest> cacheQueue;
    private boolean isCacheQueueEmpty = true;

    public CacheDispatcher() {
        cacheQueue = new LinkedBlockingDeque<>();
    }

    /**
     * 缓存过的图片请求加入缓存队列
     *
     * @param url
     * @param callback
     */
    public void addCacheQueue(String url, ImageCallback callback) {
        cacheQueue.add(new ImageRequest(url, 1, callback));
        if (isCacheQueueEmpty) {
            start();
        }
    }

    public void addCacheQueue(String url, int inSimpleSize, ImageCallback callback) {
        cacheQueue.add(new ImageRequest(url, inSimpleSize, callback));
        if (isCacheQueueEmpty) {
            start();
        }
    }

    public void addCacheQueue(String url, int reqWidth, int reqHeight, ImageCallback callback) {
        cacheQueue.add(new ImageRequest(url, reqWidth, reqHeight, callback));
        if (isCacheQueueEmpty) {
            start();
        }
    }

    public void start() {
        ImageRequest imgRequest;
        Bitmap bitmap;
        String key = "";
        while (!cacheQueue.isEmpty()) {
            imgRequest = cacheQueue.poll();

            /**
             * 通过制定图片的宽和高的方式
             */
            if (imgRequest.isControlWidthAndHeight) {

                key = Util.getCacheKey(imgRequest.url + imgRequest.reqWidth + "/" + imgRequest.reqHeight);

                /**
                 * 图片没有压缩
                 */
            } else if (imgRequest.inSampleSize <= 1) {

                key = Util.getCacheKey(imgRequest.url);

                /**
                 * 通过inSimpleSize参数进行图片压缩
                 */
            } else if (imgRequest.inSampleSize > 1) {

                key = Util.getCacheKey(imgRequest.url + imgRequest.inSampleSize);

            }

            if (MemoryCache.getInstance().get(key) != null) {
                imgRequest.callback.callback(MemoryCache.getInstance().get(key));

                RestHttpLog.i("Get compress picture from memoryCache");
            } else {
                bitmap = DiskCache.getInstance().get(key);
                imgRequest.callback.callback(bitmap);
                MemoryCache.getInstance().put(key, bitmap);

                RestHttpLog.i("Get compress picture from diskCache");
            }

            isCacheQueueEmpty = false;
        }
        isCacheQueueEmpty = true;
    }


}
