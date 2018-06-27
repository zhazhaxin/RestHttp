package cn.lemon.resthttp.request.rest;

import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import cn.lemon.resthttp.request.Method;
import cn.lemon.resthttp.request.Request;
import cn.lemon.resthttp.request.RequestDispatcher;
import cn.lemon.resthttp.request.ServerCache;
import cn.lemon.resthttp.request.ServerCacheDispatcher;
import cn.lemon.resthttp.request.callback.RestCallback;
import cn.lemon.resthttp.request.rest.method.GET;
import cn.lemon.resthttp.request.rest.param.Header;
import cn.lemon.resthttp.request.rest.method.POST;
import cn.lemon.resthttp.request.rest.param.Field;
import cn.lemon.resthttp.request.rest.param.Query;
import cn.lemon.resthttp.util.Util;

/**
 * Created by linlongxin on 2016/3/24.
 */
public class RestFactory {

    /**
     * 通过动态代理，实例化接口
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

        public Builder baseUrl(String url) {
            baseUrl = url;
            return this;
        }

        public RestFactory build() {
            return new RestFactory();
        }
    }

    private class ServiceAPIHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, final Object[] args) throws Throwable {
            /**
             * 是否异步，默认同步
             */
            boolean isAsync = false;
            /**
             * 在异步的情况下，callback参数的位置下标记录
             */
            int callbackPosition = 0;

            final Annotation[] annotations = method.getAnnotations();

            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            Class[] parameterTypes = method.getParameterTypes();

            Object resultObject = null;

            for (final Annotation methodAnnotation : annotations) {

                /**
                 * 请求头
                 */
                if (methodAnnotation instanceof Header) {
                    String headerStr = ((Header) methodAnnotation).value();
                    String[] header = headerStr.split(":");
                    RestRequestClient.getInstance().addHeader(header[0], header[1]);
                }
                /**
                 * -----------------------------------GET请求处理--------------------------------------------------------
                 */
                else if (methodAnnotation instanceof GET) {

                    StringBuilder url = new StringBuilder(Builder.baseUrl + ((GET) methodAnnotation).value() + "?");

                    for (int i = 0; i < parameterAnnotations.length; i++) {
                        for (int k = 0; k < parameterAnnotations[i].length; k++) {
                            if (parameterAnnotations[i][k] instanceof Query) {
                                url = url.append(((Query) parameterAnnotations[i][k]).value() + "=" + args[i] + "&");
                            }
                        }
                    }

                    url = url.deleteCharAt(url.length() - 1);

                    /**
                     * 判断是否异步处理
                     */
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] instanceof RestCallback) {
                            isAsync = true;
                            callbackPosition = i;
                        }
                    }

                    final String urlStr = url.toString();
                    final int finalCallbackPosition = callbackPosition;

                    if (isAsync) {
                        /**
                         * 异步处理任务，判断缓存
                         */
                        Request request = new Request(urlStr, Method.GET, null,
                                ((RestCallback) args[finalCallbackPosition]).getActualClass(),
                                (RestCallback) args[finalCallbackPosition]);
                        asyncRestRequest(request);
                    } else {
                        /**
                         * 同步处理任务
                         */
                        Request request = new Request(urlStr, Method.GET, null, method.getReturnType());
                        resultObject = syncRestRequest(request);
                    }

                } else if (methodAnnotation instanceof POST) {
                    /**
                     * -------------------------------POST请求处理----------------------------------------------------
                     */
                    final Map<String, String> params = new HashMap<>();

                    for (int i = 0; i < parameterAnnotations.length; i++) {
                        Class paramterType = parameterTypes[i]; //这里可以看出每个参数对应一个注解数组，想不通。。。

                        for (int k = 0; k < parameterAnnotations[i].length; k++) {
                            if (parameterAnnotations[i][k] instanceof Field) {
                                params.put(((Field) parameterAnnotations[i][k]).value(), args[i].toString());
                            }
                        }
                    }

                    final String url = Builder.baseUrl + ((POST) methodAnnotation).value();

                    /**
                     * 判断是否异步回调
                     */
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] instanceof RestCallback) {
                            isAsync = true;
                            callbackPosition = i;
                        }
                    }

                    /**
                     * 异步处理任务
                     */
                    if (isAsync) {
                        Request request = new Request(url, Method.POST, params,
                                ((RestCallback) args[callbackPosition]).getActualClass(),
                                (RestCallback) args[callbackPosition]);
                        asyncRestRequest(request);
                    } else {
                        Request request = new Request(url, Method.POST, params, method.getReturnType());
                        resultObject = syncRestRequest(request);
                    }

                }
            }
            /**
             * 执行的方法的返回值，如果方法是void，则返回null（默认）
             */
            return resultObject;
        }

    }

    //同步
    private Object syncRestRequest(Request request) {
        if (ServerCache.getInstance().isExistsCache(Util.getCacheKey(request.url))) {  //存在缓存
            return ServerCacheDispatcher.getInstance().getRestCacheSync(request);
        } else {  //无缓存
            Log.i("Network", "thread-name:" + Thread.currentThread().getName());
            return RestRequestClient.getInstance().request(request);
        }
    }

    //异步
    private void asyncRestRequest(Request request) {
        if (ServerCache.getInstance().isExistsCache(Util.getCacheKey(request.url))) {  //存在缓存
            ServerCacheDispatcher.getInstance().addCacheRequest(request);
        } else {  //无缓存
            RequestDispatcher.getInstance().addRestRequest(request);
        }
    }

}
