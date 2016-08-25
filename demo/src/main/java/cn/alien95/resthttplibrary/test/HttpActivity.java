package cn.alien95.resthttplibrary.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.request.http.RestHttpRequest;
import cn.alien95.resthttplibrary.R;

public class HttpActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mGet,mPost;
    private TextView mResult;

    private final String GET_URL = "http://115.29.107.20/course/v1/accounts/banner.php";
    private final String POST_URL = "http://115.29.107.20/course/v1/courses/starJCourseList.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGet = (Button) findViewById(R.id.get);
        mPost = (Button) findViewById(R.id.post);
        mResult = (TextView) findViewById(R.id.result);
        mGet.setOnClickListener(this);
        mPost.setOnClickListener(this);
    }

    public void get(){
        mResult.setText("");
        RestHttpRequest.getInstance().get(GET_URL, new HttpCallback() {
            @Override
            public void success(String info) {
                mResult.setText(new Gson().toJson(info));
            }
        });
    }

    public void post(){
        mResult.setText("");
        Map<String,String> params = new HashMap<>();
        params.put("page","1");
        RestHttpRequest.getInstance().addHeader("UID","1");
        RestHttpRequest.getInstance().addHeader("token","9ba712a6210728364ea7c2d7457cde");
        RestHttpRequest.getInstance().post(POST_URL, params,new HttpCallback() {
            @Override
            public void success(String info) {
                mResult.setText(new Gson().toJson(info));
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.get:
                get();
                break;
            case R.id.post:
                post();
                break;
        }
    }
}
