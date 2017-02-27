package com.morgan.lib.util;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;

/**
 * 提供SD卡相关的实用方法。
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2015-12-14
 */
public class SdCardUtils {

    /**
     * 获取SD卡路径
     * 
     * @return
     */
    public static String getSdCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * 获取SD卡可用空间大小
     * 
     * @return
     */
    public static long getSdCardAvailableBytes() {
        if (isSdCardBusy()) {
            return 0;
        }
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        // 等18以上再使用这个
        // long blockSize = stat.getBlockSizeLong();
        // long availableBlocks = stat.getAvailableBlocksLong();
        return blockSize * (availableBlocks - 4);
    }

    /**
     * 当前SD是否可用
     * 
     * @return
     */
    public static boolean isSdCardBusy() {
        return !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
