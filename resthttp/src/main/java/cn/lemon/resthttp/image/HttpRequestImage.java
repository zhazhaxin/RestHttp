package cn.lemon.resthttp.image;

import cn.lemon.resthttp.image.cache.CacheDispatcher;
import cn.lemon.resthttp.image.cache.DiskCache;
import cn.lemon.resthttp.image.cache.ImageRequest;
import cn.lemon.resthttp.image.cache.MemoryCache;
import cn.lemon.resthttp.image.callback.ImageCallback;
import cn.lemon.resthttp.util.Util;


/**
 * Created by linlongxin on 2015/12/26.
 */
public class HttpRequestImage {

    private CacheDispatcher cacheDispatcher;
    private ImageRequestDispatcher imgRequestDispatcher;
    private static HttpRequestImage instance;

    private HttpRequestImage() {
        cacheDispatcher = new CacheDispatcher();
        imgRequestDispatcher = new ImageRequestDispatcher();
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
     */
    public void requestImage(final String url, final ImageCallback callBack) {

        String key = Util.getCacheKey(url);
        ImageRequest request = new ImageRequest(url,1, callBack);
        if (MemoryCache.getInstance().isExist(key) || DiskCache.getInstance().isExist(key)) {
            cacheDispatcher.addCacheQueue(request);
        } else {
            imgRequestDispatcher.addImageRequest(request);
        }
    }

    /**
     * 图片网络请求压缩处理
     * 图片压缩处理的时候内存缓存和硬盘缓存的key是通过url+inSampleSize 通过MD5加密的
     */
    public void requestImageWithCompress(final String url, final int inSampleSize, final ImageCallback callBack) {

        String key;
        if (inSampleSize <= 1) {  //无压缩

            requestImage(url, callBack);

        } else if (inSampleSize > 1) {

            key = Util.getCacheKey(url + inSampleSize);
            ImageRequest request = new ImageRequest(url, inSampleSize, callBack);
            if (MemoryCache.getInstance().isExist(key) || DiskCache.getInstance().isExist(key)) {
                cacheDispatcher.addCacheQueue(request);
            } else {
                imgRequestDispatcher.addImageRequest(request);
            }
        }
    }

    /**
     * 压缩加载图片 -- 根据指定的width,height
     */
    public void requestImageWithCompress(final String url, final int reqWidth, final int reqHeight, final ImageCallback callBack) {

        String key = Util.getCacheKey(url + reqWidth + "/" + reqHeight);
        ImageRequest request = new ImageRequest(url, reqWidth, reqHeight, callBack);
        if (MemoryCache.getInstance().isExist(key) || DiskCache.getInstance().isExist(key)) {
            cacheDispatcher.addCacheQueue(request);
        } else {
            imgRequestDispatcher.addImageRequest(request);
        }
    }

}
