package com.morgan.lib.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;

/**
 * 一个自由落体后弹几下的ImageView
 * 
 * @author Morgan.ji
 * @version 1.0
 * @date 2016-05-06
 */
public class FallBallView extends ImageView {

    private static final int ANIMATION_MOVE = 0;
    private static final int ANIMATION_END = -1;
    private static final int DIVID_COUNT = 100;
    private static final int TRANSLATE_TIME = 1000;
    private int mStartX, mStartY;
    private int mEndX, mEndY;
    private Interpolator mInterpolator;
    private FallListener mOnFallListener;
    private boolean mRuning = false;

    public FallBallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInterpolator = new BounceInterpolator();
    }

    /**
     * 开始
     */
    public void start() {
        if (mRuning) {
            return;
        }
        if (null != mOnFallListener) {
            mOnFallListener.onfallStart();
        }
        mRuning = true;
        new FallThread().start();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            if (msg.what == ANIMATION_MOVE) {
                // FallBallWidget.this.setTranslationX(msg.arg1);
                // FallBallWidget.this.setTranslationY(msg.arg2);
                FallBallView.this.layout(mStartX + msg.arg1, mStartY + msg.arg2, mStartX + msg.arg1 + getWidth(),
                        mStartY + msg.arg2 + getHeight());
            } else if (msg.what == ANIMATION_END) {
                // FallBallWidget.this.setTranslationX(0);
                // FallBallWidget.this.setTranslationY(0);
                FallBallView.this.layout(mStartX, mStartY, mStartX + getWidth(), mStartY + getHeight());
                FallBallView.this.setVisibility(View.INVISIBLE);
                if (null != mOnFallListener) {
                    mOnFallListener.onfalled();
                }
            }
        }
    };

    /**
     * 下落线程，控制进度
     * 
     * @author Morgan.ji
     * @version 1.0
     * @date 2016-05-06
     */
    class FallThread extends Thread {

        @Override
        public void run() {
            int xLength = mEndX - mStartX;
            int yLength = mEndY - mStartY;
            int start = 1;
            while (start <= DIVID_COUNT) {
                try {
                    Message msg = mHandler.obtainMessage();
                    msg.what = ANIMATION_MOVE;
                    msg.arg1 = (int) (xLength * ((double) start / DIVID_COUNT));
                    msg.arg2 = (int) (yLength * mInterpolator.getInterpolation((float) ((double) start / DIVID_COUNT)));
                    mHandler.sendMessage(msg);
                    Thread.sleep(TRANSLATE_TIME / DIVID_COUNT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                start++;
            }
            mRuning = false;
            mHandler.sendEmptyMessage(ANIMATION_END);
        }
    }

    public void setStartPosition(int startX, int startY) {
        this.mStartX = startX;
        this.mStartY = startY;
    }

    public void setEndPosition(int endX, int endY) {
        this.mEndX = endX;
        this.mEndY = endY;
    }

    public void setOnFallListener(FallListener onFallListener) {
        this.mOnFallListener = onFallListener;
    }

    /**
     * 下落监听器
     * 
     * @author Morgan.ji
     * @version 1.0
     * @date 2016-05-06
     */
    public interface FallListener {

        public void onfallStart();

        public void onfalled();
    }
}
