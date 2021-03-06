package com.morgan.lib.base;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * 带有ActionBar的FragmentActivity的基类，并添加了ActionBar中返回键的响应事件。
 * 
 * @author JiGuoChao
 * @version 1.0
 * @date 2014-7-15
 */
public class BaseActionBarFragmentActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}
