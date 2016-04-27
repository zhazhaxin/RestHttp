package cn.alien95.resthttp.image.cache;

import android.graphics.Bitmap;

import cn.alien95.resthttp.image.callback.DiskCallback;


/**
 * Created by linlongxin on 2016/3/14.
 */
public interface ImageCache {

    void putBitmapToCache(String key, Bitmap bitmap);
    Bitmap getBitmapFromCache(String key);
    void getBitmapFromCacheAsync(String imageUrl, DiskCallback callback);
    boolean isCache(String key);
}
