package cn.alien95.resthttp.request.rest;

import android.util.Log;

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
import java.util.Map;

import cn.alien95.resthttp.request.HttpConnection;
import cn.alien95.resthttp.util.DebugUtils;


/**
 * Created by linlongxin on 2016/3/24.
 */
public class RestHttpConnection {

    private static final String TAG = "RestHttpConnection";
    public static final int NO_NETWORK = 999;

    private HttpURLConnection urlConnection;
    private Map<String, String> header;
    private String logUrl;

    private RestHttpConnection() {
    }

    protected static RestHttpConnection getInstance() {
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
    protected void setHttpHeader(Map<String, String> header) {
        this.header = header;
    }

    /**
     * 需要同步，通过线程池并发执行
     *
     * @param type  请求方式{POST,GET}
     * @param param 请求的参数，HashMap键值对的形式
     */
    protected synchronized <T> T quest(String url, HttpConnection.RequestType type, Map<String, String> param, Class<T> returnType) {

        logUrl = url;
        final int respondCode;

        /**
         * 只有POST请求才应该有参数
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
         * 打印网络请求日志日志
         */
        int requestTime = DebugUtils.requestLog(logUrl);

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

            if (type.equals(HttpConnection.RequestType.POST)) {
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
                 * 请求成功
                 */
                final String result = readInputStream(in);
                in.close();

                if (DebugUtils.isDebug) {
                    DebugUtils.responseLog(respondCode + "\n" + result, requestTime);
                }

                Log.i("NetWork","returnType:" + returnType.getName());
                if(returnType != null && returnType != void.class){
                    Gson gson = new Gson();
                    T object = gson.fromJson(result, returnType);
                    return object;
                }
            }

        } catch (final IOException e1) {
            e1.printStackTrace();
            DebugUtils.responseLog(NO_NETWORK + "抛出异常：" + e1.getMessage(), requestTime);
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
