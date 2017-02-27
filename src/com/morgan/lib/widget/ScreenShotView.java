package com.morgan.lib.widget;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.morgan.lib.util.DensityUtils;
import com.morgan.main.R;

/**
 * 
 * 实现截图功能的View,使用OnTouchListener而非onTouchEvent是为了在退出截图模式时方便
 * 
 * @author Morgan.ji
 * @version 1.0
 * @date 2016-05-01
 */
public class ScreenShotView extends View implements OnTouchListener {

    // 灰色覆盖区，标示排除在截图外的区域
    private Paint mGrayPaint;
    private Rect topGrayRect;
    private Rect rightGrayRect;
    private Rect bottomGrayRect;
    private Rect leftGrayRect;

    private int screenWidth;
    private int screenHeight;
    private int viewWidth;
    private int viewHeight;

    private OnScreenShotListener mListener;
    private Activity mContext;

    // 截图内框的坐标
    private int top, right, bottom, left;
    // 辅助变量，用于暂时保存内框坐标
    private int tmpLeft, tmpTop, tmpRight, tmpBottom;

    private int innerWidth, innerHeight;
    // 触摸屏幕前后坐标
    private int downX, downY, moveX, moveY;

    private final static int TOUCH_SELECT = 1; // 选择标识
    private final static int TOUCH_MOVE = 2; // 移动选区

    private int touchFlag = TOUCH_SELECT;

    private boolean isInMoveSelection; // 是否在移动选区内
    private boolean isInTopPullArea; // 上拉调整大小
    private boolean isInRightPullArea; // 右拉调整大小
    private boolean isInBottomPullArea;
    private boolean isInLeftPullArea;

    private int innnerClickCount = 0; // 选框内双击中的有效点击次数
    private int outterClickCount = 0; // 选框外双击的有效点击次数
    private long timeInterval; // 触碰事件间隔
    private long firstClickTime; // 第一次单击时刻
    private long secondClickTime; // 第二次单击时刻
    private long touchDownTime; // 刚触碰的时刻
    private long touchUpTime; // 触碰结束时刻
    private final static long VALID_CLICK_INTERVAL = 500; // 触摸down,up一下500毫秒以内为一次单击
    private final static long VALID_DOUBLE_CLICK_INTERNAL = 1000;// 两次单击之间时间距离

    private final static int VALID_PULL_DISTANCE = 30; // 有效拉大拉小距离

    private boolean isTimeToTip = false;

    private static Bitmap bmDoubleClickTip;

    /**
     * 监听截图完成
     */
    public interface OnScreenShotListener {
        public void onComplete(Bitmap bm);
    }

    public ScreenShotView(Activity context, OnScreenShotListener listener) {
        super(context);

        mContext = context;
        mListener = listener;
        Point p = DensityUtils.getDisplaySize(context);
        screenWidth = p.x;
        screenHeight = p.y;

        mGrayPaint = new Paint();
        mGrayPaint.setARGB(0, 0, 0, 0);

    }

    /**
     * 初始化各种参数
     */
    private void initView() {
        mGrayPaint.setARGB(100, 0, 0, 0);
        viewWidth = screenWidth;
        viewHeight = screenHeight - getStatusHeight() - getActionBarHeight();

        touchFlag = TOUCH_SELECT;
        topGrayRect = new Rect();
        rightGrayRect = new Rect();
        bottomGrayRect = new Rect();
        leftGrayRect = new Rect();

        topGrayRect.set(0, 0, viewWidth, viewHeight);

        tmpLeft = left = 0;
        tmpTop = top = 0;
        tmpRight = right = 0;
        tmpBottom = bottom = 0;
        innerWidth = innerHeight = 0;

        if (bmDoubleClickTip == null) {
            bmDoubleClickTip = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.lib_double_click));
        }
        setOnTouchListener(this);
        Toast.makeText(mContext, R.string.msg_move_finger_to_select_area, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(topGrayRect, mGrayPaint);
        canvas.drawRect(rightGrayRect, mGrayPaint);
        canvas.drawRect(bottomGrayRect, mGrayPaint);
        canvas.drawRect(leftGrayRect, mGrayPaint);

        if (isTimeToTip) {
            // 在中间位置出现提示
            innerWidth = right - left;
            innerHeight = bottom - top;
            int bmWidth = bmDoubleClickTip.getWidth();
            int bmHeight = bmDoubleClickTip.getHeight();
            int x = (int) (left + (innerWidth - bmWidth) * 0.5);
            int y = (int) (top + (innerHeight - bmHeight) * 0.5);
            canvas.drawBitmap(bmDoubleClickTip, x, y, null);
            isTimeToTip = false;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            isTimeToTip = false;

            downX = (int) event.getX();
            downY = (int) event.getY();

            isInMoveSelection = isInSeletion(downX, downY);
            isInTopPullArea = isInTopPullArea(downX, downY);
            isInRightPullArea = isInRightPullArea(downX, downY);
            isInBottomPullArea = isInBottomPullArea(downX, downY);
            isInLeftPullArea = isInLeftPullArea(downX, downY);

            // TODO 暂时未找到解决重新选择和双击取消的冲突
            // if (!isInMoveSelection && !isInTopPullArea && !isInRightPullArea
            // && !isInBottomPullArea
            // && !isInLeftPullArea) {
            // touchFlag = TOUCH_SELECT;// 第一次选择区域或重新选择区域
            // }

            touchDownTime = System.currentTimeMillis();
            break;
        case MotionEvent.ACTION_MOVE:
            isTimeToTip = false;
            moveX = (int) event.getX();
            moveY = (int) event.getY();
            if (touchFlag == TOUCH_SELECT) {
                tmpLeft = left = Math.min(downX, moveX);
                tmpTop = top = Math.min(downY, moveY);
                tmpRight = right = Math.max(downX, moveX);
                tmpBottom = bottom = Math.max(downY, moveY);

                isInMoveSelection = false;
            } else if (touchFlag == TOUCH_MOVE && isInMoveSelection) {
                int xDistance = moveX - downX;
                int yDistance = moveY - downY;
                moveShotArea(xDistance, yDistance);
            } else if (touchFlag == TOUCH_MOVE && isInTopPullArea) {
                int yDistance = downY - moveY;
                int extremeY = (bottom - 2 * VALID_PULL_DISTANCE);
                top = (tmpTop - yDistance) < extremeY ? (tmpTop - yDistance) : extremeY;
            } else if (touchFlag == TOUCH_MOVE && isInRightPullArea) {
                int xDistance = moveX - downX;
                int extremeX = (left + 2 * VALID_PULL_DISTANCE);
                right = (tmpRight + xDistance) > extremeX ? (tmpRight + xDistance) : extremeX;
            } else if (touchFlag == TOUCH_MOVE && isInBottomPullArea) {
                int yDistance = downY - moveY;
                int extremeY = (top + 2 * VALID_PULL_DISTANCE);
                bottom = (tmpBottom - yDistance) > extremeY ? (tmpBottom - yDistance) : extremeY;
            } else if (touchFlag == TOUCH_MOVE && isInLeftPullArea) {
                int xDistance = downX - moveX;
                int extremeX = (right - 2 * VALID_PULL_DISTANCE);
                left = (tmpLeft - xDistance) < extremeX ? (tmpLeft - xDistance) : extremeX;
            }
            setInnerBorder(left, top, right, bottom);
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            int upX = (int) event.getX();
            int upY = (int) event.getY();

            boolean noOperate = false;
            if (isInMoveSelection) {
                // 说明为移动，5是过滤指纹间隙 左右相同，上下相同，比x和y方向移动判断更为严格
                if (Math.abs(tmpLeft - left) <= 5 && Math.abs(tmpTop - top) <= 5) {
                    noOperate = true;
                }
            } else {
                if (touchFlag != TOUCH_SELECT && !isInTopPullArea && !isInRightPullArea && !isInBottomPullArea
                        && !isInLeftPullArea) {
                    noOperate = true;
                }
            }

            if (touchFlag == TOUCH_SELECT) {
                touchFlag = TOUCH_MOVE;
            } else if (touchFlag == TOUCH_MOVE) {
                tmpLeft = left;
                tmpTop = top;
                tmpRight = right;
                tmpBottom = bottom;
            }

            if (isShouldShowTip() && isTimeToTip == false) {
                isTimeToTip = true;
                invalidate();
            }

            touchUpTime = System.currentTimeMillis();
            timeInterval = touchUpTime - touchDownTime;

            // 选区内的双击则为截图，选区外的双击则为取消截图
            if (timeInterval < VALID_CLICK_INTERVAL && noOperate && Math.abs(downX - upX) <= 10
                    && Math.abs(downY - upY) <= 10) {
                if (isInMoveSelection) {
                    innnerClickCount = innnerClickCount + 1;
                    if (innnerClickCount == 1) {
                        firstClickTime = System.currentTimeMillis();
                    } else if (innnerClickCount == 2) {
                        secondClickTime = System.currentTimeMillis();
                        if ((secondClickTime - firstClickTime) < VALID_DOUBLE_CLICK_INTERNAL) {
                            isTimeToTip = false;
                            mListener.onComplete(getCutImage());
                            dismiss();
                        }
                        innnerClickCount = 0;
                    }
                } else {
                    outterClickCount = outterClickCount + 1;
                    if (outterClickCount == 1) {
                        firstClickTime = System.currentTimeMillis();
                    } else if (outterClickCount == 2) {
                        secondClickTime = System.currentTimeMillis();
                        if ((secondClickTime - firstClickTime) < VALID_DOUBLE_CLICK_INTERNAL) {
                            isTimeToTip = false;
                            dismiss();
                        }
                        outterClickCount = 0;
                    }
                }
            }
            break;
        default:
            break;
        }
        return true;
    }

    /**
     * 是否在左拉动选区
     * 
     * @return
     */
    private boolean isInLeftPullArea(int x, int y) {
        if (((left - VALID_PULL_DISTANCE) <= x && x < (left + VALID_PULL_DISTANCE))
                && ((top + VALID_PULL_DISTANCE) < y && y < (bottom - VALID_PULL_DISTANCE))) {
            return true;
        }
        return false;
    }

    /**
     * 是否在右拉动选区
     * 
     * @return
     */
    private boolean isInRightPullArea(int x, int y) {
        if (((right - VALID_PULL_DISTANCE) <= x && x < (right + VALID_PULL_DISTANCE))
                && ((top + VALID_PULL_DISTANCE) < y && y < (bottom - VALID_PULL_DISTANCE))) {
            return true;
        }
        return false;
    }

    /**
     * 是否在上拉动选区
     * 
     * @return
     */
    private boolean isInTopPullArea(int x, int y) {
        if (((left + VALID_PULL_DISTANCE) <= x && x < (right - VALID_PULL_DISTANCE))
                && ((top - VALID_PULL_DISTANCE) < y && y < (top + VALID_PULL_DISTANCE))) {
            return true;
        }
        return false;
    }

    /**
     * 是否在下拉动选区
     * 
     * @return
     */
    private boolean isInBottomPullArea(int x, int y) {
        if (((left + VALID_PULL_DISTANCE) <= x && x < (right - VALID_PULL_DISTANCE))
                && ((bottom - VALID_PULL_DISTANCE) < y && y < (bottom + VALID_PULL_DISTANCE))) {
            return true;
        }
        return false;
    }

    /**
     * 判断触碰的点有没有在移动选区内
     * 
     * @param x
     * @param y
     * @return
     */
    private boolean isInSeletion(int x, int y) {
        if ((left != 0 || right != 0 || top != 0 || bottom != 0)
                && ((left + VALID_PULL_DISTANCE) <= x && x <= (right - VALID_PULL_DISTANCE))
                && ((top + VALID_PULL_DISTANCE) <= y && y <= (bottom - VALID_PULL_DISTANCE))) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否应该出现提示
     * 
     * @return
     */
    private boolean isShouldShowTip() {
        if ((right - left) > 100 && (bottom - top) > 100) {
            return true;
        }
        return false;
    }

    /**
     * 决定内层选框的坐标
     * 
     * @param xDistance
     * @param yDistance
     */
    private void moveShotArea(int xDistance, int yDistance) {
        innerWidth = right - left;
        innerHeight = bottom - top;
        // 决定左右坐标
        if ((tmpLeft + xDistance) < 0) {
            right = innerWidth;
            left = 0;
        } else if ((tmpRight + xDistance) > viewWidth) {
            left = viewWidth - innerWidth;
            right = viewWidth;
        } else {
            left = tmpLeft + xDistance;
            right = tmpRight + xDistance;
        }

        // 决定上下坐标
        if ((tmpTop + yDistance) < 0) {
            bottom = innerHeight;
            top = 0;
        } else if ((tmpBottom + yDistance) > viewHeight) { // 实际覆盖区域不包括状态条和ActionBar
            top = viewHeight - innerHeight;
            bottom = viewHeight;
        } else {
            top = tmpTop + yDistance;
            bottom = tmpBottom + yDistance;
        }
    }

    /**
     * 设置 内层的边框坐标
     */
    private void setInnerBorder(int left, int top, int right, int bottom) {
        topGrayRect.set(0, 0, viewWidth, top);
        rightGrayRect.set(right, top, viewWidth, bottom);
        bottomGrayRect.set(0, bottom, viewWidth, viewHeight);
        leftGrayRect.set(0, top, left, bottom);
        this.invalidate();
    }

    /**
     * 截取内层边框中的View.
     */
    private Bitmap getCutImage() {
        View view = mContext.getWindow().getDecorView();
        Bitmap bmp = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        view.draw(canvas);
        return imageCrop(bmp);
    }

    /**
     * 裁剪图片
     */
    private Bitmap imageCrop(Bitmap bitmap) {
        int x = left < 0 ? 0 : left;
        int y = top + getStatusHeight() + getActionBarHeight();
        int width = right - left;
        int height = bottom - top;
        if ((width + x) > bitmap.getWidth()) {
            width = bitmap.getWidth() - x;
        }
        if ((y + height) > bitmap.getHeight()) {
            height = bitmap.getHeight() - y;
        }
        return Bitmap.createBitmap(bitmap, x, y, width, height, null, false);
    }

    /**
     * 返回状态栏的高度
     * 
     * @return
     */
    private int getStatusHeight() {
        Rect frame = new Rect();
        mContext.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }

    /**
     * 返回ActionBar的高度
     * 
     * @return
     */
    private int getActionBarHeight() {
        return mContext.getActionBar().getHeight();
    }

    /**
     * 启用该view
     */
    public void start() {
        initView();
        invalidate();
    }

    /**
     * 注销该view
     */
    public void dismiss() {
        mGrayPaint.setARGB(0, 0, 0, 0);
        setOnTouchListener(null);
        invalidate();
    }

}
