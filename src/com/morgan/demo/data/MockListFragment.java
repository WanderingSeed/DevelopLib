package com.morgan.demo.data;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.widget.ArrayAdapter;

import com.morgan.main.R;

/**
 * 一个包含测试数据的ListFragment。
 * 
 * @author JiGuoChao
 * @version 1.0
 * @date 2014-7-18
 */
public class MockListFragment extends ListFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setBackgroundColor(Color.WHITE);
        setListAdapter(ArrayAdapter.createFromResource(getActivity(), R.array.test_data,
                android.R.layout.simple_list_item_1));
    }
}
