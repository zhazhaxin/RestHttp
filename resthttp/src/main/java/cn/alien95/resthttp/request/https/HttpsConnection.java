package cn.alien95.resthttp.request.https;

import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import cn.alien95.resthttp.request.Request;
import cn.alien95.resthttp.request.http.HttpConnection;

/**
 * Created by linlongxin on 2016/8/25.
 */

public class HttpsConnection extends HttpConnection {

    private static HttpsConnection mInstance;

    private HttpsConnection() {
        super();
    }

    public static HttpsConnection getInstance() {
        if (mInstance == null) {
            synchronized (HttpsConnection.class) {
                if (mInstance == null) {
                    mInstance = new HttpsConnection();
                }
            }
        }
        return mInstance;
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
