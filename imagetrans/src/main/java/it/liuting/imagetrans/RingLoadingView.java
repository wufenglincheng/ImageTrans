package it.liuting.imagetrans;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class RingLoadingView extends View {
    private float DEFAULT_SIZE;
    private final Paint mPaint = new Paint();
    private float mStrokeWidth;
    private RectF mBounds;
    private int mColor;
    private int mArcColor;
    private float mCircleRadius;
    private float mPrecent;
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
        DEFAULT_SIZE = Util.dpToPx(48, getContext());
        mStrokeWidth = Util.dpToPx(1, getContext());
        setBackgroundResource(android.R.color.transparent);
        //根据默认的宽高,构建view矩阵
        mBounds = new RectF(0, 0, DEFAULT_SIZE, DEFAULT_SIZE);
        //向内聚,至少是半个线宽,否则会出现图行出界的情况
        mBounds.inset(mStrokeWidth, mStrokeWidth);
        //进度条范围
        mProgressBounds = new RectF();
        mProgressBounds.set(mBounds);
        mProgressBounds.inset(Util.dpToPx(6, getContext()), Util.dpToPx(6, getContext()));

        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mStrokeWidth);

        mColor = Color.parseColor("#CCFFFFFF");
        mArcColor = mColor;
        mBgColor = Color.parseColor("#55000000");

        mCircleRadius = Math.min(mBounds.height(), mBounds.width()) / 2.0f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //背景描边
        mPaint.setColor(mArcColor);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(mBounds.centerX(), mBounds.centerY(), mCircleRadius, mPaint);

        //画背景圆
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mBgColor);
        canvas.drawCircle(mBounds.centerX(), mBounds.centerY(), mCircleRadius, mPaint);

        //画进度
        mPaint.setColor(mColor);
        canvas.drawArc(mProgressBounds, -90F, 360 * mPrecent, true, mPaint);

    }

    /**
     * 设置进度
     *
     * @param percent
     */
    public void setProgress(float percent) {
        mPrecent = (percent >= 1 ? 1 : percent);
        postInvalidate();
    }
}
