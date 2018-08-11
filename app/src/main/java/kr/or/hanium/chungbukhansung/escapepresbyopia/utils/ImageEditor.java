package kr.or.hanium.chungbukhansung.escapepresbyopia.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.media.ExifInterface;

public class ImageEditor {

    /* 이미지가 90도 회전되는 현상 처리 */
    public static int exifOrientation2Degree(int exifOrientation) {
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
        }
        return 0;
    }

    public static Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

}
