package cn.alien95.resthttp.request.https;

import java.io.InputStream;
import java.util.Map;

import cn.alien95.resthttp.request.Method;
import cn.alien95.resthttp.request.Request;
import cn.alien95.resthttp.request.RequestDispatcher;
import cn.alien95.resthttp.request.ServerCache;
import cn.alien95.resthttp.request.ServerCacheDispatcher;
import cn.alien95.resthttp.request.callback.HttpsCallback;
import cn.alien95.resthttp.util.Util;

/**
 * Created by linlongxin on 2016/8/25.
 */

public class SelfSignHttpsRequest extends HttpsRequest {

    public static SelfSignHttpsRequest getInstance() {
        return getInstance(SelfSignHttpsRequest.class);
    }

    public void setCertificate(InputStream certificate) {
        SelfSignHttpsConnection.getInstance().setCertificate(certificate);
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
