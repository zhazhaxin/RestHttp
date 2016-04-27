package cn.alien95.resthttp.request;

import cn.alien95.resthttp.request.callback.HttpCallback;

/**
 * Created by linlongxin on 2016/4/27.
 */
public class Request {

    public int method;
    public String httpUrl;
    public HttpCallback callback;

    public Request(String httpUrl,HttpCallback callback){
        this.httpUrl = httpUrl;
        this.callback = callback;
    }

    public Request(String httpUrl, int method, HttpCallback callback){
        this.httpUrl = httpUrl;
        this.method = method;
        this.callback = callback;
    }
}
