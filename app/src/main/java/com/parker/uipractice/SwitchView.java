package com.parker.uipractice;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by admin on 2017/2/9.
 */

public class SwitchView extends View {
    private boolean switchStatus = false;//开关状态
    private int radius;//内部按钮半径
    private int onColor = 0xFF32C781;//打开时的背景色
    private int offColor = 0xFFC9C9C9;//关闭时的背景色
    private int buttonBgColor = 0xFFFFFFFF;
    private int innerThick = 4;//内部圆形跟边缘的距离
    private RectF rectF;
    private Paint paintRoundRect;
    private Paint paintRoundButton;
    private Paint paintRoundUnSwitchRect;
    private float rate = 0.0f;
    private float startX;
    private float startY;
    private float lastRate;
    private long downTime;
    private boolean hasMoved;
    private boolean lastStatus;
    private OnSwitchChangeListener listener;
    private boolean isCanSwitch = true;
    private float lastX;

    public SwitchView(Context context) {
        this(context, null);
    }

    public SwitchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        rectF = new RectF();
        paintRoundRect = new Paint();
        paintRoundButton = new Paint();
        paintRoundUnSwitchRect = new Paint();
        paintRoundRect.setAntiAlias(true);
        paintRoundButton.setAntiAlias(true);
        paintRoundUnSwitchRect.setAntiAlias(true);
        paintRoundUnSwitchRect.setColor(Color.parseColor("#7fffffff"));
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchView);
        onColor = mTypedArray.getColor(R.styleable.SwitchView_onColor, 0xFF32C781);
        offColor = mTypedArray.getColor(R.styleable.SwitchView_offColor, 0xFFC9C9C9);
        buttonBgColor = mTypedArray.getColor(R.styleable.SwitchView_buttonBgColor, 0xFFFFFFFF);
        switchStatus = mTypedArray.getBoolean(R.styleable.SwitchView_switchStatus, true);
        mTypedArray.recycle();
        rate = switchStatus ? 1.0f : 0.0f;
        paintRoundButton.setColor(buttonBgColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        int realWidth = width - getPaddingLeft() - getPaddingRight();
        int realHeight = height - getPaddingTop() - getPaddingBottom();
        if (realHeight <= realWidth / 2) {
            radius = realHeight / 2;
            width = realHeight * 2 + getPaddingLeft() + getPaddingRight();
        } else {
            radius = realWidth / 4;
            height = realWidth / 2 + getPaddingTop() + getPaddingBottom();
        }
        rectF.left = getPaddingLeft();
        rectF.right = width - getPaddingRight();
        rectF.top = getPaddingTop();
        rectF.bottom = height - getPaddingBottom();
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paintRoundRect.setColor(getColor(rate));
        canvas.drawRoundRect(rectF, radius, radius, paintRoundRect);
        canvas.drawCircle(getPaddingLeft() + radius + 2 * radius * rate, getPaddingTop() + radius, radius - innerThick, paintRoundButton);
        if (!isCanSwitch) {
            canvas.drawRoundRect(rectF, radius, radius, paintRoundUnSwitchRect);
        }
    }

    private int getColor(float radio) {
        int redStart = Color.red(offColor);
        int blueStart = Color.blue(offColor);
        int greenStart = Color.green(offColor);
        int redEnd = Color.red(onColor);
        int blueEnd = Color.blue(onColor);
        int greenEnd = Color.green(onColor);
        int red = (int) (redStart + ((redEnd - redStart) * radio + 0.5));
        int greed = (int) (greenStart + ((greenEnd - greenStart) * radio + 0.5));
        int blue = (int) (blueStart + ((blueEnd - blueStart) * radio + 0.5));
        return Color.argb(255, red, greed, blue);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isCanSwitch) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                lastX = startX;
                downTime = System.currentTimeMillis();
                lastRate = rate;
                lastStatus = switchStatus;
                break;
            case MotionEvent.ACTION_MOVE:
                float xMove = event.getX();
                float yMove = event.getY();
                if (Math.abs(xMove - startX) < 10 &&
                        Math.abs(yMove - startY) < 10) {
                    hasMoved = false;
                    break;
                }
                if (xMove > lastX) {

                } else {

                }
                lastX = xMove;
                hasMoved = true;
                rate = lastRate + (xMove - startX) / (radius * 2);
                if (rate > 1) {
                    rate = 1;
                    switchStatus = true;
                }
                if (rate < 0) {
                    rate = 0;
                    switchStatus = false;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (hasMoved) {
                    hasMoved = false;
                    if (rate >= 0.5 && rate != 1) {
                        rate = 1;
                        invalidate();
                        switchStatus = true;
                    } else if (rate < 0.5 && rate != 0) {
                        rate = 0;
                        invalidate();
                        switchStatus = false;
                    }
                    if (lastStatus != switchStatus) {
                        if (listener != null) {
                            listener.onSwitchChange(switchStatus);
                        }
                    }
                } else {
                    long pressTime = System.currentTimeMillis();
                    if (pressTime - downTime < 200) {
                        setSwitchStatus(!switchStatus, true);
                        if (listener != null) {
                            listener.onSwitchChange(switchStatus);
                        }
                    }
                }
                break;
        }
        return true;
    }

    public void setCanSwitch(boolean canSwitch) {
        isCanSwitch = canSwitch;
    }

    public void setSwitchStatus(boolean switchStatus) {
        if (this.switchStatus != switchStatus) {
            this.switchStatus = switchStatus;
            rate = switchStatus ? 1.0f : 0.0f;
            invalidate();
        }
    }

    public void setSwitchStatus(final boolean switchStatus, boolean isSingleClick) {
        if (this.switchStatus != switchStatus) {
            this.switchStatus = switchStatus;
            if (isSingleClick) {
                valueAnimator(!switchStatus ? 1.0f : 0.0f, switchStatus ? 1.0f : 0.0f, 300, new OnAnimUpdateListener() {
                    @Override
                    public void onAnimationUpdate(float curValue) {
                        rate = curValue;
                        invalidate();
                    }

                    @Override
                    public void onAnimationFinish() {
                        rate = switchStatus ? 1.0f : 0.0f;
                        invalidate();
                    }
                }).start();
            } else {
                rate = switchStatus ? 1.0f : 0.0f;
                invalidate();
            }
        }
    }

    /**
     * 获取当前更新的值
     *
     * @param startValue
     * @param endValue
     * @param duration
     * @param onAnimUpdateListener
     */
    public ValueAnimator valueAnimator(float startValue, float endValue, int duration, final OnAnimUpdateListener onAnimUpdateListener) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(startValue, endValue);
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                float currentValue = (Float) animator.getAnimatedValue();
                onAnimUpdateListener.onAnimationUpdate(currentValue);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                onAnimUpdateListener.onAnimationFinish();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        return valueAnimator;
    }


    public void setOnSwitchChangeListener(OnSwitchChangeListener listener) {
        this.listener = listener;
    }

    public boolean getSwitchStatus() {
        return switchStatus;
    }

    public interface OnSwitchChangeListener {
        void onSwitchChange(boolean switchStatus);
    }


    public interface OnAnimUpdateListener {

        public void onAnimationUpdate(float curValue);

        public void onAnimationFinish();

    }
}
