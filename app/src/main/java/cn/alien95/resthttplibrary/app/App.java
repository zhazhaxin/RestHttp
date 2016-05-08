package cn.alien95.resthttplibrary.app;

import android.app.Application;

import cn.alien95.resthttp.request.RestHttp;
import cn.alien95.resthttplibrary.BuildConfig;


/**
 * Created by linlongxin on 2016/1/22.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        RestHttp.initialize(this);
        if(BuildConfig.DEBUG){
            RestHttp.setDebug(true,"NetWork");
        }
    }
}
