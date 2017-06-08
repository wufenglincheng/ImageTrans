package it.liuting.imagetrans.image;

import android.animation.Animator;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;

import it.liuting.imagetrans.TransImageView;
import it.liuting.imagetrans.Util;
import it.liuting.imagetrans.listener.SimpleAnimListener;

/**
 * Created by liuting on 17/5/26.
 * 原始图片的进入和退出的变形
 */

public class ImageTransformAttacher extends TransformAttacher {
    private boolean runningOpen = false;
    private boolean runningClose = false;
    private TransformAnimation openTransAnim;
    private boolean runAtLayoutChange = false;
    private RectF initRect;

    public ImageTransformAttacher(TransImageView imageView) {
        super(imageView);
    }

    public void onLayoutChange() {
        if (runAtLayoutChange) {
            runAtLayoutChange = false;
            _runOpenTransform();
        }
    }

    //开始
    public void runOpenTransform(RectF initRect) {
        this.initRect = initRect;
        if (mImageView.getWidth() > 0) {
            _runOpenTransform();
        } else {
            runAtLayoutChange = true;
        }

    }

    private void _runOpenTransform() {
        if (runningOpen) return;
        runningOpen = true;
        //得到结束位置的显示矩形
        RectF endRect = Util.getDisplayRect(mImageView);
        Log.e("_runOpenTransform",mImageView.isAttachedToWindow() + "---");

        //创建矩形转换动画
        Matrix endMatrix = new Matrix(mImageView.getImageMatrix());
        endMatrix.postTranslate(-Util.getValue(endMatrix, Matrix.MTRANS_X), -Util.getValue(endMatrix, Matrix.MTRANS_Y));
        int drawableWidth = mImageView.getDrawable().getIntrinsicWidth();
        int drawableHeight = mImageView.getDrawable().getIntrinsicHeight();
        Matrix initMatrix = getMatrix(initRect, drawableWidth, drawableHeight, 1f);
        finalRect(endRect);
        openTransAnim = new TransformAnimation(initRect, endRect, initMatrix, endMatrix, 255);
        openTransAnim.setListener(new SimpleAnimListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                runningOpen = false;
                mImageView.update();
            }
        });
        openTransAnim.start();
    }

    public void runCloseTransform(final TransImageView.OnCloseListener closeListener) {
        if (runningClose) return;
        runningClose = true;
        boolean RunOpenFlag = runningOpen;
        if (RunOpenFlag) openTransAnim.cancel();
        RectF displayRect = Util.getDisplayRect(mImageView);
        //得到当前图像的显示矩形
        RectF initRect = RunOpenFlag ? new RectF(transformRect) : displayRect;
        //得到结束位置的显示矩形
        RectF endRect = mImageConfig.imageRectF;
        Matrix displayMatrix = new Matrix(mImageView.getImageMatrix());
        //得到当前图像的显示矩阵
        Matrix initMatrix = RunOpenFlag ? new Matrix(transformMatrix) : displayMatrix;
        if (!RunOpenFlag)
            initMatrix.postTranslate(-Util.getValue(initMatrix, Matrix.MTRANS_X), -Util.getValue(initMatrix, Matrix.MTRANS_Y));
        //新建结束图像的矩阵
        Matrix endMatrix = getMatrix(endRect, displayRect.width(), displayRect.height(), Util.getValue(displayMatrix, Matrix.MSCALE_X));
        finalRect(initRect);
        TransformAnimation closeTrans = new TransformAnimation(initRect, endRect, initMatrix, endMatrix, 0);
        closeTrans.setListener(new SimpleAnimListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (closeListener != null) closeListener.close();
            }
        });
        closeTrans.start();
    }

    private void finalRect(RectF initRect) {
        if (initRect.height() > mImageView.getHeight()) {
            switch (mImageConfig.scaleType) {
                case START_CROP:
                    initRect.bottom = mImageView.getHeight();
                    break;
                case END_CROP:
                    initRect.bottom = mImageView.getHeight();
                    break;
                case CENTER_CROP:
                    initRect.bottom = mImageView.getHeight();
                    break;
            }
        }
    }


    public Matrix getMatrix(RectF rectf, float width, float height, float oScale) {
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


    public boolean isRunning() {
        return runningClose || runningOpen;
    }

    /**
     * 绘制变形过程中的图形
     *
     * @param canvas
     */
    public void onDraw(Canvas canvas) {
        Log.e("ImageTransformAttacher", Util.getValue(transformMatrix,Matrix.MSCALE_X) +"---");
        if (mImageView.getDrawable() == null) return;
        int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.translate(transformRect.left, transformRect.top);
        canvas.clipRect(0, 0, transformRect.width(), transformRect.height());
        //这里设置矩阵 是为了查看大图根据矩阵分块加载 的逻辑
        mImageView.setImageMatrix(mImageView.getBaseMatrix());
        canvas.concat(transformMatrix);
        mImageView.getDrawable().draw(canvas);
        canvas.restoreToCount(saveCount);
    }
}
