package cn.alien95.resthttplibrary.main;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import cn.alien95.resthttp.request.callback.HttpCallback;
import cn.alien95.resthttp.request.http.HttpFile;
import cn.alien95.resthttplibrary.R;
import cn.alien95.resthttplibrary.data.Config;
import cn.alien95.util.ImageUtil;
import cn.alien95.util.Utils;

public class UploadFileActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView mImage;
    private Button upload;
    private AlertDialog alertDialog;
    private File mImageFile;
    private final String UPLOAD_URL = Config.HOST + "pick_picture/v1/pictures/uploadFile.php";
    private File dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        upload = (Button) findViewById(R.id.upload);
        mImage = (ImageView) findViewById(R.id.image);
        upload.setOnClickListener(this);
        mImage.setOnClickListener(this);
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
            ImageUtil.getBitmapFromUri(uri, new ImageUtil.Callback() {
                @Override
                public void callback(Bitmap bitmap) {
                    dir = getCacheDir();
                    mImageFile = new File(dir,"test_upload.jpg");
                    ImageUtil.saveImage(bitmap,mImageFile);
                    mImage.setImageBitmap(decodeFile(mImageFile));
                }
            });

        }
        dismissDialog();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.image:
                showSelectDialog();
                break;
            case R.id.upload:
                if(mImageFile == null){
                    Utils.Toast("请先选择文件");
                    return;
                }
                HttpFile.getInstance().uploadFile(UPLOAD_URL, null, "picture", mImageFile, new HttpCallback() {
                    @Override
                    public void success(String info) {
                        Utils.Toast(info + " : http://115.29.107.20/image/test_upload.jpg");
                    }

                    @Override
                    public void failure(int status, String info) {
                        super.failure(status, info);
                    }
                });
                break;
        }
    }
    private static Bitmap decodeFile(File f) {
        Bitmap b = null;
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            int scale = 1;
            if (o.outHeight > 200 && o.outWidth > 200) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(200 / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }
}
