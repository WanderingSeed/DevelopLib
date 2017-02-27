package com.morgan.lib.widget.slidemenu;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.morgan.lib.util.Logger;
import com.morgan.main.R;

/**
 * 通过滑动可以显示界面两侧的菜单，使用 {@link ScrollDetector}自定义界面的滑动规则
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2015-11-12
 */
public class SlideMenu extends ViewGroup {

    /**
     * 滑动动画最大时间阀值
     */
    private final static int MAX_DURATION = 500;
    /**
     * 系统状态条高度
     */
    private static int STATUS_BAR_HEIGHT;

    public final static int FLAG_LEFT_STYLE = 1 << 0;
    public final static int FLAG_RIGHT_STYLE = 1 << 1;

    public final static int MODE_SLIDE_WINDOW = 1;
    public final static int MODE_SLIDE_CONTENT = 2;

    // 这几个标示状态
    public final static int STATE_CLOSE = 1 << 0;
    public final static int STATE_OPEN_LEFT = 1 << 1;
    public final static int STATE_OPEN_RIGHT = 1 << 2;
    public final static int STATE_DRAG = 1 << 3;
    public final static int STATE_SCROLL = 1 << 4;
    public final static int STATE_OPEN_MASK = 6;

    // 此三个为中间内容界面位置
    private final static int POSITION_LEFT = -1;
    private final static int POSITION_MIDDLE = 0;
    private final static int POSITION_RIGHT = 1;

    private int mCurrentState;
    private int mCurrentContentPosition;

    private View mContent;
    private View mLeftMenu;
    private View mRightMenu;

    private int mTouchSlop;

    private float mPressedX;
    private float mLastMotionX;
    private volatile int mCurrentContentOffset;

    private int mContentBoundsLeft; // 内容界面的左侧边界值
    private int mContentBoundsRight;// 内容界面的右侧边界值

    private boolean mIsTapContent;
    private Rect mContentHitRect;

    @ExportedProperty
    private Drawable mLeftShadowDrawable;
    @ExportedProperty
    private Drawable mRightShadowDrawable;
    @ExportedProperty
    private float mLeftShadowWidth;
    @ExportedProperty
    private float mRightShadowWidth;

    private int mMenuStyle;// 菜单样式，是在左侧还是右侧
    private int mSlideMode = MODE_SLIDE_CONTENT;// 滑动模式，ActionBar是否跟随滑动
    private boolean mIsPendingResolveSlideMode;

    private int mWidth;
    private int mHeight;

    private OnSlideStateChangeListener mSlideStateChangeListener;

    private VelocityTracker mVelocityTracker;
    private Scroller mScroller;
    private int mMenuWidth = 360;
    private float mScrollScale = 0.33f;

    private static final Interpolator mInterpolator = new Interpolator() {

        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    public SlideMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller = new Scroller(context, mInterpolator);
        mContentHitRect = new Rect();
        STATUS_BAR_HEIGHT = (int) getStatusBarHeight(context);
        setWillNotDraw(false);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlideMenu, defStyle, 0);

        setLeftShadowWidth(a.getDimension(R.styleable.SlideMenu_leftShadowWidth, 30));
        setRightShadowWidth(a.getDimension(R.styleable.SlideMenu_rightShadowWidth, 30));

        Drawable primaryShadowDrawable = a.getDrawable(R.styleable.SlideMenu_leftShadowDrawable);
        if (null == primaryShadowDrawable) {
            primaryShadowDrawable = new GradientDrawable(Orientation.LEFT_RIGHT, new int[] { Color.TRANSPARENT,
                    Color.argb(99, 0, 0, 0) });
        }
        setLeftShadowDrawable(primaryShadowDrawable);

        Drawable secondaryShadowDrawable = a.getDrawable(R.styleable.SlideMenu_rightShadowDrawable);
        if (null == secondaryShadowDrawable) {
            secondaryShadowDrawable = new GradientDrawable(Orientation.LEFT_RIGHT, new int[] { Color.argb(99, 0, 0, 0),
                    Color.TRANSPARENT });
        }
        setRightShadowDrawable(secondaryShadowDrawable);
        mMenuStyle = a.getInt(R.styleable.SlideMenu_menuStyle, FLAG_LEFT_STYLE | FLAG_RIGHT_STYLE);
        setFocusable(true);
        setFocusableInTouchMode(true);
        a.recycle();
    }

    public SlideMenu(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.slideMenuStyle);
    }

    public SlideMenu(Context context) {
        this(context, null);
    }

    /**
     * 获取系统状态条高度
     * 
     * @param context
     * @return
     */
    public static float getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int statusBarIdentifier = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (0 != statusBarIdentifier) {
            return resources.getDimension(statusBarIdentifier);
        }
        return 0;
    }

    /**
     * 根据滑动模式调整布局
     */
    protected void resolveSlideMode() {
        ViewGroup decorView = (ViewGroup) getRootView();
        ViewGroup contentContainer = (ViewGroup) decorView.findViewById(android.R.id.content);
        View content = mContent;
        if (null == decorView || null == content || 0 == getChildCount()) {
            return;
        }

        TypedValue value = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.windowBackground, value, true);

        switch (mSlideMode) {
        case MODE_SLIDE_WINDOW: {
            // remove this view from parent
            removeViewFromParent(this);
            // copy the layoutparams of content
            SlideMenuLayoutParams contentLayoutParams = new SlideMenuLayoutParams(content.getLayoutParams());
            // remove content view from this view
            removeViewFromParent(content);
            // add content to layout root view
            contentContainer.addView(content);

            // get window with ActionBar
            View decorChild = decorView.getChildAt(0);// decor的第一个子控件是包括所有控件的LinearLayout
            decorChild.setBackgroundResource(0);
            removeViewFromParent(decorChild);
            addView(decorChild, contentLayoutParams);

            // add this view to root view
            decorView.addView(this);
            setBackgroundResource(value.resourceId);
        }
            break;
        case MODE_SLIDE_CONTENT: {
            // remove this view from decor view
            setBackgroundResource(0);
            removeViewFromParent(this);
            // get the origin content view from the content wrapper
            View originContent = contentContainer.getChildAt(0);
            // this is the decor child remove from decor view
            View decorChild = mContent;
            SlideMenuLayoutParams layoutParams = (SlideMenuLayoutParams) decorChild.getLayoutParams();
            // remove the origin content from content wrapper
            removeViewFromParent(originContent);
            // remove decor child from this view
            removeViewFromParent(decorChild);
            // restore the decor child to decor view
            decorChild.setBackgroundResource(value.resourceId);
            decorView.addView(decorChild);
            // add this view to content wrapper
            contentContainer.addView(this);
            // add the origin content to this view
            addView(originContent, layoutParams);
        }
            break;
        }
    }

    @Override
    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        if (!(params instanceof SlideMenuLayoutParams)) {
            throw new IllegalArgumentException("LayoutParams must a instance of SlideMenuLayoutParams");
        }

        SlideMenuLayoutParams layoutParams = (SlideMenuLayoutParams) params;
        switch (layoutParams.role) {
        case SlideMenuLayoutParams.ROLE_CONTENT:
            if (null != mContent) {
                removeView(mContent);
            }
            mContent = child;
            break;
        case SlideMenuLayoutParams.ROLE_LEFT_MENU:
            if (null != mLeftMenu) {
                removeView(mLeftMenu);
            }
            mLeftMenu = child;
            break;
        case SlideMenuLayoutParams.ROLE_RIGHT_MENU:
            if (null != mRightMenu) {
                removeView(mRightMenu);
            }
            mRightMenu = child;
            break;
        default:
            // 忽略所有没有layout_role属性的控件
            return;
        }
        invalidateMenuState();
        super.addView(child, index, params);
    }

    /**
     * 把控件从其父控件中移除出来
     * 
     * @param view
     */
    public static void removeViewFromParent(View view) {
        if (null == view) {
            return;
        }
        ViewGroup parent = (ViewGroup) view.getParent();
        if (null == parent) {
            return;
        }
        parent.removeView(view);
    }

    /**
     * 给左侧菜单设置阴影图片
     * 
     * @param shadowDrawable
     */
    public void setLeftShadowDrawable(Drawable shadowDrawable) {
        mLeftShadowDrawable = shadowDrawable;
    }

    /**
     * 获取左侧菜单的阴影图片
     * 
     * @return
     */
    public Drawable getLeftShadowDrawable() {
        return mLeftShadowDrawable;
    }

    /**
     * 获取右侧菜单的阴影图片
     * 
     * @return
     */
    public Drawable getRightShadowDrawable() {
        return mRightShadowDrawable;
    }

    /**
     * 设置右侧菜单的阴影图片
     * 
     * @param secondaryShadowDrawable
     */
    public void setRightShadowDrawable(Drawable shadowDrawable) {
        this.mRightShadowDrawable = shadowDrawable;
    }

    /**
     * 获取当前滑动模式
     * 
     * @return
     */
    public int getSlideMode() {
        return mSlideMode;
    }

    /**
     * 设置当前滑动模式:<br/>
     * {@link #MODE_SLIDE_CONTENT} {@link #MODE_SLIDE_WINDOW}
     * 
     * @param slideMode
     */
    public void setSlideMode(int slideMode) {
        if (mSlideMode == slideMode) {
            return;
        }
        mSlideMode = slideMode;
        if (0 == getChildCount()) {
            mIsPendingResolveSlideMode = true;
        } else {
            resolveSlideMode();
        }
    }

    /**
     * 当前菜单是否打开（不管左菜单还是右菜单）
     * 
     * @return true open, otherwise false
     */
    public boolean isOpen() {
        return (STATE_OPEN_MASK & mCurrentState) != 0;
    }

    /**
     * 打开菜单
     * 
     * @param isSlideLeft
     * @param isAnimated
     */
    public void open(boolean isSlideLeft, boolean isAnimated) {
        if (isOpen()) {
            return;
        }

        int targetOffset = isSlideLeft ? mContentBoundsLeft : mContentBoundsRight;

        if (isAnimated) {
            smoothScrollContentTo(targetOffset);
        } else {
            mScroller.abortAnimation();
            setCurrentOffset(targetOffset);
        }
    }

    /**
     * 关闭左右菜单
     * 
     * @param isAnimated
     */
    public void close(boolean isAnimated) {
        if (STATE_CLOSE == mCurrentState) {
            return;
        }

        if (isAnimated) {
            smoothScrollContentTo(0);
        } else {
            mScroller.abortAnimation();
            setCurrentOffset(0);
        }
    }

    /**
     * 获取菜单样式, {@link #FLAG_LEFT_STYLE}, {@link #FLAG_RIGHT_STYLE} or
     * {@link #FLAG_LEFT_STYLE}| {@link #FLAG_RIGHT_STYLE}
     * 
     * @return
     */
    public int getMenuStyle() {
        return mMenuStyle;
    }

    /**
     * 设置菜单位置
     * 
     * @param menuStyle
     */
    public void setMenuStyle(int menuStyle) {
        this.mMenuStyle = menuStyle;
    }

    /**
     * 设置滑动状态和偏距监听器
     * 
     * @return
     */
    public OnSlideStateChangeListener getOnSlideStateChangeListener() {
        return mSlideStateChangeListener;
    }

    /**
     * 滑获取当前动状态和偏距监听器
     * 
     * @param slideStateChangeListener
     */
    public void setOnSlideStateChangeListener(OnSlideStateChangeListener slideStateChangeListener) {
        this.mSlideStateChangeListener = slideStateChangeListener;
    }

    /**
     * 获取当前控件状态，
     * 
     * @return
     */
    public int getCurrentState() {
        return mCurrentState;
    }

    /**
     * 设置当前控件状态
     * 
     * @param currentState
     */
    protected void setCurrentState(int currentState) {
        if (null != mSlideStateChangeListener && currentState != mCurrentState) {
            mSlideStateChangeListener.onSlideStateChange(currentState);
        }
        this.mCurrentState = currentState;
    }

    /**
     * 以起始速度为0调用 {@link #smoothScrollContentTo(int, float)}
     * 
     * @param targetOffset
     */
    public void smoothScrollContentTo(int targetOffset) {
        smoothScrollContentTo(targetOffset, 0);
    }

    /**
     * 以一个指定的起始速度滑动内容界面到一个指定偏距
     * 
     * @param targetOffset
     * @param velocity
     */
    public void smoothScrollContentTo(int targetOffset, float velocity) {
        setCurrentState(STATE_SCROLL);
        int distance = targetOffset - mCurrentContentOffset;
        velocity = Math.abs(velocity);
        int duration = 400;
        if (velocity > 0) {
            duration = 3 * Math.round(1000 * Math.abs(distance / velocity));
        }
        duration = Math.min(duration, MAX_DURATION);
        mScroller.abortAnimation();
        mScroller.startScroll(mCurrentContentOffset, 0, distance, 0, duration);
        invalidate();
    }

    /**
     * 判断坐标是否在内容界面上
     * 
     * @param x
     * @param y
     * @return
     */
    private boolean isTapContent(float x, float y) {
        View content = mContent;
        if (null != content) {
            content.getHitRect(mContentHitRect);
            return mContentHitRect.contains((int) x, (int) y);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();
        if (STATE_DRAG == mCurrentState || STATE_SCROLL == mCurrentState) {
            return true;
        }
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mPressedX = mLastMotionX = x;
            mIsTapContent = isTapContent(x, y);
            return isOpen() && mIsTapContent;
        case MotionEvent.ACTION_MOVE:
            float distance = x - mPressedX;
            if (Math.abs(distance) >= mTouchSlop && mIsTapContent) {
                if (!canChildScroll(this, (int) distance, (int) x, (int) y)) {
                    setCurrentState(STATE_DRAG);
                    mVelocityTracker = VelocityTracker.obtain();
                    return true;
                }
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mPressedX = mLastMotionX = x;
            mIsTapContent = isTapContent(x, y);
            if (mIsTapContent) {
                if (mCurrentState == STATE_SCROLL) {
                    Logger.d("onTouchEvent stop animation");
                }
                mScroller.abortAnimation();
            }
            mVelocityTracker = VelocityTracker.obtain();
            mVelocityTracker.addMovement(event);
            break;
        case MotionEvent.ACTION_MOVE:
            if (mVelocityTracker != null) {// 正常情况下都不会为空，除非中途取消了
                mVelocityTracker.addMovement(event);
            }
            if (Math.abs(x - mPressedX) >= mTouchSlop && mIsTapContent && mCurrentState != STATE_DRAG) {
                setCurrentState(STATE_DRAG);
            }
            if (STATE_DRAG == mCurrentState) {
                float distance = x - mLastMotionX;
                // 如果内容为可滑动控件，如果划出左侧菜单后向右滑则等内容居中后继续滑动的是内容或不滑动。
                if ((mCurrentContentOffset > 0 && mCurrentContentOffset + distance <= 0)
                        || (mCurrentContentOffset < 0 && mCurrentContentOffset + distance >= 0)
                        || mCurrentContentOffset == 0) {// 等于0只能说刚开始拖拽或前面强制停止了关闭的动画
                    // 强制停止了关闭的动画要判断子控件是否可滑动，否则左侧直接滑到右侧
                    if (canChildScroll(this, (int) (distance), (int) x, (int) y)) {
                        if (mCurrentContentOffset == 0) {
                            Logger.d("onTouchEvent it happend ");
                        }
                        setCurrentOffset(0);
                        MotionEvent cancelEvent = MotionEvent.obtain(event);
                        cancelEvent.setAction(MotionEvent.ACTION_CANCEL
                                | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                        onTouchEvent(cancelEvent);
                        cancelEvent.recycle();
                        // 此后虽然不能阻止后续的move事件，但isTapContent是false且mVelocityTracker
                        // == null且mCurrentState != STATE_DRAG
                        return false;
                    }
                }
                drag(mLastMotionX, x);
            }
            mLastMotionX = x;
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_OUTSIDE:
            if (mVelocityTracker != null) {// 如果为空则说明已经取消了
                mVelocityTracker.addMovement(event);
                if (STATE_DRAG == mCurrentState) {
                    mVelocityTracker.computeCurrentVelocity(1000);
                    endDrag(mVelocityTracker.getXVelocity());
                } else if (mIsTapContent) {
                    performContentClick();
                }
                mVelocityTracker.clear();
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mIsTapContent = false;
            }
            break;
        }
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (KeyEvent.ACTION_UP == event.getAction()) {
            boolean isOpen = isOpen();
            switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                if (isOpen) {
                    close(true);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (STATE_OPEN_LEFT == mCurrentState) {
                    close(true);
                    return true;
                } else if (!isOpen) {
                    open(true, true);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (STATE_OPEN_RIGHT == mCurrentState) {
                    close(true);
                    return true;
                } else if (!isOpen) {
                    open(false, true);
                    return true;
                }
                break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 获取左侧菜单
     * 
     * @return
     */
    public View getLeftMenu() {
        return mLeftMenu;
    }

    /**
     * 获取右侧菜单
     * 
     * @return
     */
    public View getRightMenu() {
        return mRightMenu;
    }

    /**
     * 内容界面点击事件
     */
    public void performContentClick() {
        if (isOpen()) {// 打开菜单才需要相应点击事件
            smoothScrollContentTo(0);
        }
    }

    protected void drag(float lastX, float x) {
        mCurrentContentOffset += (int) (x - lastX);
        setCurrentOffset(mCurrentContentOffset);
    }

    /**
     * 调整当前内容界面位置
     */
    private void invalidateMenuState() {
        mCurrentContentPosition = mCurrentContentOffset < 0 ? POSITION_LEFT
                : (mCurrentContentOffset == 0 ? POSITION_MIDDLE : POSITION_RIGHT);
        switch (mCurrentContentPosition) {
        case POSITION_LEFT:
            invalidateViewVisibility(mLeftMenu, View.INVISIBLE);
            invalidateViewVisibility(mRightMenu, View.VISIBLE);
            break;
        case POSITION_MIDDLE:
            invalidateViewVisibility(mLeftMenu, View.INVISIBLE);
            invalidateViewVisibility(mRightMenu, View.INVISIBLE);
            break;
        case POSITION_RIGHT:
            invalidateViewVisibility(mLeftMenu, View.VISIBLE);
            invalidateViewVisibility(mRightMenu, View.INVISIBLE);
            break;
        }
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    /**
     * 设置控件是否可见
     * 
     * @param view
     * @param visibility
     */
    private void invalidateViewVisibility(View view, int visibility) {
        if (null != view && view.getVisibility() != visibility) {
            view.setVisibility(visibility);
        }
    }

    protected void endDrag(float velocity) {
        int currentContentPosition = mCurrentContentPosition;
        boolean velocityMatched = Math.abs(velocity) > 400;
        switch (currentContentPosition) {
        case POSITION_LEFT:
            if ((velocity > 0 && velocityMatched) || (velocity <= 0 && !velocityMatched)) {
                smoothScrollContentTo(0, velocity);
            } else if ((velocity < 0 && velocityMatched) || (velocity >= 0 && !velocityMatched)) {
                smoothScrollContentTo(mContentBoundsLeft, velocity);
            }
            break;
        case POSITION_MIDDLE:
            setCurrentState(STATE_CLOSE);
            break;
        case POSITION_RIGHT:
            if ((velocity > 0 && velocityMatched) || (velocity <= 0 && !velocityMatched)) {
                smoothScrollContentTo(mContentBoundsRight, velocity);
            } else if ((velocity < 0 && velocityMatched) || (velocity >= 0 && !velocityMatched)) {
                smoothScrollContentTo(0, velocity);
            }
            break;
        }
    }

    /**
     * 设置当前的偏距
     * 
     * @param currentOffset
     */
    private void setCurrentOffset(int currentOffset) {
        mCurrentContentOffset = Math.min((mMenuStyle & FLAG_RIGHT_STYLE) == FLAG_RIGHT_STYLE ? mContentBoundsRight : 0,
                Math.max(currentOffset, (mMenuStyle & FLAG_LEFT_STYLE) == FLAG_LEFT_STYLE ? mContentBoundsLeft : 0));
        if (null != mSlideStateChangeListener) {
            float slideOffsetPercent = 0;
            int currentContentOffset = mCurrentContentOffset;
            if (0 < currentContentOffset) {
                slideOffsetPercent = currentContentOffset * 1.0f / mContentBoundsRight;
            } else if (0 > currentContentOffset) {
                slideOffsetPercent = -currentContentOffset * 1.0f / mContentBoundsLeft;
            }
            mSlideStateChangeListener.onSlideOffsetPercentChange(slideOffsetPercent);
        }
        invalidateMenuState();
        invalidate();
        requestLayout();
    }

    @Override
    public void computeScroll() {
        if (STATE_SCROLL == mCurrentState || isOpen()) {
            if (mScroller.computeScrollOffset()) {
                setCurrentOffset(mScroller.getCurrX());
            } else {
                setCurrentState(mCurrentContentOffset == 0 ? STATE_CLOSE : (mCurrentContentOffset > 0 ? STATE_OPEN_LEFT
                        : STATE_OPEN_RIGHT));
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        int maxChildWidth = 0, maxChildHeight = 0;
        for (int index = 0; index < count; index++) {
            View child = getChildAt(index);
            SlideMenuLayoutParams layoutParams = (SlideMenuLayoutParams) child.getLayoutParams();
            switch (layoutParams.role) {
            case SlideMenuLayoutParams.ROLE_CONTENT:
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                break;
            case SlideMenuLayoutParams.ROLE_LEFT_MENU:
            case SlideMenuLayoutParams.ROLE_RIGHT_MENU:
                measureChild(
                        child,
                        mMenuWidth + MeasureSpec.EXACTLY,
                        mSlideMode == MODE_SLIDE_WINDOW ? MeasureSpec.makeMeasureSpec(
                                MeasureSpec.getSize(heightMeasureSpec) - STATUS_BAR_HEIGHT,
                                MeasureSpec.getMode(heightMeasureSpec)) : heightMeasureSpec);
                break;
            }

            maxChildWidth = Math.max(maxChildWidth, child.getMeasuredWidth());
            maxChildHeight = Math.max(maxChildHeight, child.getMeasuredHeight());
        }
        maxChildWidth += getPaddingLeft() + getPaddingRight();
        maxChildHeight += getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(resolveSize(maxChildWidth, widthMeasureSpec),
                resolveSize(maxChildHeight, heightMeasureSpec));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        View parent = (View) getParent();
        if (android.R.id.content != parent.getId() && MODE_SLIDE_CONTENT == mSlideMode) {
            throw new IllegalStateException("SlidingMenu must be the root of layout");
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int statusBarHeight = mSlideMode == MODE_SLIDE_WINDOW ? STATUS_BAR_HEIGHT : 0;
        for (int index = 0; index < count; index++) {
            View child = getChildAt(index);
            int measureWidth = child.getMeasuredWidth();
            int measureHeight = child.getMeasuredHeight();
            SlideMenuLayoutParams layoutParams = (SlideMenuLayoutParams) child.getLayoutParams();
            int leftOffset = (int) ((measureWidth - mCurrentContentOffset) * mScrollScale);
            int rightOffect = (int) ((mCurrentContentOffset + measureWidth) * mScrollScale);
            switch (layoutParams.role) {
            case SlideMenuLayoutParams.ROLE_CONTENT:
                // we should display the content in front of all other views
                child.bringToFront();
                child.layout(mCurrentContentOffset + paddingLeft, paddingTop, paddingLeft + measureWidth
                        + mCurrentContentOffset, paddingTop + measureHeight);
                break;
            case SlideMenuLayoutParams.ROLE_LEFT_MENU:
                mContentBoundsRight = measureWidth;
                child.layout(paddingLeft - leftOffset, statusBarHeight + paddingTop, paddingLeft + measureWidth
                        - leftOffset, statusBarHeight + paddingTop + measureHeight);
                break;
            case SlideMenuLayoutParams.ROLE_RIGHT_MENU:
                mContentBoundsLeft = -measureWidth;
                child.layout(r - paddingRight - measureWidth + rightOffect, statusBarHeight + paddingTop, r
                        - paddingRight + rightOffect, statusBarHeight + t + measureHeight);
                break;
            default:
                continue;
            }
        }
    }

    /**
     * 检测子控件是否能够滑动
     */
    protected final boolean canChildScroll(View v, int dx, int x, int y) {
        if (v instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) v;
            int scrollX = v.getScrollX();
            int scrollY = v.getScrollY();

            int childCount = viewGroup.getChildCount();
            for (int index = 0; index < childCount; index++) {
                View child = viewGroup.getChildAt(index);
                int left = child.getLeft();
                int top = child.getTop();
                if (x + scrollX >= left
                        && x + scrollX < child.getRight()
                        && y + scrollY >= top
                        && y + scrollY < child.getBottom()
                        && View.VISIBLE == child.getVisibility()
                        && (ScrollDetectorManager.canScrollHorizontal(child, dx) || canChildScroll(child, dx, x
                                + scrollX - left, y + scrollY - top))) {
                    return true;
                }
            }
        }

        return ViewCompat.canScrollHorizontally(v, -dx);
    }

    /**
     * 获取左侧菜单阴影宽度
     * 
     * @return
     */
    public float getLeftShadowWidth() {
        return mLeftShadowWidth;
    }

    /**
     * 设置左侧菜单阴影宽度
     * 
     * @param shadowWidth
     */
    public void setLeftShadowWidth(float shadowWidth) {
        this.mLeftShadowWidth = shadowWidth;
        invalidate();
    }

    /**
     * 获取右侧菜单阴影宽度
     * 
     * @return
     */
    public float getRightShadowWidth() {
        return mRightShadowWidth;
    }

    /**
     * 设置右侧菜单阴影宽度
     * 
     * @param shadowWidth
     */
    public void setRightShadowWidth(float shadowWidth) {
        this.mRightShadowWidth = shadowWidth;
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawShadow(canvas);
    }

    /**
     * 绘制菜单阴影
     * 
     * @param canvas
     */
    private void drawShadow(Canvas canvas) {
        if (null == mContent) {
            return;
        }
        int left = mContent.getLeft();
        int width = mWidth;
        int height = mHeight;
        mLeftShadowDrawable.setBounds((int) (left - mLeftShadowWidth), 0, left, height);
        mLeftShadowDrawable.draw(canvas);

        mRightShadowDrawable.setBounds(left + width, 0, (int) (width + left + mRightShadowWidth), height);
        mRightShadowDrawable.draw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        if (mIsPendingResolveSlideMode) {
            resolveSlideMode();
        }
    }

    @Override
    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        SlideMenuLayoutParams layoutParams = new SlideMenuLayoutParams(getContext(), attrs);
        return layoutParams;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.primaryShadowWidth = mLeftShadowWidth;
        savedState.secondaryShadaryWidth = mRightShadowWidth;
        savedState.menuStyle = mMenuStyle;
        savedState.slideMode = mSlideMode;
        savedState.currentState = mCurrentState;
        savedState.currentContentOffset = mCurrentContentOffset;
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mLeftShadowWidth = savedState.primaryShadowWidth;
        mRightShadowWidth = savedState.secondaryShadaryWidth;
        mMenuStyle = savedState.menuStyle;
        setSlideMode(savedState.slideMode);
        mCurrentState = savedState.currentState;
        mCurrentContentOffset = savedState.currentContentOffset;

        invalidateMenuState();
        requestLayout();
        invalidate();
    }

    /**
     * 设置菜单宽度
     * 
     * @param menuWidth
     */
    public void setMenuWidth(int menuWidth) {
        this.mMenuWidth = menuWidth;
        invalidate();
    }

    /**
     * 状态保存类，用于存放要保存的状态
     * 
     * @author Morgan.Ji
     * @version 1.0
     * @date 2015-12-06
     */
    public static class SavedState extends BaseSavedState {

        public float primaryShadowWidth;
        public float secondaryShadaryWidth;
        public int menuStyle;
        public int slideMode;
        public int currentState;
        public int currentContentOffset;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            primaryShadowWidth = in.readFloat();
            secondaryShadaryWidth = in.readFloat();
            menuStyle = in.readInt();
            slideMode = in.readInt();
            currentState = in.readInt();
            currentContentOffset = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeFloat(primaryShadowWidth);
            out.writeFloat(secondaryShadaryWidth);
            out.writeInt(menuStyle);
            out.writeInt(slideMode);
            out.writeInt(currentState);
            out.writeInt(currentContentOffset);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    /**
     * 滑动菜单布局参数，多了一个当前布局角色字段{@link #SlidingMenu}
     */
    public static class SlideMenuLayoutParams extends MarginLayoutParams {

        public final static int ROLE_CONTENT = 0;
        public final static int ROLE_LEFT_MENU = 1;
        public final static int ROLE_RIGHT_MENU = 2;

        public int role;

        public SlideMenuLayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);

            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlideMenu, 0, 0);
            role = a.getInt(R.styleable.SlideMenu_layout_role, -1);

            switch (role) {
            // content should match whole SlidingMenu
            case ROLE_CONTENT:
                width = MATCH_PARENT;
                height = MATCH_PARENT;
                break;
            case ROLE_RIGHT_MENU:
            case ROLE_LEFT_MENU:
                height = MATCH_PARENT;
                break;
            default:
                throw new IllegalArgumentException("You must specified a role for this view");
            }
            a.recycle();
        }

        public SlideMenuLayoutParams(int width, int height) {
            super(width, height);
        }

        public SlideMenuLayoutParams(int width, int height, int role) {
            super(width, height);
            this.role = role;
        }

        public SlideMenuLayoutParams(android.view.ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);

            if (layoutParams instanceof SlideMenuLayoutParams) {
                role = ((SlideMenuLayoutParams) layoutParams).role;
            }
        }
    }

    /**
     * 滑动状态变化监听器
     * 
     * @author Morgan.Ji
     * @version 1.0
     * @date 2015-12-06
     */
    public interface OnSlideStateChangeListener {

        /**
         * 滑动状态变化
         * 
         * @param slideState
         *            {@link SlideMenu#STATE_CLOSE}，{@link SlideMenu#STATE_DRAG}
         *            , {@link SlideMenu#STATE_SCROLL}
         */
        public void onSlideStateChange(int slideState);

        /**
         * 滑动偏距比例变化时调用
         * 
         * @param offsetPercent
         *            负值标示向右滑动，正值标示向左滑动
         */
        public void onSlideOffsetPercentChange(float offsetPercent);
    }
}
