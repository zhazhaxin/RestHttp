package cn.alien95.resthttp.request.rest;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import cn.alien95.resthttp.request.Method;
import cn.alien95.resthttp.request.NetworkCache;
import cn.alien95.resthttp.request.NetworkCacheDispatcher;
import cn.alien95.resthttp.request.rest.callback.RestCallback;
import cn.alien95.resthttp.request.rest.method.GET;
import cn.alien95.resthttp.request.rest.method.POST;
import cn.alien95.resthttp.request.rest.param.Field;
import cn.alien95.resthttp.request.rest.param.Query;
import cn.alien95.resthttp.util.RestHttpLog;

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
    public Object create(Class<?> clazz) {

        return Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class[]{clazz}, new ServiceAPIHandler());
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

    class ServiceAPIHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, final Object[] args) throws Throwable {
            /**
             * 考虑同步处理任务时需要自己去处理线程问题，可能引起多线程安全问题，需要同步处理
             */
            synchronized (this) {
                Log.i("NetWork", "thread-name:" + Thread.currentThread().getName());
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
                        for (int i = 0; i < args.length; i++) {
                            if (args[i] instanceof RestCallback) {
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
                                            Method.GET, null, ((RestCallback) args[finalCallbackPosition]).getActualClass());
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((RestCallback) args[finalCallbackPosition]).callback(reuslt);
                                        }
                                    });
                                }
                            });
                        } else {
                            /**
                             * 同步处理任务
                             */
                            returnObject = RestHttpConnection.getInstance().quest(url.toString(),
                                    Method.GET, null, method.getReturnType());
                        }

                    } else if (methodAnnotation instanceof POST) {
                        /**
                         * -------------------------------POST请求处理---------------------------------
                         */
                        params.clear();

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
                        for (int i = 0; i < args.length; i++) {
                            if (args[i] instanceof RestCallback) {
                                isAsynchronization = true;
                                callbackPosition = i;
                            }
                        }
                        /**
                         * 异步处理任务
                         */
                        if (isAsynchronization) {
                            final int finalCallbackPosition1 = callbackPosition;

                            /**
                             * 缓存处理
                             */
                            if (NetworkCache.getInstance().isExistsCache(getCacheKey(url,params))) {
                                NetworkCacheDispatcher.getInstance().addRestCacheRequest(url, Method.POST, params, (RestCallback) args[finalCallbackPosition1]);
                                RestHttpLog.i("network cache is exists");
                            } else {
                                RestThreadPool.getInstance().putThreadPool(new Runnable() {
                                    @Override
                                    public void run() {

                                        final Object reuslt = RestHttpConnection.getInstance().quest(url,
                                                Method.POST, params, ((RestCallback) args[finalCallbackPosition1]).getActualClass());

                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((RestCallback) args[finalCallbackPosition1]).callback(reuslt);
                                            }
                                        });
                                    }

                                });
                            }

                        } else {
                            /**
                             * 同步处理任务，并且把结果返回给API方法.切记：Android不允许在主线程网络请求
                             */
                            returnObject = RestHttpConnection.getInstance().quest(url,
                                    Method.POST, params, method.getReturnType());
                        }

                    }
                }
                /**
                 * 执行的方法的返回值，如果方法是void，则返回null（默认）
                 */
                return returnObject;
            }
        }
    }


    private String getCacheKey(String url, Map<String, String> params) {
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
        return url;
    }
}
