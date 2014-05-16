package com.michael.qrcode.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import com.michael.qrcode.R;

/**
 * Created by zhangdi on 13-12-18.
 */
public class ScanBorderView extends View {

    private Rect mSrcRect = new Rect();
    private Rect mDstRect = new Rect();

    private Paint mPaint = new Paint();

    private int mCornerWidth = 0;
    private int mCornerHeight = 0;

    private int mScanOffset = -1;
    private int mScanLinePadding = 0;
    private int mScanLineHeight = 0;

    private Bitmap mCorner1;
    private Bitmap mCorner2;
    private Bitmap mCorner3;
    private Bitmap mCorner4;
    private Bitmap mScanLine;

    private ValueAnimator mAnimator;

    private boolean mStartAnimator = false;

    public ScanBorderView(Context context) {
        this(context, null);
    }

    public ScanBorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mCorner1 = BitmapFactory.decodeResource(getResources(), R.drawable.scanqr1);
        mCorner2 = BitmapFactory.decodeResource(getResources(), R.drawable.scanqr2);
        mCorner3 = BitmapFactory.decodeResource(getResources(), R.drawable.scanqr3);
        mCorner4 = BitmapFactory.decodeResource(getResources(), R.drawable.scanqr4);
        mScanLine = BitmapFactory.decodeResource(getResources(), R.drawable.qrcode_scan_line);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        mCornerWidth = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, dm));
        mCornerHeight = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, dm));
        mScanLinePadding = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, dm));
        mScanLineHeight = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9, dm));
    }


    public void startAnimator() {
        if (mAnimator != null && mAnimator.isRunning()) {
            return;
        }
        mStartAnimator = true;
        invalidate();
    }

    public void stopAnimator() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.end();
            mAnimator = null;
        }
        mScanOffset = -1;
        invalidate();
    }

    private void innerStartAnimator(int start, int end) {
        mAnimator = ValueAnimator.ofInt(start, end);
        mAnimator.setDuration(2500);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mScanOffset = (Integer) (animation.getAnimatedValue());
                invalidate();
            }
        });
        mAnimator.setRepeatMode(ValueAnimator.RESTART);
        mAnimator.setRepeatCount(-1);
        mAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimator();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mStartAnimator) {
            mStartAnimator = !mStartAnimator;
            innerStartAnimator(0, canvas.getHeight());
        }

        // draw rectangle
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
        mDstRect.set(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.drawRect(mDstRect, mPaint);

        // draw 4 corner
        mSrcRect.set(0, 0, mCorner1.getWidth(), mCorner1.getHeight());
        mDstRect.set(0, 0, mCornerWidth, mCornerHeight);
        canvas.drawBitmap(mCorner1, mSrcRect, mDstRect, mPaint);

        mSrcRect.set(0, 0, mCorner2.getWidth(), mCorner2.getHeight());
        mDstRect.set(canvas.getWidth() - mCornerWidth, 0, canvas.getWidth(), mCornerHeight);
        canvas.drawBitmap(mCorner2, mSrcRect, mDstRect, mPaint);

        mSrcRect.set(0, 0, mCorner3.getWidth(), mCorner3.getHeight());
        mDstRect.set(0, canvas.getHeight() - mCornerHeight, mCornerWidth, canvas.getHeight());
        canvas.drawBitmap(mCorner3, mSrcRect, mDstRect, mPaint);

        mSrcRect.set(0, 0, mCorner4.getWidth(), mCorner4.getHeight());
        mDstRect.set(canvas.getWidth() - mCornerWidth, canvas.getHeight() - mCornerHeight, canvas.getWidth(), canvas.getHeight());
        canvas.drawBitmap(mCorner4, mSrcRect, mDstRect, mPaint);

        // draw scan line
        if (mScanOffset >= 0) {
            mSrcRect.set(0, 0, mScanLine.getWidth(), mScanLine.getHeight());
            mDstRect.set(mScanLinePadding, mScanOffset, canvas.getWidth() - mScanLinePadding, mScanOffset + mScanLineHeight);
            canvas.drawBitmap(mScanLine, mSrcRect, mDstRect, mPaint);
        }
    }
}
