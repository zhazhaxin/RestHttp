package cn.alien95.resthttp.request.rest;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import cn.alien95.resthttp.request.HttpConnection;
import cn.alien95.resthttp.request.rest.callback.Callback;
import cn.alien95.resthttp.request.rest.method.GET;
import cn.alien95.resthttp.request.rest.method.POST;
import cn.alien95.resthttp.request.rest.param.Field;
import cn.alien95.resthttp.request.rest.param.Query;

/**
 * Created by linlongxin on 2016/3/24.
 */
public class RestHttpRequest {

    private final String TAG = "RestHttpRequest";
    private Handler handler = new Handler(Looper.getMainLooper());
    private Map<String, String> params = new HashMap<>();
    /**
     * 通过动态代理，实例化接口
     *
     * @param clazz
     * @return
     */
    public synchronized Object create(Class<?> clazz) {

        Object object = Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class[]{
                        clazz
                }, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
                        Log.i("NetWork","invoke-method");
                        /**
                         * 是够同步，默认false(不同步)
                         */
                        boolean isAsynchronization = false;
                        /**
                         * 在异步的情况下，callback参数的位置下标记录
                         */
                        int callbackPosition = 0;

                        final Annotation[] annotations = method.getAnnotations();

                        Annotation[][] paramterAnnotations = method.getParameterAnnotations();
                        Class[] paramterTypes = method.getParameterTypes();

                        Object returnObject = null;

                        for (final Annotation methodAnnotation : annotations) {
                            /**
                             * -----------------------------------GET请求处理-------------------------------------
                             */
                            if (methodAnnotation instanceof GET) {

                                StringBuilder url = new StringBuilder(Builder.baseUrl + ((GET) methodAnnotation).value().toString() + "?");

                                for (int i = 0; i < paramterAnnotations.length; i++) {
                                    for (int k = 0; k < paramterAnnotations[i].length; k++) {
                                        if (paramterAnnotations[i][k] instanceof Query) {
                                            url = url.append(((Query) paramterAnnotations[i][k]).value() + "=" + args[i] + "&");
                                        }
                                    }
                                }

                                url = url.deleteCharAt(url.length() - 1);

                                /**
                                 * 判断是否异步处理
                                 */
                                for (int i = 0;i < args.length; i++) {
                                    if (args[i] instanceof Callback) {
                                        isAsynchronization = true;
                                        callbackPosition = i;
                                    }
                                }

                                if (isAsynchronization) {
                                    /**
                                     * 异步处理任务
                                     */
                                    final StringBuilder finalUrl2 = url;
                                    final int finalCallbackPosition = callbackPosition;
                                    RestThreadPool.getInstance().putThreadPool(new Runnable() {
                                        @Override
                                        public void run() {
                                            final Object reuslt = RestHttpConnection.getInstance().quest(finalUrl2.toString(),
                                                    HttpConnection.RequestType.GET, null, ((Callback) args[finalCallbackPosition]).getActualClass());
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ((Callback) args[finalCallbackPosition]).callback(reuslt);
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    /**
                                     * 同步处理任务
                                     */
                                    returnObject = RestHttpConnection.getInstance().quest(url.toString(),
                                            HttpConnection.RequestType.GET, null, method.getReturnType());
                                }

                            } else if (methodAnnotation instanceof POST) {
                                /**
                                 * -------------------------------POST请求处理---------------------------------
                                 */
                                params.clear();
                                if(params.isEmpty()){
                                    Log.i("NetWork","params.isEmpty()");
                                }

                                for (int i = 0; i < paramterAnnotations.length; i++) {
                                    Class paramterType = paramterTypes[i]; //这里可以看出每个参数对应一个注解数组，想不通。。。

                                    for (int k = 0; k < paramterAnnotations[i].length; k++) {
                                        if (paramterAnnotations[i][k] instanceof Field) {
                                            params.put(((Field) paramterAnnotations[i][k]).value(), args[i].toString());
                                        }
                                    }
                                }

                                final String url = Builder.baseUrl + ((POST) methodAnnotation).value();

                                /**
                                 * 判断是否异步回调
                                 */
                                for (int i = 0; i < args.length; i ++) {
                                    if (args[i] instanceof Callback) {
                                        isAsynchronization = true;
                                        callbackPosition = i;
                                    }
                                }
                                /**
                                 * 异步处理任务
                                 */
                                if (isAsynchronization) {
                                    final int finalCallbackPosition1 = callbackPosition;
                                    RestThreadPool.getInstance().putThreadPool(new Runnable() {
                                        @Override
                                        public void run() {

                                            for(Map.Entry<String,String> entry : params.entrySet()){
                                                Log.i("NetWork","异步key :　" + entry.getKey() + "    " + "异步value : " + entry.getValue());
                                            }

                                            final Object reuslt = RestHttpConnection.getInstance().quest(url,
                                                    HttpConnection.RequestType.POST, params, ((Callback) args[finalCallbackPosition1]).getActualClass());
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ((Callback) args[finalCallbackPosition1]).callback(reuslt);
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    /**
                                     * 同步处理任务，并且把结果返回给API方法.切记：Android不允许在主线程网络请求
                                     */
                                    for(Map.Entry<String,String> entry : params.entrySet()){
                                        Log.i("NetWork","同步key :　" + entry.getKey() + "    " + "同步value : " + entry.getValue());
                                    }
                                    returnObject = RestHttpConnection.getInstance().quest(url,
                                            HttpConnection.RequestType.POST, params, method.getReturnType());
                                }

                            }
                        }
                        /**
                         * 执行的方法的返回值，如果方法是void，则返回null（默认）
                         */
                        return returnObject;
                    }
                });

        return object;
    }


    /**
     * Builder模式来添加信息
     */
    public static final class Builder {

        private static String baseUrl = "";

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public RestHttpRequest build() {
            return new RestHttpRequest();
        }
    }
}
