package com.xyp.tiange.pathdemo;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * User: xyp
 * Date: 2017/7/14
 * Time: 16:12
 */

public class SearchView extends View {
    private Paint paint;
    private Path pathSearch, pathCircle;
    private PathMeasure pathMeasure;
    private int measureW, measureH;
    private float[] pos;//用来存放外面圆的起始点坐标
    private float animationValue;
    private State currentState = State.DRAW_CIRCLE;
    private ValueAnimator searchAni, circleAni;

    private int animationDuration;
    private int strokeWidth;
    private int circleColor;

    public SearchView(Context context) {
        this(context, null);
    }

    public SearchView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SearchView);
        animationDuration = typedArray.getInt(R.styleable.SearchView_animationDuration, 2000);
        strokeWidth = typedArray.getInt(R.styleable.SearchView_strokeWidth, 12);
        circleColor = typedArray.getColor(R.styleable.SearchView_circleColor, Color.WHITE);
        typedArray.recycle();
        init();
    }

    public enum State{
        DRAW_CIRCLE,
        DRAW_SEARCH,
        END
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setAntiAlias(true);
        //画笔为圆
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(circleColor);


        pathSearch = new Path();
        pathCircle = new Path();
        pathMeasure = new PathMeasure();
        pos = new float[2];
        //先画出整个图形路径
        pathSearch.addArc(-50, -50, 50, 50, 45, 359.9f);
        pathCircle.addArc(-100, -100, 100, 100, 45, 359.9f);

        //测量的是外面这个圆环
        pathMeasure.setPath(pathCircle, false);
        //得到距离起始点distance的坐标，这里的起始点是45度开始的地方(distance = 0)，传null是不需要得到正切值
        pathMeasure.getPosTan(0, pos, null);
        //pathSearch已经把里面这个圆环画出来了，把他最后的一个点连接到外面圆环开始点处，形成放大镜的把手
        pathSearch.lineTo(pos[0], pos[1]);
    }

    private void initAni() {
        circleAni = ValueAnimator.ofFloat(1, 0);
        circleAni.setDuration(animationDuration);
        circleAni.setRepeatCount(1);
        circleAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animationValue = (float) animation.getAnimatedValue();
                currentState = State.DRAW_CIRCLE;
                invalidate();
            }
        });

        searchAni = ValueAnimator.ofFloat(1, 0);
        searchAni.setDuration(animationDuration);
        searchAni.setRepeatCount(1);
        searchAni.setRepeatMode(ValueAnimator.REVERSE);
        searchAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animationValue = (float) animation.getAnimatedValue();
                currentState = State.DRAW_SEARCH;
                invalidate();
            }
        });

        circleAni.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if(searchAni != null)
                    searchAni.start();
            }
        });

        searchAni.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if(circleAni != null)
                    circleAni.start();
            }
        });
    }

    public void startSearch() {
        if (circleAni == null && searchAni == null) {
            initAni();
        }
        circleAni.start();
    }

    public void stopSearch() {
        if (circleAni != null) {
            circleAni.cancel();
            circleAni = null;
        }
        if (searchAni != null) {
            searchAni.cancel();
            searchAni = null;
        }
        currentState = State.END;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        measureW = w;
        measureH = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //把android坐标系（左上角为原点）移动为数学坐标系
        canvas.translate(measureW / 2, measureH / 2);

        if (currentState == State.DRAW_CIRCLE) {//画外面的圆
            pathMeasure.setPath(pathCircle, false);
            Path dst = new Path();
            float stop = pathMeasure.getLength() * animationValue;
            float start = (float) (stop - ((0.5 - Math.abs(animationValue - 0.5)) * 200f));
            //获得这个path上某一段的path(存放在dst),true表示不连接起始点
            pathMeasure.getSegment(start, stop, dst, true);
            canvas.drawPath(dst, paint);
        } else if (currentState == State.DRAW_SEARCH) {//画放大镜
            pathMeasure.setPath(pathSearch, false);
            Path dst = new Path();
            //放大镜是从圆圈开始画，最后画的把手，所以可以改变start位置，从最后开始往前取
            pathMeasure.getSegment(pathMeasure.getLength() * animationValue, pathMeasure.getLength(), dst, true);
            canvas.drawPath(dst, paint);
        } else if (currentState == State.END) {
            canvas.drawPath(pathSearch, paint);
        }
    }
}
