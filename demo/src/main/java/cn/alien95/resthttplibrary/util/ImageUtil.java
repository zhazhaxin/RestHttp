package cn.alien95.resthttplibrary.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.alien95.util.Utils;

/**
 * Created by linlongxin on 2016/5/12.
 */
public class ImageUtil {

    public static final int REQUEST_CODE_PICK_IMAGE = 357;
    public static final int REQUEST_CODE_CAPTURE_CAMEIA = 951;
    private static Handler handler;

    /**
     * 相册获取照片
     *
     * @param activity
     */
    public static void getImageFromAlbum(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    /**
     * 从相机获取图片
     */
    public static void getImageFromCamera(Activity activity) {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent getImageByCamera = new Intent("android.media.action.IMAGE_CAPTURE");
            activity.startActivityForResult(getImageByCamera, REQUEST_CODE_CAPTURE_CAMEIA);
        } else {
            Utils.ToastLong("请确认已经插入SD卡");
        }
    }

    public static boolean saveImage(Bitmap photo, File file) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(file, false));
            int size = photo.getByteCount();
            if (size > 2 * 1024 * 1024) {
                photo.compress(Bitmap.CompressFormat.JPEG, 10, bos);
            } else if (size > 1024 * 1024) {
                photo.compress(Bitmap.CompressFormat.JPEG, 20, bos);
            } else if (size > 512 * 1024) {
                photo.compress(Bitmap.CompressFormat.JPEG, 40, bos);
            } else if (size > 200 * 1024) {
                photo.compress(Bitmap.CompressFormat.JPEG, 60, bos);
            } else {
                photo.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            }
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void getBitmapFromUri(final Uri uri, final Callback callback) {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(Utils.getContext().getContentResolver(), uri);
                    File file = new File(Utils.getCacheDir(), System.currentTimeMillis() + ".jpg");
                    saveImage(bitmap, file);
                    final Bitmap newBitmap = decodeFile(file);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.callback(newBitmap);
                        }
                    });
                } catch (Exception e) {
                    Utils.Log("[Android]  " + e.getMessage());
                    Utils.Log("[Android] -- 目录为：" + uri);
                    e.printStackTrace();
                }
            }
        }).start();

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

    public interface Callback {

        void callback(Bitmap bitmap);
    }

}
