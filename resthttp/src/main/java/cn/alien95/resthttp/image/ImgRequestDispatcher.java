package cn.alien95.resthttp.image;

import cn.alien95.resthttp.image.callback.ImageCallback;
import cn.alien95.resthttp.request.RequestDispatcher;
import cn.alien95.resthttp.util.DebugUtils;
import cn.alien95.resthttp.util.RestHttpLog;

/**
 * Created by linlongxin on 2016/4/26.
 */
public class ImgRequestDispatcher {

    /**
     * 这里没有请求队列是因为使用了ThreadPool里面的请求队列
     */

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

        RequestDispatcher.getInstance().addRequestImg(url, inSampleSize, callback);
    }

    public void loadImgWithCompress(final String url, final int reqWidth, final int reqHeight, final ImageCallback callBack) {
        RestHttpLog.i("Get compress picture from network");
        DebugUtils.requestImageLog(url);

        RequestDispatcher.getInstance().addRequestImg(url, reqWidth, reqHeight, callBack);
    }


}
