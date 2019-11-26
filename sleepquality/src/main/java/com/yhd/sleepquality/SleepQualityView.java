package com.yhd.sleepquality;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 渐变心电图
 * Created by haide.yin(haide.yin@tcl.com) on 2019/7/25 13:34.
 */
public class SleepQualityView extends View {

    /* ********************* 外部使用的属性 *********************** */
    public static final float TYPE_WAKE = 1f;//坐标为wake类型
    public static final float TYPE_SHALLOW = 2f;//坐标为shallowe类型
    public static final float TYPE_DEEP = 3f;//坐标为deep类型
    private static final String TAG = SleepQualityView.class.getSimpleName();
    /* ********************* 外部设置的属性 *********************** */
    //颜色属性
    private int deepColor = Color.parseColor("#10e191");//深度睡眠的颜色
    private int shallowColor = Color.parseColor("#398eff");//浅度睡眠的颜色
    private int wakeColor = Color.parseColor("#ffa239");//清醒的颜色
    private int textGrayColor = Color.parseColor("#61000000");//文字灰色
    //线条属性
    private float widthRatio = 0.05f;//线条宽度百分比
    private float vWidthRatio = 0.005f;//竖线宽度百分比
    private float marginYRatio = 0.05f;//线条距离y轴的距离
    //画板四周边距
    private float marginLeftRatio = 0.05f;//画板左边距百分比
    private float marginRightRatio = 0.05f;//画板右边距百分比
    private float marginTopRatio = 0.05f;//画板上边距百分比
    private float marginBottomRatio = 0.05f;//画板下边距百分比
    //文字属性
    private float xTextRatio = 0.05f;//x轴文字的大小百分比
    private float yTextRatio = 0.05f;//y轴文字的大小百分比
    //y坐标的描述语
    private String wakeString = "Wake";
    private String shallowString = "Shallow";
    private String deepString = "Deep";
    //动画
    private int animationTime = 1000;//动画持续时间

    /**
     * 睡眠数据列表,规则如下
     * float[0]:类型,1f：醒来 2f：浅睡 3f：深睡
     * float[1]:距离开始时间的百分比(开始时间/横坐标总时长)
     * float[2]:持续时间长的百分比(持续时间/横坐标总时长)
     */
    private List<float[]> timeArray = new ArrayList<>();
    //x坐标轴的文字描述列表
    private List<String> xAxisArray = new ArrayList<>();

    /* ********************* 内部使用的属性 *********************** */
    private Paint linePaint;//画线的画笔
    private Paint textPaint;//画文字画笔
    private float wakeYAxis;//睡醒坐标点的y坐标值
    private float shallowYAxis;//浅睡坐标点的y坐标值
    private float deepYAxis;//深睡坐标点的y坐标值
    //动画监听
    private LineAnimator lineAnimator = new LineAnimator(animation -> postInvalidate());

    public SleepQualityView(Context context) {
        this(context, null);
    }

    public SleepQualityView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SleepQualityView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //View被窗体移除的时候释放动画资源
        lineAnimator.release();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFoucus) {
        super.onWindowFocusChanged(hasFoucus);
        //View焦点变化
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        lineAnimator.start(animationTime);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        if (getWidth() > 0 && getHeight() > 0) {
            //线条区域的高度
            float canvasHeight = (1f - marginTopRatio - marginBottomRatio - xTextRatio) * getHeight();
            wakeYAxis = (yTextRatio + marginTopRatio) * getHeight();
            shallowYAxis = (yTextRatio + marginTopRatio) * getHeight() + canvasHeight / 3;
            deepYAxis = (yTextRatio + marginTopRatio) * getHeight() + 2 * canvasHeight / 3;
            //画y坐标
            textPaint.setTextSize(yTextRatio * getHeight());
            textPaint.setColor(Color.BLACK);
            textPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(wakeString, marginLeftRatio * getWidth(), wakeYAxis, textPaint);
            canvas.drawText(shallowString, marginLeftRatio * getWidth(), shallowYAxis, textPaint);
            canvas.drawText(deepString, marginLeftRatio * getWidth(), deepYAxis, textPaint);
            float yWidth = getMaxYWidth(textPaint);
            //伸展动画
            float mPhaseX = lineAnimator.getPhaseX();
            //线条区域的宽度
            float canvasWidth = ((1f - marginLeftRatio - marginRightRatio - marginYRatio) * getWidth() - yWidth) * mPhaseX;
            float xDistcance = yWidth + (marginLeftRatio + marginYRatio) * getWidth();
            //画x坐标
            if (this.xAxisArray != null && this.xAxisArray.size() > 0) {
                textPaint.setTextSize(xTextRatio * getHeight());
                textPaint.setColor(textGrayColor);
                textPaint.setTextAlign(Paint.Align.CENTER);
                for (int i = 0; i < xAxisArray.size(); i++) {
                    canvas.drawText(xAxisArray.get(i), xDistcance + i * canvasWidth / xAxisArray.size(), (1 - marginBottomRatio) * getHeight(), textPaint);
                }
            }
            //画线
            if (this.timeArray != null && this.timeArray.size() > 0) {
                linePaint.setStrokeWidth(widthRatio * getHeight());
                for (int i = 0; i < timeArray.size(); i++) {
                    float[] selectValue = timeArray.get(i);
                    if (selectValue.length >= 3) {
                        float sleepType = selectValue[0];//类型
                        float startXAxis = xDistcance + selectValue[1] * canvasWidth;//开始时间
                        float endXAxis = startXAxis + selectValue[2] * canvasWidth;//持续时间
                        float yAxis = wakeYAxis;
                        //求坐标点Y轴坐标
                        if (sleepType == TYPE_WAKE) {
                            yAxis = wakeYAxis;
                            linePaint.setColor(wakeColor);
                        } else if (sleepType == TYPE_SHALLOW) {
                            yAxis = shallowYAxis;
                            linePaint.setColor(shallowColor);
                        } else if (sleepType == TYPE_DEEP) {
                            yAxis = deepYAxis;
                            linePaint.setColor(deepColor);
                        }
                        //设置横线宽度
                        linePaint.setStrokeWidth(widthRatio * canvasWidth);
                        float vWidth = vWidthRatio * canvasWidth;
                        linePaint.setShader(null);
                        canvas.drawLine(startXAxis - vWidth / 2, yAxis, endXAxis + vWidth / 2, yAxis, linePaint);
                        if (i < timeArray.size() - 1) {
                            drawNext(canvas, linePaint, sleepType, endXAxis, yAxis, xDistcance, canvasWidth, timeArray.get(i + 1));
                        }
                    }
                }
            }
        }
    }

    /**
     * 初始化
     */
    private void init(AttributeSet attrs) {
        //初始化属性
        if (attrs != null) {
            //初始化布局属性
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.SleepQualityView, 0, 0);
            marginLeftRatio = typedArray.getFloat(R.styleable.SleepQualityView_sq_marginLeftRatio, marginLeftRatio);
            marginRightRatio = typedArray.getFloat(R.styleable.SleepQualityView_sq_marginRightRatio, marginRightRatio);
            marginTopRatio = typedArray.getFloat(R.styleable.SleepQualityView_sq_marginTopRatio, marginTopRatio);
            marginBottomRatio = typedArray.getFloat(R.styleable.SleepQualityView_sq_marginBottomRatio, marginBottomRatio);
            widthRatio = typedArray.getFloat(R.styleable.SleepQualityView_sq_widthRatio, widthRatio);
            vWidthRatio = typedArray.getFloat(R.styleable.SleepQualityView_sq_vwidthRatio, vWidthRatio);
            marginYRatio = typedArray.getFloat(R.styleable.SleepQualityView_sq_marginYRatio, marginYRatio);
            xTextRatio = typedArray.getFloat(R.styleable.SleepQualityView_sq_xTextRatio, xTextRatio);
            yTextRatio = typedArray.getFloat(R.styleable.SleepQualityView_sq_yTextRatio, yTextRatio);
            animationTime = typedArray.getInteger(R.styleable.SleepQualityView_sq_animationTime, animationTime);
        }
        //初始化画笔
        linePaint = new Paint();
        linePaint.setAntiAlias(true); // 抗锯齿
        linePaint.setDither(true); // 防抖动
        //linePaint.setStrokeCap(Paint.Cap.ROUND); // 把每段圆弧改成圆角的
        linePaint.setStyle(Paint.Style.STROKE);
        //文字画笔
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setDither(true); // 防抖动
        textPaint.setAntiAlias(true);// 抗锯齿
        textPaint.setStrokeWidth(1);//画笔宽度

        timeArray.add(new float[]{1f, 0.0f, 0.05f});
        timeArray.add(new float[]{2f, 0.05f, 0.1f});
        timeArray.add(new float[]{3f, 0.15f, 0.05f});
        timeArray.add(new float[]{2f, 0.2f, 0.2f});
        timeArray.add(new float[]{3f, 0.4f, 0.05f});
        timeArray.add(new float[]{2f, 0.45f, 0.1f});
        timeArray.add(new float[]{3f, 0.55f, 0.05f});
        timeArray.add(new float[]{2f, 0.6f, 0.15f});
        timeArray.add(new float[]{1f, 0.75f, 0.25f});

        xAxisArray.add("23");
        xAxisArray.add("24");
        xAxisArray.add("1");
        xAxisArray.add("2");
        xAxisArray.add("3");
        xAxisArray.add("4");
        xAxisArray.add("5");
        xAxisArray.add("6");
        xAxisArray.add("7");
    }

    /**
     * 读取x轴开始距离
     */
    private float getMaxYWidth(Paint paint) {
        float defaultValue;
        float wakeDistance = paint.measureText(wakeString);
        float shallowDistance = paint.measureText(shallowString);
        float deepDistance = paint.measureText(deepString);
        defaultValue = Math.max(wakeDistance, shallowDistance);
        defaultValue = Math.max(defaultValue, deepDistance);
        return defaultValue;
    }

    /**
     * 绘制两点间隔线
     */
    private void drawNext(Canvas canvas, Paint paint, float sleepType, float endXAxis, float yAxis, float xDistcance, float canvasWidth, float[] nextValue) {
        float sleepNextType = nextValue[0];//类型
        float startNextXAxis = xDistcance + nextValue[1] * canvasWidth;//开始时间
        float yNextAxis = wakeYAxis;
        int beginColor = wakeColor;
        int endColor = shallowColor;
        if (sleepType == TYPE_WAKE) {
            beginColor = wakeColor;
        } else if (sleepType == TYPE_SHALLOW) {
            beginColor = shallowColor;
        } else if (sleepType == TYPE_DEEP) {
            beginColor = deepColor;
        }
        if (sleepNextType == TYPE_WAKE) {
            endColor = wakeColor;
            yNextAxis = wakeYAxis;
        } else if (sleepNextType == TYPE_SHALLOW) {
            endColor = shallowColor;
            yNextAxis = shallowYAxis;
        } else if (sleepNextType == TYPE_DEEP) {
            endColor = deepColor;
            yNextAxis = deepYAxis;
        }
        Shader mShader = new LinearGradient(endXAxis, yAxis, startNextXAxis, yNextAxis, new int[]{beginColor, endColor}, null, Shader.TileMode.MIRROR);
        paint.setShader(mShader);
        //设置竖线宽度
        paint.setStrokeWidth(vWidthRatio * canvasWidth);
        canvas.drawLine(endXAxis, yAxis, startNextXAxis, yNextAxis, paint);
    }

    /**
     * 设置数据源
     *
     * @param timeArray  高度比
     * @param xAxisArray x坐标值
     */
    public void setDataSource(List<float[]> timeArray, List<String> xAxisArray) {
        if(timeArray != null && xAxisArray != null) {
            List<float[]> newTimeArray = new ArrayList<>();
            float[] firstTimeValue = timeArray.get(0);
            if(firstTimeValue.length > 2){
                float begin = firstTimeValue[1];
                if(begin != 0f){
                    //补上头
                    newTimeArray.add(new float[]{1f,0f,begin});
                }
            }
            //加上第一个
            newTimeArray.add(firstTimeValue);
            //补中间
            for(int i = 1;i < timeArray.size();i++){
                float[] lastValue = timeArray.get(i-1);
                float[] timeValue = timeArray.get(i);
                if(timeValue.length > 2 && lastValue.length > 2){
                    float begin = timeValue[1];
                    float lastBegin = lastValue[1];
                    float lastProgress= lastValue[2];
                    //误差大于五位小数就表示中间要插值
                    if(begin - (lastBegin +lastProgress) > 0.00001){
                        newTimeArray.add(new float[]{1f,lastBegin +lastProgress,begin-(lastBegin +lastProgress)});
                    }
                }
                newTimeArray.add(timeValue);
            }
            float[] lastTimeValue = timeArray.get(timeArray.size() -1);
            if(lastTimeValue.length > 2){
                float begin = lastTimeValue[1];
                float progress = lastTimeValue[2];
                if((begin + progress) != 1f){
                    //补上尾
                    newTimeArray.add(new float[]{1f,(begin + progress),(1f - begin - progress)});
                }
            }
            this.timeArray = newTimeArray;
            this.xAxisArray = xAxisArray;
            lineAnimator.start(animationTime);
        }
    }

    /**
     * 设置Y坐标描述符
     */
    public void setYAxisString(String wakeString, String shallowString, String deepString) {
        this.wakeString = wakeString;
        this.shallowString = shallowString;
        this.deepString = deepString;
        invalidate();
    }

    /**
     * 设置横线的宽度
     */
    public void setWidthRatio(float widthRatio) {
        if (this.widthRatio != widthRatio) {
            this.widthRatio = widthRatio;
            invalidate();
        }
    }

    /**
     * 设置顔色
     */
    public void setLineColor(int wakeColor,int shallowColor,int deepColor){
        this.wakeColor = wakeColor;
        this.shallowColor = shallowColor;
        this.deepColor = deepColor;
        invalidate();
    }

    /**
     * 清空画布
     */
    public void clearView() {
        linePaint.setShader(null);
        textPaint.setShader(null);
    }

    /**
     * 直线绘制持续的动画类
     */
    private class LineAnimator {

        private float mPhaseX = 1f; //默认动画值0f-1f
        private ValueAnimator.AnimatorUpdateListener mListener;//监听
        private ObjectAnimator objectAnimator;

        private LineAnimator(ValueAnimator.AnimatorUpdateListener listener) {
            mListener = listener;
        }

        private float getPhaseX() {
            return mPhaseX;
        }

        private void setPhaseX(float phase) {
            mPhaseX = phase;
        }

        /**
         * Y轴动画
         *
         * @param durationMillis 持续时间
         */
        private void start(int durationMillis) {
            release();
            objectAnimator = ObjectAnimator.ofFloat(this, "phaseX", 0f, 1f);
            objectAnimator.setDuration(durationMillis);
            objectAnimator.addUpdateListener(mListener);
            objectAnimator.start();
        }

        /**
         * 释放动画
         */
        private void release() {
            if (objectAnimator != null) {
                objectAnimator.end();
                objectAnimator.cancel();
                objectAnimator = null;
            }
        }
    }
}
