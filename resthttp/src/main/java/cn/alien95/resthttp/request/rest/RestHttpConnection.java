package cn.alien95.resthttp.request.rest;

import com.google.gson.Gson;

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
import cn.alien95.resthttp.request.http.HttpHeaderParser;
import cn.alien95.resthttp.util.HttpLog;
import cn.alien95.resthttp.util.RestHttpLog;
import cn.alien95.resthttp.util.Util;


/**
 * Created by linlongxin on 2016/3/24.
 */
public class RestHttpConnection extends Connection{

    public static synchronized RestHttpConnection getInstance() {
        return getInstance(RestHttpConnection.class);
    }

    /**
     * 这里很疑惑到底应该需不需要同步，看了JVM后觉得应该不需要同步处理，通过线程池并发执行
     */
    public <T> T request(Request<T> request) {

        String url = request.url;
        Map<String,String> param = request.params;
        Class<T> returnType = request.resultType;

        final int respondCode;
        String logUrl = getPostLog(url,param);

        /**
         * 打印网络请求日志日志
         */
        int requestTime = HttpLog.requestLog(request.method,logUrl);

        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection = configURLConnection(urlConnection,request); //配置HttpURLConnection
            InputStream in = urlConnection.getInputStream();
            respondCode = urlConnection.getResponseCode();
            /**
             * 请求失败
             */
            if (respondCode != HttpURLConnection.HTTP_OK) {
                in = urlConnection.getErrorStream();
                final String info = readInputStream(in);
                in.close();
                /**
                 * 错误日志打印
                 */
                if (HttpLog.isDebug) {
                    HttpLog.responseLog(respondCode + info, requestTime);
                }

                return null;
            } else {
                /**
                 * 请求成功,处理缓存
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
                if (entry != null) {  //应该存储缓存数据
                    ServerCache.getInstance().put(Util.getCacheKey(logUrl), entry);
                    RestHttpLog.i(entry.toString());
                }

                if (HttpLog.isDebug) {
                    HttpLog.responseLog(respondCode + "\n" + result, requestTime);
                }

                if (returnType != null && returnType != void.class) {
                    if (returnType == String.class) {
                        return (T) result;
                    }
                    return new Gson().fromJson(result, returnType);
                }
            }

        } catch (final IOException e1) {
            e1.printStackTrace();
            HttpLog.responseLog("NETWORK_ERROR" + " 抛出异常：" + e1.getMessage(), requestTime);
        }
        return null;
    }

}
