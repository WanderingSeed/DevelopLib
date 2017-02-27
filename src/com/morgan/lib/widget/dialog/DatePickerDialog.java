package com.morgan.lib.widget.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import com.morgan.main.R;

/**
 * 时间选择器
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2016-02-21
 */
public class DatePickerDialog extends AlertDialog {

    private DatePicker mDatePicker;
    private Button mSubmitBtn;
    private DatePickerListener mDatePickerListener;

    protected DatePickerDialog(Context context, DatePickerListener datePickerListener) {
        super(context);
        this.mDatePickerListener = datePickerListener;
        setContentView(R.layout.lib_dialog_date_picker);
        initView();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mDatePicker = (DatePicker) findViewById(R.id.datePicker);
        mSubmitBtn = (Button) findViewById(R.id.btn_submit);
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null != mDatePickerListener) {
                    mDatePickerListener.onCheckDate(mDatePicker.getCalendarView().getDate());
                } else {
                    cancel();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (null != mDatePickerListener) {
            mDatePickerListener.onCancel();
        }
        super.onBackPressed();
    }

    /**
     * 日期选择对话框监听器
     * 
     * @author Morgan.Ji
     * @version 1.0
     * @date 2015年7月30日
     */
    public interface DatePickerListener {

        /**
         * 取消对话框
         */
        public void onCancel();

        /**
         * 选择一个日期
         * 
         * @param time
         */
        public void onCheckDate(long time);
    }
}
