package com.demo.imagetransdemo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;

import it.liuting.imagetrans.ScaleType;

/**
 * Created by liuting on 17/6/1.
 */

public class CustomTransform extends BitmapTransformation {
    ScaleType scaleType;

    public CustomTransform(Context context, ScaleType scaleType) {
        super(context);
        this.scaleType = scaleType;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap source, int outWidth, int outHeight) {
        if (null == source || source.isRecycled()) {
            return null;
        }
        switch (scaleType) {
            case CENTER_CROP: {
                final Bitmap toReuse = pool.get(outWidth, outHeight, source.getConfig() != null
                        ? source.getConfig() : Bitmap.Config.ARGB_8888);
                Bitmap transformed = TransformationUtils.centerCrop(toReuse, source, outWidth, outHeight);
                if (toReuse != null && toReuse != transformed && !pool.put(toReuse)) {
                    toReuse.recycle();
                }
                return transformed;
            }
            case START_CROP: {
                float scaleX = (float) outWidth / source.getWidth();
                float scaleY = (float) outHeight / source.getHeight();
                float scale = Math.max(scaleX, scaleY);
                int finalWidth = (int) (outWidth / scale);
                int finalHeight = (int) (outHeight / scale);
                if (finalWidth > source.getWidth()) finalWidth = source.getWidth();
                if (finalHeight > source.getHeight()) finalHeight = source.getHeight();
                Bitmap result = Bitmap.createBitmap(source, 0, 0, finalWidth, finalHeight);
                return result;
            }
            case END_CROP: {
                float scaleX = (float) outWidth / source.getWidth();
                float scaleY = (float) outHeight / source.getHeight();
                float scale = Math.max(scaleX, scaleY);
                int finalWidth = (int) (outWidth / scale);
                int finalHeight = (int) (outHeight / scale);
                if (finalWidth > source.getWidth()) finalWidth = source.getWidth();
                if (finalHeight > source.getHeight()) finalHeight = source.getHeight();
                Bitmap result = Bitmap.createBitmap(source, source.getWidth() - finalWidth, source.getHeight() - finalHeight, finalWidth, finalHeight);
                return result;
            }
            case FIT_XY: {
                float scaleX = (float) outWidth / source.getWidth();
                float scaleY = (float) outHeight / source.getHeight();
                Bitmap result = Bitmap.createBitmap(outWidth, outHeight, getSafeConfig(source));
                Matrix matrix = new Matrix();
                matrix.setScale(scaleX, scaleY);
                final Canvas canvas = new Canvas(result);
                final Paint paint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
                canvas.drawBitmap(source, matrix, paint);
                return result;
            }
        }
        return source;
    }

    private static Bitmap.Config getSafeConfig(Bitmap bitmap) {
        return bitmap.getConfig() != null ? bitmap.getConfig() : Bitmap.Config.ARGB_8888;
    }

    @Override
    public String getId() {
        return scaleType.name();
    }
}
