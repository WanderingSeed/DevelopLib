package com.morgan.main.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.morgan.lib.base.BaseActivity;
import com.morgan.lib.base.BaseApplication;
import com.morgan.lib.widget.slidemenu.SlideMenu;
import com.morgan.main.DevelopLibApplication;
import com.morgan.main.R;
import com.morgan.main.adapter.HomePageListAdapter;
import com.morgan.main.data.Constants;

/**
 * 应用主界面
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2015-12-09
 */
public class MainActivity extends BaseActivity {

    private long mFirstBackTime = 0; // 记录第一次按返回键时间，连续按两次返回键退出程序
    private PackageManager mPackageManager;
    protected SlideMenu mSlideMenu;
    private ViewPager mContextViewPager;// 中间内容
    private PagerTabStrip mContextTab;
    private List<View> mViews = new ArrayList<View>();
    private String[] mTitles = new String[0];
    private ListView mToolsListView; // 存放应用
    private ListView mAppListView; // 存放工具
    private ListView mLeftListView;// 左侧内容，存放测试
    private ListView mRightListView;// 右侧内容，存放示例

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initListener();
        ((BaseApplication)getApplicationContext()).init();
    }

    private void initView() {
        mSlideMenu = (SlideMenu) findViewById(R.id.slideMenu);
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mSlideMenu.setMenuWidth((int)(point.x * 0.8));//菜单弄宽一些
        
        mContextViewPager = (ViewPager) findViewById(R.id.context_Viewpager);
        mContextTab = (PagerTabStrip) findViewById(R.id.context_Tabstrip);
        // 取消tab下面的长横线
        mContextTab.setDrawFullUnderline(false);
        // 设置tab的背景色
        // mContextTab.setBackgroundColor(getResources().getColor(R.color.white));
        // 设置当前tab页签的下划线颜色
        // mContextTab.setTabIndicatorColor(getResources().getColor(R.color.red));
        mContextTab.setTextSpacing(40);

        mLeftListView = (ListView) findViewById(R.id.leftMenu_listView);
        mRightListView = (ListView) findViewById(R.id.rightMenu_listView);
        mToolsListView = (ListView) LayoutInflater.from(this).inflate(R.layout.layout_listview, null);
        mAppListView = (ListView) LayoutInflater.from(this).inflate(R.layout.layout_listview, null);

        mViews.add(mToolsListView);
        mViews.add(mAppListView);
    }

    private void initData() {
        mTitles = getResources().getStringArray(R.array.context_tab_titles);

        mPackageManager = getPackageManager();
        List<ResolveInfo> appActivities = mPackageManager.queryIntentActivities(createIntent(Constants.ACTION_APP),
                PackageManager.GET_META_DATA);
        mAppListView.setAdapter(new HomePageListAdapter(this, appActivities, mPackageManager));
        List<ResolveInfo> toolActivities = mPackageManager.queryIntentActivities(createIntent(Constants.ACTION_TOOL),
                PackageManager.GET_META_DATA);
        mToolsListView.setAdapter(new HomePageListAdapter(this, toolActivities, mPackageManager));
        List<ResolveInfo> testActivities = mPackageManager.queryIntentActivities(createIntent(Constants.ACTION_TEST),
                PackageManager.GET_META_DATA);
        mLeftListView.setAdapter(new HomePageListAdapter(this, testActivities, mPackageManager));
        List<ResolveInfo> demoActivities = mPackageManager.queryIntentActivities(createIntent(Constants.ACTION_DEMO),
                PackageManager.GET_META_DATA);
        mRightListView.setAdapter(new HomePageListAdapter(this, demoActivities, mPackageManager));
    }

    private void initListener() {
        OnItemClickListener intentItemClickListener = new IntentItemClickListener();
        mAppListView.setOnItemClickListener(intentItemClickListener);
        mToolsListView.setOnItemClickListener(intentItemClickListener);
        mLeftListView.setOnItemClickListener(intentItemClickListener);
        mRightListView.setOnItemClickListener(intentItemClickListener);

        mContextViewPager.setAdapter(new PagerAdapter() {

            @Override
            public int getCount() {
                return mViews.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                ((ViewPager) container).removeView(mViews.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ((ViewPager) container).addView(mViews.get(position));
                return mViews.get(position);
            }

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getItemPosition(Object object) {
                return super.getItemPosition(object);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                if (mTitles.length <= position) {
                    return "";
                }
                return mTitles[position];
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * 创建一个包含指定Action的的Intent。
     * 
     * @return
     */
    public Intent createIntent(String action) {
        return new Intent(action);
    }

    @Override
    public void onBackPressed() {
        // 两次返回键按键间隔在3秒内则退出程序
        if ((System.currentTimeMillis() - mFirstBackTime) > 3000) {
            Toast.makeText(getApplicationContext(), R.string.tip_another_click_exit_app, Toast.LENGTH_SHORT).show();
            mFirstBackTime = System.currentTimeMillis();
        } else {
            DevelopLibApplication.exit();
        }
    }

    /**
     * 一个由ResolveInfo组成的列表中的每项点击事件，跳转到这个Intent的Activity
     * 
     * @author Morgan.Ji
     * @version 1.0
     * @date 2015-12-20
     */
    private class IntentItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // 关闭菜单，下次返回还在主界面
            mSlideMenu.close(false);
            ActivityInfo resolveInfo = ((ResolveInfo) parent.getItemAtPosition(position)).activityInfo;
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(resolveInfo.packageName, resolveInfo.name));
            startActivity(intent);
        }

    }
}
