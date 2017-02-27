package com.morgan.main.adapter;

import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 主页面列表的适配器。
 * 
 * @author JiGuoChao
 * @version 1.0
 * @date 2014-7-18
 */
public class HomePageListAdapter extends BaseAdapter {

    private List<ResolveInfo> mData;
    private LayoutInflater mInflater;
    private PackageManager mPackageManager;

    public HomePageListAdapter(Context context, List<ResolveInfo> data, PackageManager packageManager) {
        mData = data;
        mInflater = LayoutInflater.from(context);
        mPackageManager = packageManager;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public ResolveInfo getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ResolveInfo resolveInfo = getItem(position);
        if (null == convertView) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        ((TextView) convertView).setText(resolveInfo.loadLabel(mPackageManager)); // 把Mainfest中声明Acivity的label作为该Activity的名称
        return convertView;
    }
}
