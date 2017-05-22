package cn.lemon.resthttp.request.http;

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

import cn.lemon.resthttp.request.Cache;
import cn.lemon.resthttp.request.ConfigClient;
import cn.lemon.resthttp.request.Request;
import cn.lemon.resthttp.request.Response;
import cn.lemon.resthttp.request.ServerCache;
import cn.lemon.resthttp.request.callback.HttpCallback;
import cn.lemon.resthttp.util.HttpLog;
import cn.lemon.resthttp.util.RestHttpLog;
import cn.lemon.resthttp.util.Util;


/**
 * Created by linlongxin on 2015/12/26.
 */
public class HttpRequestClient extends ConfigClient {

    public static final int NO_NETWORK = 999;
    protected Handler mHandler;

    public HttpRequestClient() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static HttpRequestClient getInstance() {
        return getInstance(HttpRequestClient.class);
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

        int respondCode = -1;
        String logUrl = getPostLog(url, request.params);

        /**
         * 打印网络请求日志
         */
        final int requestTime = HttpLog.requestLog(request.method, logUrl);
        try {
            urlConnection = configURLConnection(urlConnection, request); //配置HttpURLConnection
            respondCode = urlConnection.getResponseCode();
            InputStream in = urlConnection.getInputStream();

            // 重定向
            if (respondCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                String location = urlConnection.getHeaderField("Location");
                request.url = location;
                HttpLog.Log("302重定向Location : " + location);
                requestURLConnection((HttpURLConnection) new URL(location).openConnection(), request);
            } else if (respondCode == HttpURLConnection.HTTP_OK) {
                //状态码为200，请求成功。获取响应头，处理缓存
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

                // 打印Entry日志
                Cache.Entry entry = HttpHeaderParser.parseCacheHeaders(response);
                if (entry != null) {  //响应头带有缓存
                    ServerCache.getInstance().put(Util.getCacheKey(logUrl), entry);
                    RestHttpLog.i(entry.toString());
                }

                final int logCode = respondCode;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.success(result);
                            HttpLog.responseLog(logCode + "\n" + result, requestTime);
                        }
                    }
                });
            } else {
                final String info = readInputStream(in);
                if (in != null) {
                    in.close();
                } else {
                    RestHttpLog.i("urlConnection.getErrorStream() == null,respondCode : " + respondCode);
                }

                //打印错误日志
                final int logCode = respondCode;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.failure(logCode, info);
                            HttpLog.responseLog(logCode + "\n" + info, requestTime);
                        }
                    }
                });
            }
        } catch (final IOException e) {
            if (RestHttpLog.isDebug()) {
                RestHttpLog.i("网络异常 : " + url);
                e.printStackTrace();
            }
            final int logCode = respondCode;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.failure(NO_NETWORK, "抛出异常,没有连接网络");
                        HttpLog.responseLog(logCode + " 抛出异常：" + e.getMessage(), requestTime);
                    }
                }
            });
        }
    }

}
