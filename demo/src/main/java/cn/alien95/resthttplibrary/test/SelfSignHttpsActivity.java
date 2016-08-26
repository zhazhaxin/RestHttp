package cn.alien95.resthttplibrary.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import java.io.IOException;

import cn.alien95.resthttp.request.callback.HttpsCallback;
import cn.alien95.resthttp.request.https.SelfSignHttpsRequest;
import cn.alien95.resthttplibrary.R;

public class SelfSignHttpsActivity extends AppCompatActivity {

    private TextView mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_sign_https);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mResult = (TextView) findViewById(R.id.result);
        try {
            SelfSignHttpsRequest.getInstance().setCertificate(getAssets().open("12306.cer"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        SelfSignHttpsRequest.getInstance().get("https://kyfw.12306.cn/otn/", new HttpsCallback() {
            @Override
            public void success(String info) {
                mResult.setText(info);
            }
        });

    }
}
