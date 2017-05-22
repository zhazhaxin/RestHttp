package cn.lemon.resthttp.request.https;

import java.io.InputStream;
import java.util.Map;

import cn.lemon.resthttp.request.Method;
import cn.lemon.resthttp.request.Request;
import cn.lemon.resthttp.request.RequestDispatcher;
import cn.lemon.resthttp.request.ServerCache;
import cn.lemon.resthttp.request.ServerCacheDispatcher;
import cn.lemon.resthttp.request.callback.HttpsCallback;
import cn.lemon.resthttp.util.Util;
import cn.lemon.resthttp.request.RestHttp;

/**
 * Created by linlongxin on 2016/8/25.
 */

public class SelfSignRequest extends HttpsRequest {

    public static SelfSignRequest getInstance() {
        return RestHttp.getInstance(SelfSignRequest.class);
    }

    public void setCertificate(InputStream certificate) {
        SelfSignRequestClient.getInstance().setCertificate(certificate);
    }

    @Override
    public void get(String url, HttpsCallback callBack) {
        Request request = new Request(url, Method.GET, null, true, callBack);
        if (ServerCache.getInstance().isExistsCache(Util.getCacheKey(url))) {
            ServerCacheDispatcher.getInstance().addCacheRequest(request);
        } else {
            RequestDispatcher.getInstance().addHttpsRequest(request);
        }

    }

    @Override
    public void post(String url, Map<String, String> params, HttpsCallback callBack) {
        Request request = new Request(url, Method.POST, params, true, callBack);
        if (ServerCache.getInstance().isExistsCache(Util.getCacheKey(url))) {
            ServerCacheDispatcher.getInstance().addCacheRequest(request);
        } else {
            RequestDispatcher.getInstance().addHttpsRequest(request);
        }
    }
}
