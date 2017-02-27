package com.morgan.lib.util;

import java.math.BigDecimal;

/**
 * 字符串与其他类型数据转换相关工具类
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2015年8月30日
 */
public class StrUtils {

    /**
     * 把double类型的数字转换成精度为3的字符串,四舍五入
     * 
     * @param value
     * @return
     */
    public static String toString(double value) {
        return toString(value, 3);
    }

    /**
     * 把double类型的数字转换成指定精度的字符串,四舍五入
     * 
     * @param value
     * @param precision
     * @return
     */
    public static String toString(double value, int precision) {
        BigDecimal bigDecimal = new BigDecimal(value);
        String result = bigDecimal.toPlainString();// 防止转成科学计数法
        int index = result.indexOf(".");
        if ((result.length() - index) > (precision + 1)) {
            result = result.substring(0,
                    (result.length() > index + precision + 1) ? index + precision + 1 : result.length());
        } else {
            if (index < 0) {
                result += ".";
                index = result.length() - 1;
            }
            int oldLength = result.length();
            for (int i = 0; i < (precision - oldLength + index + 1); i++) {
                result += "0";
            }
        }
        return result;
    }
}
