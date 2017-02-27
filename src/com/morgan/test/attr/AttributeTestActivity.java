package com.morgan.test.attr;

import android.os.Bundle;

import com.morgan.lib.base.BaseActionBarActivity;
import com.morgan.main.R;

/**
 * Android中主题和属性的优先级测试
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2015-11-12
 */
public class AttributeTestActivity extends BaseActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_attribute);
    }
}
