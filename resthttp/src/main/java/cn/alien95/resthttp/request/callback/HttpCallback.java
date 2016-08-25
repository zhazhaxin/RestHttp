package cn.alien95.resthttp.request.callback;

import cn.alien95.resthttp.util.DebugLog;

/**
 * Created by linlongxin on 2015/12/26.
 */
public abstract class HttpCallback {


    public abstract void success(String info);

    public void failure(int status, String info) {

    }

    public void logNetworkInfo(int responseCode, String info, int requestNum) {
        if (DebugLog.isDebug)
            DebugLog.responseLog(responseCode + "\n" + info, requestNum);
    }

    public void logNetworkInfo(String info, int requestNum) {
        if (DebugLog.isDebug)
            DebugLog.responseLog(info, requestNum);
    }
}