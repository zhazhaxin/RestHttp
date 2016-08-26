package cn.alien95.resthttp.request.http;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.alien95.resthttp.request.Cache;
import cn.alien95.resthttp.request.Connection;
import cn.alien95.resthttp.request.Request;
import cn.alien95.resthttp.request.Response;
import cn.alien95.resthttp.request.ServerCache;
import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.util.DebugLog;
import cn.alien95.resthttp.util.RestHttpLog;
import cn.alien95.resthttp.util.Util;


/**
 * Created by linlongxin on 2015/12/26.
 */
public class HttpConnection extends Connection {

    public static final int NO_NETWORK = 999;
    protected Handler mHandler;

    public HttpConnection() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static HttpConnection getInstance() {
        return getInstance(HttpConnection.class);
    }

    /**
     * 网络请求
     */
    public void request(Request request) {
        String url = request.url;
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            requestURLConnection(urlConnection, request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestURLConnection(HttpURLConnection urlConnection, Request request) {
        String url = request.url;

        final HttpCallback callback;
        if (request.isHttps) {
            callback = request.httpsCallback;
        } else {
            callback = request.callback;
        }

        final int respondCode;
        String logUrl = getPostLog(url, request.params);

        /**
         * 打印网络请求日志
         */
        final int requestTime = DebugLog.requestLog(request.method,logUrl);
        try {
            urlConnection = configURLConnection(urlConnection, request); //配置HttpURLConnection
            respondCode = urlConnection.getResponseCode();
            InputStream in = urlConnection.getInputStream();
            /**
             * 重定向
             */
            if (respondCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                String location = urlConnection.getHeaderField("Location");
                request.url = location;
                requestURLConnection((HttpURLConnection) new URL(location).openConnection(), request);
                RestHttpLog.i("重定向url : " + location);
            } else if (respondCode == HttpURLConnection.HTTP_OK) {
                /**
                 * 状态码为200，请求成功。获取响应头，处理缓存
                 */
                Map<String, List<String>> headers = urlConnection.getHeaderFields();
                Set<String> keys = headers.keySet();
                HashMap<String, String> headersStr = new HashMap<>();
                RestHttpLog.i(logUrl + "  响应头信息：");
                for (String key : keys) {
                    String value = urlConnection.getHeaderField(key);
                    headersStr.put(key, value);
                    RestHttpLog.i(key + "  " + value);
                }

                final String result = readInputStream(in);
                in.close();

                Response response = new Response(result, headersStr);

                /**
                 * 打印Entry日志
                 */
                Cache.Entry entry = HttpHeaderParser.parseCacheHeaders(response);
                if (entry != null) {  //响应头带有缓存
                    ServerCache.getInstance().put(Util.getCacheKey(logUrl), entry);
                    RestHttpLog.i(entry.toString());
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.success(result);
                            callback.logNetworkInfo(respondCode, result, requestTime);
                        }
                    }
                });
            } else {
                in = urlConnection.getErrorStream();
                final String info = readInputStream(in);
                if (in != null) {
                    in.close();
                } else {
                    RestHttpLog.i("urlConnection.getErrorStream() == null,respondCode : " + respondCode);
                }

                /**
                 * 打印错误日志
                 */
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.failure(respondCode, info);
                            callback.logNetworkInfo(respondCode, info, requestTime);
                        }
                    }
                });
            }
        } catch (final IOException e1) {
            e1.printStackTrace();
            RestHttpLog.i("网络异常 : " + e1.getMessage());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.failure(NO_NETWORK, "抛出异常,没有连接网络");
                        callback.logNetworkInfo("抛出异常：" + e1.getMessage(), requestTime);
                    }
                }
            });
        }
    }

}
