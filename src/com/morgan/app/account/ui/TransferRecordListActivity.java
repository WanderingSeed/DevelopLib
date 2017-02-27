package com.morgan.app.account.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.morgan.app.account.adapter.TransferRecordAdapter;
import com.morgan.app.account.data.DataBaseManager;
import com.morgan.app.account.data.StockDataManager;
import com.morgan.app.account.model.TransferRecord;
import com.morgan.app.account.model.TransferType;
import com.morgan.lib.base.BaseActionBarActivity;
import com.morgan.main.R;

/**
 * 转账记录列表，可传入一个整形参数{@link #TRANSFER_RECORD_LIST_TYPE}
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2015年8月8日
 */
public class TransferRecordListActivity extends BaseActionBarActivity {

    /**
     * 列表显示类型,其中{@link #TRANSFER_RECORD_LIST_TYPE_ALL}为全部，{@link #TRANSFER_RECORD_LIST_TYPE_IN}为转入，
     * {@link #TRANSFER_RECORD_LIST_TYPE_OUT}为转出
     */
    public static final String TRANSFER_RECORD_LIST_TYPE = "transfer_record_list_type";
    /**
     * 当前列表显示的是全部转账记录
     */
    public static final int TRANSFER_RECORD_LIST_TYPE_ALL = 0x00;
    /**
     * 当前列表显示的是全部转入记录
     */
    public static final int TRANSFER_RECORD_LIST_TYPE_IN = 0x01;
    /**
     * 当前列表显示的是全部转出记录
     */
    public static final int TRANSFER_RECORD_LIST_TYPE_OUT = 0x02;
    /**
     * 添加转入记录的请求代码
     */
    private static final int ADD_TRANSFER_RECORD_REQUEST_CODE = 0x00;
    private int mTransferRecordListType = 0;// 此类型和TransferType不同
    private TextView mTotalTransferTxt;
    private TextView mTotalTransferTypeTxt;
    private ListView mTransferrRecordListView;
    private List<TransferRecord> mTransferRecords = new ArrayList<TransferRecord>();
    private TransferRecordAdapter mTransferRecordAdapter;
    private ProgressDialog mProgressDialog;
    private RadioGroup mRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_account_activity_transfer_record_list);
        setTitle(R.string.transfer_record_list);
        if (null != getIntent()) {
            mTransferRecordListType = getIntent().getIntExtra(TRANSFER_RECORD_LIST_TYPE, 0);
        }
        initView();
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        mProgressDialog = new ProgressDialog(TransferRecordListActivity.this);
        mRadioGroup = (RadioGroup) findViewById(R.id.transfer_type_radioGroup);
        mTransferrRecordListView = (ListView) findViewById(R.id.transfer_record_listView);
        mTotalTransferTxt = (TextView) findViewById(R.id.txt_total_transfer);
        mTotalTransferTypeTxt = (TextView) findViewById(R.id.txt_total_transfer_record_type);
        mTransferRecordAdapter = new TransferRecordAdapter(TransferRecordListActivity.this, mTransferRecords);
        mTransferrRecordListView.setAdapter(mTransferRecordAdapter);

        switch (mTransferRecordListType) {
        case TRANSFER_RECORD_LIST_TYPE_ALL:
            mRadioGroup.check(R.id.transfer_all_radioButton);
            break;
        case TRANSFER_RECORD_LIST_TYPE_IN:
            mRadioGroup.check(R.id.transfer_in_radioButton);
            break;
        case TRANSFER_RECORD_LIST_TYPE_OUT:
            mRadioGroup.check(R.id.transfer_out_radioButton);
            break;
        default:
            break;
        }
        mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                case R.id.transfer_all_radioButton:
                    mTransferRecordListType = TRANSFER_RECORD_LIST_TYPE_ALL;
                    break;
                case R.id.transfer_in_radioButton:
                    mTransferRecordListType = TRANSFER_RECORD_LIST_TYPE_IN;
                    break;
                case R.id.transfer_out_radioButton:
                    mTransferRecordListType = TRANSFER_RECORD_LIST_TYPE_OUT;
                    break;
                default:
                    break;
                }
                loadTransferRecordList();
                updateTransferTotalView();
            }
        });
        // TODO: 使用异步线程
        loadTransferRecordList();
        updateTransferTotalView();
    }

    /**
     * 加载转账记录列表
     */
    private void loadTransferRecordList() {
        mTransferRecords.clear();
        switch (mTransferRecordListType) {
        case TRANSFER_RECORD_LIST_TYPE_ALL:
            mTransferRecords.addAll(DataBaseManager.getInstance().getTransferRecordList());
            break;
        case TRANSFER_RECORD_LIST_TYPE_IN:
            mTransferRecords.addAll(DataBaseManager.getInstance().getTransferInRecordList());
            break;
        case TRANSFER_RECORD_LIST_TYPE_OUT:
            mTransferRecords.addAll(DataBaseManager.getInstance().getTransferOutRecordList());
            break;
        default:
            break;
        }
        mTransferRecordAdapter.notifyDataSetChanged();
    }

    /**
     * 更新总转账记录
     */
    public void updateTransferTotalView() {
        switch (mTransferRecordListType) {
        case TRANSFER_RECORD_LIST_TYPE_ALL:
            mTotalTransferTypeTxt.setText(R.string.total_investment);
            mTotalTransferTxt.setText(String.valueOf(StockDataManager.getInstance().getTotalInvestment()));
            break;
        case TRANSFER_RECORD_LIST_TYPE_IN:
            mTotalTransferTypeTxt.setText(R.string.total_transfer_in);
            mTotalTransferTxt.setText(String.valueOf(StockDataManager.getInstance().getTotalTransferIn()));
            break;
        case TRANSFER_RECORD_LIST_TYPE_OUT:
            mTotalTransferTypeTxt.setText(R.string.total_transfer_out);
            mTotalTransferTxt.setText(String.valueOf(StockDataManager.getInstance().getTotalTransferOut()));
            break;
        default:
            break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_account_transfer_record_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
        case R.id.action_add:
            Intent intent = new Intent(TransferRecordListActivity.this, AddTransferRecordActivity.class);
            switch (mTransferRecordListType) {
            case TRANSFER_RECORD_LIST_TYPE_ALL:
                break;
            case TRANSFER_RECORD_LIST_TYPE_IN:
                intent.putExtra(AddTransferRecordActivity.TRANSFER_TYPE, TransferType.IN.getValue());
                break;
            case TRANSFER_RECORD_LIST_TYPE_OUT:
                intent.putExtra(AddTransferRecordActivity.TRANSFER_TYPE, TransferType.OUT.getValue());
                break;
            default:
                break;
            }
            startActivityForResult(intent, ADD_TRANSFER_RECORD_REQUEST_CODE);
            break;
        case R.id.action_compute:
            startActivity(new Intent(TransferRecordListActivity.this, ComputeActivity.class));
            break;
        case R.id.action_export:
            mProgressDialog.setMessage(getString(R.string.msg_data_export_now));
            mProgressDialog.show();
            ExportTask exportTask = new ExportTask();
            exportTask.execute();
            break;
        case R.id.action_import:
            mProgressDialog.setMessage(getString(R.string.msg_data_import_now));
            mProgressDialog.show();
            ImportTask importTask = new ImportTask();
            importTask.execute();
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case ADD_TRANSFER_RECORD_REQUEST_CODE:
                loadTransferRecordList();
                updateTransferTotalView();
                break;
            default:
                break;
            }
        }
    }

    /**
     * 数据库数据导出到SD上
     * 
     * @author Morgan.Ji
     * @version 1.0
     * @date 2015年8月30日
     */
    private class ExportTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Thread.sleep(1000);// 防止过快导致进度条闪烁
            } catch (Exception e) {
            }
            return DataBaseManager.getInstance().exportData();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(TransferRecordListActivity.this, R.string.msg_data_export_success, Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(TransferRecordListActivity.this, R.string.msg_data_export_fail, Toast.LENGTH_SHORT)
                        .show();
            }
            mProgressDialog.hide();
            super.onPostExecute(result);
        }

    }

    /**
     * SD卡上的数据导入到数据库
     * 
     * @author Morgan.Ji
     * @version 1.0
     * @date 2015年8月30日
     */
    private class ImportTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Thread.sleep(1000);// 防止过快导致进度条闪烁
            } catch (Exception e) {
            }
            return DataBaseManager.getInstance().importData();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.hide();
            if (result) {
                loadTransferRecordList();
                updateTransferTotalView();
                Toast.makeText(TransferRecordListActivity.this, R.string.msg_data_import_success, Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(TransferRecordListActivity.this, R.string.msg_data_import_fail, Toast.LENGTH_SHORT)
                        .show();
            }
            super.onPostExecute(result);
        }
    }
}
