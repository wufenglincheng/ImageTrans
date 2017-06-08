package it.liuting.imagetrans;

import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Created by liuting on 17/6/6.
 */

public interface SourceImageViewParam {
    /**
     * 获得图片的原始 imageView
     *
     * @param position
     */
    View getSourceView(int position);

    /**
     * 获得图片的原始scaleType
     *
     * @param position
     * @return
     */
    ScaleType getScaleType(int position);
}
