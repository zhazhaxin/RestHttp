package cn.alien95.resthttp.image.callback;

import android.graphics.Bitmap;

public interface ImageCallback {

    void success(Bitmap bitmap);
    void failure();

}