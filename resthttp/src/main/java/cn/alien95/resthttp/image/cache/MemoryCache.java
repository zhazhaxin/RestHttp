package cn.alien95.resthttp.image.cache;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import cn.alien95.resthttp.image.callback.DiskCallback;
import cn.alien95.resthttp.util.Utils;


/**
 * Created by linlongxin on 2015/12/29.
 */
public class MemoryCache implements Cache {

    private final String TAG = "MemoryCache";

    private LruCache<String, Bitmap> lruCache;

    int maxMemory;

    public MemoryCache() {
        maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        malloc(maxMemory / 8);
    }

    /**
     * @param size 单位：KB，内存分配大小
     */
    private void malloc(int size) {

        lruCache = new LruCache<String, Bitmap>(size) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // 重写此方法来衡量每张图片的大小，默认返回图片数量。
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @Override
    public void putBitmapToCache(String key, Bitmap bitmap) {
        Log.i(TAG,"memory---putBitmapToCache");
        String cacheKey = getCacheKey(key);
        if (getBitmapFromCache(cacheKey) == null) {
            lruCache.put(cacheKey, bitmap);
        }
    }

    @Override
    public Bitmap getBitmapFromCache(String key) {
        Log.i(TAG,"memory---getBitmapFromCache");
        return lruCache.get(getCacheKey(key));
    }

    @Override
    public void getBitmapFromCacheAsync(String imageUrl, DiskCallback callback) {

    }

    /**
     * 对图片地址进行md5处理得到缓存key
     * @param key
     * @return
     */
    private String getCacheKey(String key){
        return Utils.MD5(key);
    }

}
