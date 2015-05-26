package com.zzmstring.ganjilayout;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by zzmstring on 2015/5/25.
 */
public class GanjiLayout extends FrameLayout {
    private ViewDragHelper dragHelper;

    private Context context;
    private RelativeLayout topView;
    private RelativeLayout contentView;
    private Status status = Status.Close;
    private boolean shouldIntercept = true;
    private int topViewHeight;
    private int dragRange;
    private int contentTop;
    private int topViewWid;
    private int tempDy;
    private View floatingLayerView;
    public GanjiLayout(Context context) {
        this(context, null);
    }
    public GanjiLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
        this.context = context;
    }
    public GanjiLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        dragHelper = ViewDragHelper.create(this, 1.0f,dragHelperCallback);
        floatingLayerView=new View(context);
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        dragRange = getHeight();
        topViewHeight = topView.getHeight();
        topViewWid=topView.getWidth();
        contentView.layout(0,contentTop,topViewWid,contentTop+dragRange);
        topView.layout(0,0-topViewHeight/2+contentTop/2,topViewWid,topViewHeight/2+contentTop/2);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() < 2) {
            throw new RuntimeException("Content view must contains two child views at least.");
        }
        topView = (RelativeLayout) getChildAt(0);
        contentView = (RelativeLayout) getChildAt(1);
        int viewcount = topView.getChildCount();
        floatingLayerView.setLayoutParams(topView.getLayoutParams());
        floatingLayerView.setBackgroundColor(Color.BLACK);
        int index = viewcount == 0 ? 0 : viewcount ;

        topView.addView(floatingLayerView,index);
    }
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        try {
            dragHelper.processTouchEvent(e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }
    @Override
    public void computeScroll() {
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            boolean intercept = shouldIntercept && dragHelper.shouldInterceptTouchEvent(ev);
            return intercept;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }
    private ViewDragHelper.Callback dragHelperCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View view, int i) {
            if (view == topView) {
                dragHelper.captureChildView(contentView, i);
                return false;
            }
            return view == contentView;
        }
        @Override
        public int getViewVerticalDragRange(View child) {
            return dragRange;
        }
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return 0;
        }
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            tempDy = dy;
            return Math.min(topViewHeight, Math.max(top, getPaddingTop()));
        }
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            int top = 0;
            if (yvel > 0 ||tempDy > 0) {
                top = topViewHeight + getPaddingTop();
            }else if(yvel < 0||tempDy <= 0){
                top = getPaddingTop();
            }else {
                top = contentTop > topViewHeight/2 ? topViewHeight + getPaddingTop() : getPaddingTop();
            }
            dragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);
            postInvalidate();
        }
        @Override
        public void onViewPositionChanged(View changedView, int left, int top,
                                          int dx, int dy) {
            contentTop = top;
            animate(contentTop);
            requestLayout();
        }
        @Override
        public void onViewDragStateChanged(int state) {
            if (state == ViewDragHelper.STATE_IDLE) {
                if (contentTop == getPaddingTop() ) {
                    status = Status.Close;
                } else {
                    status = Status.Open;
                }
            } else {
                status = Status.Sliding;
            }
            super.onViewDragStateChanged(state);
        }
    };
    public enum Status {
        Open, Close,Sliding
    }
    private void animate(int t){
        float percent = 1.0f - (float) t / topViewHeight;
        ViewHelper.setAlpha(floatingLayerView, percent);
    }
}
