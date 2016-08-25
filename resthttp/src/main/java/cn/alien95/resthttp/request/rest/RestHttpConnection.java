package cn.alien95.resthttp.request.rest;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.alien95.resthttp.request.Cache;
import cn.alien95.resthttp.request.http.HttpHeaderParser;
import cn.alien95.resthttp.request.Method;
import cn.alien95.resthttp.request.Request;
import cn.alien95.resthttp.request.ServerCache;
import cn.alien95.resthttp.request.Response;
import cn.alien95.resthttp.util.DebugUtils;
import cn.alien95.resthttp.util.RestHttpLog;
import cn.alien95.resthttp.util.Util;


/**
 * Created by linlongxin on 2016/3/24.
 */
public class RestHttpConnection {

    private Map<String, String> header;

    private RestHttpConnection() {
    }

    public static RestHttpConnection getInstance() {
        return SingletonInstance.instance;
    }

    private static class SingletonInstance {
        private static final RestHttpConnection instance = new RestHttpConnection();
    }

    /**
     * 设置请求头header
     *
     * @param header 请求头内容
     */
    protected void setHeader(Map<String, String> header) {
        this.header = header;
    }

    protected void setHeader(String key, String value) {
        if (header == null) {
            header = new HashMap<>();
        }
        header.put(key, value);
    }

    /**
     * 这里很疑惑到底应该需不需要同步，看了JVM后应该觉得不需要同步处理，通过线程池并发执行
     */
    public <T> T request(Request<T> request) {

        String url = request.url;
        Map<String,String> param = request.params;
        int method = request.method;
        Class<T> returnType = request.resultType;

        String logUrl = url;
        final int respondCode;

        /**
         * 只有POST请求才应该有参数
         */
        StringBuilder paramStrBuilder = new StringBuilder();
        if (param != null) {
            synchronized (this) {
                for (Map.Entry<String, String> map : param.entrySet()) {
                    try {
                        paramStrBuilder = paramStrBuilder.append("&").append(URLEncoder.encode(map.getKey(), "UTF-8")).append("=")
                                .append(URLEncoder.encode(map.getValue(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                paramStrBuilder.deleteCharAt(0);
                logUrl = logUrl + "?" + paramStrBuilder;
            }
        }

        /**
         * 打印网络请求日志日志
         */
        int requestTime = DebugUtils.requestLog(logUrl);

        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setReadTimeout(10 * 1000);
            if (method == Method.GET) {
                urlConnection.setRequestMethod("GET");
            } else if (method == Method.POST) {
                urlConnection.setRequestMethod("POST");
            }

            if (header != null) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            if (method == Method.POST) {
                OutputStream ops = urlConnection.getOutputStream();
                ops.write(paramStrBuilder.toString().getBytes());
                ops.flush();
                ops.close();
            }

            /**
             * 对HttpURLConnection对象的一切配置都必须要在connect()函数执行之前完成。
             */
            urlConnection.connect();
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
                if (DebugUtils.isDebug) {
                    DebugUtils.responseLog(respondCode + info, requestTime);
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

                if (DebugUtils.isDebug) {
                    DebugUtils.responseLog(respondCode + "\n" + result, requestTime);
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
            DebugUtils.responseLog("NETWORK_ERROR" + " 抛出异常：" + e1.getMessage(), requestTime);
        }
        return null;
    }

    /**
     * 读取输入流信息，转化成String
     *
     * @param in
     * @return
     */
    private String readInputStream(InputStream in) {
        String result = "";
        String line;
        if (in != null) {
            BufferedReader bin = new BufferedReader(new InputStreamReader(in));
            try {
                while ((line = bin.readLine()) != null) {
                    result += line;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
