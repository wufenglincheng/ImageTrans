package it.liuting.imagetrans.image;

import android.animation.Animator;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

import it.liuting.imagetrans.TransImageView;
import it.liuting.imagetrans.listener.SimpleAnimListener;

/**
 * Created by liuting on 17/5/31.
 * 预览图片的进入和退出的变形
 */

public class TempTransformAttacher extends TransformAttacher {
    private RectF initRect;
    private RectF endRect;
    private Matrix initMatrix;
    private Matrix endMatrix;
    private TransformAnimation openTransformAnim;
    private boolean runFlagAtLayoutChange = false;
    private boolean withTransform = true;

    public TempTransformAttacher(TransImageView imageView) {
        super(imageView);
    }

    private void init() {
        initRect = new RectF(mImageConfig.imageRectF);
        float tempWidth = mImageView.getWidth() / 5;
        float tempHeight = tempWidth * initRect.height() / initRect.width();
        endRect = new RectF(mImageView.getWidth() / 2 - tempWidth / 2, mImageView.getHeight() / 2 - tempHeight / 2, mImageView.getWidth() / 2 + tempWidth / 2, mImageView.getHeight() / 2 + tempHeight / 2);
        int drawableWidth = mImageView.getDrawable().getIntrinsicWidth();
        int drawableHeight = mImageView.getDrawable().getIntrinsicHeight();
        initMatrix = getMatrix(initRect, drawableWidth, drawableHeight);
        endMatrix = getMatrix(endRect, drawableWidth, drawableHeight);
    }

    public void onLayoutChange() {
        if (runFlagAtLayoutChange) {
            runFlagAtLayoutChange = false;
            if (withTransform) _runOpenTransform();
            else _showPreview();
        }
    }

    private Matrix getMatrix(RectF rectF, int drawableWidth, int drawableHeight) {
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

    public void showPreview() {
        withTransform = false;
        if (mImageView.getWidth() > 0) {
            _showPreview();
        } else {
            runFlagAtLayoutChange = true;
        }
    }

    private void _showPreview() {
        init();
        transformRect.set(endRect);
        transformMatrix.set(endMatrix);
        mImageView.invalidate();
    }

    public void runOpenTransform() {
        withTransform = true;
        if (mImageView.getWidth() > 0) {
            _runOpenTransform();
        } else {
            runFlagAtLayoutChange = true;
        }
    }

    private void _runOpenTransform() {
        init();
        openTransformAnim = new TransformAnimation(initRect, endRect, initMatrix, endMatrix, 255);
        openTransformAnim.start();
    }

    public void runCloseTransform(final TransImageView.OnCloseListener onCloseListener) {
        if (openTransformAnim != null) openTransformAnim.cancel();
        TransformAnimation closeTransformAnim = new TransformAnimation(new RectF(transformRect), initRect, new Matrix(transformMatrix), initMatrix, 0);
        closeTransformAnim.setListener(new SimpleAnimListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onCloseListener != null) onCloseListener.close();
            }
        });
        closeTransformAnim.start();
    }

    public void onDraw(Canvas canvas) {
        int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.translate(transformRect.left, transformRect.top);
        canvas.clipRect(0, 0, transformRect.width(), transformRect.height());
        canvas.concat(transformMatrix);
        mImageView.getDrawable().draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    public RectF getTempRectF() {
        return transformRect;
    }
}
