package cn.alien95.resthttplibrary.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import cn.alien95.resthttp.request.callback.HttpsCallback;
import cn.alien95.resthttp.request.https.RestHttpsRequest;
import cn.alien95.resthttplibrary.R;

public class HttpsActivity extends AppCompatActivity {

    private TextView mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_https);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mResult = (TextView) findViewById(R.id.result);

        RestHttpsRequest.getInstance().get("https://baidu.com/", new HttpsCallback() {
            @Override
            public void success(String info) {
                mResult.setText(info);
            }
        });
    }
}
