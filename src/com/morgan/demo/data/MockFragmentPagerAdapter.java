package com.morgan.demo.data;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * 一个包含pageNumber个测试页面的FragmentPagerAdapter。
 * 
 * @author JiGuoChao
 * @version 1.0
 * @date 2014-7-18
 */
public class MockFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private int pageNumber = 1; // 生成Page的个数。

    public MockFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        return Fragment.instantiate(mContext, MockListFragment.class.getName());
    }

    @Override
    public int getCount() {
        return pageNumber;
    }

}
