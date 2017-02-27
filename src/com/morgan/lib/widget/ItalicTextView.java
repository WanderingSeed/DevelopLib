package com.morgan.lib.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 一个显示粗体斜体字的TextView
 * 
 * @author Morgan.ji
 * @version 1.0
 * @date 2016-05-06
 */
public class ItalicTextView extends TextView {

    public ItalicTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(Typeface.MONOSPACE, Typeface.ITALIC);
        TextPaint paint = this.getPaint();
        paint.setFakeBoldText(true);
    }

}
