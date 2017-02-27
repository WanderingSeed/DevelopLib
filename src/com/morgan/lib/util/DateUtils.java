package com.morgan.lib.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 提供日期相关的实用方法（如格式化等）。
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2015-12-14
 */
public class DateUtils {

    private static final String TAG = DateUtils.class.getName();
    /**
     * {@value}
     */
    public static final String FORMAT_TIME_ONLY = "HH:mm";
    /**
     * {@value}
     */
    public static final String FORMAT_DATE_ONLY = "yyyy-MM-dd";
    /**
     * {@value}
     */
    public static final String FORMAT_DATE = "yyyy-MM-dd HH:mm:ss";
    /**
     * {@value}
     */
    public static final String FORMAT_NO_SYMBOL = "yyyyMMddHHmmss";

    /**
     * 按照format输出date的字符串，format格式如：{@link #FORMAT_DATE_ONLY}。
     * 
     * @param date
     *            要格式化的日期
     * @param format
     *            格式
     * @return
     */
    public static String dateToString(Date date, String format) {
        String timeString = null;
        if (date == null) {
            Logger.w(TAG, "format date is null");
            return "";
        }
        try {
            DateFormat formatDate = new SimpleDateFormat(format, Locale.CHINESE);
            timeString = formatDate.format(date);
        } catch (Exception e) {
            Logger.e(TAG, "Error on format date ", e);
        }
        return timeString;
    }

    /**
     * 获取当前系统时间（年-月-日 时:分:秒）
     * 
     * @return 当前系统时间字符串
     */
    public static String getCurrentTime() {
        return dateToString(new Date(), FORMAT_DATE);
    }

    /**
     * 获取当前系统时间
     * 
     * @param format
     *            格式
     * @return 当前系统时间字符串
     */
    public static String getCurrentTime(String format) {
        return dateToString(new Date(), format);
    }
}
