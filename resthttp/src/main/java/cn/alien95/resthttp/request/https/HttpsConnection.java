package cn.alien95.resthttp.request.https;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import cn.alien95.resthttp.request.Request;
import cn.alien95.resthttp.request.callback.HttpsCallback;

/**
 * Created by linlongxin on 2016/8/25.
 */

public class HttpsConnection {

    private static HttpsConnection mInstance;
    private Map<String, String> mHeaders;
    private Handler mHandler;

    private HttpsConnection(){
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static HttpsConnection getInstance(){
        if(mInstance == null){
            synchronized (HttpsConnection.class){
                if(mInstance == null){
                    mInstance = new HttpsConnection();
                }
            }
        }
        return mInstance;
    }

    public void request(Request request){
        String url = request.url;
        Map<String,String> params = new HashMap<>();
        int method = request.method;
        HttpsCallback callback = request.httpsCallback;

        HttpsURLConnection urlConnection = null;
        try {
            urlConnection = (HttpsURLConnection)new URL(url).openConnection();
            InputStream in = urlConnection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
