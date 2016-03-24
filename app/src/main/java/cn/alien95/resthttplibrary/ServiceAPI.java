package cn.alien95.resthttplibrary;


import cn.alien95.resthttp.request.rest.method.POST;
import cn.alien95.resthttp.request.rest.param.Field;
import cn.alien95.resthttplibrary.bean.UserInfo;

/**
 * Created by linlongxin on 2016/3/23.
 */
public interface ServiceAPI {

    @POST("/v1/users/login.php")
    UserInfo login(@Field("name")
                   String name,
                   @Field("password")
                   String password);
}
