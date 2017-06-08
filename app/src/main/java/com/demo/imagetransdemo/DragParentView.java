package com.demo.imagetransdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

/**
 * Created by liuting on 17/5/27.
 */

public class DragParentView extends ViewGroup {
    View childView;
    GestureDetector gestureDetector;
    private float lastDownX;
    private float lastDownY;
    private float mTouchSlop;
    private Paint textPaint;
    private String drawText = "在此区域拖拽";

    public DragParentView(Context context) {
        this(context, null);
    }

    public DragParentView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragParentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Color.parseColor("#43434343"));
        mTouchSlop = ViewConfiguration
                .get(getContext()).getScaledTouchSlop();
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                float downX = e.getX();
                float downY = e.getY();
                RectF rect = getChildViewRect();
                if (rect.contains(downX, downY)) {
                    childView.performClick();
                }
                return false;
            }
        });
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(60);
    }

    public void setView(View view, int width, int height) {
        removeAllViews();
        MarginLayoutParams lp = new MarginLayoutParams(width, height);
        addView(view, lp);
    }

    public void requestChildWidth(int width) {
        if (childView == null) return;
        childView.getLayoutParams().width = width;
        childView.setLayoutParams(childView.getLayoutParams());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float textWidth = textPaint.measureText(drawText);
        canvas.drawText(drawText, getWidth() / 2 - textWidth / 2, getHeight() / 2, textPaint);
    }

    public void requestChildHeight(int height) {
        if (childView == null) return;
        childView.getLayoutParams().height = height;
        childView.setLayoutParams(childView.getLayoutParams());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() == 0) return;
        childView = getChildAt(0);
        MarginLayoutParams lp = (MarginLayoutParams) childView.getLayoutParams();
        int left = lp.leftMargin;
        int top = lp.topMargin;
        if (left < 0) left = 0;
        if (top < 0) top = 0;
        if (left + lp.width > getWidth())
            left = getWidth() - lp.width;
        if (top + lp.height > getHeight())
            top = getHeight() - lp.height;

        int right = left + lp.width;
        int bottom = top + lp.height;
        childView.layout(left, top, right, bottom);
    }

    boolean isDragFlag = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    float dx;
    float dy;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                lastDownX = event.getX();
                lastDownY = event.getY();
                if (childView != null) {
                    RectF rect = getChildViewRect();
                    if (rect.contains(lastDownX, lastDownY)) {
                        isDragFlag = true;
                        dx = lastDownX - rect.left;
                        dy = lastDownY - rect.top;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (isDragFlag) {
                    float downX = event.getX();
                    float downY = event.getY();
                    float tempX = downX - lastDownX;
                    float tempY = downY - lastDownY;
                    if (Math.sqrt((tempX * tempX) + (tempY * tempY)) < mTouchSlop) {
                        break;
                    }
                    lastDownX = downX;
                    lastDownY = downY;
                    MarginLayoutParams lp = (MarginLayoutParams) childView.getLayoutParams();
                    int left = (int) (downX - dx);
                    int top = (int) (downY - dy);
                    lp.leftMargin = left;
                    lp.topMargin = top;
                    childView.setLayoutParams(lp);
                    requestLayout();
                }
                break;
            }
            default:
                isDragFlag = false;
                break;
        }
        gestureDetector.onTouchEvent(event);
        return true;
    }

    private RectF getChildViewRect() {
        RectF rect = new RectF(childView.getLeft(), childView.getTop(), childView.getRight(), childView.getBottom());
        return rect;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    @Override
    public LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new MarginLayoutParams(p.width, p.height);
    }
}
