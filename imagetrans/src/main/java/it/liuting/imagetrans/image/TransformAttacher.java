package it.liuting.imagetrans.image;

import android.animation.Animator;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.v4.view.ViewCompat;
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
    private final static int STATE_THUMBNAIL_OPEN = 0;
    private final static int STATE_THUMBNAIL_OPEN_TRANS = 1;
    private final static int STATE_THUMBNAIL_CLOSE = 2;
    private final static int STATE_ORIGINAL_OPEN_TRANS = 3;
    private final static int STATE_ORIGINAL_OPEN_TRANS_FORM_THUM = 4;
    private final static int STATE_ORIGINAL_CLOSE = 5;
    protected TransImageView mImageView;
    protected ImageConfig mImageConfig;
    protected Matrix transformMatrix = new Matrix();
    protected RectF transformRect = new RectF();
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private int currentState = -1;
    private RectF thumbnailInitRect;
    private RectF thumbnailEndRect;
    private Matrix thumbnailInitMatrix;
    private Matrix thumbnailEndMatrix;
    private TransformAnimation thumbnailOpenTransAnim;
    private boolean showFlagAtLayoutChange;
    private TransImageView.OnCloseListener onCloseListener;
    private TransformAnimation originalOpenTransAnim;
    private boolean running = false;
    private boolean runningOriginalTrans = false;

    public void setImageConfig(ImageConfig imageConfig) {
        this.mImageConfig = imageConfig;
    }

    public TransformAttacher(TransImageView imageView) {
        this.mImageView = imageView;
    }

    /*******以下是查看缩略图的变形动画**********/
    private void initThumb() {
        thumbnailInitRect = new RectF(mImageConfig.imageRectF);
        float tempWidth = mImageView.getWidth() / 5;
        float tempHeight = tempWidth * thumbnailInitRect.height() / thumbnailInitRect.width();
        thumbnailEndRect = new RectF(mImageView.getWidth() / 2 - tempWidth / 2, mImageView.getHeight() / 2 - tempHeight / 2, mImageView.getWidth() / 2 + tempWidth / 2, mImageView.getHeight() / 2 + tempHeight / 2);
        int drawableWidth = mImageView.getDrawable().getIntrinsicWidth();
        int drawableHeight = mImageView.getDrawable().getIntrinsicHeight();
        thumbnailInitMatrix = getThumbnailMatrix(thumbnailInitRect, drawableWidth, drawableHeight);
        thumbnailEndMatrix = getThumbnailMatrix(thumbnailEndRect, drawableWidth, drawableHeight);
    }

    private Matrix getThumbnailMatrix(RectF rectF, int drawableWidth, int drawableHeight) {
        Matrix matrix = new Matrix();
        float scaleX = rectF.width() / drawableWidth;
        float scaleY = rectF.height() / drawableHeight;
        float scale = Math.max(scaleX, scaleY);
        matrix.setScale(scale, scale);
        if (Math.abs(rectF.width() / rectF.height() - (float) drawableWidth / drawableHeight) > 0.1) {
            switch (mImageConfig.scaleType) {
                case CENTER_CROP: {
                    //当预览图是居中裁剪
                    float initDx = (scale * drawableWidth - rectF.width()) * .5f;
                    float initDy = (scale * drawableHeight - rectF.height()) * .5f;
                    matrix.postTranslate(-initDx, -initDy);
                    break;
                }
                case START_CROP: {
                    //当预览图是顶部裁剪
                    if (drawableWidth > drawableHeight) {
                        float dy = (scale * drawableHeight - rectF.height()) * .5f;
                        matrix.postTranslate(0, -dy);
                    } else {
                        float dx = (scale * drawableWidth - rectF.width()) * .5f;
                        matrix.postTranslate(-dx, 0);
                    }
                    break;
                }
                case END_CROP: {
                    //当预览图是底部裁剪
                    if (drawableWidth > drawableHeight) {
                        float dx = scale * drawableWidth - rectF.width();
                        float dy = (scale * drawableHeight - rectF.height()) * .5f;
                        matrix.postTranslate(-dx, -dy);
                    } else {
                        float dx = (scale * drawableWidth - rectF.width()) * .5f;
                        float dy = scale * drawableHeight - rectF.height();
                        matrix.postTranslate(-dx, -dy);
                    }
                    break;
                }
                case FIT_XY: {
                    //当预览图是充满宽高
                    matrix.setScale(scaleX, scaleY);
                    break;
                }
                default: {
                    //尚未支持其他裁剪方式
                    break;
                }
            }
        }
        return matrix;
    }

    public void showThumb() {
        currentState = STATE_THUMBNAIL_OPEN;
        running = true;
        if (mImageView.getWidth() == 0) {
            showFlagAtLayoutChange = true;
        } else {
            _showThumb();
        }
    }

    private void _showThumb() {
        initThumb();
        transformRect.set(thumbnailEndRect);
        transformMatrix.set(thumbnailEndMatrix);
        mImageView.invalidate();
    }

    public void showThumbWithTransform() {
        currentState = STATE_THUMBNAIL_OPEN_TRANS;
        running = true;
        if (mImageView.getWidth() == 0) {
            showFlagAtLayoutChange = true;
        } else {
            _showThumbWithTransform();
        }
    }

    private void _showThumbWithTransform() {
        initThumb();
        thumbnailOpenTransAnim = new TransformAnimation(thumbnailInitRect, thumbnailEndRect, thumbnailInitMatrix, thumbnailEndMatrix, 255);
        thumbnailOpenTransAnim.start();
    }

    public void closeThumbTransform() {
        currentState = STATE_THUMBNAIL_CLOSE;
        if (thumbnailOpenTransAnim != null) thumbnailOpenTransAnim.cancel();
        TransformAnimation thumbCloseTransAnim = new TransformAnimation(new RectF(transformRect), thumbnailInitRect, new Matrix(transformMatrix), thumbnailInitMatrix, 0);
        thumbCloseTransAnim.setListener(new SimpleAnimListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onCloseListener != null) onCloseListener.close();
            }
        });
        thumbCloseTransAnim.start();
    }

    /*******以下是查看原图的变形动画**********/

    public void showOriginalWithTransform() {
        if (currentState == STATE_THUMBNAIL_CLOSE)
            return;
        if (currentState == STATE_THUMBNAIL_OPEN_TRANS) {
            thumbnailOpenTransAnim.cancel();
        }
        if (currentState == STATE_THUMBNAIL_OPEN || currentState == STATE_THUMBNAIL_OPEN_TRANS)
            currentState = STATE_ORIGINAL_OPEN_TRANS_FORM_THUM;
        else
            currentState = STATE_ORIGINAL_OPEN_TRANS;
        running = true;
        if (mImageView.getWidth() == 0) {
            showFlagAtLayoutChange = true;
        } else {
            _showOriginalWithTransform();
        }
    }

    private void _showOriginalWithTransform() {
        RectF initRect;
        if (currentState == STATE_ORIGINAL_OPEN_TRANS_FORM_THUM) {
            initRect = new RectF(transformRect);
        } else {
            initRect = mImageConfig.imageRectF;
        }
        //得到结束位置的显示矩形
        RectF endRect = Util.getDisplayRect(mImageView);
        Matrix endMatrix = new Matrix(mImageView.getImageMatrix());
        endMatrix.postTranslate(-Util.getValue(endMatrix, Matrix.MTRANS_X), -Util.getValue(endMatrix, Matrix.MTRANS_Y));
        int drawableWidth = mImageView.getDrawable().getIntrinsicWidth();
        int drawableHeight = mImageView.getDrawable().getIntrinsicHeight();
        Matrix initMatrix = getOriginalMatrix(initRect, drawableWidth, drawableHeight, 1f);
        finalRect(endRect);
        originalOpenTransAnim = new TransformAnimation(initRect, endRect, initMatrix, endMatrix, 255);
        originalOpenTransAnim.setListener(new SimpleAnimListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                running = false;
                mImageView.resetMatrix();
                runningOriginalTrans = false;
            }
        });
        runningOriginalTrans = true;
        originalOpenTransAnim.start();
    }

    public void closeOriginalTransform() {
        currentState = STATE_ORIGINAL_CLOSE;
        running = true;
        boolean runOpenFlag = (originalOpenTransAnim != null && originalOpenTransAnim.isRunning());
        if (runOpenFlag) originalOpenTransAnim.cancel();
        RectF displayRect = Util.getDisplayRect(mImageView);
        //得到当前图像的显示矩形
        RectF initRect = runOpenFlag ? new RectF(transformRect) : displayRect;
        //得到结束位置的显示矩形
        RectF endRect = mImageConfig.imageRectF;
        Matrix displayMatrix = new Matrix(mImageView.getImageMatrix());
        //得到当前图像的显示矩阵
        Matrix initMatrix = runOpenFlag ? new Matrix(transformMatrix) : displayMatrix;
        if (!runOpenFlag)
            initMatrix.postTranslate(-Util.getValue(initMatrix, Matrix.MTRANS_X), -Util.getValue(initMatrix, Matrix.MTRANS_Y));
        //新建结束图像的矩阵
        Matrix endMatrix = getOriginalMatrix(endRect, displayRect.width(), displayRect.height(), Util.getValue(displayMatrix, Matrix.MSCALE_X));
        finalRect(initRect);
        TransformAnimation closeTrans = new TransformAnimation(initRect, endRect, initMatrix, endMatrix, 0);
        closeTrans.setListener(new SimpleAnimListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onCloseListener != null) onCloseListener.close();
            }
        });
        runningOriginalTrans = true;
        closeTrans.start();
    }

    /**
     * 避免长图的时候原图矩阵太大导致矩阵变形效果差
     *
     * @param rect
     */
    private void finalRect(RectF rect) {
        if (rect.height() > mImageView.getHeight()) {
            switch (mImageConfig.scaleType) {
                case START_CROP:
                case END_CROP:
                case CENTER_CROP:
                    rect.bottom = mImageView.getHeight();
                    break;
            }
        }
    }

    private Matrix getOriginalMatrix(RectF rectf, float width, float height, float oScale) {
        //新建结束图像的矩阵
        Matrix matrix = new Matrix();
        //得到目标矩阵相对于当前矩阵的宽和高的缩放比例
        float scaleX = rectf.width() / width;
        float scaleY = rectf.height() / height;
        //由于图片比例不定,这里得到最匹配目标矩形的scale
        float scale = Math.max(scaleX, scaleY);
        //得到最终的矩阵scale
        float tempScale = scale * oScale;
        //根据不同的裁剪类型
        switch (mImageConfig.scaleType) {
            case CENTER_CROP: {
                //当预览图是居中裁剪
                matrix.setScale(tempScale, tempScale);
                float dx = (width * scale - rectf.width()) * .5f;
                float dy = (height * scale - rectf.height()) * .5f;
                matrix.postTranslate(-dx, -dy);
                break;
            }
            case START_CROP: {
                //当预览图是顶部裁剪
                if (width > height) {
                    matrix.setScale(tempScale, tempScale);
                    float dy = (height * scale - rectf.height()) * .5f;
                    matrix.postTranslate(0, -dy);
                } else {
                    matrix.setScale(tempScale, tempScale);
                    float dx = (width * scale - rectf.width()) * .5f;
                    matrix.postTranslate(-dx, 0);
                }
                break;
            }
            case END_CROP: {
                //当预览图是尾部裁剪
                if (width > height) {
                    matrix.setScale(tempScale, tempScale);
                    float dx = width * scale - rectf.width();
                    float dy = (height * scale - rectf.height()) * .5f;
                    matrix.postTranslate(-dx, -dy);
                } else {
                    matrix.setScale(tempScale, tempScale);
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

    public boolean isRunningOriginalTrans(){
        return runningOriginalTrans;
    }
    public boolean isRunning() {
        return running;
    }


    /**
     * 绘制变形过程中的图形
     *
     * @param canvas
     */
    public void onDraw(Canvas canvas) {
        if (mImageView.getDrawable() == null) return;
        int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.translate(transformRect.left, transformRect.top);
        canvas.clipRect(0, 0, transformRect.width(), transformRect.height());
        canvas.concat(transformMatrix);
        mImageView.getDrawable().draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    public void onLayoutChange() {
        if (showFlagAtLayoutChange) {
            showFlagAtLayoutChange = false;
            switch (currentState) {
                case STATE_THUMBNAIL_OPEN:
                    _showThumb();
                    break;
                case STATE_THUMBNAIL_OPEN_TRANS:
                    _showThumbWithTransform();
                    break;
                case STATE_ORIGINAL_OPEN_TRANS:
                case STATE_ORIGINAL_OPEN_TRANS_FORM_THUM:
                    _showOriginalWithTransform();
                    break;
            }
        }
    }

    public void runCloseTransform() {
        switch (currentState) {
            case STATE_THUMBNAIL_OPEN:
            case STATE_THUMBNAIL_OPEN_TRANS:
                closeThumbTransform();
                break;
            case STATE_ORIGINAL_OPEN_TRANS:
            case STATE_ORIGINAL_OPEN_TRANS_FORM_THUM:
                closeOriginalTransform();
                break;
            default:
                closeOriginalTransform();
                break;
        }
    }

    public void setOnCloseListener(TransImageView.OnCloseListener onCloseListener) {
        this.onCloseListener = onCloseListener;
    }

    public void pause() {
        running = false;
    }

    /**
     * 图片变形动画
     */
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
        protected boolean isRunning = false;

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
            if (!isRunning) return;
            float t = interpolate();
            rectFEvaluator.evaluate(t, srcRectF, dstRectF);
            matrixEvaluator.evaluate(t, srcMatrix, dstMatrix);

            //设置背景颜色的透明度
            mImageView.setBackgroundAlpha((int) (mStartAlpha + (mEndAlpha - mStartAlpha) * t));
            ViewCompat.postInvalidateOnAnimation(mImageView);
            if (t < 1f) {
                Compat.postOnAnimation(mImageView, this);
            } else {
                isRunning = false;
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
            isRunning = true;
            Compat.postOnAnimation(mImageView, this);
        }

        public void cancel() {
            isRunning = false;
        }

        public boolean isRunning() {
            return isRunning;
        }
    }
}
