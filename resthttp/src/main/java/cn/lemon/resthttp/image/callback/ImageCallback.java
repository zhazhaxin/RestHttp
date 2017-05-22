package cn.lemon.resthttp.image.callback;

import android.graphics.Bitmap;

public interface ImageCallback {

    void callback(Bitmap bitmap);
    void failure();
}