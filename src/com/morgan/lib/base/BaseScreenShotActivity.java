package com.morgan.lib.base;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;

import com.morgan.lib.widget.ScreenShotView;
import com.morgan.lib.widget.ScreenShotView.OnScreenShotListener;

/**
 * 提供截屏功能的Activity的基类,需要用到截屏自定义控件。
 * 
 * @author Morgan.Ji
 * 
 * @version 1.0
 * 
 * @date 2014年7月9日
 */
public abstract class BaseScreenShotActivity extends BaseActivity implements OnScreenShotListener {

    // 是否允许销毁
    private boolean mAllowDestroy = true;
    // 截屏的View
    private ScreenShotView mScreenShotView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 如果当前正在处于截屏的过程，则按back键只是退出截图功能。
        if (keyCode == KeyEvent.KEYCODE_BACK && !mAllowDestroy) {
            this.mScreenShotView.dismiss();
            mAllowDestroy = true;
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void startScreenShot() {
        if (mScreenShotView == null) {
            mScreenShotView = new ScreenShotView(BaseScreenShotActivity.this, this);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            getWindow().addContentView(mScreenShotView, lp);
        }
        mAllowDestroy = false;
        mScreenShotView.start();
    }
}
