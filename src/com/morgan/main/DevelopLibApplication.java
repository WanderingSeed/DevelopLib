package com.morgan.main;

import com.baidu.mapapi.SDKInitializer;
import com.morgan.lib.base.BaseApplication;
import com.morgan.lib.util.Logger;

/**
 * 示例的Application类
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2015-11-05
 */
public class DevelopLibApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(getApplicationContext());
        Logger.DEBUG = true;
        Logger.ENABLE_FILE_LOG = true;
    }
}
