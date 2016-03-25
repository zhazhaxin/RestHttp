package cn.alien95.resthttp.request.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import cn.alien95.resthttp.request.HttpConnection;
import cn.alien95.resthttp.request.rest.method.GET;
import cn.alien95.resthttp.request.rest.method.POST;
import cn.alien95.resthttp.request.rest.param.Field;
import cn.alien95.resthttp.request.rest.param.Query;

/**
 * Created by linlongxin on 2016/3/24.
 */
public class RestHttpRequest {

    /**
     * 通过动态代理，实例化接口
     * @param clss
     * @return
     */
    public Object create(Class<?> clss) {
        Object object = Proxy.newProxyInstance(clss.getClassLoader(),
                new Class[]{
                        clss
                }, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, final Method method, Object[] args) throws Throwable {
                        Annotation[] annotations = method.getAnnotations();

                        Annotation[][] paramterAnnotations = method.getParameterAnnotations();
                        Class[] paramterTypes = method.getParameterTypes();

                        final Map<String, String> params = new HashMap<>();

                        Object returnObject = null;

                        for (final Annotation methodAnnotation : annotations) {
                            /**
                             * GET请求处理
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

                                if (method.getReturnType() != Void.class) {
                                    final StringBuilder finalUrl = url;
                                    /**
                                     * 任务加入线程池
                                     */
                                    Future result = RestThreadPool.getInstance().putThreadPool(new Callable<Object>() {
                                        @Override
                                        public Object call() throws Exception {
                                            return RestHttpConnection.getInstance().quest(finalUrl.toString(), HttpConnection.RequestType.GET,null,method.getReturnType());
                                        }
                                    });
                                    returnObject = result.get();
                                }
                            } else if (methodAnnotation instanceof POST) {
                                /**
                                 * POST请求处理
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

                                /**
                                 * 任务加入线程池
                                 */
                                Future result = RestThreadPool.getInstance().putThreadPool(new Callable() {
                                    @Override
                                    public Object call() throws Exception {
                                        return RestHttpConnection.getInstance().quest(Builder.baseUrl + ((POST) methodAnnotation).value(), HttpConnection.RequestType.POST,params,method.getReturnType());
                                    }
                                });
                                returnObject = result.get();
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
    public static final class Builder{

        private static String baseUrl = "";

        public Builder baseUrl(String baseUrl){
            this.baseUrl = baseUrl;
            return this;
        }

        public RestHttpRequest build(){
            return new RestHttpRequest();
        }
    }
}
