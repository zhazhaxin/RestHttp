package cn.alien95.resthttp.request;

import android.os.Handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import cn.alien95.resthttp.request.callback.HttpCallBack;
import cn.alien95.resthttp.util.DebugUtils;


/**
 * Created by linlongxin on 2015/12/26.
 */
public class HttpConnection {

    public static final int NO_NETWORK = 999;
    private HttpURLConnection urlConnection;
    private Handler handler = new Handler();
    private Map<String, String> header;
    private String logUrl;

    private HttpConnection() {
    }

    protected static HttpConnection getInstance() {
        return SingtonInstance.instance;
    }

    private static class SingtonInstance{
        private static final HttpConnection instance = new HttpConnection();
    }

    public enum RequestType {
        GET("GET"), POST("POST");
        private String requestType;

        RequestType(String type) {
            this.requestType = type;
        }
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
     * @param type     请求方式{POST,GET}
     * @param param    请求的参数，HashMap键值对的形式
     * @param callback 请求返回的回调
     */
    protected void quest(String url, RequestType type, Map<String, String> param, final HttpCallBack callback) {

        logUrl = url;
        final int respondCode;

        /**
         * 只有POST才会有参数
         */
        String paramStr = "";
        if (param != null) {
            for (Map.Entry<String, String> map : param.entrySet()) {
                try {
                    paramStr += "&" + URLEncoder.encode(map.getKey(), "UTF-8") + "=" + URLEncoder.encode(map.getValue(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        logUrl += paramStr;
        /**
         * 打印网络请求日志
         */
        final int requestTime = DebugUtils.requestLog(logUrl);
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setReadTimeout(10 * 1000);
            urlConnection.setRequestMethod(String.valueOf(type));

            if (header != null) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            if (type.equals(RequestType.POST)) {
                OutputStream ops = urlConnection.getOutputStream();
                ops.write(paramStr.getBytes());
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
                return;
            } else {
                final String result = readInputStream(in);
                in.close();
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
