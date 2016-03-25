package cn.alien95.resthttplibrary;


import cn.alien95.resthttp.request.rest.callback.Callback;
import cn.alien95.resthttp.request.rest.method.POST;
import cn.alien95.resthttp.request.rest.param.Field;
import cn.alien95.resthttplibrary.bean.UserInfo;

/**
 * Created by linlongxin on 2016/3/23.
 */
public interface ServiceAPI {

    /**
     * 同步请求方式：不能包含Callback参数，切记：Android主线程不能进行网络操作
     * @param name
     * @param password
     * @return 返回一个经过Gson解析后的对象
     */

    @POST("/v1/users/login.php")
    UserInfo login(@Field("name")
                   String name,
                   @Field("password")
                   String password);

    /**
     * 异步请求：必须有一个Callback参数作为回调
     * @param name
     * @param password
     * @param callback  回调泛型类
     */

    @POST("/v1/users/login.php")
    void login2(@Field("name") String name, @Field("password") String password, Callback<UserInfo> callback);
}
