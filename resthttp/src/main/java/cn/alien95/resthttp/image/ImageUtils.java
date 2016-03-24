package cn.alien95.resthttp.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by linlongxin on 2015/12/29.
 */
public class ImageUtils {

    /**
     * 压缩从网络获取的图片，加载到内存
     *
     * @param inputStream  网络获取的输入流
     * @param inSampleSize 压缩的长或宽比例，大小缩小平方倍
     * @return
     */
    public static Bitmap compressBitmapFromInputStream(InputStream inputStream, int inSampleSize) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        return BitmapFactory.decodeStream(inputStream, null, options);
    }

    /**
     * 压缩图片通过要求的宽和高
     * @param inputStream
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap compressBitmapFromInputStream(InputStream inputStream, int reqWidth,int reqHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(inputStream,null,options);
    }

    /**
     * 压缩Bitmap
     * @param bitmap
     * @param size 大小应该时KB为单位
     * @return
     */
    public static Bitmap compressBitmap(Bitmap bitmap, int size) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
        float zoom = (float) Math.sqrt(size * 1024 / (float) out.toByteArray().length);

        Matrix matrix = new Matrix();
        matrix.setScale(zoom, zoom);

        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        out.reset();
        result.compress(Bitmap.CompressFormat.JPEG, 85, out);
        while (out.toByteArray().length > size * 1024) {
            System.out.println(out.toByteArray().length);
            matrix.setScale(0.9f, 0.9f);
            result = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
            out.reset();
            result.compress(Bitmap.CompressFormat.JPEG, 85, out);
        }
        return result;
    }

    /**
     * 为图片压缩做准备，通过设置inSampleSize参数来压缩
     * 通过reqWidth和reqHeight来计算出合理的inSampleSize
     *
     * @param options   BitmapFactory.Options bitmap参数
     * @param reqWidth  需要设置的宽
     * @param reqHeight 需要设置的高
     * @return int 返回一个inSampleSize值来压缩图片
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

}
