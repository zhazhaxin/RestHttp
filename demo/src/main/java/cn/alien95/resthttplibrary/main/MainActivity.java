package cn.alien95.resthttplibrary.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import cn.alien95.resthttplibrary.R;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        Button mHttp = (Button) findViewById(R.id.http);
        Button mHttps = (Button) findViewById(R.id.https);
        Button mSelfSignHttps = (Button) findViewById(R.id.self_sign_https);
        Button mRestHttp = (Button) findViewById(R.id.rest);
        Button mUploadFile = (Button) findViewById(R.id.upload_file);
        Button mImage = (Button) findViewById(R.id.image);

        mHttp.setOnClickListener(this);
        mHttps.setOnClickListener(this);
        mSelfSignHttps.setOnClickListener(this);
        mRestHttp.setOnClickListener(this);
        mUploadFile.setOnClickListener(this);
        mImage.setOnClickListener(this);
    }

    public void startActivity(Class clazz) {
        startActivity(new Intent(MainActivity.this, clazz));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.http:
                startActivity(HttpActivity.class);
                break;
            case R.id.https:
                startActivity(HttpsActivity.class);
                break;
            case R.id.self_sign_https:
                startActivity(SelfSignHttpsActivity.class);
                break;
            case R.id.rest:
                startActivity(MusicListActivity.class);
                break;
            case R.id.upload_file:
                startActivity(UploadFileActivity.class);
                break;
            case R.id.image:
                startActivity(ImageActivity.class);
                break;
        }
    }
}
