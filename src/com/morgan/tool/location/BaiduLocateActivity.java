package com.morgan.tool.location;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.morgan.lib.base.BaseActionBarActivity;
import com.morgan.lib.util.Logger;
import com.morgan.main.R;

/**
 * 百度定位功能界面
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2016-02-20
 */
public class BaiduLocateActivity extends BaseActionBarActivity {

    private static final String TAG = BaiduLocateActivity.class.getName();
    private MapView mMapView;
    private Button mLocateBtn;
    private boolean mLocating;
    private LocationClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tool_activity_baidu_locate);
        mLocateBtn = (Button) findViewById(R.id.locate_btn);
        // 获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapView.getMap().setMyLocationEnabled(true);

        mClient = new LocationClient(this);
        // 设置定位参数包括：定位模式（单次定位，定时定位），返回坐标类型，是否打开GPS等等
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("gcj02");
        option.setScanSpan(1000);
        // 发起定位请求。请求过程是异步的，定位结果在上面的监听函数onReceiveLocation中获取。
        mClient.setLocOption(option);
        mClient.registerLocationListener(new BDLocationListener() {

            @Override
            public void onReceiveLocation(BDLocation location) {
                Logger.i(TAG, "locType  " + location.getLocType());
                MyLocationData locData = new MyLocationData.Builder().direction(100).latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                mMapView.getMap().setMyLocationData(locData);
                mMapView.getMap().setMapStatus(
                        MapStatusUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                mMapView.getMap().setMapStatus(MapStatusUpdateFactory.zoomTo(15));
            }
        });
        mLocateBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mLocating) {
                    mLocating = false;
                    mClient.stop();
                } else {
                    mLocating = true;
                    mClient.start();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        if (mLocating) {
            mLocating = false;
            mClient.stop();
        }
        super.onDestroy();
    }
}
