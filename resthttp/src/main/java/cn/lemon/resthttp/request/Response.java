package cn.lemon.resthttp.request;

import java.util.Map;

/**
 * Created by linlongxin on 2016/4/27.
 */
public class Response {

    public String data;
    public Map<String, String> headers;

    public Response(String data, Map<String, String> headers) {
        this.data = data;
        this.headers = headers;
    }
}
