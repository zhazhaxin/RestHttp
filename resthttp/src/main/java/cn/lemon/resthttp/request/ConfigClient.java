package cn.lemon.resthttp.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import cn.lemon.resthttp.util.RestHttpLog;

/**
 * Created by linlongxin on 2016/8/25.
 */

public abstract class ConfigClient {

    //全局静态Headers共用
    private static Map<String, String> mHeaders;
    private static Map<String, ConfigClient> mInstanceMap = new HashMap<>();

    //这样去写单例模式虽然可以省去很多代码，不过因为newInstance方法有限制：构造函数必须public,必须有一个构造函数没有参数
    public static <T extends ConfigClient> T getInstance(Class<T> conn) {
        String key = conn.getSimpleName();
        if (!mInstanceMap.containsKey(key)) {
            synchronized (ConfigClient.class) {
                if (!mInstanceMap.containsKey(key)) {
                    try {
                        T instance = conn.newInstance();
                        mInstanceMap.put(key, instance);
                        return instance;
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                        return null;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }
        return (T) mInstanceMap.get(key);
    }

    public void addHeader(Map<String, String> header) {
        mHeaders = header;
    }

    public void addHeader(String key, String value) {
        if (mHeaders == null) {
            mHeaders = new HashMap<>();
        }
        mHeaders.put(key, value);
    }

    public void clearHeaders() {
        mHeaders = null;
    }

    public String getPostLog(String baseUrl, Map<String, String> params) {
        String paramsStr = getPostParamBody(params);
        if (paramsStr.isEmpty()) {
            return baseUrl;
        } else
            return baseUrl + "?" + paramsStr;
    }

    private String getPostParamBody(Map<String, String> params) {

        if (params != null) {
            StringBuilder paramStrBuilder = new StringBuilder();
            synchronized (this) {
                for (Map.Entry<String, String> map : params.entrySet()) {
                    try {
                        paramStrBuilder = paramStrBuilder.append("&").append(URLEncoder.encode(map.getKey(), "UTF-8")).append("=")
                                .append(URLEncoder.encode(map.getValue(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                paramStrBuilder.deleteCharAt(0);
            }
            return paramStrBuilder.toString();
        } else {
            return "";
        }
    }

    public HttpURLConnection configURLConnection(HttpURLConnection urlConnection, Request request) {
        try {
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setReadTimeout(10 * 1000);
            urlConnection.setInstanceFollowRedirects(true);  //重定向默认是true
            if (request.method == Method.GET) {
                urlConnection.setRequestMethod("GET");
            } else if (request.method == Method.POST) {
                urlConnection.setRequestMethod("POST");
            }

            if (mHeaders != null) {
                for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
                    urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
                    RestHttpLog.i("header : " + entry.getKey() + "  " + entry.getValue());
                }
            }

            if (request.method == Method.POST) {
                OutputStream ops = urlConnection.getOutputStream();
                ops.write(getPostParamBody(request.params).getBytes());
                ops.flush();
                ops.close();
            }
            urlConnection.connect();
        } catch (IOException e) {
            if (RestHttpLog.isDebug()) {
                e.printStackTrace();
            }
            return null;
        }
        return urlConnection;
    }

    /**
     * 读取输入流信息，转化成String
     */
    public String readInputStream(InputStream in) {
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
