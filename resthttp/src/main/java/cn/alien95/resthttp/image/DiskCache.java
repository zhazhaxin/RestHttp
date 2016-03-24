package cn.alien95.resthttp.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.alien95.resthttp.image.callback.DiskCallback;
import cn.alien95.resthttp.request.HttpQueue;
import cn.alien95.resthttp.util.Utils;


/**
 * Created by linlongxin on 2015/12/29.
 */
public class DiskCache implements ImageCache {

    private final String IMAGE_CACHE_PATH = "IMAGE_CACHE";
    private DiskLruCache diskLruCache;
    private Handler handler = new Handler();

    public DiskCache() {
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

    @Override
    public void putBitmapToCache(String imageUrl, Bitmap bitmap) {
        final String key = Utils.MD5(imageUrl);
        getBitmapFromCacheAsync(imageUrl, new DiskCallback() {
            @Override
            public void callback(Bitmap bitmap) {
                if (bitmap == null) {
                    return;
                }
                DiskLruCache.Editor editor;
                try {
                    editor = diskLruCache.edit(key);
                    if (editor != null) {
                        OutputStream outputStream = editor.newOutputStream(0);
                        boolean success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
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
     * 从硬盘异步获取bitmap
     *
     * @param imageUrl
     * @param callback
     */
    @Override
    public void getBitmapFromCacheAsync(String imageUrl, final DiskCallback callback) {
        final String key = Utils.MD5(imageUrl);
        HttpQueue.getInstance().addQuest(new Runnable() {
            @Override
            public void run() {
                try {
                    DiskLruCache.Snapshot snapShot = diskLruCache.get(key);
                    if (snapShot != null) {
                        InputStream is = snapShot.getInputStream(0);
                        final Bitmap bitmap = BitmapFactory.decodeStream(is);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.callback(bitmap);
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
}
