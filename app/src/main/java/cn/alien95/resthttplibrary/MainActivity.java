package cn.alien95.resthttplibrary;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import cn.alien95.resthttp.request.rest.RestHttpRequest;
import cn.alien95.resthttp.view.HttpImageView;
import cn.alien95.resthttplibrary.bean.UserInfo;


public class MainActivity extends AppCompatActivity {

    private TextView get, post;
    private HttpImageView smallImage, bigImage;
    private static final String IMAGE_SMALL_URL = "http://i02.pictn.sogoucdn.com/5602ce182cd6899e";
    private static final String IMAGE_BIG_URL = "http://img03.sogoucdn.com/app/a/100520093/84bbacd9cddc14de-71e1f69c051f39b5-9b2699bc39567827fca983cfb05efe0a.jpg";
    private static final String BASE_URL = "http://alien95.cn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        get = (TextView) findViewById(R.id.get);
        post = (TextView) findViewById(R.id.post);
        smallImage = (HttpImageView) findViewById(R.id.small_image);
        bigImage = (HttpImageView) findViewById(R.id.big_image);

        smallImage.setImageUrlWithCompress(IMAGE_SMALL_URL, 800, 600);
        bigImage.setImageUrl(IMAGE_BIG_URL);

        RestHttpRequest restHttpRequest = new RestHttpRequest.Builder()
                .baseUrl(BASE_URL)
                .build();

        final ServiceAPI serviceAPI = (ServiceAPI) restHttpRequest.create(ServiceAPI.class);

        UserInfo userInfo = serviceAPI.login("alien95", "123456");
        serviceAPI.login("alien", "123456");
        serviceAPI.login("Lemon", "123456");
        serviceAPI.login("Lemon95", "123456");

        post.setText(userInfo.toString());

    }

}
