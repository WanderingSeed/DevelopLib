package com.morgan.lib.base;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;

import com.morgan.main.R;

/**
 * 应用程序ListActivity的基类，在创建时把自己添加到AppManager中管理，销毁时移除。<br/>
 * 添加了界面右滑进入和右滑退出动画。
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2014-7-15
 */
public class BaseListActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 新建时添加Activity到列表中
        ActivitysManager.getInstance().addActivity(this);
    }

    @Override
    protected void onDestroy() {
        // 结束时把Activity从列表中移除
        ActivitysManager.getInstance().removeActivity(this);
        super.onDestroy();
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.lib_slide_right_in, R.anim.lib_stay);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.lib_slide_right_out);
    }
}
