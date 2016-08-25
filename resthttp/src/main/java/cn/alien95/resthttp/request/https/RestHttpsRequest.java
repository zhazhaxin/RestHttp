package cn.alien95.resthttp.request.https;

import java.util.Map;

import cn.alien95.resthttp.request.Method;
import cn.alien95.resthttp.request.Request;
import cn.alien95.resthttp.request.RequestDispatcher;
import cn.alien95.resthttp.request.ServerCache;
import cn.alien95.resthttp.request.ServerCacheDispatcher;
import cn.alien95.resthttp.request.callback.HttpsCallback;
import cn.alien95.resthttp.request.http.RestHttpRequest;
import cn.alien95.resthttp.util.Util;

/**
 * Created by linlongxin on 2016/8/24.
 */

public class RestHttpsRequest extends RestHttpRequest {

    private static RestHttpsRequest mInstance;

    private RestHttpsRequest() {
        super();
    }

    public static RestHttpsRequest getInstance() {
        if (mInstance == null) {
            synchronized (RestHttpsRequest.class) {
                if (mInstance == null) {
                    mInstance = new RestHttpsRequest();
                }
            }
        }
        return mInstance;
    }

    public void addHeader(Map<String, String> header) {
        HttpsConnection.getInstance().addHeader(header);
    }

    public void addHeader(String key, String value) {
        HttpsConnection.getInstance().addHeader(key, value);
    }

    @Override
    public void get(String url, HttpsCallback callBack) {
        if (ServerCache.getInstance().isExistsCache(Util.getCacheKey(url))) {
            ServerCacheDispatcher.getInstance().addCacheRequest(url, Method.GET, null, callBack);
        } else {
            RequestDispatcher.getInstance().addHttpsRequest(new Request(url, Method.GET, null, callBack));
        }

    }

    @Override
    public void post(String url, Map<String, String> params, HttpsCallback callBack) {
        if (ServerCache.getInstance().isExistsCache(Util.getCacheKey(url, params))) {
            ServerCacheDispatcher.getInstance().addCacheRequest(url, Method.POST, params, callBack);
        } else {
            RequestDispatcher.getInstance().addHttpsRequest(new Request(url, Method.POST, params, callBack));
        }

    }
}
