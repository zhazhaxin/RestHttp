package cn.alien95.resthttplibrary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import cn.alien95.resthttp.request.RestHttpFile;
import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.util.DebugUtils;
import cn.alien95.resthttplibrary.util.ImageUtil;
import cn.alien95.util.Utils;

public class UploadFileActivity extends AppCompatActivity {

    private Button upload;
    private AlertDialog alertDialog;
    private String imagePath = null;
    private final String UPLOAD_URL = "http://115.29.107.20/v1/pictures/uploadFile.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);

        upload = (Button) findViewById(R.id.upload);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectDialog();
            }
        });

    }


    private void showSelectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        final TextView album = new TextView(this);
        album.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        album.setGravity(Gravity.CENTER);
        album.setTextColor(this.getResources().getColor(R.color.colorPrimary));
        album.setTextSize(18);
        album.setPadding(Utils.dip2px(16), Utils.dip2px(16), Utils.dip2px(16), Utils.dip2px(16));
        album.setText("从相册选取");

        final TextView camera = new TextView(this);
        camera.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        camera.setGravity(Gravity.CENTER);
        camera.setTextColor(this.getResources().getColor(R.color.colorPrimary));
        camera.setTextSize(16);
        camera.setPadding(Utils.dip2px(16), Utils.dip2px(16), Utils.dip2px(16), Utils.dip2px(16));
        camera.setText("拍照");

        linearLayout.addView(album);
        linearLayout.addView(camera);

        builder.setView(linearLayout);
        alertDialog = builder.create();
        alertDialog.show();

        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageUtil.getImageFromAlbum(UploadFileActivity.this);
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageUtil.getImageFromCamera(UploadFileActivity.this);
            }
        });
    }

    public void dismissDialog() {
        alertDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        Uri uri = data.getData();
        File file;
        if (uri == null) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                Bitmap photo = (Bitmap) bundle.get("data");

                file = new File(Utils.getCacheDir(), System.currentTimeMillis() + ".jpg");
                ImageUtil.saveImage(photo, file);   //压缩后上传
            } else {
                Utils.Toast("获取图片失败");
                return;
            }
        } else {
            file = new File(uri.getPath());
        }

        imagePath = file.getAbsolutePath();

        dismissDialog();

        RestHttpFile.getInstance().uploadFile(UPLOAD_URL, null, "picture", file, new HttpCallback() {
            @Override
            public void success(String info) {
                DebugUtils.Log("image upload result : " + info);
            }

            @Override
            public void failure(int status, String info) {
                super.failure(status, info);
            }
        });
    }
}
