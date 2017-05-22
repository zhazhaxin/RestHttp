package cn.lemon.resthttp.request.callback;

import java.lang.reflect.ParameterizedType;

/**
 * Created by linlongxin on 2016/3/25.
 */
public abstract class RestCallback<T> {

    public abstract void callback(T result);

    public Class getActualClass(){
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
