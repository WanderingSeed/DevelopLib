package com.morgan.app.account.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.morgan.lib.base.BaseActionBarActivity;
import com.morgan.lib.util.StrUtils;
import com.morgan.main.R;

/**
 * 价格快速计算界面，输入一个价格，自动计算出其一定百分比的价格
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2015年8月30日
 */
public class ComputeActivity extends BaseActionBarActivity {

    private EditText mInputEditText;
    private TextView mGainTwoTextView;
    private TextView mGainThreeTextView;
    private TextView mGainFourTextView;
    private TextView mGainFiveTextView;
    private TextView mGainSixTextView;
    private TextView mGainEightTextView;
    private TextView mGainTenETextView;
    private TextView mLossTwoTextView;
    private TextView mLossThreeTextView;
    private TextView mLossFourTextView;
    private TextView mLossFiveTextView;
    private TextView mLossSixTextView;
    private TextView mLossEightTextView;
    private TextView mLossTenTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_account_activity_compute);
        initView();
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        mInputEditText = (EditText) findViewById(R.id.input_value_editText);
        mGainTwoTextView = (TextView) findViewById(R.id.gain_two_textView);
        mGainThreeTextView = (TextView) findViewById(R.id.gain_three_textView);
        mGainFourTextView = (TextView) findViewById(R.id.gain_four_textView);
        mGainFiveTextView = (TextView) findViewById(R.id.gain_five_textView);
        mGainSixTextView = (TextView) findViewById(R.id.gain_six_textView);
        mGainEightTextView = (TextView) findViewById(R.id.gain_eight_textView);
        mGainTenETextView = (TextView) findViewById(R.id.gain_ten_textView);
        mLossTwoTextView = (TextView) findViewById(R.id.loss_two_textView);
        mLossThreeTextView = (TextView) findViewById(R.id.loss_three_textView);
        mLossFourTextView = (TextView) findViewById(R.id.loss_four_textView);
        mLossFiveTextView = (TextView) findViewById(R.id.loss_five_textView);
        mLossSixTextView = (TextView) findViewById(R.id.loss_six_textView);
        mLossEightTextView = (TextView) findViewById(R.id.loss_eight_textView);
        mLossTenTextView = (TextView) findViewById(R.id.loss_ten_textView);
        mInputEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().trim();
                float value = 0f;
                if (!TextUtils.isEmpty(input)) {
                    value = Float.valueOf(input);
                }
                mGainTwoTextView.setText(StrUtils.toString(value * 1.02));
                mGainThreeTextView.setText(StrUtils.toString(value * 1.03));
                mGainFourTextView.setText(StrUtils.toString(value * 1.04));
                mGainFiveTextView.setText(StrUtils.toString(value * 1.05));
                mGainSixTextView.setText(StrUtils.toString(value * 1.06));
                mGainEightTextView.setText(StrUtils.toString(value * 1.08));
                mGainTenETextView.setText(StrUtils.toString(value * 1.1));
                mLossTwoTextView.setText(StrUtils.toString(value * 0.98));
                mLossThreeTextView.setText(StrUtils.toString(value * 0.97));
                mLossFourTextView.setText(StrUtils.toString(value * 0.96));
                mLossFiveTextView.setText(StrUtils.toString(value * 0.95));
                mLossSixTextView.setText(StrUtils.toString(value * 0.94));
                mLossEightTextView.setText(StrUtils.toString(value * 0.92));
                mLossTenTextView.setText(StrUtils.toString(value * 0.9));
            }
        });
        mInputEditText.setText("0");
    }
}
