package com.morgan.lib.util;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.morgan.lib.base.BaseApplication;

/**
 * 提供软键盘相关的实用方法。
 * 
 * @author Morgan.ji
 * @version 1.0
 * @date 2016-05-02
 */
public class KeyBoardUtils {

    /**
     * 弹出软键盘
     * 
     * @param editText
     */
    public static void showKeyBoard(final EditText editText) {
        if (editText == null) {
            return;
        }
        editText.requestFocus();
        editText.post(new Runnable() {
            public void run() {
                InputMethodManager imm = (InputMethodManager) BaseApplication.getContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.RESULT_UNCHANGED_SHOWN);
            }

        });
    }

    /**
     * 隐藏软键盘
     * 
     * @param editText
     */
    public static void hideSoftInput(final EditText editText) {
        if (editText == null) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) BaseApplication.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputMethodManager
                .hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
}
