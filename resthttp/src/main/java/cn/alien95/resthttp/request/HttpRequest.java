package cn.alien95.resthttp.request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import cn.alien95.resthttp.request.callback.HttpCallback;


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
         * 请求加入队列
         */
        if (NetworkCache.getInstance().isExistsCache(url)) {
            NetworkCacheDispatcher.getInstance().addCacheRequest(url,Method.GET,null,callBack);
        } else
            RequestQueue.getInstance().addRequest(url, Method.GET,null, callBack);
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
        if (NetworkCache.getInstance().isExistsCache(getCacheKey(url,params))) {
            NetworkCacheDispatcher.getInstance().addCacheRequest(url,Method.POST,params,callBack);
        } else
            RequestQueue.getInstance().addRequest(url, Method.POST, params, callBack);
    }

    private String getCacheKey(String url, Map<String, String> params) {
        /**
         * 只有POST才会有参数
         */
        StringBuilder paramStrBuilder = new StringBuilder();
        if (params != null) {
            for (Map.Entry<String, String> map : params.entrySet()) {
                try {
                    paramStrBuilder = paramStrBuilder.append("&").append(URLEncoder.encode(map.getKey(), "UTF-8")).append("=")
                            .append(URLEncoder.encode(map.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            paramStrBuilder.deleteCharAt(0);
            url = url + "?" + paramStrBuilder;
        }
        return url;
    }

}
