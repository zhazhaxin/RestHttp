package cn.alien95.resthttp.image.cache;

import cn.alien95.resthttp.image.callback.ImageCallback;

public class Requst {

    public String url;
    public int inSimpleSize;
    public ImageCallback callback;

    public Requst(String url, ImageCallback callback) {
        this.url = url;
        this.callback = callback;
    }

    public Requst(String url,int inSimpleSize,ImageCallback callback){
        this.url = url;
        this.inSimpleSize = inSimpleSize;
        this.callback = callback;
    }
}