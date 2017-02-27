package com.morgan.lib.dir;

/**
 * 文件配置保存类，目前只做简单做Assets里面的文件复制(递归)，还不能重命名等操作
 * 
 * @author Morgan.ji
 * @version 1.0
 * @date 2016-04-17
 */
public class FileInfo {
    /**
     * 是否对用户可见
     */
    public boolean isPrivate = false;
    /**
     * 是否需要从asset里面拷贝出来的
     */
    public boolean isAsset = false;
    /**
     * 是不是目录
     */
    public boolean isDir = false;

    /**
     * 是否会被编辑，只针对asset里面拷贝出来的文件，不可编辑文件大小变化会被程序自动覆盖
     */
    public boolean editable = false;

    /**
     * 文件名，可以包括路径，可以是文件夹也可以是文件
     */
    public String name = "";

    /**
     * 文件标识符
     */
    public String tag = "";

}
