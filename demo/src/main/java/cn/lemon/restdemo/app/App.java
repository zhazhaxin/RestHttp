package cn.lemon.restdemo.app;

import android.app.Application;

import cn.alien95.restdemo.BuildConfig;
import cn.lemon.resthttp.request.RestHttp;
import cn.alien95.util.Utils;


/**
 * Created by linlongxin on 2016/1/22.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        RestHttp.initialize(this);
        Utils.initialize(this);
        RestHttp.setDiskCacheSize(100 * 1024 * 1024);
        if (BuildConfig.DEBUG) {
            Utils.setDebug(true,"Debug");
            RestHttp.setDebug(true, "network");
        }
    }
}
