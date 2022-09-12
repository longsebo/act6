package com.activiti.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    /**
     * @description 按指定格式获取指定时间字符串
     * @param date
     *            待转换日期
     * @param patterns
     *            时间格式 e.g yyyy-MM-dd HH:mm:ss
     * @return 返回指定格式指定时间字符串
     */
    public static String getDateStr(Date date, String patterns) {
        if (date == null) {
            date = new Date();
        }
        SimpleDateFormat formatter = new SimpleDateFormat(patterns);
        String dateString = formatter.format(date);
        return dateString;
    }
    /**
     *
     * 方法功能描述: 取当前日期或时间
     *
     * @param 参数名
     *            参数作用描述 …
     * @return 返回类型 返回值描述，如果有返回值，则写。没有不用写，可以描述什么情况 返回什么值
     * @remark 该方法使用注意事项描述 是可选
     */
    public static String getNowDate(String pattern) {
        return DateUtil.getDateStr(new Date(), pattern);
    }

}
