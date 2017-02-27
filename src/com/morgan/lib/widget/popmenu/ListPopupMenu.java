package com.morgan.lib.widget.popmenu;

import java.util.ArrayList;
import java.util.List;

import com.morgan.main.R;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * 一个基于View的弹出的下拉菜单（对PopupWindow的封装，内容为一个ListView）。
 * 
 * @author Morgan.ji
 * @version 1.0
 * @date 2016-05-02
 */
public class ListPopupMenu {

    private Activity mContext;
    private ListView mListView;
    private PopupWindow mPopupWindow;
    private List<MenuItem> mPopupList = new ArrayList<MenuItem>();
    private PopupListAdapter mPopupListAdapter;

    public ListPopupMenu(Activity context) {
        this(context, 0);
    }

    public ListPopupMenu(Activity context, int width) {
        this.mContext = context;
        if (width <= 0) {
            DisplayMetrics metrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            if (metrics.widthPixels >= 720) {
                width = 220;
            } else {
                width = 180;
            }
        }

        View view = LayoutInflater.from(context).inflate(R.layout.lib_list_popup_menu_list, null);
        mListView = (ListView) view.findViewById(R.id.list);
        mPopupListAdapter = new PopupListAdapter();
        mListView.setAdapter(mPopupListAdapter);
        mListView.setFocusableInTouchMode(true);
        mListView.setFocusable(true);

        mPopupWindow = new PopupWindow(view, width, LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(android.R.color.white));
    }

    /**
     * 显示弹窗内容
     * 
     * @param parent
     */
    public void showAsDropDown(View parent) {
        mPopupWindow.showAsDropDown(parent,
                mContext.getResources().getDimensionPixelSize(R.dimen.list_popup_menu_xoff), mContext.getResources()
                        .getDimensionPixelSize(R.dimen.list_popup_menu_yoff));
        mPopupWindow.setAnimationStyle(R.style.animation_list_popup_menu);
        mPopupWindow.update();
    }

    /**
     * 设置弹窗菜单内容
     * 
     * @param popupList
     */
    public void setItems(List<MenuItem> popupList) {
        mPopupList.clear();
        mPopupList.addAll(popupList);
        mPopupListAdapter.notifyDataSetChanged();
    }

    public void addItem(MenuItem popup) {
        mPopupList.add(popup);
        mPopupListAdapter.notifyDataSetChanged();
    }

    /**
     * 隐藏弹窗
     */
    public void dismiss() {
        mPopupWindow.dismiss();
    }

    /**
     * 设置列表菜单的单击事件
     * 
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListView.setOnItemClickListener(listener);
    }

    class PopupListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mPopupList.size();
        }

        @Override
        public Object getItem(int position) {
            return mPopupList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.lib_list_popup_menu_list_item, null);
                holder.mImgView = (ImageView) convertView.findViewById(R.id.popup_img);
                holder.mTextView = (TextView) convertView.findViewById(R.id.popup_txt);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            MenuItem popup = mPopupList.get(position);
            if (popup.isHideImg()) {
                holder.mImgView.setVisibility(View.GONE);
            } else {
                holder.mImgView.setVisibility(View.VISIBLE);
                holder.mImgView.setImageResource(popup.getResId());
            }
            holder.mTextView.setText(popup.getText());
            return convertView;
        }

        class ViewHolder {
            public ImageView mImgView;
            public TextView mTextView;
        }
    }

    /**
     * 换掉某个位置菜单的图片
     * 
     * @param position
     * @param resourceId
     */
    public void changeItemImage(int position, int resourceId) {
        if (position < 0 || position >= mPopupList.size()) {
            return;
        }
        mPopupList.get(position).setResId(resourceId);
        mPopupListAdapter.notifyDataSetChanged();
    }
}
