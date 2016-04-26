package cn.alien95.resthttp.image.cache;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import cn.alien95.resthttp.image.callback.DiskCallback;
import cn.alien95.resthttp.util.Utils;


/**
 * Created by linlongxin on 2015/12/29.
 * 这里需要使用单例模式，防止读取缓存的时候出现问题
 */
public class MemoryCache implements Cache {

    private final String TAG = "MemoryCache";

    private static MemoryCache instance;

    private LruCache<String, Bitmap> lruCache;

    private MemoryCache() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory());
        lruCache = new LruCache<>(maxMemory / 8);
    }

    public static MemoryCache getInstance() {
        if (instance == null) {
            instance = new MemoryCache();
        }
        return instance;
    }

    @Override
    public void putBitmapToCache(String key, Bitmap bitmap) {
        String cacheKey = getCacheKey(key);
        if (getBitmapFromCache(cacheKey) == null) {
            Log.i(TAG, "memory---getBitmapFromCache == null");
            if (lruCache.put(cacheKey, bitmap) != null) {
                Log.i(TAG, "memory cache success");
            }
        }
    }

    @Override
    public Bitmap getBitmapFromCache(String key) {
        Log.i(TAG, "memory---getBitmapFromCache");
        return lruCache.get(getCacheKey(key));
    }

    @Override
    public void getBitmapFromCacheAsync(String imageUrl, DiskCallback callback) {

    }

    @Override
    public boolean isCache(String key) {
        return lruCache.get(getCacheKey(key)) != null;
    }

    /**
     * 对图片地址进行md5处理得到缓存key
     *
     * @param key
     * @return
     */
    private String getCacheKey(String key) {
        return Utils.MD5(key);
    }

}
