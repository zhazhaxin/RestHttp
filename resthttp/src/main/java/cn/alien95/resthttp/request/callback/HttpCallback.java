package cn.alien95.resthttp.request.callback;

/**
 * Created by linlongxin on 2015/12/26.
 */
public abstract class HttpCallback {


    public abstract void success(String info);

    public void failure(int status, String info) {

    }
}