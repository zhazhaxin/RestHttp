package cn.alien95.resthttp.image.cache;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;


/**
 * Created by linlongxin on 2015/12/29.
 * 这里需要使用单例模式，防止读取缓存的时候出现问题
 */
public class MemoryCache implements ImageCache {

    private static MemoryCache instance;

    private LruCache<String, Bitmap> lruCache;

    private MemoryCache() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory());
        lruCache = new LruCache<String, Bitmap>(maxMemory / 8) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
        Log.i("RestHttp", "memory cache size : " + maxMemory / 8);
    }

    public static MemoryCache getInstance() {
        if (instance == null) {
            instance = new MemoryCache();
        }
        return instance;
    }

    @Override
    public void put(String key, Bitmap bitmap) {
        lruCache.put(key, bitmap);
    }

    @Override
    public Bitmap get(String key) {
        return lruCache.get(key);
    }

    @Override
    public boolean isExist(String key) {
        return lruCache.get(key) != null;
    }

    @Override
    public void remove(String key) {
        lruCache.remove(key);
    }

    @Override
    public void clear() {

    }

}
