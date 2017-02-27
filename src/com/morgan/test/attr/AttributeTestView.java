package com.morgan.test.attr;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import com.morgan.lib.util.Logger;
import com.morgan.main.R;

/**
 * 用于属性测试的View，因为是使用xml来声明，系统来创建的View， <br/>
 * 所以系统会调用第一个构造函数，第一个构造函数会调用第二个构造函数, <br/>
 * 最后打印出这个View使用的属性值
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2015-11-12
 */
public class AttributeTestView extends View {

    private static final String TAG = "AttributeTest";

    public AttributeTestView(Context context, AttributeSet attrs) {
        // this(context, attrs, 0);// defStyle为0时obtainStyledAttributes第四个参数才会有效
        this(context, attrs, R.attr.CustomizeThemeStyle);
    }

    public AttributeTestView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Customize, defStyle,
                R.style.DefaultCustomizeStyle);
        String one = a.getString(R.styleable.Customize_attr_one);
        String two = a.getString(R.styleable.Customize_attr_two);
        String three = a.getString(R.styleable.Customize_attr_three);
        String four = a.getString(R.styleable.Customize_attr_four);
        String five = a.getString(R.styleable.Customize_attr_five);
        Logger.i(TAG, "one:" + one);
        Logger.i(TAG, "two:" + two);
        Logger.i(TAG, "three:" + three);
        Logger.i(TAG, "four:" + four);
        Logger.i(TAG, "five:" + five);
        a.recycle();
    }
}
