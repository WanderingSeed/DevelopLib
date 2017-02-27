package com.morgan.lib.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;
import android.util.Log;

/**
 * 该类是为了开发android应用时更好的调试及记录应用产生的错误信息，<br>
 * 在开发时设置debug为true,信息会在logcat中显示出来， 安装到手机上时设置debug为false,ENABLE_FILE_LOG为true, <br>
 * 信息会记录在SD卡上的log文件中
 * 
 * @author Morgan.Ji
 */
public class Logger {

    private static final String TAG = Logger.class.getName();
    /**
     * 是否在调试，不调试都没必要输出logcat
     */
    public static boolean DEBUG = true;

    /**
     * 如果记录到文件，则记录的最低程度
     */
    private static int mStoreLevel = Log.WARN;

    /**
     * 是否将错误记录到文件
     */
    public static boolean ENABLE_FILE_LOG = false;
    /**
     * 日志文件目录
     */
    public static String FILE_LOG_DIR = "";
    /**
     * 错误日志文件大小最大值为5M
     */
    private static final int LOG_FILE_MAX_SIZE = 1024 * 1024 * 5;

    private static final String NEW_LINE = "\r\n";

    private static final FileModifiedComparator mComparator = new FileModifiedComparator();

    /**
     * 每条文件日志前面加上具体的应用信息
     */
    public static String APP_INFO = "";

    /**
     * 在非debug模式下存储到文件的最低等级(共六个等级从verbose到assert(2到7))
     */
    public static void d(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
        if (mStoreLevel <= Log.DEBUG && ENABLE_FILE_LOG) {
            writeFile(msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
        if (mStoreLevel <= Log.DEBUG && ENABLE_FILE_LOG) {
            writeFile(tag + "----" + msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.d(tag, msg, tr);
        }
        if (mStoreLevel <= Log.DEBUG && ENABLE_FILE_LOG) {
            writeFile(tag + "----" + msg + NEW_LINE + getExceptionInfo(tr));
        }
    }

    public static void i(String msg) {
        if (DEBUG) {
            Log.i(TAG, msg);
        }
        if (mStoreLevel <= Log.INFO && ENABLE_FILE_LOG) {
            writeFile(msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(tag, msg);
        }
        if (mStoreLevel <= Log.INFO && ENABLE_FILE_LOG) {
            writeFile(tag + "----" + msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.i(tag, msg, tr);
        }
        if (mStoreLevel <= Log.INFO && ENABLE_FILE_LOG) {
            writeFile(tag + "----" + msg + NEW_LINE + getExceptionInfo(tr));
        }
    }

    public static void w(String msg) {
        if (DEBUG) {
            Log.w(TAG, msg);
        }
        if (mStoreLevel <= Log.WARN && ENABLE_FILE_LOG) {
            writeFile(msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(tag, msg);
        }
        if (mStoreLevel <= Log.WARN && ENABLE_FILE_LOG) {
            writeFile(tag + "----" + msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.w(tag, msg, tr);
        }
        if (mStoreLevel <= Log.WARN && ENABLE_FILE_LOG) {
            writeFile(tag + "----" + msg + NEW_LINE + getExceptionInfo(tr));
        }
    }

    public static void e(String msg) {
        if (DEBUG) {
            Log.e(TAG, msg);
        }
        if (mStoreLevel <= Log.ERROR && ENABLE_FILE_LOG) {
            writeFile(msg);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(tag, msg);
        }
        if (mStoreLevel <= Log.ERROR && ENABLE_FILE_LOG) {
            writeFile(tag + "----" + msg);
        }
    }

    public static void e(String tag, Throwable tr) {
        if (DEBUG) {
            Log.e(tag, "", tr);
        }
        if (mStoreLevel <= Log.ERROR && ENABLE_FILE_LOG) {
            writeFile(tag + "----" + getExceptionInfo(tr));
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Log.e(tag, msg, tr);
        }
        if (mStoreLevel <= Log.ERROR && ENABLE_FILE_LOG) {
            writeFile(tag + "----" + msg + NEW_LINE + getExceptionInfo(tr));
        }
    }

    /**
     * 这里单独写写文件方法而不是用{@link FileUtils}是为了让此类完全独立，FileUtils也能使用此类
     * 
     * @param msg
     */
    private static void writeFile(String msg) {
        // 是否有SD
        if (!SdCardUtils.isSdCardBusy()) {
            try {
                // 获取编号最大的文件
                File dir = new File(getLogDir());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = null;
                if (dir.isDirectory()) {
                    File[] list = dir.listFiles();
                    if (list.length > 0) {
                        file = Collections.max(Arrays.asList(list), mComparator);
                    } else {
                        file = new File(dir, "log" + getCurrentTime() + ".txt");
                    }
                    // 打开一个随机访问文件流，按读写方式
                    RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
                    // 文件长度，字节数
                    long fileLength = randomFile.length();
                    if (fileLength >= LOG_FILE_MAX_SIZE) {
                        file = new File(dir, "log" + getCurrentTime() + ".txt");
                        try {
                            // 先关闭前一个文件流
                            randomFile.close();
                        } catch (Exception e) {
                            Log.e(TAG, "log class error when close log file", e);
                        }
                        randomFile = new RandomAccessFile(file, "rw");
                        fileLength = 0;
                    }
                    // 将写文件指针移到文件尾。
                    randomFile.seek(fileLength);
                    if (APP_INFO != null && APP_INFO.length() > 0) {
                        // 写入应用信息
                        randomFile.writeBytes(APP_INFO + NEW_LINE);
                    }
                    // 写入应用程序完整标识和日期
                    randomFile.writeBytes("log time: " + getCurrentTime() + NEW_LINE);
                    // 写入日志
                    randomFile.writeBytes(msg + NEW_LINE);
                    randomFile.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "log class error when write log file", e);
            }
        }
    }

    /**
     * 获取当前时间字符串
     * 
     * @return
     */
    private static String getCurrentTime() {
        try {
            DateFormat formatDate = new SimpleDateFormat("yyyyMMddhhmmss", Locale.CHINESE);
            return formatDate.format(new Date());
        } catch (Exception e) {
            Logger.e(TAG, "Error on format date ", e);
        }
        return "nodate";
    }

    /**
     * 获取日志目录
     * 
     * @return
     */
    private static String getLogDir() {
        if (FILE_LOG_DIR != null && FILE_LOG_DIR.length() > 0) {
            return FILE_LOG_DIR;
        } else {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "log";
        }
    }

    /**
     * 获取异常信息
     * 
     * @param ex
     * @return
     */
    private static String getExceptionInfo(Throwable ex) {
        if (null == ex) {
            return "";
        }
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        return info.toString();
    }

    /**
     * 文件比较器，比较最后修改时间
     * 
     * @author JiGuoChao
     * @version 1.0
     * @date 2015-7-30
     */
    public static class FileModifiedComparator implements Comparator<File> {

        @Override
        public int compare(File file1, File file2) {
            long last1 = file1.lastModified();
            long last2 = file2.lastModified();
            if (last1 == last2) {
                return 0;
            } else if (last1 < last2) {
                return -1;
            } else {
                return 1;
            }
        }

    }
}