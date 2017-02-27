package com.morgan.lib.util;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

/**
 * 提供硬件相关的实用方法。
 * 
 * @author Morgan.Ji
 */
public class HardwareUtils {

    /**
     * 判断GPS是否可用
     * 
     * @param context
     * @return
     */
    public static boolean isGPSAvailable(Context context) {
        LocationManager alm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 打开GPS配置界面
     * 
     * @param context
     */
    public static void openGPS(Context context) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
    }

    /**
     * 检测网络是否可用
     * 
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 获取当前网络类型，-1为无可用网络
     * 
     * @param context
     * @return
     */
    public static int getActiveNetworkType(Context context) {
        int defaultValue = -1;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return defaultValue;
        }
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            return defaultValue;
        }
        return info.getType();
    }

    /**
     * 当前WIFI是否可用
     * 
     * @param context
     * @return
     */
    public static boolean isWifiActive(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info;
        if (connectivity != null) {
            info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getType() == ConnectivityManager.TYPE_WIFI && info[i].isConnected()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 是否有可用置摄像头
     * 
     * @return 有没有
     */
    public static boolean isBackCameraAvailable() {
        try {
            Camera camera = Camera.open();
            if (null != camera) {
                camera.release();
                camera = null;
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 打开手电筒，使用之前可以用{@link #isBackCameraAvailable()}判断设备是否支持，<br/>
     * 之后使用{@link #closeFlightLight(Camera)}关闭手电筒
     * 
     * @return 若返回null则表示打开失败，若成功则返回摄像头对象
     */
    public static Camera openFlightLight() {
        try {
            Camera camera = Camera.open();
            if (null != camera) {
                Parameters parameters = camera.getParameters();
                List<String> list = parameters.getSupportedFlashModes();
                for (String string : list) {
                    if (Parameters.FLASH_MODE_TORCH.equals(string)) {
                        parameters.setFlashMode(string);
                        camera.setParameters(parameters);
                        camera.startPreview();
                        return camera;
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 关闭手电筒
     * 
     * @param camera
     *            正在使用的摄像头对象
     * @return 是否成功
     */
    public static boolean closeFlightLight(Camera camera) {
        if (null != camera) {
            Parameters parameters = camera.getParameters();
            List<String> list = parameters.getSupportedFlashModes();
            for (String string : list) {
                if (Parameters.FLASH_MODE_OFF.equals(string)) {
                    parameters.setFlashMode(string);
                    camera.setParameters(parameters);
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                    return true;
                }
            }
        }
        return false;
    }
}
