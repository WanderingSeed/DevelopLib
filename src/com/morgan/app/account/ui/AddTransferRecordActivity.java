package com.morgan.app.account.ui;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.morgan.app.account.data.DataBaseManager;
import com.morgan.app.account.model.TransferRecord;
import com.morgan.app.account.model.TransferType;
import com.morgan.lib.base.BaseActionBarActivity;
import com.morgan.lib.util.DateUtils;
import com.morgan.main.R;

/**
 * 添加资金转账记录界面,进入该界面时如果传入整形参数{@link #TRANSFER_TYPE}，则转账类型不再可编辑，其中0：转入，1：转出
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2015年7月30日
 */
public class AddTransferRecordActivity extends BaseActionBarActivity {

    /**
     * 转账类型
     */
    public static final String TRANSFER_TYPE = "transfer_type";
    private Spinner mTransferTypeSpinner;
    private String[] mTransferTypeArray;
    private TransferType mCurrentType = TransferType.IN;
    private TextView mTransferDateTxt;
    private TextView mTransferTimeTxt;
    private EditText mTransferValueEditText;
    private Button mSubmitBtn;
    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private OnDateSetListener mDateSetListener;
    private OnTimeSetListener mTimeSetListener;
    private int mYear, mMonth, mDay, mHour, mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_activity_add_transfer_record);
        initView();
        Intent intent = getIntent();
        if (null != intent && intent.hasExtra(TRANSFER_TYPE)) {
            int index = intent.getIntExtra(TRANSFER_TYPE, TransferType.IN.getValue());
            mCurrentType = TransferType.valueOf(index);
            mTransferTypeSpinner.setSelection(index);
        }
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        mTransferTypeArray = getResources().getStringArray(R.array.transfer_type);
        mTransferTypeSpinner = (Spinner) findViewById(R.id.transfer_type_spinner);
        mTransferDateTxt = (TextView) findViewById(R.id.transfer_date_textView);
        mTransferTimeTxt = (TextView) findViewById(R.id.transfer_time_textView);
        mTransferValueEditText = (EditText) findViewById(R.id.transfer_money_editText);
        mSubmitBtn = (Button) findViewById(R.id.btn_submit);

        Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH) + 1;
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
        updateDateView();

        mTransferTypeSpinner.setAdapter(new ArrayAdapter<String>(AddTransferRecordActivity.this,
                android.R.layout.simple_list_item_1, mTransferTypeArray));
        mTransferTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentType = TransferType.valueOf(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mDateSetListener = new OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mYear = year;
                mMonth = monthOfYear + 1;
                mDay = dayOfMonth;
                updateDateView();
            }
        };
        mTransferDateTxt.setClickable(true);
        mTransferDateTxt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null == mDatePickerDialog) {
                    mDatePickerDialog = new DatePickerDialog(AddTransferRecordActivity.this, mDateSetListener, mYear,
                            mMonth - 1, mDay);
                }
                mDatePickerDialog.show();
            }
        });
        mTimeSetListener = new OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mHour = hourOfDay;
                mMinute = minute;
                updateDateView();
            }
        };
        mTransferTimeTxt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null == mTimePickerDialog) {
                    mTimePickerDialog = new TimePickerDialog(AddTransferRecordActivity.this, mTimeSetListener, mHour,
                            mMinute, true);
                }
                mTimePickerDialog.show();
            }
        });
        mSubmitBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String transferValue = mTransferValueEditText.getText().toString().trim();
                if (TextUtils.isEmpty(transferValue)) {
                    Toast.makeText(AddTransferRecordActivity.this, R.string.msg_value_can_not_be_zero,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                // 插入一条数据到数据库，由于所需时间较短，暂时不用异步线程
                TransferRecord record = new TransferRecord();
                record.setType(mCurrentType);
                record.setId(getRecordId());
                record.setMoney(Float.valueOf(transferValue));
                record.setTime(getSelectedDate());
                if (DataBaseManager.getInstance().addTransferRecord(record)) {
                    Toast.makeText(AddTransferRecordActivity.this, R.string.msg_add_success, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddTransferRecordActivity.this, R.string.msg_add_fail, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 更新日期控件
     */
    private void updateDateView() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(mYear, mMonth - 1, mDay, mHour, mMinute);
        mTransferDateTxt.setText(DateUtils.dateToString(calendar.getTime(), DateUtils.FORMAT_DATE_ONLY));
        mTransferTimeTxt.setText(DateUtils.dateToString(calendar.getTime(), DateUtils.FORMAT_TIME_ONLY));
    }

    /**
     * 获取选择的时间
     * 
     * @return
     */
    private String getSelectedDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(mYear, mMonth - 1, mDay, mHour, mMinute);
        return DateUtils.dateToString(calendar.getTime(), DateUtils.FORMAT_DATE);
    }

    /**
     * 获取新增记录的Id
     * 
     * @return
     */
    private String getRecordId() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(mYear, mMonth - 1, mDay, mHour, mMinute);
        return String.valueOf(calendar.getTimeInMillis());
    }
}
