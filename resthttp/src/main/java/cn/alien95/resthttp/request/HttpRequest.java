package cn.alien95.resthttp.request;

import java.util.Map;

import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.util.Util;


/**
 * Created by linlongxin on 2015/12/26.
 */
public class HttpRequest extends Http {

    private static HttpRequest instance;

    private HttpRequest() {
    }

    /**
     * 获取HttpRequest实例,这是一个单例模式
     *
     * @return HttpRequest一个实例
     */
    public static HttpRequest getInstance() {
        if (instance == null) {
            synchronized (HttpRequest.class) {
                if (instance == null)
                    instance = new HttpRequest();
            }
        }
        return instance;
    }

    /**
     * 设置请求头
     *
     * @param header
     */
    public void setHeader(Map<String, String> header) {
        RequestConnection.getInstance().setHttpHeader(header);
    }

    /**
     * GET请求
     *
     * @param url      请求地址
     * @param callBack 回调接口
     */
    @Override
    public void get(final String url, final HttpCallback callBack) {
        /**
         * 缓存判断
         */
        if (ServerCache.getInstance().isExistsCache(Util.getCacheKey(url))) {
            ServerCacheDispatcher.getInstance().addCacheRequest(url, Method.GET, null, callBack);
        } else
            RequestDispatcher.getInstance().addRequest(url, Method.GET, null, callBack);
    }

    /**
     * POST请求
     *
     * @param url      请求地址
     * @param params   请求参数，HashMap的格式
     * @param callBack 回调接口
     */
    @Override
    public void post(final String url, final Map<String, String> params, final HttpCallback callBack) {
        /**
         * 缓存判断
         */
        if (ServerCache.getInstance().isExistsCache(Util.getCacheKey(url, params))) {
            ServerCacheDispatcher.getInstance().addCacheRequest(url, Method.POST, params, callBack);
        } else
            RequestDispatcher.getInstance().addRequest(url, Method.POST, params, callBack);
    }

}
