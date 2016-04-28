package cn.alien95.resthttp.request;

import java.util.Map;

import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.request.rest.callback.RestCallback;

/**
 * Created by linlongxin on 2016/4/27.
 */
public class Request {

    public int method;
    public String httpUrl;
    public Map<String,String> params;
    public HttpCallback callback;
    public RestCallback<?> restRestCallback;

    public Request(String httpUrl,int method, Map<String,String> params, HttpCallback callback){
        this.httpUrl = httpUrl;
        this.method = method;
        this.params = params;
        this.callback = callback;
    }

    /**
     * Restful接口请求缓存构造函数
     * @param httpUrl
     * @param method
     * @param params
     * @param restCallback
     */
    public Request(String httpUrl,int method,Map<String,String> params,RestCallback<?> restCallback){
        this.httpUrl = httpUrl;
        this.method = method;
        this.params = params;
        this.restRestCallback = restCallback;
    }

}
