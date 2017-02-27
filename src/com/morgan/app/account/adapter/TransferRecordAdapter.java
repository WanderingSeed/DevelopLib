package com.morgan.app.account.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.morgan.app.account.model.TransferRecord;
import com.morgan.app.account.model.TransferType;
import com.morgan.main.R;

/**
 * 转账记录列表适配器
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2016-02-21
 */
public class TransferRecordAdapter extends BaseAdapter {

    private List<TransferRecord> mRecords = new ArrayList<TransferRecord>();
    private Context mContext;

    public TransferRecordAdapter(Context context, List<TransferRecord> records) {
        this.mContext = context;
        this.mRecords = records;
    }

    @Override
    public int getCount() {
        return mRecords.size();
    }

    @Override
    public Object getItem(int position) {
        return mRecords.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (null == convertView) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.app_account_transfer_record_list_item, null);
            viewHolder.mTransferDateTxt = (TextView) convertView.findViewById(R.id.transfer_date_textView);
            viewHolder.mTransferMoneyTxt = (TextView) convertView.findViewById(R.id.transfer_money_textView);
            viewHolder.mTransferTypeTxt = (TextView) convertView.findViewById(R.id.transfer_type_textView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        TransferRecord record = mRecords.get(position);
        viewHolder.mTransferDateTxt.setText(record.getTime());
        viewHolder.mTransferMoneyTxt.setText(String.valueOf(record.getMoney()));
        viewHolder.mTransferTypeTxt.setText(record.getType() == TransferType.IN ? mContext.getResources().getString(
                R.string.transfer_in) : mContext.getResources().getString(R.string.transfer_out));
        return convertView;
    }

    class ViewHolder {
        TextView mTransferDateTxt;
        TextView mTransferMoneyTxt;
        TextView mTransferTypeTxt;
    }
}
