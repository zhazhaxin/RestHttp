### RestHttp网络库

- gradle依赖：

        compile 'cn.alien95:resthttp:1.0.0'

- 初始化：设置是否开启调试模式（默认关闭）,设置日志Log打印Tag
    
        Http.initialize(this);
        if(BuildConfig.DEBUG){
                    Http.setDebug(true,"NetWork");
                }

- 面向接口请求：

        在接口中写好API

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

        
  java代码：

        final RestHttpRequest restHttpRequest = new RestHttpRequest.Builder()
                        .baseUrl(BASE_URL)
                        .build();
        
                final ServiceAPI serviceAPI = (ServiceAPI) restHttpRequest.create(ServiceAPI.class);
        
                /**
                 * 同步操作,不受线程池控制，自己处理线程问题
                 */
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final UserInfo userInfo = serviceAPI.login("Lemon" , "123456");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                post.setText(post.getText().toString() + "\n\n"
                                        + userInfo.toString());
                            }
                        });
                    }
                }).start();
        
                /**
                 * 异步操作，受线程池控制
                 */
                serviceAPI.login2("alien95", "123456" ,new Callback<UserInfo>() {
                    @Override
                    public void callback(UserInfo result) {
                        post.setText(post.getText().toString() + "\n\n"
                                + result.toString());
                    }
                });
        
        
- 普通请求方式：
        
    (1)get请求：
        
                HttpRequest.getInstance().get(GET_URL, new HttpCallBack() {
                            @Override
                            public void success(String info) {
                                get.setText("GET:\n" + info);
                            }
                        });
        
    (2)post请求：
        
                 public void httpPostRequest() {
                        Map<String, String> params = new HashMap<>();
                        params.put("name", "alien95");
                        params.put("password", "123456");
                        HttpRequest.getInstance().post(POST_URL, params, new HttpCallBack() {
                            @Override
                            public void success(String info) {
                                post.setText("POST:\n" + info);
                            }
                            
    (3)添加header

        public void setHttpHeader(Map<String, String> header);

- 图片加载（包括了内存缓存和硬盘缓存）

    (1)加载小图：

         <cn.alien95.resthttp.view.HttpImageView
                     android:id="@+id/small_image"
                     android:layout_width="match_parent"
                     android:layout_height="wrap_content"
                     android:layout_marginTop="16dp"
                     android:adjustViewBounds="true" />
                    
    然后在java代码中：

        smallImage.setImageUrl(IMAGE_SMALL_URL);    设置一个图片地址就好了。                                       

    (2)加载大图时可以进行图片压缩处理：

         <cn.alien95.resthttp.view.HttpImageView
                     android:id="@+id/big_image"
                     android:layout_width="match_parent"
                     android:layout_height="wrap_content"
                     android:layout_marginTop="16dp"
                     android:adjustViewBounds="true" />
                    
    也可以通过：

        public void setInSimpleSize(int inSimpleSize);  设置压缩参数。
        
    还可以通过：

        public void setImageUrlWithCompress(String url, int inSimpleSize);  设置压缩参数。

- 注意事项：
        
    还依赖了其他库(避免重复依赖)：

        compile 'com.jakewharton:disklrucache:2.0.2'
        compile 'com.google.code.gson:gson:2.6.2'      
        
    效果图：

    <img src="post.png" width="320" height="569" alt="POST"/>
    <img src="image.png" width="320" height="569"/>

    日志打印輸出：

    <img src="log.png" width="1001" height="303"/>