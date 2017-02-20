package cn.alien95.restdemo.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import java.io.IOException;

import cn.alien95.restdemo.R;
import cn.alien95.resthttp.request.callback.HttpsCallback;
import cn.alien95.resthttp.request.https.SelfSignRequest;
import cn.alien95.restdemo.data.Config;

public class SelfSignHttpsActivity extends AppCompatActivity {

    private TextView mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_sign_https);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mResult = (TextView) findViewById(R.id.result);
        try {
            SelfSignRequest.getInstance().setCertificate(getAssets().open("12306.cer"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        SelfSignRequest.getInstance().get(Config.HTTPS_URL, new HttpsCallback() {
            @Override
            public void success(String info) {
                mResult.setText(info);
            }
        });

    }
}
