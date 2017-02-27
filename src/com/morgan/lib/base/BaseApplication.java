package com.morgan.lib.base;

import java.io.File;

import com.morgan.lib.dir.FileManager;
import com.morgan.lib.exception.ExceptionCatcher;
import com.morgan.lib.util.Logger;

import android.app.Application;
import android.content.Context;

/**
 * 自定义全局Application。
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2014年7月9日
 */
public class BaseApplication extends Application {

    /**
     * 提供全局的Context，为一些非界面相关功能提供环境（如果是界面显示则最好不要使用，因为没有主题）
     */
    private static Context mContext;

    public BaseApplication() {
        mContext = this;
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionCatcher());
    }

    /**
     * 比较耗时的初始化，用于初始化应用的各种资源，一般在应用启动的引导页调用
     */
    public void init() {
        FileManager.getInstance().init();
        Logger.FILE_LOG_DIR = FileManager.getInstance().getMainPath() + File.separator + "log";
    }

    /**
     * 获取可使用的Context（如果是界面显示则最好不要使用，因为没有主题）
     * 
     * @return Context
     */
    public static Context getContext() {
        return mContext;
    }

    /**
     * 完全退出整个应用
     */
    public static void exit() {
        ActivitysManager.getInstance().exitApp(mContext);
    }
}
