package com.demo.imagetransdemo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.demo.imagetransdemo.MyApplication;
import com.demo.imagetransdemo.R;


/**
 * Created by qihuan on 16/9/6.
 */
public class RingLoadingView extends View {
    private static final float DEFAULT_SIZE = MyApplication.dpToPx(48);
    private final Paint mPaint = new Paint();
    private float mStrokeWidth = MyApplication.dpToPx(1);
    private RectF mBounds;
    private int mColor;
    private int mArcColor;
    private float mCircleRadius;
    private float mpercent;
    private RectF mProgressBounds;
    private int mBgColor;

    public RingLoadingView(Context context) {
        super(context);
        init();
    }

    public RingLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RingLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundResource(android.R.color.transparent);
        //根据默认的宽高,构建view矩阵
        mBounds = new RectF(0, 0, DEFAULT_SIZE, DEFAULT_SIZE);
        //向内聚,至少是半个线宽,否则会出现图行出界的情况
        mBounds.inset(mStrokeWidth, mStrokeWidth);
        //进度条范围
        mProgressBounds = new RectF();
        mProgressBounds.set(mBounds);
        mProgressBounds.inset(MyApplication.dpToPx(6), MyApplication.dpToPx(6));

        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mStrokeWidth);

        mColor = Color.parseColor("#CCFFFFFF");
        mArcColor = mColor;
        mBgColor = getContext().getResources().getColor(R.color.pers30_black);

        mCircleRadius = Math.min(mBounds.height(), mBounds.width()) / 2.0f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //背景描边
        mPaint.setColor(mArcColor);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(mBounds.centerX(),mBounds.centerY(), mCircleRadius, mPaint);

        //画背景圆
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mBgColor);
        canvas.drawCircle(mBounds.centerX(), mBounds.centerY(), mCircleRadius, mPaint);

        //画进度
        mPaint.setColor(mColor);
        canvas.drawArc(mProgressBounds, -90F, 360 * mpercent, true, mPaint);

    }

    /**
     * 设置进度
     *
     * @param percent
     */
    public void setProgress(float percent) {
        mpercent = (percent >= 1 ? 1 : percent);
        postInvalidate();
    }

    private int halfAlphaColor(int colorValue) {
        int startA = (colorValue >> 24) & 0xff;
        int startR = (colorValue >> 16) & 0xff;
        int startG = (colorValue >> 8) & 0xff;
        int startB = colorValue & 0xff;

        return ((startA / 2) << 24)
                | (startR << 16)
                | (startG << 8)
                | startB;
    }
}
