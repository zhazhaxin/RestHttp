package cn.lemon.resthttp.request.https;

import java.util.Map;

import cn.lemon.resthttp.request.Method;
import cn.lemon.resthttp.request.Request;
import cn.lemon.resthttp.request.RequestDispatcher;
import cn.lemon.resthttp.request.ServerCache;
import cn.lemon.resthttp.request.ServerCacheDispatcher;
import cn.lemon.resthttp.request.callback.HttpsCallback;
import cn.lemon.resthttp.request.http.HttpRequest;
import cn.lemon.resthttp.util.Util;

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
