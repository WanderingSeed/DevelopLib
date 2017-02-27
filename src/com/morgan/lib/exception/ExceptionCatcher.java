package com.morgan.lib.exception;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;
import java.util.TreeSet;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.morgan.lib.base.BaseApplication;
import com.morgan.lib.dir.FileManager;
import com.morgan.lib.util.AppUtils;
import com.morgan.lib.util.FileUtils;

/**
 * 运行时异常捕获器
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2016-01-05
 */
public class ExceptionCatcher implements UncaughtExceptionHandler {

    public static final String TAG = ExceptionCatcher.class.getName();

    private boolean mIsDebug = false;
    /** 系统默认的UncaughtException处理类 */
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    /** 程序的Context对象 */
    private Context mContext;
    /** 使用Properties来保存设备的信息 */
    private Properties mDeviceCrashInfo = new Properties();
    /** 错误堆栈信息 */
    private String traceInfo = "";// 因Properties不会换行，所以此处单独保存
    private static final String APP_NAME = "appName";
    private static final String APP_VERSION = "appVersion";
    private static final String VERSION_CODE = "versionCode";
    /** 错误报告文件的扩展名 */
    private static final String CRASH_REPORTER_EXTENSION = ".err";

    public ExceptionCatcher() {
        mContext = BaseApplication.getContext();
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    /**
     * 在程序启动时候, 可以调用该函数来发送以前没有发送的报告
     */
    public void sendPreviousReportsToServer() {
        sendReportsToServer();
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        // 判断是否在debug模式
        mIsDebug = AppUtils.isDebug(mContext);
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果程序没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            // Sleep一会后结束程序,因会在异步线程toast提示用户
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Log.e(TAG, "sleep Interrupted: ", e);
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑
     * 
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            Log.w(TAG, "handleException --- exception is null");
            return true;
        }
        // if (ex.getLocalizedMessage() == null) {
        // return false;
        // }
        // 使用Toast来显示异常信息
        showExitTip();
        // 收集软件信息
        collectAppInfo();
        // 收集设备信息
        collectDeviceInfo();
        // 收集异常信息
        collectExceptionInfo(ex);
        // 保存错误报告文件
        saveCrashInfoToFile();
        // 发送错误报告到服务器
        sendReportsToServer();
        return true;
    }

    /**
     * 使用Toast来显示异常信息
     */
    private void showExitTip() {
        new Thread() {

            @Override
            public void run() {
                Looper.prepare();
                Toast toast = Toast.makeText(mContext, "Sorry, The application is exit !", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            }
        }.start();
    }

    /**
     * 把错误报告发送给服务器,包含新产生的和以前没发送的.
     * 
     */
    private void sendReportsToServer() {
        String[] crFiles = getCrashReportFiles(mContext);
        if (crFiles != null && crFiles.length > 0) {
            TreeSet<String> sortedFiles = new TreeSet<String>();
            sortedFiles.addAll(Arrays.asList(crFiles));
            for (String fileName : sortedFiles) {
                File cr = new File(mContext.getFilesDir(), fileName);
                if (postReport(cr)) {
                    cr.delete();// 删除已发送的报告
                }
            }
        }
    }

    private boolean postReport(File file) {
        if (mIsDebug) {// 测试时就不要往服务器发报告了
            return true;
        }
        // TODO 发送错误报告或邮件到服务器
        return false;
    }

    /**
     * 获取错误报告文件名
     * 
     * @param ctx
     * @return
     */
    private String[] getCrashReportFiles(Context ctx) {
        File filesDir = ctx.getFilesDir();
        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(CRASH_REPORTER_EXTENSION);
            }
        };
        return filesDir.list(filter);
    }

    /**
     * 保存错误信息到文件中
     * 
     */
    private void saveCrashInfoToFile() {
        try {
            Time t = new Time(Time.TIMEZONE_UTC);
            t.setToNow(); // 取得系统时间
            int date = t.year * 10000 + (t.month + 1) * 100 + t.monthDay;
            int time = ((t.hour + 8) % 24) * 10000 + t.minute * 100 + t.second;
            String fileName = "crash-" + date + "-" + time + CRASH_REPORTER_EXTENSION;
            File file = new File(FileManager.getInstance().getMainPath(), fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);
            mDeviceCrashInfo.store(fileWriter, "");
            fileWriter.append(traceInfo);
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            Log.e(TAG, "An error occured while writing report file...", e);
        }
    }

    /**
     * 收集应用相关信息
     * 
     */
    private void collectAppInfo() {
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                mDeviceCrashInfo.put(APP_NAME, mContext.getString(mContext.getApplicationInfo().labelRes));
                mDeviceCrashInfo.put(APP_VERSION, pi.versionName == null ? "unknown" : pi.versionName);
                mDeviceCrashInfo.put(VERSION_CODE, "" + pi.versionCode);
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Error while collect package info", e);
        }
    }

    /**
     * 收集程序所在的设备信息
     * 
     */
    private void collectDeviceInfo() {
        // 使用反射来收集设备信息.在Build类中包含各种设备信息,
        // 例如: 系统版本号,设备生产商 等帮助调试程序的有用信息
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                mDeviceCrashInfo.put(field.getName(), "" + field.get(null));
            } catch (Exception e) {
                Log.e(TAG, "Error while collect crash info", e);
            }
        }
    }

    /**
     * 获取异常对象的详细信息
     * 
     * @param ex
     * @return
     */
    private void collectExceptionInfo(Throwable ex) {
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        printWriter.append("exception:" + ex.getMessage() + FileUtils.NEW_LINE);
        StackTraceElement[] trace = ex.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            printWriter.append(trace[i].toString() + FileUtils.NEW_LINE);
        }
        // 两种方法都可以
        // ex.printStackTrace(printWriter);
        // Throwable cause = ex.getCause();
        // while (cause != null) {
        // cause.printStackTrace(printWriter);
        // cause = cause.getCause();
        // }
        traceInfo = info.toString();
        printWriter.close();
        if (mIsDebug) {
            Log.e(TAG, traceInfo);
        }
    }

}
