package it.liuting.imagetrans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import it.liuting.imagetrans.image.ImageGesturesAttacher;
import it.liuting.imagetrans.image.ImageTransformAttacher;
import it.liuting.imagetrans.image.TempTransformAttacher;
import it.liuting.imagetrans.listener.OnPullCloseListener;

/**
 * Created by liuting on 17/5/25.
 */

public class TransImageView extends ImageView implements OnPullCloseListener, View.OnLayoutChangeListener {

    private ImageGesturesAttacher mGesturesAttacher;
    private ImageTransformAttacher mTransformAttacher;
    private TempTransformAttacher mTempTransformAttacher;
    private OnCloseListener onCloseListener;
    private boolean isPreview = false;
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
        setBackgroundColor(Color.argb(0, 0, 0, 0));
        mGesturesAttacher = new ImageGesturesAttacher(this);
        mTransformAttacher = new ImageTransformAttacher(this);
        mTempTransformAttacher = new TempTransformAttacher(this);
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
        mTempTransformAttacher.setImageConfig(imageConfig);
    }

    public void startPreviewWithTransform() {
        if (mImageConfig == null || mImageConfig.thumbnailWeakRefe == null) return;
        isPreview = true;
        super.setImageDrawable(mImageConfig.thumbnailWeakRefe.get());
//        mGesturesAttacher.setPreviewMode(true);
        mTempTransformAttacher.runOpenTransform();

    }

    public void startPreView() {
        setBackgroundColor(Color.argb(255, 0, 0, 0));
        if (mImageConfig == null || mImageConfig.thumbnailWeakRefe == null) return;
        isPreview = true;
        super.setImageDrawable(mImageConfig.thumbnailWeakRefe.get());
        mTempTransformAttacher.showPreview();
    }

    public void setImageWithTransform(Drawable drawable) {
        setImageDrawable(drawable);
        if (isPreview) {
            isPreview = false;
            mTransformAttacher.runOpenTransform(mTempTransformAttacher.getTempRectF());
        } else {
            mTransformAttacher.runOpenTransform(mImageConfig.imageRectF);
        }
    }

    public void setImageWithOutTransform(Drawable drawable) {
        if (isPreview) isPreview = false;
        noAlpha();
        setImageDrawable(drawable);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mGesturesAttacher.update();
    }

    public void runGestures() {
        mGesturesAttacher.run();
        mGesturesAttacher.resetMatrix();
    }

    public Matrix getMinMatrix() {
        return mGesturesAttacher.getMinMatrix();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mTransformAttacher.isRunning()) {
            mTransformAttacher.onDraw(canvas);
        } else if (isPreview) {
            mTempTransformAttacher.onDraw(canvas);
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
        if (isPreview) {
            mTempTransformAttacher.runCloseTransform(onCloseListener);
            return;
        }
        mTransformAttacher.runCloseTransform(onCloseListener);
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        // 在边界改变的时候更新矩阵
        if (null != mGesturesAttacher) {
            mGesturesAttacher.update();
        }
        mTransformAttacher.onLayoutChange();
        mTempTransformAttacher.onLayoutChange();
    }

    public void setOnCloseListener(OnCloseListener listener) {
        this.onCloseListener = listener;
    }

    public void noAlpha() {
        setBackgroundColor(Color.argb(255, 0, 0, 0));
    }

    public interface OnCloseListener {
        void close();
    }
}
