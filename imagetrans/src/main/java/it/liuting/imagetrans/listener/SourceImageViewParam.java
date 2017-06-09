package it.liuting.imagetrans.listener;

import android.view.View;

import it.liuting.imagetrans.ScaleType;

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
