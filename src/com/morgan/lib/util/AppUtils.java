package com.morgan.lib.util;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Bitmap;

/**
 * 提供应用相关的实用方法。
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2015-12-14
 */
public class AppUtils {

    private static final String TAG = AppUtils.class.getName();
    /**
     * 用于判断当前APK签名是不是debug签名
     */
    private static final X500Principal DEBUG_DN = new X500Principal("CN=Android Debug,O=Android,C=US");

    /**
     * 在手机主页上添加启动图标
     * 
     * @param context
     * @param intent
     *            图标要启动的意图
     * @param shortCutName
     *            图标下面的名称
     * @param icon
     *            图标
     * @param duplicate
     *            是否可重复
     */
    public static void installLaunchShortCut(Context context, Intent intent, String shortCutName, Bitmap icon,
            boolean duplicate) {
        Intent shortCutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        shortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortCutName);
        shortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
        shortCutIntent.putExtra("duplicate", duplicate);
        intent.setAction(Intent.ACTION_MAIN);
        shortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        context.sendBroadcast(shortCutIntent);
    }

    /**
     * 获取App安装包信息
     * 
     * @param context
     * @return
     */
    public static PackageInfo getPackageInfo(Context context) {
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            Logger.e(TAG, "Error on get package info ", e);
        }
        if (info == null) {
            info = new PackageInfo();
        }
        return info;
    }

    /**
     * 获取当前应用是调试上去的还是发布版本
     * 
     * @param context
     * @return
     */
    public static boolean isDebug(Context context) {
        boolean isDebug = false;
        try {
            /** 通过包管理器获得指定包名包含签名的包信息 **/
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            Signature signatures[] = packageInfo.signatures;
            for (int i = 0; i < signatures.length; i++) {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                ByteArrayInputStream stream = new ByteArrayInputStream(signatures[i].toByteArray());
                X509Certificate cert = (X509Certificate) cf.generateCertificate(stream);
                // 判断是否在debug模式
                isDebug = cert.getSubjectX500Principal().equals(DEBUG_DN);
                if (isDebug) {// 找到了就没必要继续了
                    break;
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "Error when juge is app debug", e);
        }
        return isDebug;
    }
}
