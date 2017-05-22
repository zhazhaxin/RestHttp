package cn.lemon.resthttp.request;

import java.util.Map;

import cn.lemon.resthttp.request.callback.HttpCallback;
import cn.lemon.resthttp.request.callback.HttpsCallback;
import cn.lemon.resthttp.request.callback.RestCallback;

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
    public boolean isSelfSign = false; //是否是自签名证书

    public RestCallback<T> restCallback;
    public Class resultType;

    /**
     * Http
     */
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
     * Https自签名证书
     */
    public Request(String url, int method, Map<String,String> params,boolean isSelfSign, HttpsCallback httpsCallback) {
        this.method = method;
        this.url = url;
        this.params = params;
        this.isSelfSign = isSelfSign;
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
