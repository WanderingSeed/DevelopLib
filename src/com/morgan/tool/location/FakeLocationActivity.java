package com.morgan.tool.location;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.morgan.lib.base.BaseActionBarActivity;
import com.morgan.lib.util.Logger;
import com.morgan.lib.util.ToastUtils;
import com.morgan.main.R;

/**
 * 位置模拟功能界面
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2016-01-10
 */
public class FakeLocationActivity extends BaseActionBarActivity {

    private static final String TAG = FakeLocationActivity.class.getName();
    private MapView mMapView;
    private LatLng mLastLatLng, mFakeLatLng;
    private TextView mTipTextView;
    private EditText mCityEditText;
    private EditText mAddressEditText;
    private TextView mAddressTextView;
    private GeoCoder mSearch;
    private Button mFakeBtn, mGeoCodeBtn;
    private boolean mFaking = false;
    private boolean mFirstTimeIn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tool_activity_fake_location);
        mTipTextView = (TextView) findViewById(R.id.tip_textview);
        mAddressTextView = (TextView) findViewById(R.id.address_textview);
        mCityEditText = (EditText) findViewById(R.id.city_editText);
        mAddressEditText = (EditText) findViewById(R.id.geocodekey_editText);
        mFakeBtn = (Button) findViewById(R.id.fake_location_btn);
        mGeoCodeBtn = (Button) findViewById(R.id.geocode_btn);
        // 获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapView.getMap().setMyLocationEnabled(true);
        mMapView.getMap().setOnMapClickListener(new OnMapClickListener() {

            @Override
            public boolean onMapPoiClick(MapPoi arg0) {
                return false;
            }

            @Override
            public void onMapClick(LatLng arg0) {
                mLastLatLng = arg0;
                MyLocationData locData = new MyLocationData.Builder().direction(100).latitude(arg0.latitude)
                        .longitude(arg0.longitude).build();
                mMapView.getMap().setMyLocationData(locData);
                mTipTextView.setText(R.string.tip_get_location_name_now);
                mAddressTextView.setText("");
                mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(mLastLatLng));
            }
        });

        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                if (null == mLastLatLng || DistanceUtil.getDistance(mLastLatLng, result.getLocation()) > 50) {
                    return;// 界面退出或又选择了其他位置
                }
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    // 没有检索到结果
                    mTipTextView.setText(R.string.tip_get_location_fail);
                } else {
                    mTipTextView.setText(R.string.current_location_is);
                    mAddressTextView.setText(result.getAddress());
                }
            }

            @Override
            public void onGetGeoCodeResult(GeoCodeResult result) {
                mGeoCodeBtn.setEnabled(true);
                if (null != mLastLatLng) {
                    return;// 选择了其他位置
                }
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    // 没有找到检索结果
                    mTipTextView.setText(R.string.tip_get_coordinate_fail);
                } else {
                    mTipTextView.setText(R.string.current_location_is);
                    mAddressTextView.setText(result.getAddress());
                    mLastLatLng = result.getLocation();
                    MyLocationData locData = new MyLocationData.Builder().direction(100).latitude(mLastLatLng.latitude)
                            .longitude(mLastLatLng.longitude).build();
                    mMapView.getMap().setMyLocationData(locData);
                    mMapView.getMap().setMapStatus(MapStatusUpdateFactory.newLatLng(result.getLocation()));
                    mMapView.getMap().setMapStatus(MapStatusUpdateFactory.zoomTo(15));
                }
            }
        });

        mGeoCodeBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mGeoCodeBtn.setEnabled(false);
                mLastLatLng = null;// 防止两种方式交叉
                // Geo搜索
                mSearch.geocode(new GeoCodeOption().city(mCityEditText.getText().toString()).address(
                        mAddressEditText.getText().toString()));
                mTipTextView.setText(R.string.tip_get_location_coordinate_now);
                mAddressTextView.setText("");
            }
        });
        mFakeBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mFaking) {
                    mFaking = false;
                    mFakeBtn.setText(R.string.start);
                } else {
                    if (null == mLastLatLng) {
                        ToastUtils.toastShort(FakeLocationActivity.this, R.string.tip_set_one_coordinate_before_start);
                    } else {
                        mFaking = true;
                        mFakeBtn.setText(R.string.stop);
                        mFakeLatLng = mLastLatLng;
                        startFakeLocation();
                    }
                }
            }
        });
    }

    /**
     * 开始模拟位置
     */
    protected void startFakeLocation() {
        new Thread() {

            public void run() {
                try {
                    Location loc = getGpsLocation(mFakeLatLng);
                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, true, false, false, true,
                            true, true, 0, 5);
                    locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
                    locationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER, LocationProvider.AVAILABLE,
                            null, System.currentTimeMillis());
                    locationManager.addTestProvider(LocationManager.NETWORK_PROVIDER, false, true, false, false, true,
                            true, true, 0, 5);
                    locationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
                    locationManager.setTestProviderStatus(LocationManager.NETWORK_PROVIDER, LocationProvider.AVAILABLE,
                            null, System.currentTimeMillis());
                    while (mFaking) {
                        loc.setTime(System.currentTimeMillis());
                        loc.setProvider(LocationManager.GPS_PROVIDER);
                        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, loc);
                        try {
                            if (mFaking) {// 加此判断是为了及时退出
                                Thread.sleep(500);
                            }
                        } catch (Exception e) {
                        }
                        loc.setTime(System.currentTimeMillis());
                        loc.setProvider(LocationManager.NETWORK_PROVIDER);
                        locationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, loc);
                        try {
                            if (mFaking) {// 加此判断是为了及时退出
                                Thread.sleep(500);
                            }
                        } catch (Exception e) {
                        }
                    }
                    // 结束后清空模拟位置
                    locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
                    locationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER);
                } catch (Exception e) {
                    Logger.e(TAG, e);
                }
            };
        }.start();
    }

    private Location getGpsLocation(LatLng latLng) {
        // TODO 百度经纬度转gps经纬度
        Location loc = new Location(LocationManager.GPS_PROVIDER);
        loc.setTime(System.currentTimeMillis());
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        loc.setAltitude(2.0f);
        loc.setAccuracy(3.0f);
        return loc;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            if (mFaking) {
                exitWarn();
            } else {
                finish();
                return true;
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (mFaking) {
            exitWarn();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 退出时还在模拟位置则提示用户
     */
    private void exitWarn() {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setTitle(R.string.tip);
        build.setMessage(R.string.tip_back_will_stop_fake_location);
        build.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mFaking = false;
                FakeLocationActivity.super.onBackPressed();
            }
        });
        build.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        build.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        // 目前只有通过异常来判断用户是否打开了模拟位置了
        if (!mFaking) {
            try {
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                locationManager.addTestProvider(LocationManager.NETWORK_PROVIDER, false, true, false, false, true,
                        true, true, 0, 5);
                // 测试完清空模拟位置
                locationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER);
                mFakeBtn.setEnabled(true);
            } catch (Exception e) {
                Logger.e(TAG, e);
                // 没有打开模拟位置功能
                mFakeBtn.setEnabled(false);
                if (mFirstTimeIn) {
                    mFirstTimeIn = false;// 第一次进入时才提示并进入开发者选项界面
                    startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                    ToastUtils.toastLong(this, R.string.tip_please_allow_fake_location);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        mFaking = false;
        mLastLatLng = null;
        mSearch.destroy();
        mMapView.onDestroy();
        super.onDestroy();
    }
}
