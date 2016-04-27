package cn.alien95.resthttp.request.rest;

import cn.alien95.resthttp.request.callback.HttpCallback;

/**
 * Created by linlongxin on 2016/4/27.
 */
public class NetworkRequest {

    public int method;
    public String httpUrl;
    public HttpCallback callback;

    public NetworkRequest(String httpUrl, int method, HttpCallback callback){
        this.httpUrl = httpUrl;
        this.method = method;
        this.callback = callback;
    }
}
