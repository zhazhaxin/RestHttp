package cn.alien95.resthttp.image.cache;

import android.graphics.Bitmap;


/**
 * Created by linlongxin on 2016/3/14.
 */
public interface ImgCache {

    void put(String key, Bitmap bitmap);

    Bitmap get(String key);

    boolean isExist(String key);

    void remove(String key);

    void clear();
}
