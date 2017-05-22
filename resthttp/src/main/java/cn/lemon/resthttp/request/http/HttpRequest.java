package cn.lemon.resthttp.request.http;

import java.util.Map;

import cn.lemon.resthttp.request.Method;
import cn.lemon.resthttp.request.Request;
import cn.lemon.resthttp.request.RequestDispatcher;
import cn.lemon.resthttp.request.RestHttp;
import cn.lemon.resthttp.request.ServerCache;
import cn.lemon.resthttp.request.ServerCacheDispatcher;
import cn.lemon.resthttp.request.callback.HttpCallback;
import cn.lemon.resthttp.util.Util;


/**
 * Created by linlongxin on 2015/12/26.
 */
public class HttpRequest extends RestHttp {

    public static HttpRequest getInstance(){
        return getInstance(HttpRequest.class);
    }

    public void addHeader(Map<String, String> header) {
        HttpRequestClient.getInstance().addHeader(header);
    }

    public void addHeader(String key, String value) {
        HttpRequestClient.getInstance().addHeader(key, value);
    }

    /**
     * GET
     */
    @Override
    public void get(final String url, final HttpCallback callBack) {
        Request request = new Request(url, Method.GET, null, callBack);
        httpRequest(request);
    }

    /**
     * POST
     */
    @Override
    public void post(final String url, final Map<String, String> params, final HttpCallback callBack) {
        Request request = new Request(url, Method.POST, params, callBack);
        httpRequest(request);
    }

    public void cancelAllRequest() {
        RequestDispatcher.getInstance().cancelAllNetRequest();
        RequestDispatcher.getInstance().cancelAllImageRequest();
    }

    public void cancelRequest(String httpUrl, Map<String, String> params) {
        RequestDispatcher.getInstance().cancelRequest(httpUrl, params);
    }

    public void cancelRequest(String httpUrl) {
        RequestDispatcher.getInstance().cancelRequest(httpUrl);
    }

    private void httpRequest(Request request){
        if (ServerCache.getInstance().isExistsCache(Util.getCacheKey(request.url))) {
            ServerCacheDispatcher.getInstance().addCacheRequest(request);
        } else
            RequestDispatcher.getInstance().addHttpRequest(request);
    }
}
