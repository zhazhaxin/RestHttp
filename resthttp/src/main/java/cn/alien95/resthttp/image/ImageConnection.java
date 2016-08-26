package cn.alien95.resthttp.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.alien95.resthttp.image.cache.DiskCache;
import cn.alien95.resthttp.image.cache.ImageRequest;
import cn.alien95.resthttp.image.cache.MemoryCache;
import cn.alien95.resthttp.util.Util;

/**
 * Created by linlongxin on 2016/8/26.
 */

public class ImageConnection {

    private static ImageConnection mInstance;
    private Handler mHandler;

    private ImageConnection(){
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static ImageConnection getInstance(){
        if(mInstance == null) {
            synchronized (ImageConnection.class){
                if(mInstance == null){
                    mInstance = new ImageConnection();
                }
            }
        }
        return mInstance;
    }

    public void request(ImageRequest request) {
        String url = request.url;
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            requestURLConnection(urlConnection, request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestURLConnection(HttpURLConnection urlConnection, final ImageRequest request) {
        try {
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setReadTimeout(10 * 1000);
            final InputStream inputStream = urlConnection.getInputStream();
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                //如果两次都调用BitmapFactory.decodeStream,由于输入流失有序的输入流，第二次会得到null
                byte[] bytes = new byte[0];
                try {
                    bytes = readStream(inputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (request.inSampleSize == 0) {
                    options.inSampleSize = Util.calculateInSampleSize(options, request.reqWidth, request.reqHeight);
                } else {
                    options.inSampleSize = request.inSampleSize;
                }
                options.inJustDecodeBounds = false;

                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                String key;
                if (bitmap != null) {
                    if (request.isControlWidthAndHeight) {
                        key = Util.getCacheKey(request.url + request.reqWidth + "/" + request.reqHeight);
                    } else if (request.inSampleSize <= 1) {
                        key = Util.getCacheKey(request.url);
                    } else {
                        key = Util.getCacheKey(request.url + request.inSampleSize);
                    }
                    MemoryCache.getInstance().put(key, bitmap);
                    DiskCache.getInstance().put(key, bitmap);

                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        request.callback.callback(bitmap);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从inputStream中获取字节流 数组大小
     *
     * @param inStream
     * @return
     * @throws Exception
     */
    public byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }
}
