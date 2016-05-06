package cn.alien95.resthttp.image;

import android.os.Handler;
import android.os.Looper;

import cn.alien95.resthttp.image.callback.ImageCallback;
import cn.alien95.resthttp.request.ThreadPool;
import cn.alien95.resthttp.util.DebugUtils;
import cn.alien95.resthttp.util.RestHttpLog;

/**
 * Created by linlongxin on 2016/4/26.
 */
public class RequestImageDispatcher {

    private Handler handler;

    public RequestImageDispatcher() {
        handler = new Handler(Looper.getMainLooper());
    }

    public void addRequestImg(String url, ImageCallback callback) {
        loadImg(url, 1, callback);
    }

    public void addRequestImgWithCompress(String url, int inSimpleSize, ImageCallback callback) {
        loadImg(url, inSimpleSize, callback);
    }

    public void addRequestImgWithCompress(String url, int reqWidth, int reqHeight, ImageCallback callback) {
        loadImgWithCompress(url, reqWidth, reqHeight, callback);
    }

    public void loadImg(final String url, final int inSampleSize, final ImageCallback callback) {

        RestHttpLog.i("Get picture from network");
        DebugUtils.requestImageLog(url);

        ThreadPool.getInstance().addRequestImg(url, inSampleSize, callback);
    }

    public synchronized void loadImgWithCompress(final String url, final int reqWidth, final int reqHeight, final ImageCallback callBack) {
        RestHttpLog.i("Get compress picture from network");
        DebugUtils.requestImageLog(url);

        ThreadPool.getInstance().addRequestImg(url, reqWidth, reqHeight, callBack);
    }


}
