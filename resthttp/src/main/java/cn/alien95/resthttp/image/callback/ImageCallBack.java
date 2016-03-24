package cn.alien95.resthttp.image.callback;

import android.graphics.Bitmap;

/**
 * Created by linlongxin on 2015/12/26.
 */
public interface ImageCallBack{

    void success(Bitmap bitmap);
    void failure();

}
