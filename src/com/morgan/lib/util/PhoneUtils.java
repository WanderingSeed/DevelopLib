package com.morgan.lib.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.KeyEvent;

/**
 * 提供手机相关的实用方法。
 * 
 * @author Morgan.ji
 * @version 1.0
 * @date 2016-05-02
 */
public class PhoneUtils {

    public static String TAG = PhoneUtils.class.getName();

    /**
     * 挂断电话
     * 
     * @param context
     */
    public static void endCall(Context context) {
        try {
            Object telephonyObject = getTelephonyObject(context);
            if (null != telephonyObject) {
                Class telephonyClass = telephonyObject.getClass();
                Method endCallMethod = telephonyClass.getMethod("endCall");
                endCallMethod.setAccessible(true);
                endCallMethod.invoke(telephonyObject);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取ITelephony实现对象
     * 
     * @param context
     * @return
     */
    private static Object getTelephonyObject(Context context) {
        Object telephonyObject = null;
        try {
            // 初始化iTelephony
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            // Will be used to invoke hidden methods with reflection
            // Get the current object implementing ITelephony interface
            Class telManager = telephonyManager.getClass();
            Method getITelephony = telManager.getDeclaredMethod("getITelephony");
            getITelephony.setAccessible(true);
            telephonyObject = getITelephony.invoke(telephonyManager);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return telephonyObject;
    }

    /**
     * 通过反射调用的方法，接听电话，该方法只在android 2.3之前的系统上有效。
     * 
     * @param context
     */
    private static void answerRingingCallWithReflect(Context context) {
        try {
            Object telephonyObject = getTelephonyObject(context);
            if (null != telephonyObject) {
                Class telephonyClass = telephonyObject.getClass();
                Method endCallMethod = telephonyClass.getMethod("answerRingingCall");
                endCallMethod.setAccessible(true);
                endCallMethod.invoke(telephonyObject);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

    }

    /**
     * 伪造一个有线耳机插入，并按接听键的广播，让系统开始接听电话。
     * 
     * @param context
     */
    private static void answerRingingCallWithBroadcast(Context context) {
        AudioManager localAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        // 判断是否插上了耳机
        boolean isWiredHeadsetOn = localAudioManager.isWiredHeadsetOn();
        if (!isWiredHeadsetOn) {
            Intent headsetPluggedIntent = new Intent(Intent.ACTION_HEADSET_PLUG);
            headsetPluggedIntent.putExtra("state", 1);
            headsetPluggedIntent.putExtra("microphone", 0);
            headsetPluggedIntent.putExtra("name", "");
            context.sendBroadcast(headsetPluggedIntent);

            Intent meidaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);
            meidaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
            context.sendOrderedBroadcast(meidaButtonIntent, null);

            Intent headsetUnpluggedIntent = new Intent(Intent.ACTION_HEADSET_PLUG);
            headsetUnpluggedIntent.putExtra("state", 0);
            headsetUnpluggedIntent.putExtra("microphone", 0);
            headsetUnpluggedIntent.putExtra("name", "");
            context.sendBroadcast(headsetUnpluggedIntent);

        } else {
            Intent meidaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);
            meidaButtonIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
            context.sendOrderedBroadcast(meidaButtonIntent, null);
        }
    }

    /**
     * 接听电话
     * 
     * @param context
     */
    public static void answerRingingCall(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) { // 2.3或2.3以上系统
            answerRingingCallWithBroadcast(context);
        } else {
            answerRingingCallWithReflect(context);
        }
    }

    /**
     * 打电话
     * 
     * @param context
     * @param phoneNumber
     */
    public static void callPhone(Context context, String phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            try {
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                context.startActivity(callIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拨电话
     * 
     * @param context
     * @param phoneNumber
     */
    public static void dialPhone(Context context, String phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            try {
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                context.startActivity(callIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void editMessage(Context context, String phoneNumber, String msg) {
        Uri smsToUri = Uri.parse("smsto:" + phoneNumber);
        Intent msgIntent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        msgIntent.putExtra("sms_body", msg);
        context.startActivity(msgIntent);
    }

    public static void editMessage(Context context, String msg) {
        Intent msgIntent = new Intent();
        msgIntent.setAction(Intent.ACTION_SENDTO);
        msgIntent.putExtra("sms_body", msg);
        context.startActivity(msgIntent);
    }

    public static void sendMessage(Context context, String phoneNumber, String msg) {
        SmsManager smsManager = SmsManager.getDefault();
        PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, new Intent(), 0);
        smsManager.sendTextMessage(phoneNumber, null, msg, sentIntent, null);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // 收信人已经成功接收
            }
        }, new IntentFilter("SENT_SMS_ACTION"));
    }
}
