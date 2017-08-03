package com.iccrookie.circlenumberprogressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import bingo.library.R;


/**
 * desc:
 * author：rookie on 2017/8/3 0003 19:08
 */
public class CircleNumberProgressBar extends View {

    private OnProgressBarListener mListener;
    private int mViewWidth, mViewHeight;

    //color
    private int mReachedBarColor;
    private int mUnreachedBarColor;
    private int progressTextColor;
    private int centerTextColor;

    //size
    private float reachedbarStrokeWidth;
    private float unreachedbarStrokeWidth;
    private float progressTextSize;
    private float centerTextSize;

    //画笔
    private Paint mReachedBarPaint;
    private Paint mUnreachedBarPaint;
    private Paint mCenterTextPaint, mBarTextPaint;

    private RectF rectF;
    private float bStartAngle = 200; //背景圆圈
    private float bSweepAngle = 0;
    private float fStartAngle = 180; //前景起始角度
    private float fSweepAngle = 1;  //扫过角度
    private float tStartAngle = 190; //文字起始角度
    private float tSweepAngle = 1; //文字起始角度
    private float yOffsets;        //中间文字的y轴偏移值
    private float middle;
    private int radius; //圆形的半径
    private int padding;
    float offsetAngle; //每次设置的偏移角度

    private String text = "";  //中间文字
    private String mSuffix = "%";
    private boolean isDrawCenterText = false;
    private boolean isDrawBarText = true;
    private float maxProgress; //最大值
    private int currentProgress;
    private float proportion; //比例


    private final int default_progress_text_color = Color.rgb(66, 145, 241);
    private final int default_center_text_color = Color.rgb(66, 145, 241);
    private final int default_reached_color = Color.parseColor("#33CCCC");
    private final int default_unreached_color = Color.parseColor("#CCCCCC");
    private float default_text_size;
    private float default_reached_bar_height;
    private float default_unreached_bar_height;

    public CircleNumberProgressBar(Context context) {
        super(context, null);
    }

    public CircleNumberProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        default_reached_bar_height = dp2px(10);
        default_unreached_bar_height = dp2px(10);
        default_text_size = sp2px(30);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleNumberProgressBar);
        mReachedBarColor = a.getColor(R.styleable.CircleNumberProgressBar_progress_reached_color, default_reached_color);
        mUnreachedBarColor = a.getColor(R.styleable.CircleNumberProgressBar_progress_unreached_color, default_unreached_color);

        reachedbarStrokeWidth = a.getDimension(R.styleable.CircleNumberProgressBar_progress_reached_width, default_reached_bar_height);
        unreachedbarStrokeWidth = a.getDimension(R.styleable.CircleNumberProgressBar_progress_unreached_width, default_unreached_bar_height);

        progressTextColor = a.getColor(R.styleable.CircleNumberProgressBar_progress_text_color, default_progress_text_color);
        centerTextColor = a.getColor(R.styleable.CircleNumberProgressBar_progress_center_color, default_center_text_color);

        progressTextSize = a.getDimension(R.styleable.CircleNumberProgressBar_progress_text_size, sp2px(15));
        centerTextSize = a.getDimension(R.styleable.CircleNumberProgressBar_progress_center_size, default_text_size);

        padding = (int) a.getDimension(R.styleable.CircleNumberProgressBar_progress_padding, dp2px(20));

        isDrawBarText = a.getBoolean(R.styleable.CircleNumberProgressBar_progress_text_visibility, true);
        isDrawCenterText = a.getBoolean(R.styleable.CircleNumberProgressBar_progress_text_center_visibility, true);

        setMax(a.getInt(R.styleable.CircleNumberProgressBar_progress_max, 100));
        setProgress(a.getInt(R.styleable.CircleNumberProgressBar_progress_current, 0));


        a.recycle();
        initRes();
    }

    public CircleNumberProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //测量大小
        setMeasuredDimension(measure(widthMeasureSpec, true), measure(heightMeasureSpec, false));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = getWidth() / 2;
        mViewHeight = getHeight() / 2;
        radius = Math.min(mViewWidth, mViewHeight) - padding;
        rectF = new RectF(-radius, -radius, radius, radius);
        middle = (rectF.bottom + rectF.top) / 2;
        yOffsets = middle + yOffsets;  //中间文字偏移值
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(mViewWidth, mViewHeight);

        text = String.format("%d", (int) (getProgress() * 100 / getMax()));

        //获取圆上坐标
        float textAngle = tStartAngle + tSweepAngle; //获取弧长中点
        float pointY = (float) (Math.sin(textAngle * Math.PI / 180) * radius);
        float pointX = (float) (Math.cos(textAngle * Math.PI / 180) * radius);

        //画弧
        canvas.drawArc(rectF, fStartAngle, fSweepAngle, false, mReachedBarPaint);

        if (isDrawCenterText) {
            //画中间文字
            canvas.drawText(text+mSuffix, rectF.centerX(), yOffsets, mCenterTextPaint);
        }
        if (isDrawBarText && tSweepAngle < 355) { //画进度条上文字
            canvas.save();
            canvas.translate(pointX, pointY);
            canvas.drawText(text, 0, 25, mBarTextPaint);
            canvas.restore();
        }
        //画背景
        if (bSweepAngle > proportion) { //重合
            canvas.drawArc(rectF, bStartAngle, bSweepAngle, false, mUnreachedBarPaint);
        }
    }

    private int measure(int measureSpec, boolean isWidth) {
        int result;
        int model = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        int padding = isWidth ? getPaddingLeft() + getPaddingRight() : getTop() + getBottom();
        if (model == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = isWidth ? getSuggestedMinimumWidth() : getSuggestedMinimumHeight();
            result += padding;
            if (model == MeasureSpec.AT_MOST) {
                result = isWidth ? Math.max(result, size) : Math.min(result, size);
            }
        }
        return result;
    }


    private void initRes() {
        mReachedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mReachedBarPaint.setStyle(Paint.Style.STROKE);
        mReachedBarPaint.setStrokeWidth(reachedbarStrokeWidth);
        mReachedBarPaint.setColor(mReachedBarColor);
        mReachedBarPaint.setStrokeCap(Paint.Cap.ROUND);

        mUnreachedBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnreachedBarPaint.setColor(mUnreachedBarColor);
        mUnreachedBarPaint.setStyle(Paint.Style.STROKE);
        mUnreachedBarPaint.setStrokeWidth(unreachedbarStrokeWidth);
        mUnreachedBarPaint.setStrokeCap(Paint.Cap.ROUND);

        mCenterTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterTextPaint.setTextSize(centerTextSize);
        mCenterTextPaint.setFakeBoldText(true);
        mCenterTextPaint.setColor(centerTextColor);
        mCenterTextPaint.setTextAlign(Paint.Align.CENTER);

        mBarTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBarTextPaint.setTextSize(progressTextSize);
        mBarTextPaint.setColor(progressTextColor);
        mBarTextPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics metrics = mCenterTextPaint.getFontMetrics();
        yOffsets = -(metrics.ascent + metrics.descent) / 2;



    }

    public float dp2px(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    public float sp2px(float sp) {
        final float scale = getResources().getDisplayMetrics().scaledDensity;
        return sp * scale;
    }


    public void incrementProgressBy(int by) {
        if (by > 0 && getProgress()<getMax() ) {
            setProgress(getProgress() + by);
            if(mListener != null ){
                mListener.onProgressChange(getProgress(),getMax());
            }
        }
    }

    public void setProgress(int progress) {
        if (progress <= getMax() && progress >= 0) {
            bStartAngle = 200; //初始化每次启动角度

            this.currentProgress = progress;
            offsetAngle = getProgress() * proportion;
            float tempAngle =  bStartAngle+offsetAngle;
            if(tempAngle>=360){
                bStartAngle = (tempAngle-360+proportion);
                bSweepAngle = 170-bStartAngle;
            }else{
                bStartAngle =  tempAngle;
                if(bStartAngle>230){
                    bStartAngle +=proportion;
                }
                bSweepAngle = 330-(bStartAngle-200);
            }
            fSweepAngle = offsetAngle-proportion;
            tSweepAngle = offsetAngle;
            invalidate();
        }
    }

    public void setMax(float maxProgress) {
        if (maxProgress > 0) {
            this.maxProgress = maxProgress;
            this.proportion = 360 / getMax();
            invalidate();
        }

    }

    public float getMax() {
        return maxProgress;
    }

    public int getProgress() {
        return currentProgress;
    }

    public void setSuffix(String suffix) {
        if (suffix == null) {
            this.mSuffix = "";
        } else {
            this.mSuffix = suffix;
        }
    }

    public void setReachedBarColor(int color) {
        this.mReachedBarColor = color;
        mReachedBarPaint.setColor(color);
        invalidate();
    }

    public void setUnreachedBarColor(int color) {
        this.mUnreachedBarColor = color;
        mUnreachedBarPaint.setColor(color);
        invalidate();
    }

    public void setUnreachedbarStrokeWidth(float width) {
        this.unreachedbarStrokeWidth = width;
        mUnreachedBarPaint.setStrokeWidth(width);
        invalidate();
    }

    public void setReachedbarStrokeWidth(float width) {
        this.reachedbarStrokeWidth = width;
        mReachedBarPaint.setStrokeWidth(width);
        invalidate();
    }

    public void setProgressTextColor(int progressTextColor) {
        this.progressTextColor = progressTextColor;
        mBarTextPaint.setColor(progressTextColor);
        invalidate();
    }

    public void setCenterTextColor(int centerTextColor) {
        this.centerTextColor = centerTextColor;
        mCenterTextPaint.setColor(centerTextColor);
        invalidate();
    }

    public void setProgressTextSize(float progressTextSize) {
        this.progressTextSize = progressTextSize;
        mBarTextPaint.setTextSize(progressTextSize);
        invalidate();
    }

    public void setCenterTextSize(float centerTextSize) {
        this.centerTextSize = centerTextSize;
        mCenterTextPaint.setTextSize(centerTextSize);
        invalidate();
    }

    public void setOnProgressBarListener(OnProgressBarListener listener){
        mListener = listener;
    }
}

