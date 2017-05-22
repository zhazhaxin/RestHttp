package cn.lemon.resthttp.request.https;

import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import cn.lemon.resthttp.request.Request;
import cn.lemon.resthttp.request.http.HttpRequestClient;

/**
 * Created by linlongxin on 2016/8/25.
 */

public class HttpsRequestClient extends HttpRequestClient {

    public static HttpsRequestClient getInstance() {
        return getInstance(HttpsRequestClient.class);
    }

    public void request(Request request) {
        String url = request.url;
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(url).openConnection();
            requestURLConnection(urlConnection, request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
