package it.liuting.imagetrans.image;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import it.liuting.imagetrans.ImageConfig;
import it.liuting.imagetrans.TransImageView;
import it.liuting.imagetrans.Util;
import it.liuting.imagetrans.evaluator.MatrixEvaluator;
import it.liuting.imagetrans.evaluator.RectFEvaluator;
import it.liuting.imagetrans.listener.SimpleAnimListener;

/**
 * Created by liuting on 17/6/6.
 */

public class TransformAttacher {
    private final static int ANIM_TIME = 300;
    protected TransImageView mImageView;
    protected ImageConfig mImageConfig;
    protected Matrix transformMatrix = new Matrix();
    protected RectF transformRect = new RectF();
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    public void setImageConfig(ImageConfig imageConfig) {
        this.mImageConfig = imageConfig;
    }


    /**
     * 图片变形动画
     *
     * @param imageView
     */
    public TransformAttacher(TransImageView imageView) {
        this.mImageView = imageView;
    }

    protected class TransformAnimation implements Runnable {
        private final RectF srcRectF;
        private final RectF dstRectF;
        private final Matrix srcMatrix;
        private final Matrix dstMatrix;
        private final int mStartAlpha;
        private final int mEndAlpha;
        private final long mStartTime;
        private RectFEvaluator rectFEvaluator;
        private MatrixEvaluator matrixEvaluator;
        private SimpleAnimListener listener;
        private boolean isCancel = false;

        public TransformAnimation(RectF src, RectF dst, Matrix srcMatrix, Matrix dstMatrix, int endAlpha) {
            this.srcRectF = src;
            this.dstRectF = dst;
            this.srcMatrix = srcMatrix;
            this.dstMatrix = dstMatrix;
            this.mEndAlpha = endAlpha;
            this.mStartAlpha = mImageView.getBackground().getAlpha();
            mStartTime = System.currentTimeMillis();
            rectFEvaluator = new RectFEvaluator(transformRect);
            matrixEvaluator = new MatrixEvaluator(transformMatrix);
        }

        public void setListener(SimpleAnimListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            if (isCancel) return;
            float t = interpolate();
            rectFEvaluator.evaluate(t, srcRectF, dstRectF);
            matrixEvaluator.evaluate(t, srcMatrix, dstMatrix);
//            Log.e("_runOpenTransform", Util.getValue(transformMatrix,Matrix.MSCALE_X) + "---"+transformRect.top+"---"+transformRect.right+"-----"+transformRect.bottom);
//            Log.e("matrixEvaluator", Util.getValue(transformMatrix,Matrix.MSCALE_X) +"---");

            //设置背景颜色的透明度
            mImageView.setBackgroundColor(Color.argb((int) (mStartAlpha + (mEndAlpha - mStartAlpha) * t), 0, 0, 0));
            ViewCompat.postInvalidateOnAnimation(mImageView);
            if (t < 1f) {
                Compat.postOnAnimation(mImageView, this);
            } else {
                if (listener != null) listener.onAnimationEnd(null);
            }
        }

        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - mStartTime) / ANIM_TIME;
            t = Math.min(1f, t);
            t = mInterpolator.getInterpolation(t);
            return t;
        }

        public void start() {
            Compat.postOnAnimation(mImageView, this);
        }

        public void cancel() {
            isCancel = true;
        }
    }
}
