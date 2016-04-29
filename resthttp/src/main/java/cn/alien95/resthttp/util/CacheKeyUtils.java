package cn.alien95.resthttp.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by linlongxin on 2016/4/29.
 */
public class CacheKeyUtils {

    public static String getCacheKey(String url){
        return Utils.MD5(url);
    }

    public static String getCacheKey(String url, Map<String, String> params) {
        /**
         * 只有POST才会有参数
         */
        StringBuilder paramStrBuilder = new StringBuilder();
        if (params != null) {
            for (Map.Entry<String, String> map : params.entrySet()) {
                try {
                    paramStrBuilder = paramStrBuilder.append("&").append(URLEncoder.encode(map.getKey(), "UTF-8")).append("=")
                            .append(URLEncoder.encode(map.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            paramStrBuilder.deleteCharAt(0);
            url = url + "?" + paramStrBuilder;
        }
        RestHttpLog.i("cache-key :　" + url);
        return Utils.MD5(url);
    }


}
