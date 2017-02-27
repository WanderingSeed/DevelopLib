package com.morgan.lib.dir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;

import com.morgan.lib.base.BaseApplication;
import com.morgan.lib.util.Logger;
import com.morgan.lib.util.SdCardUtils;

public class FileManager {
    private static final String TAG = FileManager.class.getName();
    /**
     * 主工作目录名称，一般位于SD上
     */
    public static final String MAIN_DIR_NAME = "morgan";
    /**
     * 应用程序主目录路径
     */
    private String appMainPath = "";
    /**
     * 应用程序私有的文件夹路径
     */
    private String innerPath = "";
    /**
     * 应用程序临时目录
     */
    private String tempPath = "";
    private Context context;
    private List<FileInfo> fileInfos = new ArrayList<FileInfo>();

    private static FileManager instance;

    private FileManager(Context ctx) {
        this.context = ctx;
        this.innerPath = ctx.getFilesDir().getAbsolutePath();
        initMainPath();
        initTempPath();
    }

    /**
     * 由于解析xml和复制assets资源文件比较耗时，所以此方法就不放在构造函数里了，需要单独调用
     */
    public void init() {
        initConfigFile();
    }

    /**
     * 获取目录管理类
     * 
     * @return
     */
    public static FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager(BaseApplication.getContext());
        }
        return instance;
    }

    /**
     * 应用程序主目录初始化
     */
    private void initMainPath() {
        File mainDir = null;
        if (!SdCardUtils.isSdCardBusy()) {
            mainDir = new File(SdCardUtils.getSdCardPath() + File.separator + MAIN_DIR_NAME); // sd卡目录下
        } else {
            mainDir = context.getCacheDir();// 把资源文件复制到缓存里才会被清掉，如果一次sd卡有问题，下次还是会重新复制的
        }
        if (!mainDir.exists()) {
            mainDir.mkdirs();
        }
        appMainPath = mainDir.getAbsolutePath();
    }

    /**
     * 初始化应用程序临时目录（正常情况下是存在在SD卡中）
     * 
     */
    private void initTempPath() {
        File cache = null;
        if (SdCardUtils.isSdCardBusy()) {
            cache = context.getCacheDir();
        } else {
            cache = context.getExternalCacheDir();
        }
        if (!cache.exists()) {
            cache.mkdirs();
        }
        tempPath = cache.getAbsolutePath();
    }

    /**
     * 路径初始化
     */
    private void initConfigFile() {
        readXML();
        initAllFile();
    }

    /**
     * 读xml配置文件
     * 
     * @return
     */
    private boolean readXML() {
        int id = context.getResources().getIdentifier("file_config", "xml", context.getPackageName());
        if (id == 0) {
            return false;
        }
        XmlResourceParser xml = context.getResources().getXml(id);
        String strNode;
        int eventType = -1;
        FileInfo file = new FileInfo();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            try {
                eventType = xml.next();
                if (eventType == XmlPullParser.START_TAG) {
                    strNode = xml.getName();
                    if (strNode.equals("File")) {
                        file = new FileInfo();
                        file.name = xml.getAttributeValue(0);
                    } else if ("isPrivate".equals(strNode)) {
                        file.isPrivate = xml.nextText().equals("true");
                    } else if ("isAsset".equals(strNode)) {
                        file.isAsset = xml.nextText().equals("true");
                    } else if ("isDir".equals(strNode)) {
                        file.isDir = xml.nextText().equals("true");
                    } else if ("editable".equals(strNode)) {
                        file.editable = xml.nextText().equals("true");
                    } else if ("tag".equals(strNode)) {
                        file.tag = xml.nextText();
                    } else {
                        // unknown or new tag, just pass it
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    strNode = xml.getName();
                    if (strNode.equals("File")) {
                        fileInfos.add(file);
                    }
                }
            } catch (XmlPullParserException e) {
                // logger要用到这个类，此处就不要记录了
                Logger.e(TAG, "parse file config error", e);
            } catch (IOException e) {
                Logger.e(TAG, "parse file config error", e);
            }
        }
        return true;
    }

    /**
     * 初始化所有文件
     */
    private void initAllFile() {
        FileInfo fileInfo;
        File file;
        String desPath = appMainPath;
        for (int i = 0, len = fileInfos.size(); i < len; i++) {
            fileInfo = fileInfos.get(i);
            if (fileInfo.isPrivate) {
                desPath = innerPath;
            } else {
                desPath = appMainPath;
            }
            if (fileInfo.isAsset) { // 一般是要复制文件
                if (fileInfo.isDir) {
                    copyAssetsDirectory(fileInfo, desPath);
                } else {
                    copyAssetsFile(fileInfo, desPath);
                }
            } else {// 不是asset一般只需创建空的文件夹
                file = new File(desPath + File.separator + fileInfo.name);
                if (!file.exists()) { // 存在的话就没必要麻烦了
                    if (fileInfo.isDir) {
                        file.mkdirs();
                    } else {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            Logger.e(TAG, "create config file error", e);
                        }
                    }
                }
            }
        }
    }

    /**
     * 复制Assets里面的文件
     * 
     * @param fileInfo
     *            文件信息
     * @param destPath
     *            目标路径
     * @return
     */
    private boolean copyAssetsFile(FileInfo fileInfo, String destPath) {
        AssetManager am = context.getAssets();
        File targetFile = new File(destPath + File.separator + fileInfo.name);
        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = am.open(fileInfo.name);
            if (targetFile.exists()) {
                if (!targetFile.isFile() || (!fileInfo.editable && is.available() != targetFile.length())) {
                    if (!targetFile.delete()) {// 文件夹还有子文件的话就会删除失败
                        return false;
                    }
                } else {
                    return true;
                }
            }
            os = new FileOutputStream(targetFile, true);
            targetFile.createNewFile();
            byte[] buff = new byte[1024];
            int count = 0;
            while ((count = is.read(buff)) != -1) {
                os.write(buff, 0, count);
            }
        } catch (IOException e) {
            Logger.e(TAG, "copy assets file error", e);
            return false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (Exception e2) {
            }
        }
        return true;
    }

    /**
     * 复制Assets里面的文件夹
     * 
     * @param dir
     *            文件夹信息
     * @param destPath
     *            目的地
     * @return
     */
    private boolean copyAssetsDirectory(FileInfo dirInfo, String destPath) {

        InputStream is = null;
        OutputStream os = null;
        try {
            File targetDir = new File(destPath + File.separator + dirInfo.name);
            if (targetDir.exists()) {
                if (!targetDir.isDirectory()) {
                    if (!targetDir.delete()) {// 删除失败
                        return false;
                    }
                    targetDir.mkdirs();
                }
            } else {
                targetDir.mkdirs();
            }
            AssetManager am = context.getAssets();
            String[] dirNames = am.list(dirInfo.name);
            for (int i = 0; i < dirNames.length; i++) {
                String name = dirNames[i];
                if (name.indexOf(".") == -1) {//暂时认为没有后缀就是文件夹
                    //构造一个文件信息，递归调用自己
                    FileInfo sonDir = new FileInfo();
                    sonDir.name = dirInfo.name + File.separator + name;
                    sonDir.isDir = true;
                    sonDir.isAsset = dirInfo.isAsset;
                    sonDir.editable = dirInfo.editable;
                    sonDir.isPrivate = dirInfo.isPrivate;
                    copyAssetsDirectory(sonDir, destPath);
                } else {
                    File targetFile = new File(targetDir + File.separator + name);
                    is = am.open(dirInfo.name + File.separator + name);
                    if (targetFile.exists()) {
                        if (!targetFile.isFile() || (!dirInfo.editable && is.available() != targetFile.length())) {
                            if (!targetFile.delete()) {// 文件夹还有子文件的话就会删除失败
                                return false;
                            }
                        } else {
                            return true;
                        }
                    }
                    targetFile.createNewFile();
                    os = new FileOutputStream(targetFile, true);
                    byte[] buff = new byte[1024];
                    int count = 0;
                    while ((count = is.read(buff)) != -1) {
                        os.write(buff, 0, count);
                    }
                    is.close();
                    is = null;
                    os.close();
                    os = null;
                }
            }
        } catch (IOException e) {
            Logger.e(TAG, "copy assets dir error", e);
            return false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (Exception e2) {
            }
        }
        return true;
    }

    /**
     * 获取应用程序主目录
     * 
     * @return
     */
    public String getMainPath() {
        return appMainPath;
    }

    /**
     * 获取应用程序临时目录（正常情况下是存在在SD卡中）
     * 
     * @return 返回临时目录
     */
    public String getTempPath() {
        return tempPath;
    }
}
