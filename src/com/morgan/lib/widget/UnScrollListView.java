package com.morgan.lib.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * 一个可以嵌套在scrollView中的ListView
 * 
 * @author Morgan.ji
 * @version 1.0
 * @date 2016-05-06
 */
public class UnScrollListView extends ListView {
    public UnScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UnScrollListView(Context context) {
        super(context);
    }

    public UnScrollListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
