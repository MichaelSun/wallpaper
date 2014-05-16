package com.michael.qrcode.view;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;

public class CropImageView extends View {
    public enum CropMode {
        NONE, DRAG, ZOOM
    }

    private static final float ZOOM_MIN_DISTANCE = 10.0f;

    public static final long MIN_PIXEL = 50;

    private CropMode mCropMode = CropMode.NONE;

    private PointF mCropStartPoint = new PointF();
    private float mCropInitDistance;

    private RectF mOffset = new RectF();
    private Rect mHighlightRect = new Rect();

    private float mScale = 1.0f;
    private float mMaxScale;

    private boolean mIsFirst = false;

    private Drawable mDrawable;
    private int mOrientation = 0;
    private int mRealWidth;
    private int mRealHeight;

    private Context mContext;
    private float mDensity;

    private Paint mPaint = new Paint();

    private boolean mIsCrop = true;

    public CropImageView(Context context) {
        super(context);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);

    }

    private void init(Context context) {
        this.mContext = context;
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mDensity = mContext.getResources().getDisplayMetrics().density;
    }

    // 放大后裁剪出的图片最小长度不小于ImageProcessConstants.MIN_PIXEL
    private void initMaxScale() {
        mMaxScale = Math.min(mRealHeight, mRealWidth) / MIN_PIXEL;
    }

    public void setImageDrawable(Drawable drawable) {
        if (drawable == null) {
            return;
        }

        mDrawable = drawable;
        mRealWidth = (int) (drawable.getIntrinsicWidth() * mDensity);
        mRealHeight = (int) (drawable.getIntrinsicHeight() * mDensity);
        mIsFirst = true;
        initMaxScale();
        invalidate();
    }

    public void setImageBitmap(Bitmap bitmap) {
        mDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
        setImageDrawable(mDrawable);
    }

    public void setIsCrop(boolean isCrop) {
        mIsCrop = isCrop;
    }

    public boolean isCrop() {
        return mIsCrop;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mCropMode = CropMode.DRAG;
                mCropStartPoint.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                mCropMode = CropMode.NONE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mCropInitDistance = distance(event);
                if (mCropInitDistance > ZOOM_MIN_DISTANCE) {
                    mCropMode = CropMode.ZOOM;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mCropMode = CropMode.NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCropMode == CropMode.DRAG) {
                    mOffset.offset(event.getX() - mCropStartPoint.x, event.getY() - mCropStartPoint.y);
                    checkBounds();
                    invalidate();
                    mCropStartPoint.set(event.getX(), event.getY());
                } else if (mCropMode == CropMode.ZOOM) {
                    float endDistance = distance(event);
                    if (endDistance > ZOOM_MIN_DISTANCE) {
                        float scale = endDistance / mCropInitDistance;
                        if (scale * mScale < 1.0f) {
                            scale = 1.0f / mScale;
                        } else if (scale * mScale > mMaxScale) {
                            scale = mMaxScale / mScale;
                        }
                        mScale *= scale;
                        mCropInitDistance = endDistance;

                        float midX = (event.getX(0) + event.getX(1)) / 2.0f;
                        float midY = (event.getY(0) + event.getY(1)) / 2.0f;

                        if (midX < mHighlightRect.left) {
                            midX = mHighlightRect.left;
                        }
                        if (midX > mHighlightRect.right) {
                            midX = mHighlightRect.right;
                        }
                        if (midY < mHighlightRect.top) {
                            midY = mHighlightRect.top;
                        }
                        if (midY > mHighlightRect.bottom) {
                            midY = mHighlightRect.bottom;
                        }

                        mOffset.left = (mOffset.left - midX) * scale + midX;
                        mOffset.right = (mOffset.right - midX) * scale + midX;
                        mOffset.top = (mOffset.top - midY) * scale + midY;
                        mOffset.bottom = (mOffset.bottom - midY) * scale + midY;
                        checkBounds();
                        invalidate();
                    }
                }

                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mDrawable == null || mDrawable.getIntrinsicWidth() == 0 || mDrawable.getIntrinsicHeight() == 0) {
            return;
        }

        if (mIsFirst) {
            mIsFirst = false;

            int w = mRealWidth;
            int h = mRealHeight;
            int width = getWidth();
            int height = getHeight();
            int midX = width / 2;
            int midY = height / 2;

            mHighlightRect.set(0, (height - width) / 2, width, height - (height - width) / 2);

            if (w < h) {
                h = h * width / w;
                w = width;
            } else {
                w = w * width / h;
                h = width;
            }
            mOffset.set(midX - w / 2, midY - h / 2, midX + w / 2, midY + h / 2);
        }

        mDrawable.setBounds((int) mOffset.left, (int) mOffset.top, (int) mOffset.right, (int) mOffset.bottom);
        mDrawable.draw(canvas);

        if (isCrop()) {
            mPaint.reset();
            mPaint.setColor(Color.WHITE);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(4f);
            canvas.drawLine(mHighlightRect.left, mHighlightRect.top, mHighlightRect.right, mHighlightRect.top, mPaint);
            canvas.drawLine(mHighlightRect.left, mHighlightRect.top, mHighlightRect.left, mHighlightRect.bottom, mPaint);
            canvas.drawLine(mHighlightRect.left, mHighlightRect.bottom, mHighlightRect.right, mHighlightRect.bottom, mPaint);
            canvas.drawLine(mHighlightRect.right, mHighlightRect.top, mHighlightRect.right, mHighlightRect.bottom, mPaint);

            canvas.save();
            canvas.clipRect(mHighlightRect, Region.Op.DIFFERENCE);
            canvas.drawColor(Color.parseColor("#4d000000"));
            canvas.restore();
        }
    }

    private float distance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return FloatMath.sqrt(x * x + y * y);
    }

    private void checkBounds() {
        if (mOffset.left > mHighlightRect.left) {
            mOffset.offset(mHighlightRect.left - mOffset.left, 0);
        }
        if (mOffset.top > mHighlightRect.top) {
            mOffset.offset(0, mHighlightRect.top - mOffset.top);
        }
        if (mOffset.right < mHighlightRect.right) {
            mOffset.offset(mHighlightRect.right - mOffset.right, 0);
        }
        if (mOffset.bottom < mHighlightRect.bottom) {
            mOffset.offset(0, mHighlightRect.bottom - mOffset.bottom);
        }
    }

    public Rect getCropArea() {
        float w = mOffset.right - mOffset.left;
        float h = mOffset.bottom - mOffset.top;

        Rect rect = new Rect();
        rect.left = (int) (mRealWidth * (mHighlightRect.left - mOffset.left) / w);
        rect.top = (int) (mRealHeight * (mHighlightRect.top - mOffset.top) / h);

//        rect.right = (int) (Math.min(mRealHeight, mRealWidth) / mScale);
//        rect.bottom = rect.right;
        rect.right = rect.left + (int) (mHighlightRect.width() * mRealWidth / w);
        rect.bottom = rect.top + (int) (mHighlightRect.height() * mRealHeight / h);

        return rect;
    }

    public int getRealWidth() {
        return mRealWidth;
    }

    public int getRealHeight() {
        return mRealHeight;
    }

    public int getOrientation() {
        return mOrientation;
    }
}
