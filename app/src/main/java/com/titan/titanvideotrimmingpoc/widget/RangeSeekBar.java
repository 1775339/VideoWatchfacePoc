package com.titan.titanvideotrimmingpoc.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;


/**
 * ================================================
 * 作    者：顾修忠-guxiuzhong@youku.com/gfj19900401@163.com
 * 版    本：
 * 创建日期：2017/4/4-下午1:22
 * 描    述：
 * 修订历史：
 * ================================================
 */

public class RangeSeekBar extends View {

    private static final String TAG = RangeSeekBar.class.getSimpleName();
    private long minValue;
    private long maxValue;
    private long minLimit = 1000;//1s
    private double normalizedMinValue = 0d;//点坐标占总长度的比例值，范围从0-1
    private double normalizedMaxValue = 1d;//点坐标占总长度的比例值，范围从0-1
    private double normalizedOffsetValue = 0d;//点坐标占总长度的比例值，范围从0-1
    private int borderHeight;
    private Paint maskPaint;
    private Paint thumbPaint;
    private Paint thumbGrayPaint;
    private int thumbWidth;
    private int thumbHeight;
    private final RectF thumbRect = new RectF();
    private Thumb pressedThumb;
    private boolean isTouchDown;
    private boolean mIsDragging;
    private boolean notifyWhileDragging = false;

    public enum Thumb {
        MIN, MAX
    }

    public RangeSeekBar(Context context) {
        super(context);
        init();
    }

    public RangeSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RangeSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);

        thumbWidth = dip2px(8);
        borderHeight = dip2px(2);

        //绘制黑色遮罩
        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setColor(Color.BLACK);
        maskPaint.setAlpha(80);
        //绘制白色边框
        thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setColor(Color.parseColor("#FFFFFF"));
        //绘制左右两边灰色线条
        thumbGrayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbGrayPaint.setColor(Color.parseColor("#CCCCCC"));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width =getDefaultSize(dip2px(39), widthMeasureSpec);
        int height =getDefaultSize(dip2px(56), heightMeasureSpec);
        thumbHeight = height;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float rangeL = normalizedToScreen(normalizedMinValue);
        float rangeR = normalizedToScreen(normalizedMaxValue) + 2 * thumbWidth;

        //画左边的半透明遮罩
        int leftWidth = (int) rangeL + thumbWidth / 2;
        canvas.drawRect(0, 0, leftWidth, getHeight(), maskPaint);

        //画右边的半透明遮罩
        int rightWidth = (int) (getWidth() - rangeR) + thumbWidth / 2;
        canvas.drawRect((int) (rangeR - thumbWidth / 2), 0, (int) (rangeR - thumbWidth / 2) + rightWidth, getHeight(), maskPaint);

        //画上下的边框
        canvas.drawRect(rangeL, 0, rangeR, borderHeight, thumbPaint);
        canvas.drawRect(rangeL, getHeight() - borderHeight, rangeR, getHeight(), thumbPaint);

        //画左thumb
        thumbRect.left = normalizedToScreen(normalizedMinValue);
        thumbRect.top = 0;
        thumbRect.right = normalizedToScreen(normalizedMinValue) + thumbWidth;
        thumbRect.bottom = thumbHeight;
        canvas.drawRect(thumbRect, thumbPaint);

        thumbRect.left = thumbRect.left + thumbWidth / 3f;
        thumbRect.top = thumbHeight / 3f;
        thumbRect.right = thumbRect.right - thumbWidth / 3f;
        thumbRect.bottom = thumbRect.top + thumbHeight / 3f;
        canvas.drawRect(thumbRect, thumbGrayPaint);

        //画右thumb
        thumbRect.left = normalizedToScreen(normalizedMaxValue) + thumbWidth;
        thumbRect.top = 0;
        thumbRect.right = normalizedToScreen(normalizedMaxValue) + 2 * thumbWidth;
        thumbRect.bottom = thumbHeight;
        canvas.drawRect(thumbRect, thumbPaint);

        thumbRect.left = thumbRect.left + thumbWidth / 3f;
        thumbRect.top = thumbHeight / 3f;
        thumbRect.right = thumbRect.right - thumbWidth / 3f;
        thumbRect.bottom = thumbRect.top + thumbHeight / 3f;
        canvas.drawRect(thumbRect, thumbGrayPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isTouchDown) {
            return super.onTouchEvent(event);
        }

        if (event.getPointerCount() > 1) {
            return super.onTouchEvent(event);
        }

        if (!isEnabled()) {
            return false;
        }

        if (maxValue <= minLimit) {
            return super.onTouchEvent(event);
        }

        final int action = event.getAction();
        if (action != MotionEvent.ACTION_DOWN && pressedThumb == null) {
            return super.onTouchEvent(event);
        }

        Log.e(TAG, "onTouchEvent: " + action + " x: " + event.getX());
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 判断touch到的是最大值thumb还是最小值thumb
                pressedThumb = evalPressedThumb(event.getX());
                if (pressedThumb == null) {
                    return super.onTouchEvent(event);
                }
                attemptClaimDrag();
                setPressed(true);// 设置该控件被按下了
                onStartTrackingTouch();
                trackTouchEvent(event);
                notifyRangeChanged(MotionEvent.ACTION_DOWN);
                break;

            case MotionEvent.ACTION_MOVE:
                if (pressedThumb != null) {
                    if (mIsDragging) {
                        trackTouchEvent(event);
                        if (isNotifyWhileDragging()) {
                            notifyRangeChanged(MotionEvent.ACTION_MOVE);
                        }
                    } else {
                        Log.e(TAG, "没有拖住最大最小值");// 一直不会执行？
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (pressedThumb != null) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                    notifyRangeChanged(MotionEvent.ACTION_UP);
                    pressedThumb = null;
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void notifyRangeChanged(int actionDown) {
        if (listener != null) {
            long offset = normalizedToValue(normalizedOffsetValue);
            listener.onRangeChanged(this, getSelectedMinValue(), getSelectedMaxValue(), offset, actionDown, pressedThumb);
        }
    }

    private void trackTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1) return;
        float x = event.getX();
        if (Thumb.MIN.equals(pressedThumb)) {
            setNormalizedMinValue(screenToNormalized(x, 0));
        } else if (Thumb.MAX.equals(pressedThumb)) {
            setNormalizedMaxValue(screenToNormalized(x, 1));
        }
    }

    private double screenToNormalized(float x, int position) {
        int frameWidth = getWidth() - thumbWidth * 2;
        if (frameWidth <= 0) {
            // prevent division by zero, simply return 0.
            return 0d;
        }

        double xOffset = x - thumbWidth;
        if (position == 2) {
            if (xOffset < 0) {
                xOffset = 0;
            }
            return xOffset / frameWidth;
        }

        if (xOffset < 0) {
            Log.w(TAG, String.format("x(%s) < 0", xOffset));
            xOffset = 0;
        } else if (xOffset > frameWidth) {
            Log.w(TAG, String.format("x(%s) > frameWidth(%s)", xOffset, frameWidth));
            xOffset = frameWidth;
        }

        double result = xOffset / frameWidth;
        double min = minLimit * 1.0d / (maxValue - minValue);

        if (position == 0) {
            if ((result + min) > normalizedMaxValue) {
                return normalizedMaxValue - min;
            }
            return Math.abs(result - normalizedMaxValue) > min ? result : (normalizedMaxValue - min);
        } else {
            if ((result - min) < normalizedMinValue) {
                return normalizedMinValue + min;
            }
            return Math.abs(result - normalizedMinValue) > min ? result : (normalizedMinValue + min);
        }
    }

    /**
     * 计算位于哪个Thumb内
     *
     * @param touchX touchX
     * @return 被touch的是空还是最大值或最小值
     */
    private Thumb evalPressedThumb(float touchX) {
        Thumb result = null;
        boolean minThumbPressed = isInThumbRangeLeft(touchX);
        boolean maxThumbPressed = isInThumbRangeRight(touchX);
        if (minThumbPressed && maxThumbPressed) {
            // 如果两个thumbs重叠在一起，无法判断拖动哪个，做以下处理
            // 触摸点在屏幕右侧，则判断为touch到了最小值thumb，反之判断为touch到了最大值thumb
            Log.w(TAG, "thumbs overlap");
            result = (touchX / getWidth() > 0.5f) ? Thumb.MIN : Thumb.MAX;
        } else if (minThumbPressed) {
            result = Thumb.MIN;
        } else if (maxThumbPressed) {
            result = Thumb.MAX;
        }
        return result;
    }

    private boolean isInThumbRangeLeft(float touchX) {
        // 当前触摸点X坐标-最小值图片中心点在屏幕的X坐标之差<=最小点图片的宽度的一般
        // 即判断触摸点是否在以最小值图片中心为原点，宽度一半为半径的圆内。
        float thumbHalfWidth = thumbWidth / 2f;
        return Math.abs(touchX - normalizedToScreen(normalizedMinValue) - thumbWidth / 2f) <= thumbHalfWidth * 2;
    }

    private boolean isInThumbRangeRight(float touchX) {
        // 当前触摸点X坐标-最小值图片中心点在屏幕的X坐标之差<=最小点图片的宽度的一般
        // 即判断触摸点是否在以最小值图片中心为原点，宽度一半为半径的圆内。
        float thumbHalfWidth = thumbWidth / 2f;
        return Math.abs(touchX - normalizedToScreen(normalizedMaxValue) - thumbWidth - thumbWidth / 2f) <= thumbHalfWidth * 2;
    }

    /**
     * 试图告诉父view不要拦截子控件的drag
     */
    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    void onStartTrackingTouch() {
        mIsDragging = true;
    }


    void onStopTrackingTouch() {
        mIsDragging = false;
    }

    public void setMinValue(long minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(long maxValue) {
        this.maxValue = maxValue;
    }

    public void setMinLimit(long minLimit) {
        this.minLimit = minLimit;
    }


    private float normalizedToScreen(double normalizedCoord) {
        return (float) (getPaddingLeft() + normalizedCoord * (getWidth() - thumbWidth * 2 - getPaddingLeft() - getPaddingRight()));
    }

    private double valueToNormalized(long value) {
        if (0 == maxValue - minValue) {
            return 0d;
        }
        return (value - minValue) * 1.0d / (maxValue - minValue);
    }

    public void setSelectedMinValue(long value) {
        if (0 == (maxValue - minValue)) {
            setNormalizedMinValue(0d);
        } else {
            setNormalizedMinValue(valueToNormalized(value));
        }
    }

    public void setSelectedMaxValue(long value) {
        if (0 == (maxValue - minValue)) {
            setNormalizedMaxValue(1d);
        } else {
            setNormalizedMaxValue(valueToNormalized(value));
        }
    }

    public void setNormalizedMinValue(double value) {
        normalizedMinValue = Math.max(0d, Math.min(1d, Math.min(value, normalizedMaxValue)));
        invalidate();
    }

    public void setNormalizedMaxValue(double value) {
        normalizedMaxValue = Math.max(0d, Math.min(1d, Math.max(value, normalizedMinValue)));
        invalidate();
    }

    public long getSelectedMinValue() {
        return normalizedToValue(normalizedMinValue);
    }

    public long getSelectedMaxValue() {
        return normalizedToValue(normalizedMaxValue);
    }

    private long normalizedToValue(double normalized) {
        return (long) (minValue + normalized * (maxValue - minValue));
    }

    public long setScrollOffset(float offset) {
        double normalized = screenToNormalized(offset, 2);
        if (normalized != normalizedOffsetValue) {
            normalizedOffsetValue = normalized;
        }
        return normalizedToValue(normalizedOffsetValue);
    }

    /**
     * 供外部activity调用，控制是都在拖动的时候打印log信息，默认是false不打印
     */
    public boolean isNotifyWhileDragging() {
        return notifyWhileDragging;
    }

    public void setNotifyWhileDragging(boolean flag) {
        this.notifyWhileDragging = flag;
    }

    public int dip2px(int dip) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) ((float) dip * scale + 0.5F);
    }

    public void setTouchDown(boolean touchDown) {
        isTouchDown = touchDown;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable("SUPER", super.onSaveInstanceState());
        bundle.putDouble("MIN", normalizedMinValue);
        bundle.putDouble("MAX", normalizedMaxValue);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable parcel) {
        final Bundle bundle = (Bundle) parcel;
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"));
        normalizedMinValue = bundle.getDouble("MIN");
        normalizedMaxValue = bundle.getDouble("MAX");
    }

    private OnRangeChangedListener listener;

    public interface OnRangeChangedListener {
        void onRangeChanged(RangeSeekBar bar, long minValue, long maxValue, long offset, int action, @Nullable Thumb pressedThumb);
    }

    public void setOnRangeChangedListener(OnRangeChangedListener listener) {
        this.listener = listener;
    }
}
