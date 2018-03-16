package it.liuting.imagetrans;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import it.liuting.imagetrans.evaluator.MatrixEvaluator;
import it.liuting.imagetrans.evaluator.RectFEvaluator;
import it.liuting.imagetrans.listener.OnTransformListener;

/**
 * Created by liuting on 18/3/13.
 */

class TransformAttacher {
    private final static int ANIM_TIME = 300;

    private TransImageView imageView;
    private ThumbConfig thumbConfig;
    private ITConfig itConfig;
    private TransState currentState = TransState.DEFAULT;
    private RectF thumbRectF;
    private Matrix thumbMatrix;
    private boolean running;
    private boolean drawing;
    private RectF transformRect = new RectF();
    private Matrix transformMatrix = new Matrix();
    private TransState transformState = TransState.DEFAULT;
    private TransStateChangeListener listener;

    TransformAttacher(TransImageView imageView) {
        this.imageView = imageView;
    }

    void settingConfig(ITConfig itConfig, ThumbConfig thumbConfig) {
        this.itConfig = itConfig;
        this.thumbConfig = thumbConfig;
    }

    private void initThumbInfo() {
        int drawableWidth = getThumbDrawableWidth();
        int drawableHeight = getThumbDrawableHeight();
        int viewWidth = getImageViewWidth(imageView);
        int viewHeight = getImageViewHeight(imageView);
        float thumbScale = 0.5f;
        if (itConfig.thumbLarge) {
            thumbScale = 1f;
        }
        if (drawableWidth * 1f / drawableHeight >= viewWidth * 1f / viewHeight) {
            float tempWidth = viewWidth * thumbScale;
            float tempHeight = tempWidth * drawableHeight / drawableWidth;
            float left = (viewWidth - tempWidth) * .5f;
            float top = (viewHeight - tempHeight) * .5f;
            thumbRectF = new RectF(left, top, left + tempWidth, top + tempHeight);
        } else {
            float tempHeight = viewHeight * thumbScale;
            float tempWidth = tempHeight * drawableWidth / drawableHeight;
            float left = (viewWidth - tempWidth) * .5f;
            float top = (viewHeight - tempHeight) * .5f;
            thumbRectF = new RectF(left, top, tempWidth + left, tempHeight + top);
        }
        thumbMatrix = getMatrix(thumbRectF, drawableWidth, drawableHeight, 1);
    }

    void showThumb(boolean needTrans) {
        initThumbInfo();
        if (needTrans)
            showState(TransState.OPEN_TO_THUMB);
        else
            showState(TransState.THUMB);
    }

    void showImage(boolean needTrans) {
        if (needTrans) {
            if (currentState == TransState.DEFAULT) {
                showState(TransState.OPEN_TO_ORI);
            } else if (currentState == TransState.THUMB) {
                showState(TransState.THUMB_TO_ORI);
            }
        } else {
            showState(TransState.ORI);
        }
    }

    void showClose() {
        if (currentState == TransState.THUMB) {
            showState(TransState.THUMB_TO_CLOSE);
        } else if (currentState == TransState.ORI) {
            showState(TransState.ORI_TO_CLOSE);
        }
    }

    private void showState(TransState state) {
        if (running) return;
        currentState = state;
        if (listener != null) listener.onChange(currentState);
        if (currentState == TransState.THUMB) {
            transformRect = new RectF(thumbRectF);
            transformMatrix = new Matrix(thumbMatrix);
            transformState = currentState;
        } else if (currentState != TransState.ORI && currentState != TransState.CLOSEED) {
            runTransform();
        }
    }

    private void runTransform() {
        if (getDrawable(currentState) == null) {
            autoChangeState();
            return;
        }
        running = true;
        RectF startRf = getStartRectF();
        RectF endRf = getEndRectF();
        if (currentState == TransState.THUMB_TO_ORI) {
            if (startRf.width() == endRf.width() && startRf.height() == endRf.height()) {
                //图片比例没有变化，就不需要进行预览图到原图的变形
                running = false;
                showState(TransState.ORI);
                return;
            }
        }
        Matrix startM = getStartMatrix(startRf);
        Matrix endM = getEndMatrix(endRf);
        int toAlpha = getEndAlpha();
        TransformAnimation animation = new TransformAnimation(startRf, endRf, startM, endM, toAlpha, currentState);
        animation.setListener(new OnTransformListener() {
            @Override
            public void transformStart() {
                drawing = true;
            }

            @Override
            public void transformEnd() {
                running = false;
                drawing = false;
                autoChangeState();
            }
        });
        animation.start();
    }

    private void autoChangeState() {
        if (currentState == TransState.OPEN_TO_THUMB) {
            showState(TransState.THUMB);
        } else if (currentState == TransState.OPEN_TO_ORI || currentState == TransState.THUMB_TO_ORI) {
            showState(TransState.ORI);
        } else if (currentState == TransState.THUMB_TO_CLOSE || currentState == TransState.ORI_TO_CLOSE) {
            showState(TransState.CLOSEED);
        }
    }

    private int getEndAlpha() {
        switch (currentState) {
            case OPEN_TO_THUMB:
            case OPEN_TO_ORI:
            case THUMB_TO_ORI:
                return 255;
            case ORI_TO_CLOSE:
            case THUMB_TO_CLOSE:
                return 0;
            default:
                return 255;
        }
    }

    private RectF getStartRectF() {
        switch (currentState) {
            case OPEN_TO_THUMB:
            case OPEN_TO_ORI:
                return new RectF(thumbConfig.imageRectF);
            case THUMB_TO_ORI:
            case THUMB_TO_CLOSE: {
                return thumbRectF;
            }
            case ORI_TO_CLOSE: {
                return imageView.getDisplayRect(false);
            }
        }
        return new RectF();
    }

    private RectF getEndRectF() {
        switch (currentState) {
            case OPEN_TO_THUMB: {
                return thumbRectF;
            }
            case THUMB_TO_ORI:
            case OPEN_TO_ORI: {
                return imageView.getDisplayRect(true);
            }
            case THUMB_TO_CLOSE:
            case ORI_TO_CLOSE: {
                return thumbConfig.imageRectF;
            }
        }
        return new RectF();
    }

    private Matrix getStartMatrix(RectF rectF) {
        switch (currentState) {
            case OPEN_TO_THUMB: {
                return getMatrix(rectF, getThumbDrawableWidth(), getThumbDrawableHeight(), 1);
            }
            case OPEN_TO_ORI: {
                return getMatrix(rectF, getDrawableWidth(), getDrawableHeight(), 1);
            }
            case THUMB_TO_ORI: {
                return getMatrix(thumbRectF, getDrawableWidth(), getDrawableHeight(), 1);
            }
            case THUMB_TO_CLOSE: {
                return thumbMatrix;
            }
            case ORI_TO_CLOSE: {
                Matrix matrix = new Matrix(imageView.getDrawMatrix());
                matrix.postTranslate(-Util.getValue(matrix, Matrix.MTRANS_X), -Util.getValue(matrix, Matrix.MTRANS_Y));
                return matrix;
            }
        }
        return new Matrix();
    }

    private Matrix getEndMatrix(RectF rectF) {
        switch (currentState) {
            case OPEN_TO_THUMB: {
                return thumbMatrix;
            }
            case THUMB_TO_ORI:
            case OPEN_TO_ORI: {
                Matrix endMatrix = new Matrix(imageView.getDrawMatrix());
                endMatrix.postTranslate(-Util.getValue(endMatrix, Matrix.MTRANS_X), -Util.getValue(endMatrix, Matrix.MTRANS_Y));
                return endMatrix;
            }
            case THUMB_TO_CLOSE: {
                return getMatrix(rectF, getThumbDrawableWidth(), getThumbDrawableWidth(), 1);
            }
            case ORI_TO_CLOSE: {
                RectF initRectF = imageView.getDisplayRect(false);
                return getMatrix(rectF, initRectF.width(), initRectF.height(), Util.getValue(imageView.getDrawMatrix(), Matrix.MSCALE_X));
            }
        }
        return new Matrix();
    }

    private Matrix getMatrix(RectF rectf, float width, float height, float oScale) {
        //新建结束图像的矩阵
        Matrix matrix = new Matrix();
        //得到目标矩阵相对于当前矩阵的宽和高的缩放比例
        float scaleX = rectf.width() / width;
        float scaleY = rectf.height() / height;
        //由于图片比例不定,这里得到最匹配目标矩形的scale
        float scale = Math.max(scaleX, scaleY);
        //得到最终的矩阵scale
        float tempScale = scale * oScale;
        matrix.setScale(tempScale, tempScale);
        ScaleType type = thumbConfig.scaleType;
        //根据不同的裁剪类型
        switch (type) {
            case CENTER_CROP: {
                //当预览图是居中裁剪
                float dx = (width * scale - rectf.width()) * .5f;
                float dy = (height * scale - rectf.height()) * .5f;
                matrix.postTranslate(-dx, -dy);
                break;
            }
            case START_CROP: {
                //当预览图是顶部裁剪
                if (width > height) {
                    float dy = (height * scale - rectf.height()) * .5f;
                    matrix.postTranslate(0, -dy);
                } else {
                    float dx = (width * scale - rectf.width()) * .5f;
                    matrix.postTranslate(-dx, 0);
                }
                break;
            }
            case END_CROP: {
                //当预览图是尾部裁剪
                if (width > height) {
                    float dx = width * scale - rectf.width();
                    float dy = (height * scale - rectf.height()) * .5f;
                    matrix.postTranslate(-dx, -dy);
                } else {
                    float dx = (width * scale - rectf.width()) * .5f;
                    float dy = height * scale - rectf.height();
                    matrix.postTranslate(-dx, -dy);
                }
                break;
            }
            case FIT_XY: {
                //当预览图是充满宽高
                matrix.setScale(scaleX * oScale, scaleY * oScale);
                break;
            }
            default: {
                //尚未支持其他裁剪方式
                break;
            }
        }
        return matrix;
    }

    private int getImageViewWidth(ImageView imageView) {
        return imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
    }

    private int getImageViewHeight(ImageView imageView) {
        return imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();
    }

    private int getThumbDrawableWidth() {
        return thumbConfig.thumbnailWeakRefe == null || thumbConfig.thumbnailWeakRefe.get() == null ? 0 : thumbConfig.thumbnailWeakRefe.get().getIntrinsicWidth();
    }

    private int getThumbDrawableHeight() {
        return thumbConfig.thumbnailWeakRefe == null || thumbConfig.thumbnailWeakRefe.get() == null ? 0 : thumbConfig.thumbnailWeakRefe.get().getIntrinsicHeight();
    }

    private int getDrawableWidth() {
        return imageView.getImageDrawable() == null ? 0 : imageView.getImageDrawable().getIntrinsicWidth();
    }

    private int getDrawableHeight() {
        return imageView.getImageDrawable() == null ? 0 : imageView.getImageDrawable().getIntrinsicHeight();
    }

    boolean needIntercept() {
        return currentState != TransState.ORI;
    }

    boolean isDrawing(){
        return drawing;
    }

    public enum TransState {
        DEFAULT, OPEN_TO_THUMB, THUMB, OPEN_TO_ORI, ORI, THUMB_TO_ORI, THUMB_TO_CLOSE, ORI_TO_CLOSE, CLOSEED;
    }

    public interface TransStateChangeListener {
        void onChange(TransState state);
    }

    public void setTransStateChangeListener(TransStateChangeListener listener) {
        this.listener = listener;
    }

    /**
     * 绘制变形过程中的图形
     *
     * @param canvas
     */
    void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable(transformState);
        if (drawable == null) return;
        int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.translate(transformRect.left, transformRect.top);
        canvas.clipRect(0, 0, transformRect.width(), transformRect.height());
        canvas.concat(transformMatrix);
        drawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    private Drawable getDrawable(TransState state) {
        switch (state) {
            case OPEN_TO_THUMB:
            case THUMB_TO_CLOSE:
            case THUMB:
                return thumbConfig.thumbnailWeakRefe == null ? null : thumbConfig.thumbnailWeakRefe.get();
            default:
                return imageView.getImageDrawable();
        }
    }

    /**
     * 图片变形动画
     */
    class TransformAnimation implements Runnable {
        private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
        private final RectF srcRectF;
        private final RectF dstRectF;
        private final Matrix srcMatrix;
        private final Matrix dstMatrix;
        private final int mStartAlpha;
        private final int mEndAlpha;
        private final long mStartTime;
        private RectFEvaluator rectFEvaluator;
        private MatrixEvaluator matrixEvaluator;
        private OnTransformListener listener;
        protected boolean isRunning = false;

        TransformAnimation(RectF src, RectF dst, Matrix srcMatrix, Matrix dstMatrix, int endAlpha, TransState state) {
            this.srcRectF = src;
            this.dstRectF = dst;
            this.srcMatrix = srcMatrix;
            this.dstMatrix = dstMatrix;
            this.mEndAlpha = endAlpha;
            transformRect = new RectF(src);
            transformMatrix = new Matrix(srcMatrix);
            transformState = state;
            mStartAlpha = Compat.getBackGroundAlpha(imageView.getBackground());
            mStartTime = System.currentTimeMillis();
            rectFEvaluator = new RectFEvaluator(transformRect);
            matrixEvaluator = new MatrixEvaluator(transformMatrix);
        }

        void setListener(OnTransformListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            if (!isRunning) return;
            float t = interpolate();
            rectFEvaluator.evaluate(t, srcRectF, dstRectF);
            matrixEvaluator.evaluate(t, srcMatrix, dstMatrix);

            //设置背景颜色的透明度
            imageView.setBackgroundAlpha((int) (mStartAlpha + (mEndAlpha - mStartAlpha) * t));
            ViewCompat.postInvalidateOnAnimation(imageView);
            if (t < 1f) {
                Compat.postOnAnimation(imageView, this);
            } else {
                isRunning = false;
                if (listener != null) listener.transformEnd();
            }
        }

        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - mStartTime) / ANIM_TIME;
            t = Math.min(1f, t);
            t = mInterpolator.getInterpolation(t);
            return t;
        }

        void start() {
            isRunning = true;
            if (listener != null) listener.transformStart();
            Compat.postOnAnimation(imageView, this);
        }
    }
}
