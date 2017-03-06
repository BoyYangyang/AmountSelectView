package houyi.mime.com.amountselected;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>write the description
 *
 * @author houyi
 * @version [版本号]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */


public class ScaleView extends View {
    private static final int DEFAULT_MIN_AMOUNT = 1000;
    private static final int DEFAULT_MAX_AMOUNT = 5000;
    public static final String TAG = "ScaleView";

    private int mMinAmount;
    private int mMaxAmount;
    private int mSelectionAmount;
    private Paint mScaleMarkPaint;
    private Paint mSelectionMarkPaint;
    private int mScaleMarkColor;
    private int mSelectionMarkColor;
    private Paint mScaleNumberPaint;
    private int mScaleNumberColor;
    private List<String> mShowAmountList;
    private List<Rect> mShowAmountRectList;
    private float mScaleNumberTextSize;
    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private float mSelectionOffset;
    private float mScaleCellSpace;
    private float mMiddleMarkSize;
    private float mSmallMarkSize;
    private float startX;
    private ValueAnimator mValueAnimator;
    private boolean isCanDrag;
    private ValueAnimator.AnimatorUpdateListener animatorUpdateListener =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mSelectionOffset = (float) animation.getAnimatedValue();
                    postOffset(mSelectionOffset);
                }
            };
    ;
    private ValueAnimator mFlingAnimator;
    private GestureDetector mGestureDetector;
    private float mBeforeValue;

    public ScaleView(Context context) {
        this(context, null);
    }

    public ScaleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = getContext().obtainStyledAttributes(attrs,
                R.styleable.ScaleView, defStyleAttr, 0);
        mScaleMarkColor = ta.getColor(R.styleable.ScaleView_scaleMarkColor, 0);
        mScaleNumberColor = ta.getColor(R.styleable.ScaleView_scaleNumberColor, 0);
        mSelectionMarkColor = ta.getColor(R.styleable.ScaleView_selectionMarkColor, 0);
        mScaleNumberTextSize = ta.getDimension(R.styleable.ScaleView_scaleNumberSize, 0);
        mScaleCellSpace = ta.getDimension(R.styleable.ScaleView_scaleCellSpace, 0);
        mSmallMarkSize = ta.getDimension(R.styleable.ScaleView_smallMarkSize, 0);
        mMiddleMarkSize = ta.getDimension(R.styleable.ScaleView_middleMarkSize, 0);
        ta.recycle();
        init();
    }

    private void init() {
        isCanDrag = true;
        mMinAmount = DEFAULT_MIN_AMOUNT;
        mMaxAmount = mSelectionAmount = DEFAULT_MAX_AMOUNT;

        mGestureDetector = new GestureDetector(getContext(), onGestureListener);

        mValueAnimator = new ValueAnimator();
        mValueAnimator.setDuration(100);
        mValueAnimator.addUpdateListener(animatorUpdateListener);
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isCanDrag = true;
            }
        });

        mFlingAnimator = new ValueAnimator();
        mFlingAnimator.setDuration(500);
        mFlingAnimator.setInterpolator(new DecelerateInterpolator());
        mFlingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                float changeValue = value - mBeforeValue;
                postOffset(mSelectionOffset -= changeValue);
                mBeforeValue = value;
            }
        });
        mFlingAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                moveToRightPosition();
                mBeforeValue = 0;
            }
        });

        mScaleMarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleMarkPaint.setColor(mScaleMarkColor);
        mScaleMarkPaint.setStrokeWidth(getResources().getDimension(R.dimen.mar_pad_len_2px));

        mSelectionMarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSelectionMarkPaint.setColor(mSelectionMarkColor);
        mSelectionMarkPaint.setStrokeWidth(getResources().getDimension(R.dimen.mar_pad_len_2px));

        mScaleNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleNumberPaint.setColor(mScaleNumberColor);
        mScaleNumberPaint.setTextSize(mScaleNumberTextSize);
        //TODO 设置字体
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureAllScaleNumber();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 计算所有的刻度金额及其宽度.
     */
    private void measureAllScaleNumber() {
        mShowAmountList = getAllShowAmount();
        mShowAmountRectList = new ArrayList<>();
        for (String showAmount : mShowAmountList) {
            Rect bounds = new Rect();
            mScaleNumberPaint.getTextBounds(showAmount, 0, showAmount.length(), bounds);
            mShowAmountRectList.add(new Rect(0, 0, bounds.width(), bounds.height()));
        }
    }

    private List<String> getAllShowAmount() {
        ArrayList<String> amountList = new ArrayList<>();
        for (int amount = mMinAmount; amount <= mMaxAmount; amount += 1000) {
            if (amount % 1000 == 0) {
                amountList.add(StringUtils.formatDouble(amount));
            } else {
                amountList.add(StringUtils.formatDouble((amount / 1000 + 1) * 1000));
            }
        }
        return amountList;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBaseLine(canvas);
        drawScaleMark(canvas);
        drawScaleNumber(canvas);
        drawSelectionMark(canvas);
    }

    private void drawBaseLine(Canvas canvas) {
        canvas.drawLine(0, mMeasuredHeight, mMeasuredWidth, mMeasuredHeight, mScaleMarkPaint);
    }

    private void drawSelectionMark(Canvas canvas) {
        canvas.drawLine((float) mMeasuredWidth / 2, 0, (float) mMeasuredWidth / 2, mMeasuredHeight,
                mSelectionMarkPaint);
    }

    private void drawScaleNumber(Canvas canvas) {
        float amountEndY = mMeasuredHeight - mMiddleMarkSize - getResources().getDimension(
                R.dimen.mar_pad_len_12px);
        //计算理论屏幕上可以显示的最小和最大金额
        int theoryMinShowAmount = mSelectionAmount
                - (int) ((mMeasuredWidth / 2 - mSelectionOffset) / mScaleCellSpace) * 100;
        int theoryMaxShowAmount = mSelectionAmount
                + (int) ((mMeasuredWidth / 2 + mSelectionOffset) / mScaleCellSpace) * 100;

        for (int amount = mMinAmount, amountCount = 0; amount <= mMaxAmount;
                amount += 1000, amountCount++) {
            //判断金额所在的刻度是否在显示范围内,或者是选中的金额，不在则不显示
            if (amount < theoryMinShowAmount || amount > theoryMaxShowAmount
                    || amount == mSelectionAmount) {
                continue;
            }
            //计算金额中心位置
            float amountMiddleX = mMeasuredWidth / 2 - mSelectionOffset -
                    (mSelectionAmount - ((amount % 1000 == 0) ? amount
                            : ((amount / 1000 + 1) * 1000)))
                            / 100 * mScaleCellSpace;
            String showAmount = mShowAmountList.get(amountCount);
            Rect showRect = mShowAmountRectList.get(amountCount);
            canvas.drawText(showAmount, 0, showAmount.length(),
                    amountMiddleX - showRect.width() / 2, amountEndY, mScaleNumberPaint);
        }
    }

    private void drawScaleMark(Canvas canvas) {
        //绘制selectionMark右侧刻度
        float middleX = mMeasuredWidth / 2;
        for (int amount = mSelectionAmount + 100, amountCount = 1; amount <= mMaxAmount;
                amount += 100, amountCount++) {
            float amountStartX = middleX - mSelectionOffset + mScaleCellSpace * amountCount;
            if (amountStartX > mMeasuredWidth) {
                break;
            }
            drawScaleLine(canvas, amount, amountStartX);
        }
        //绘制selectionMark左侧刻度
        for (int amount = mSelectionAmount, amountCount = 0; amount >= mMinAmount;
                amount -= 100, amountCount++) {
            float amountStartX = middleX - mSelectionOffset - mScaleCellSpace * amountCount;
            if (amountStartX < 0) {
                break;
            }
            drawScaleLine(canvas, amount, amountStartX);
        }
    }

    private void drawScaleLine(Canvas canvas, int amount, float amountStartX) {
        float smallMarkStartY = mMeasuredHeight - mSmallMarkSize;
        float middleMarkStartY = mMeasuredHeight - mMiddleMarkSize;
        if (amount % 1000 == 0) {
            canvas.drawLine(amountStartX, middleMarkStartY, amountStartX, mMeasuredHeight,
                    mScaleMarkPaint);
        } else {
            canvas.drawLine(amountStartX, smallMarkStartY, amountStartX, mMeasuredHeight,
                    mScaleMarkPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mMeasuredWidth = getMeasuredWidth();
        mMeasuredHeight = getMeasuredHeight();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (isCanDrag) {
            mGestureDetector.onTouchEvent(event);
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (startX == -1) {
                        startX = event.getX();
                    }
                    float endX = event.getX();
                    float moveX = endX - startX;
                    postOffset(mSelectionOffset -= moveX);
                    startX = endX;
                    break;
                case MotionEvent.ACTION_OUTSIDE:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    moveToRightPosition();
                    startX = -1;
                    break;
            }
        }
        return true;
    }

    private void moveToRightPosition() {
        isCanDrag = false;
        if (Math.abs(mSelectionOffset) < mScaleCellSpace / 2) {
            mValueAnimator.setFloatValues(mSelectionOffset, 0);
        } else if (mSelectionOffset > 0) {
            mValueAnimator.setFloatValues(mSelectionOffset, mScaleCellSpace);
        } else {
            mValueAnimator.setFloatValues(mSelectionOffset, -mScaleCellSpace);
        }
        mValueAnimator.start();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    private void postOffset(float offset) {
        while (mSelectionOffset >= mScaleCellSpace) {
            mSelectionOffset = mSelectionOffset - mScaleCellSpace;
            mSelectionAmount += 100;
        }
        while (mSelectionOffset <= -mScaleCellSpace) {
            mSelectionOffset = mSelectionOffset + mScaleCellSpace;
            mSelectionAmount -= 100;
        }
        if (mSelectionAmount > mMaxAmount || (mSelectionAmount == mMaxAmount
                && mSelectionOffset > 0)) {
            mSelectionOffset = 0;
            mSelectionAmount = mMaxAmount;
            return;
        }
        if (mSelectionAmount < mMinAmount || (mSelectionAmount == mMinAmount
                && mSelectionOffset < 0)) {
            mSelectionOffset = 0;
            mSelectionAmount = mMinAmount;
            return;
        }
        invalidate();
    }

    public void setAmount(int minAmount, int maxAmount) {
        setAmount(minAmount, maxAmount, maxAmount);
    }

    public void setAmount(int minAmount, int maxAmount, int selectionAmount) {
        if (mMaxAmount < mMinAmount || mMinAmount < 0 || mSelectionAmount < mMinAmount
                || mSelectionAmount > mMaxAmount) {
            throw new IllegalArgumentException(
                    "the max amount and the mix amount is incorrect ,now the maxAmount = ["
                            + mMaxAmount + "],the minAmount = [" + mMinAmount
                            + "],the selectionAmount = [" + mSelectionAmount + "]");
        }
        mMaxAmount = maxAmount;
        mMinAmount = minAmount;
        mSelectionAmount = selectionAmount;
        invalidate();
    }

    private GestureDetector.OnGestureListener onGestureListener =
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                        float velocityY) {
                    isCanDrag = false;
                    float x = e2.getX() - e1.getX();
                    if (x > 0) {
                        float distance = velocityX / 20;
                        Log.d(TAG, "onFling: distance" + distance);
                        mFlingAnimator.setFloatValues(0, distance);
                        mFlingAnimator.start();
                    } else if (x < 0) {
                        float distance = velocityX / 20;
                        mFlingAnimator.setFloatValues(0, distance);
                        Log.d(TAG, "onFling: distance" + distance);
                        mFlingAnimator.start();
                    } else {
                        isCanDrag = true;
                    }
                    return true;
                }
            };
}
