package com.morgan.lib.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * 提供文件相关的实用方法。
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2016-01-05
 */
public class FileUtils {

    private static final String TAG = FileUtils.class.getName();

    public static final String NEW_LINE = "\r\n";

    /**
     * 追加内容到文件中
     * 
     * @param filePath
     *            文件的路径
     * @param content
     *            想要写入的信息
     */
    public static void appendFile(String filePath, String content) {
        writeFile(filePath, content, true);
    }

    /**
     * @param filePath
     *            文件的路径
     * @param content
     *            想要写入的信息
     * @param append
     *            添加方式(true为追加,false为覆盖)
     */
    public static void writeFile(String filePath, String content, boolean append) {
        FileWriter fw = null;
        PrintWriter pw = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file, append);
            pw = new PrintWriter(fw);
            pw.write(content + NEW_LINE);
            pw.close();
            fw.close();
        } catch (Exception e) {
            Logger.e(TAG, e);
        } finally {
            if (pw != null) {
                pw.close();
            }
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    Logger.e(TAG, e);
                }
            }
        }
    }

    /**
     * 用于读取较小文本内的数据
     * 
     * @param inputStream
     * @return
     */
    public static String readFile(String filePath) {
        BufferedInputStream buffered = null;
        String content = "";
        try {
            File file = new File(filePath);
            if (!file.exists() || file.isDirectory()) {
                return "";
            }
            buffered = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[buffered.available()];
            buffered.read(buffer);
            content = new String(buffer);
        } catch (Exception e) {
            Logger.e(TAG, e);
        } finally {
            if (buffered != null) {
                try {
                    buffered.close();
                } catch (IOException e) {
                    Logger.e(TAG, e);
                }
            }
        }
        return content;
    }

    /**
     * 读取文件内的数据
     * 
     * @param filePath
     * @param charset
     * @return
     */
    public static String readFile(String filePath, String charset) {
        StringBuffer fileContent = new StringBuffer();
        BufferedReader br = null;
        try {
            File file = new File(filePath);
            if (!file.exists() || file.isDirectory()) {
                return "";
            }
            FileInputStream inputStream = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(inputStream, charset);
            br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                fileContent.append(line);
            }
        } catch (Exception e) {
            Logger.e(TAG, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Logger.e(TAG, e);
                }
            }
        }
        return fileContent.toString();
    }

    /**
     * 复制文件
     * 
     * @param sourceFile
     *            源文件
     * @param targetFile
     *            目标文件
     * @throws IOException
     */
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // 新建文件输入流并对它进行缓冲
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));
            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));
            // 缓冲数组
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();
        } catch (Exception e) {
            Logger.e(TAG, "copy file error", e);
        } finally {
            // 关闭流
            if (inBuff != null) {
                inBuff.close();
            }
            if (outBuff != null) {
                outBuff.close();
            }
        }
    }

    /**
     * 复制文件,文件夹
     * 
     * @param srcFile
     *            源文件
     * @param targetFile
     *            目标文件
     * @param override
     *            如果已经存在，是否覆盖（已存在的目标文件大小有变化才覆盖）
     * @return
     */
    public static boolean copyFile(File srcFile, File targetFile, boolean override) {
        if (srcFile == null || !srcFile.exists()) {
            return false;
        }
        if (srcFile.isDirectory()) {
            if (targetFile.exists()) {
                if (!targetFile.isDirectory()) {
                    targetFile.delete();
                    targetFile.mkdirs();
                }
            } else {
                targetFile.mkdirs();
            }
            // 子目录复制
            File[] sons = srcFile.listFiles();
            for (int i = 0; i < sons.length; i++) {
                File newTarget = new File(targetFile.getAbsolutePath() + File.separator + sons[i].getName());
                if (!copyFile(sons[i], newTarget, override)) {
                    return false;
                }
            }
        } else { // 文件
            if (targetFile.exists()) {
                if (!targetFile.isFile() || (override && srcFile.length() != targetFile.length())) {
                    if (!targetFile.delete()) {// 文件夹还有子文件的话就会删除失败
                        return false;
                    }
                } else {
                    return true;
                }
            }
            try {
                targetFile.createNewFile();
                FileInputStream is = new FileInputStream(srcFile);
                FileOutputStream os = new FileOutputStream(targetFile);
                byte[] data = new byte[2048];
                long srcFileSize = srcFile.length();
                int count = 0;
                while (data.length * count < srcFileSize) {
                    int n = is.read(data);
                    os.write(data, 0, n);
                    count++;
                }
                is.close();
                os.flush();
                os.close();
            } catch (IOException e) {
                Logger.e(TAG, "copy file error", e);
                return false;
            }
        }
        return true;
    }
}
