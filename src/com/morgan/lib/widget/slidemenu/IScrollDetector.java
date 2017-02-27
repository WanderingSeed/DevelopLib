package com.morgan.lib.widget.slidemenu;

import android.view.View;

/**
 * 滑动探测器接口，探测是否能够朝某个方向滑动
 * 
 * @author JiGuoChao
 * @version 1.0
 * @date 2015-11-09
 */
public interface IScrollDetector {

    /**
     * 探测一个View是否能继续横向滑动
     * 
     * @param v
     *            探测的View
     * @param direction
     *            手指滑动方向left: direction<0, right: direction>0
     * @return
     */
    public boolean canScrollHorizontal(View v, int direction);

    /**
     * 探测一个View是否能继续纵向滑动
     * 
     * @param v
     *            探测的View
     * @param direction
     *            手指滑动方向bottom: direction<0, top: direction>0
     * @return
     */
    public boolean canScrollVertical(View v, int direction);

}
