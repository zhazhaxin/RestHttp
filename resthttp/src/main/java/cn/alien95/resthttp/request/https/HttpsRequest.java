package cn.alien95.resthttp.request.https;

import java.util.Map;

import cn.alien95.resthttp.request.Method;
import cn.alien95.resthttp.request.Request;
import cn.alien95.resthttp.request.RequestDispatcher;
import cn.alien95.resthttp.request.ServerCache;
import cn.alien95.resthttp.request.ServerCacheDispatcher;
import cn.alien95.resthttp.request.callback.HttpsCallback;
import cn.alien95.resthttp.request.http.HttpRequest;
import cn.alien95.resthttp.util.Util;

/**
 * Created by linlongxin on 2016/8/24.
 */

public class HttpsRequest extends HttpRequest {

    public static HttpsRequest getInstance() {
        return getInstance(HttpsRequest.class);
    }

    public void addHeader(Map<String, String> header) {
        HttpsRequestClient.getInstance().addHeader(header);
    }

    public void addHeader(String key, String value) {
        HttpsRequestClient.getInstance().addHeader(key, value);
    }

    @Override
    public void get(String url, HttpsCallback callBack) {
        Request request = new Request(url, Method.GET, null, callBack);
        httpsRequest(request);
    }

    @Override
    public void post(String url, Map<String, String> params, HttpsCallback callBack) {
        Request request = new Request(url, Method.POST, params, callBack);
        httpsRequest(request);
    }

    private void httpsRequest(Request request){
        if (ServerCache.getInstance().isExistsCache(Util.getCacheKey(request.url))) {
            ServerCacheDispatcher.getInstance().addCacheRequest(request);
        } else {
            RequestDispatcher.getInstance().addHttpsRequest(request);
        }
    }
}
