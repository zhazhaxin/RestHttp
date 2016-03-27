package cn.alien95.resthttp.image.cache;

import android.graphics.Bitmap;
import android.util.Log;

import cn.alien95.resthttp.image.HttpRequestImage;
import cn.alien95.resthttp.image.callback.DiskCallback;
import cn.alien95.resthttp.image.callback.ImageCallBack;

/**
 * Created by linlongxin on 2016/3/27.
 */
public class CacheDispatcher {

    private final String TAG = "CacheDispatcher";

    private MemoryCache memoryCache;
    private DiskCache diskCache;

    public CacheDispatcher(){
        memoryCache = new MemoryCache();
        diskCache = new DiskCache();
    }

    public void getImage(final String url, final ImageCallBack callBack){
        if (memoryCache.getBitmapFromCache(url) != null) {
            Log.i(TAG, "Get Picture from memoryCache");
            callBack.success(memoryCache.getBitmapFromCache(url));
        } else {
            diskCache.getBitmapFromCacheAsync(url, new DiskCallback() {
                @Override
                public void callback(Bitmap bitmap) {
                    if (bitmap != null) {
                        Log.i(TAG, "Get Picture from diskCache");
                        callBack.success(bitmap);
                    } else {
                        Log.i(TAG, "Get Picture from the network");
                        HttpRequestImage.getInstance().loadImageFromNet(url, callBack);
                    }
                }
            });
        }
    }

    public void getImageWithCompress(final String url, final int inSampleSize, final ImageCallBack callBack){
        if (inSampleSize <= 1) {
            getImage(url, callBack);
            return;
        }
        if (memoryCache.getBitmapFromCache(url + inSampleSize) != null) {
            Log.i(TAG, "Compress Get Picture from memoryCache");
            callBack.success(memoryCache.getBitmapFromCache(url + inSampleSize));
        } else {
            diskCache.getBitmapFromCacheAsync(url + inSampleSize, new DiskCallback() {
                @Override
                public void callback(Bitmap bitmap) {
                    if (bitmap != null) {
                        Log.i(TAG, "Compress Get Picture from diskCache");
                        callBack.success(bitmap);
                    } else {
                        Log.i(TAG, "Compress Get Picture from the network");
                        HttpRequestImage.getInstance().loadImageFromNetWithCompress(url, inSampleSize, callBack);
                    }
                }
            });
        }
    }

    public void getImageWithCompress(final String url, final int reqWidth, final int reqHeight, final ImageCallBack callBack){
        String key = url + reqWidth + reqHeight;
        if (memoryCache.getBitmapFromCache(key) != null) {
            Log.i(TAG, "Compress Get Picture from memoryCache");
            callBack.success(memoryCache.getBitmapFromCache(key));
        } else {
            diskCache.getBitmapFromCacheAsync(key, new DiskCallback() {
                @Override
                public void callback(Bitmap bitmap) {
                    if (bitmap != null) {
                        Log.i(TAG, "Compress Get Picture from diskCache");
                        callBack.success(bitmap);
                    } else {
                        Log.i(TAG, "Compress Get Picture from the network");
                        HttpRequestImage.getInstance().loadImageFromNetWithCompress(url, reqWidth, reqHeight, callBack);
                    }
                }
            });
        }
    }
}
