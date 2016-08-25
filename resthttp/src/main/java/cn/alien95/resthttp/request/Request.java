package cn.alien95.resthttp.request;

import java.util.Map;

import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.request.callback.HttpsCallback;
import cn.alien95.resthttp.request.callback.RestCallback;

/**
 * Created by linlongxin on 2016/4/27.
 */
public class Request<T> {

    public int method;
    public String url;
    public Map<String,String> params;

    public HttpCallback callback;

    public HttpsCallback httpsCallback;
    public boolean isHttps = false;

    public RestCallback<T> restCallback;
    public Class resultType;

    public Request(String url, int method, Map<String,String> params, HttpCallback callback){
        this.url = url;
        this.method = method;
        this.params = params;
        this.callback = callback;
    }

    /**
     * Https
     */
    public Request(String url, int method, Map<String,String> params, HttpsCallback httpsCallback) {
        this.method = method;
        this.url = url;
        this.params = params;
        this.httpsCallback = httpsCallback;
        isHttps = true;
    }

    /**
     * Restful接口请求缓存构造函数
     */
    public Request(String url, int method, Map<String,String> params, Class resultType, RestCallback<T> restCallback){
        this.url = url;
        this.method = method;
        this.params = params;
        this.resultType = resultType;
        this.restCallback = restCallback;
    }

    public Request(String url, int method, Map<String,String> params, Class resultType){
        this.url = url;
        this.method = method;
        this.params = params;
        this.resultType = resultType;
    }

}
