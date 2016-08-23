package cn.alien95.resthttp.image.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import cn.alien95.resthttp.request.RequestDispatcher;
import cn.alien95.resthttp.util.Util;


/**
 * Created by linlongxin on 2015/12/29.
 * 这里需要使用单例模式，防止读取缓存的时候出现问题
 */
public class DiskCache implements ImageCache {

    private final String IMAGE_CACHE_PATH = "ImageCache";
    private static DiskCache instance;
    private DiskLruCache diskLruCache;
    private static long maxStoreSize = 50 * 1024 * 1024; //50MB

    private DiskCache() {
        try {
            File cacheDir = Util.getDiskCacheDir(IMAGE_CACHE_PATH);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            //50MB硬盘缓存
            diskLruCache = DiskLruCache.open(cacheDir, Util.getAppVersion(), 1, maxStoreSize);
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

    public static void setMaxStoreSize(long maxStoreSize) {
        DiskCache.maxStoreSize = maxStoreSize;
    }

    /**
     * 把Bitmap写入到缓存中
     *
     * @param key
     * @param resourceBitmap
     */
    @Override
    public void put(final String key, final Bitmap resourceBitmap) {
        if (isExist(key)) {
            return;
        } else {
            try {
                DiskLruCache.Editor editor = diskLruCache.edit(key);
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
    }


    @Override
    public Bitmap get(final String key) {
        try {
            final DiskLruCache.Snapshot snapShot = diskLruCache.get(key);
            if (snapShot != null) {
                try {
                    return (Bitmap) RequestDispatcher.getInstance().submitCallable(new Callable<Bitmap>() {
                        @Override
                        public Bitmap call() throws Exception {
                            InputStream is = snapShot.getInputStream(0);
                            return BitmapFactory.decodeStream(is);
                        }
                    }).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isExist(String key) {
        try {
            DiskLruCache.Snapshot snapShot = diskLruCache.get(key);
            if (snapShot != null)
                return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void remove(String key) {
        try {
            diskLruCache.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clear() {
        diskLruCache.getDirectory().delete();
    }
}
