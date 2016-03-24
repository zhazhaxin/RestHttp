package cn.alien95.resthttp.image;

import android.graphics.Bitmap;
import android.util.LruCache;

import cn.alien95.resthttp.image.callback.DiskCallback;


/**
 * Created by linlongxin on 2015/12/29.
 */
public class MemoryCache implements ImageCache{

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
        if (getBitmapFromCache(key) == null) {
            lruCache.put(key, bitmap);
        }
    }

    @Override
    public Bitmap getBitmapFromCache(String key) {
        return lruCache.get(key);
    }

    @Override
    public void getBitmapFromCacheAsync(String imageUrl, DiskCallback callback) {

    }

}
