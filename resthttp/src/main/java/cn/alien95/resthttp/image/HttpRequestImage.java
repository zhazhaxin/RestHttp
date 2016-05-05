package cn.alien95.resthttp.image;

import android.os.Handler;

import cn.alien95.resthttp.image.cache.CacheDispatcher;
import cn.alien95.resthttp.image.cache.DiskCache;
import cn.alien95.resthttp.image.cache.MemoryCache;
import cn.alien95.resthttp.image.callback.ImageCallback;
import cn.alien95.resthttp.util.Util;


/**
 * Created by linlongxin on 2015/12/26.
 */
public class HttpRequestImage {

    private CacheDispatcher cacheDispatcher;
    private NetworkDispatcher networkDispatcher;
    private static HttpRequestImage instance;
    private Handler handler;

    private HttpRequestImage() {
        cacheDispatcher = new CacheDispatcher();
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
        if (MemoryCache.getInstance().isExist(Util.getCacheKey(url))) {
            cacheDispatcher.addCacheQueue(url, callBack);
        } else if (DiskCache.getInstance().isExist(Util.getCacheKey(url))) {
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
        if (inSampleSize <= 1) {
            if (MemoryCache.getInstance().isExist(Util.getCacheKey(url)) || DiskCache.getInstance().isExist(Util.getCacheKey(url))) {
                cacheDispatcher.addCacheQueue(url, callBack);
            } else {
                networkDispatcher.addNetwork(url, callBack);
            }
        } else if (inSampleSize > 1) {
            if (MemoryCache.getInstance().isExist(Util.getCacheKey(url + inSampleSize)) ||
                    DiskCache.getInstance().isExist(Util.getCacheKey(url + inSampleSize))) {
                cacheDispatcher.addCacheQueue(url, inSampleSize, callBack);
            } else {
                networkDispatcher.addRequestImgWithCompress(url, inSampleSize, callBack);
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
        if (MemoryCache.getInstance().isExist(Util.getCacheKey(url + reqWidth + "/" + reqHeight)) ||
                DiskCache.getInstance().isExist(Util.getCacheKey(url + reqWidth + "/" + reqHeight))) {
            cacheDispatcher.addCacheQueue(url, reqWidth, reqHeight, callBack);
        } else {
            networkDispatcher.addRequestImgWithCompress(url, reqWidth, reqHeight, callBack);
        }
    }

}
