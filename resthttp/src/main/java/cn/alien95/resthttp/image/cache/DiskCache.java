package cn.alien95.resthttp.image.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.alien95.resthttp.image.callback.DiskCallback;
import cn.alien95.resthttp.request.RequestQueue;
import cn.alien95.resthttp.util.Utils;


/**
 * Created by linlongxin on 2015/12/29.
 * 这里需要使用单例模式，防止读取缓存的时候出现问题
 */
public class DiskCache implements ImageCache {

    private final String IMAGE_CACHE_PATH = "IMAGE_CACHE";
    private static DiskCache instance;
    private DiskLruCache diskLruCache;
    private Handler handler;

    private DiskCache() {
        handler = new Handler(Looper.getMainLooper());
        try {
            File cacheDir = Utils.getDiskCacheDir(IMAGE_CACHE_PATH);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            //50MB硬盘缓存
            diskLruCache = DiskLruCache.open(cacheDir, Utils.getAppVersion(), 1, 50 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DiskCache getInstance() {
        if (instance == null) {
            instance = new DiskCache();
        }
        return instance;
    }

    /**
     * 把Bitmap写入到缓存中
     *
     * @param imageUrl
     * @param resourceBitmap
     */
    @Override
    public void putBitmapToCache(final String imageUrl, final Bitmap resourceBitmap) {

        getBitmapFromCacheAsync(imageUrl, new DiskCallback() {
            @Override
            public void callback(Bitmap bitmap) {
                if (bitmap != null) {
                    return;
                }
                DiskLruCache.Editor editor;
                try {
                    editor = diskLruCache.edit(getCacheKey(imageUrl));
                    if (editor != null) {
                        OutputStream outputStream = editor.newOutputStream(0);
                        boolean success = resourceBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        outputStream.close();
                        if (success) {
                            editor.commit();
                        } else {
                            editor.abort();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public Bitmap getBitmapFromCache(String key) {
        return null;
    }

    /**
     * 根据imageUrl从缓存读取Bitmap
     *
     * @param imageUrl
     * @param callback
     */
    @Override
    public void getBitmapFromCacheAsync(final String imageUrl, final DiskCallback callback) {

        RequestQueue.getInstance().addRequestForImage(new Runnable() {
            @Override
            public void run() {
                try {
                    DiskLruCache.Snapshot snapShot = diskLruCache.get(getCacheKey(imageUrl));
                    if (snapShot != null) {
                        InputStream is = snapShot.getInputStream(0);
                        final Bitmap bitmap = BitmapFactory.decodeStream(is);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.callback(bitmap);
                                MemoryCache.getInstance().putBitmapToCache(imageUrl,bitmap);
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.callback(null);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean isCache(String imageUrl) {
        try {
            if(diskLruCache.get(getCacheKey(imageUrl)) != null){
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getCacheKey(String key) {
        return Utils.MD5(key);
    }
}
