package cn.alien95.resthttp.request.http;

import java.util.Map;

import cn.alien95.resthttp.request.Method;
import cn.alien95.resthttp.request.RequestDispatcher;
import cn.alien95.resthttp.request.RestHttp;
import cn.alien95.resthttp.request.ServerCache;
import cn.alien95.resthttp.request.ServerCacheDispatcher;
import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.util.Util;


/**
 * Created by linlongxin on 2015/12/26.
 */
public class RestHttpRequest extends RestHttp {

    private static RestHttpRequest instance;

    private RestHttpRequest() {
    }

    /**
     * 获取HttpRequest实例,这是一个单例模式
     *
     * @return HttpRequest一个实例
     */
    public static RestHttpRequest getInstance() {
        if (instance == null) {
            synchronized (RestHttpRequest.class) {
                if (instance == null)
                    instance = new RestHttpRequest();
            }
        }
        return instance;
    }

    /**
     * 设置请求头
     */
    public void setHeader(Map<String, String> header) {
        HttpConnection.getInstance().setHeader(header);
    }

    public void setHeader(String key, String value) {
        HttpConnection.getInstance().setHeader(key, value);
    }

    /**
     * GET请求
     */
    @Override
    public void get(final String url, final HttpCallback callBack) {
        /**
         * 缓存判断
         */
        if (ServerCache.getInstance().isExistsCache(Util.getCacheKey(url))) {
            ServerCacheDispatcher.getInstance().addCacheRequest(url, Method.GET, null, callBack);
        } else
            RequestDispatcher.getInstance().addNetRequest(url, Method.GET, null, callBack);
    }

    /**
     * POST请求
     */
    @Override
    public void post(final String url, final Map<String, String> params, final HttpCallback callBack) {
        /**
         * 缓存判断
         */
        if (ServerCache.getInstance().isExistsCache(Util.getCacheKey(url, params))) {
            ServerCacheDispatcher.getInstance().addCacheRequest(url, Method.POST, params, callBack);
        } else
            RequestDispatcher.getInstance().addNetRequest(url, Method.POST, params, callBack);
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

}
