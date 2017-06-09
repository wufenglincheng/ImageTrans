package it.liuting.imagetrans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import it.liuting.imagetrans.image.ImageGesturesAttacher;
import it.liuting.imagetrans.image.TransformAttacher;
import it.liuting.imagetrans.listener.OnPullCloseListener;

/**
 * Created by liuting on 17/5/25.
 */

public class TransImageView extends ImageView implements OnPullCloseListener, View.OnLayoutChangeListener {

    private ImageGesturesAttacher mGesturesAttacher;
    private TransformAttacher mTransformAttacher;
    private ImageConfig mImageConfig;

    public TransImageView(Context context) {
        this(context, null);
    }

    public TransImageView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public TransImageView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        init();
    }

    protected void init() {
        setBackgroundAlpha(0);
        mGesturesAttacher = new ImageGesturesAttacher(this);
        mTransformAttacher = new TransformAttacher(this);
        mGesturesAttacher.setOnPullCloseListener(this);
        super.setScaleType(ScaleType.MATRIX);
        mGesturesAttacher.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClose();
            }
        });
        addOnLayoutChangeListener(this);
    }

    public void setImageConfig(ImageConfig imageConfig) {
        this.mImageConfig = imageConfig;
        mTransformAttacher.setImageConfig(imageConfig);
    }

    public void showThumbWithTransform() {
        if (mImageConfig == null || mImageConfig.thumbnailWeakRefe == null) return;
        super.setImageDrawable(mImageConfig.thumbnailWeakRefe.get());
        mTransformAttacher.showThumbWithTransform();

    }

    public void showThumb() {
        setBackgroundAlpha(255);
        if (mImageConfig == null || mImageConfig.thumbnailWeakRefe == null) return;
        super.setImageDrawable(mImageConfig.thumbnailWeakRefe.get());
        mTransformAttacher.showThumb();
    }

    public void setImageWithTransform(Drawable drawable) {
        setImageDrawable(drawable);
        mTransformAttacher.showOriginalWithTransform();
    }

    public void setImage(Drawable drawable) {
        mTransformAttacher.pause();
        setBackgroundAlpha(255);
        setImageDrawable(drawable);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mGesturesAttacher.update();
    }

    public void resetMatrix() {
        mGesturesAttacher.resetMatrix();
    }
    public boolean isRunTransform(){
        return mTransformAttacher.isRunning();
    }

    public Matrix getMinMatrix() {
        return mGesturesAttacher.getMinMatrix();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mTransformAttacher.isRunning()) {
            mTransformAttacher.onDraw(canvas);
        } else {
            super.onDraw(canvas);
        }
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        mGesturesAttacher.setScaleType(scaleType);
    }

    @Override
    public void onClose() {
        mTransformAttacher.runCloseTransform();
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        // 在边界改变的时候更新矩阵
        if (null != mGesturesAttacher) {
            mGesturesAttacher.update();
        }
        mTransformAttacher.onLayoutChange();
    }

    public void setOnCloseListener(OnCloseListener listener) {
        mTransformAttacher.setOnCloseListener(listener);
    }

    public void setBackgroundAlpha(@IntRange(from = 0, to = 255) int alpha) {
        setBackgroundColor(Color.argb(alpha, 0, 0, 0));
    }

    public interface OnCloseListener {
        void close();
    }
}
