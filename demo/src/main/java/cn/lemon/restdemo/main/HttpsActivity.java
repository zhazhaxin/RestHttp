package cn.lemon.restdemo.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import cn.alien95.restdemo.R;
import cn.lemon.resthttp.request.callback.HttpsCallback;
import cn.lemon.resthttp.request.https.HttpsRequest;

public class HttpsActivity extends AppCompatActivity {

    private TextView mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_https);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mResult = (TextView) findViewById(R.id.result);

        HttpsRequest.getInstance().get("https://baidu.com/", new HttpsCallback() {
            @Override
            public void success(String info) {
                mResult.setText(info);
            }
        });
    }
}
