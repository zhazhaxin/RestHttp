package cn.alien95.resthttp.request;

import java.util.Map;

import cn.alien95.resthttp.request.callback.HttpCallBack;


/**
 * Created by linlongxin on 2015/12/26.
 */
public class HttpRequest extends Http {

    private static HttpConnection httpConnection;

    private static HttpRequest instance;

    private HttpRequest() {
    }

    /**
     * 获取HttpRequest实例,这是一个单例模式
     *
     * @return HttpRequest一个实例
     */
    public static HttpRequest getInstance() {
        if (instance == null) {
            synchronized (HttpRequest.class) {
                if (instance == null)
                    instance = new HttpRequest();
            }
        }
        httpConnection = HttpConnection.getInstance();
        return instance;
    }

    /**
     * 设置请求头
     *
     * @param header
     */
    public void setHeader(Map<String, String> header) {
        httpConnection.setHttpHeader(header);
    }

    /**
     * GET请求
     *
     * @param url      请求地址
     * @param callBack 回调接口
     */
    @Override
    public void get(final String url, final HttpCallBack callBack) {
        //请求加入队列，队列通过start()方法自动请求网络
        RequestQueue.getInstance().addQuest(new Runnable() {
            @Override
            public void run() {
                httpConnection.quest(url, HttpConnection.RequestType.GET, null, callBack);
            }
        });
    }

    /**
     * POST请求
     *
     * @param url      请求地址
     * @param params   请求参数，HashMap的格式
     * @param callBack 回调接口
     */
    @Override
    public void post(final String url, final Map<String, String> params, final HttpCallBack callBack) {
        RequestQueue.getInstance().addQuest(new Runnable() {
            @Override
            public void run() {
                httpConnection.quest(url, HttpConnection.RequestType.POST, params, callBack);
            }
        });
    }

}
