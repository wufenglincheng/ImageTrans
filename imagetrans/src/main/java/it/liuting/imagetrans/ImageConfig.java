package it.liuting.imagetrans;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by liuting on 17/5/27.
 */

public class ImageConfig {

    public RectF imageRectF = new RectF();
    public ScaleType scaleType = ScaleType.CENTER_CROP;
    public WeakReference<Drawable> thumbnailWeakRefe;

    public void setView(View view, ImageTransParam imageTransParam) {
        Rect rect = new Rect();
        if (view == null) {
            int screenWidth = imageTransParam.screenWidth;
            int screenHeight = imageTransParam.screenHeight;
            rect.left = (int) (screenWidth * .5f);
            rect.right = (int) (screenWidth * .5f);
            rect.top = (int) (screenHeight * .5f);
            rect.bottom = (int) (screenHeight * .5f);
            imageRectF.set(rect);
            return;
        }
        int[] a = new int[2];
        view.getLocationInWindow(a);
        rect.left = a[0];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rect.top = a[1];
        } else {
            rect.top = a[1] - getStatesBarHeight(view.getContext());
        }
        rect.right = rect.left + view.getWidth();
        rect.bottom = rect.top + view.getHeight();
        imageRectF.set(rect);
        if (view instanceof ImageView) {
            thumbnailWeakRefe = new WeakReference<>(((ImageView) view).getDrawable());
        }
    }

    public void setScaleType(ScaleType scaleType) {
        this.scaleType = scaleType;
    }

    public static int getStatesBarHeight(Context context) {
        int resourceId = context.getResources()
                .getIdentifier("status_bar_height",
                        "dimen", "android");
        int cStatusHeight = 0;
        if (resourceId > 0) {
            cStatusHeight = context.getResources()
                    .getDimensionPixelSize(resourceId);
        }
        return cStatusHeight;
    }
}
