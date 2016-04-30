package cn.alien95.resthttp.request;

import android.os.Handler;
import android.os.Looper;

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

import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.util.CacheKeyUtils;
import cn.alien95.resthttp.util.DebugUtils;
import cn.alien95.resthttp.util.RestHttpLog;


/**
 * Created by linlongxin on 2015/12/26.
 */
public class RequestConnection {

    public static final int NO_NETWORK = 999;
    private Map<String, String> header;
    private Handler handler;

    private RequestConnection() {
        handler = new Handler(Looper.getMainLooper());
    }

    protected static RequestConnection getInstance() {
        return SingtonInstance.instance;
    }

    private static class SingtonInstance {
        private static final RequestConnection instance = new RequestConnection();
    }

    /**
     * 设置请求头header
     *
     * @param header 请求头内容
     */
    protected void setHttpHeader(Map<String, String> header) {
        this.header = header;
    }

    /**
     * 网络请求
     *
     * @param method   请求方式{POST,GET}
     * @param param    请求的参数，HashMap键值对的形式
     * @param callback 请求返回的回调
     */
    protected void quest(String url, int method, Map<String, String> param, final HttpCallback callback) {

        String logUrl = url;
        final int respondCode;

        /**
         * 只有POST才会有参数
         */
        StringBuilder paramStrBuilder = new StringBuilder();
        if (param != null) {
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

        /**
         * 打印网络请求日志
         */
        final int requestTime = DebugUtils.requestLog(logUrl);
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setReadTimeout(10 * 1000);
            if(method == Method.GET){
                urlConnection.setRequestMethod("GET");
            }else if(method == Method.POST){
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
                final int finalRespondCode = respondCode;
                final String info = readInputStream(in);
                in.close();
                /**
                 * 打印错误日志
                 */
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.failure(finalRespondCode, info);
                            callback.logNetworkInfo(respondCode, info, requestTime);
                        }
                    }
                });
            } else {
                /**
                 * 状态码为200，请求成功。获取相应头，处理缓存
                 */
                Map<String, List<String>> headers = urlConnection.getHeaderFields();
                Set<String> keys = headers.keySet();
                HashMap<String, String> headersStr = new HashMap<>();
                RestHttpLog.i(logUrl + "响应头信息：");
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
                    NetworkCache.getInstance().put(CacheKeyUtils.getCacheKey(logUrl), entry);
                    RestHttpLog.i(entry.toString());
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.success(result);
                            callback.logNetworkInfo(respondCode, result, requestTime);
                        }
                    }
                });
            }
        } catch (final IOException e1) {
            e1.printStackTrace();
            handler.post(new Runnable() {
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
