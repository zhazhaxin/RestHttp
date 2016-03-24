package cn.alien95.resthttplibrary.app;

import android.app.Application;

import cn.alien95.resthttp.request.Http;
import cn.alien95.resthttplibrary.BuildConfig;


/**
 * Created by linlongxin on 2016/1/22.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Http.initialize(this);
        if(BuildConfig.DEBUG){
            Http.setDebug(true,"NetWork");
        }
    }
}
