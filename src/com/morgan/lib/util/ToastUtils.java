package com.morgan.lib.util;

import com.morgan.lib.base.BaseApplication;

import android.content.Context;
import android.widget.Toast;

/**
 * 用于显示一些toast
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2016-01-12
 */
public class ToastUtils {

    /**
     * 较短时间显示Toast
     * 
     * @param context
     *            上下文环境
     * @param text
     *            文本id
     */
    public static void toast(int text) {
        Toast.makeText(BaseApplication.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 较长时间显示Toast
     * 
     * @param context
     *            上下文环境
     * @param text
     *            文本id
     */
    public static void toastLong(Context context, int text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    /**
     * 较短时间显示Toast
     * 
     * @param context
     *            上下文环境
     * @param text
     *            文本id
     */
    public static void toastShort(Context context, int text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
