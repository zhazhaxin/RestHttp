package cn.alien95.resthttp.request.rest.callback;

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
