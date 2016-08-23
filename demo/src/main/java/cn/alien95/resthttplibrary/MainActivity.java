package cn.alien95.resthttplibrary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import cn.alien95.resthttplibrary.image.ImageActivity;
import cn.alien95.resthttplibrary.music.MusicListActivity;


public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button testImage, testNet, uploadFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        testImage = (Button) findViewById(R.id.test_image);
        testNet = (Button) findViewById(R.id.test_net);
        uploadFile = (Button) findViewById(R.id.upload_file);

        testImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ImageActivity.class));
            }
        });
        testNet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MusicListActivity.class));
            }
        });

        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UploadFileActivity.class));
            }
        });
    }

}
