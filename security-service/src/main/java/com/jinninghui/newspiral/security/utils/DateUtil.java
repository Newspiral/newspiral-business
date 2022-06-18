package com.jinninghui.newspiral.security.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * All rights Reserved, Designed By www.freemud.cn
 *
 * @version v1.0
 * @title: DateUtil
 * @package com.freemud.common.utils
 * @Description:
 * @author: 佳乐<loverichy8 @ gmail.com>
 * @date: 17/5/18
 * @copyright: 2017 www.freemud.cn Inc. All rights reserved.
 * 注意：本内容仅限于上海非码科技内部传阅，禁止外泄以及用于其他的商业目
 */
public class DateUtil {

    public static final String FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_SIMPLE = "yyyy-MM-dd";

    public static String convert2Str(Date date, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setLenient(false);
            return sdf.format(date);
        } catch (Exception ex) {
            return null;
        }
    }


    /**
     * 把日期字符串格式化成日期类型
     *
     * @param dateStr
     * @param format
     * @return
     */
    public static Date convert2Date(String dateStr, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            sdf.setLenient(false);
            return sdf.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 把日期类型格式化成字符串
     *
     * @param date
     * @param format
     * @return
     */
    public static String convert2String(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.format(date);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 获取当前日期
     *
     * @param format
     * @return
     */
    public static String getCurrentDate(String format) {
        return new SimpleDateFormat(format).format(new Date());
    }

    /**
     * 获取时间戳
     *
     * @param time
     * @return
     */
    public static long getTimestamp(String time, String format) {
        Date date = convert2Date(time, format);
        if (null == date) return 0l;
        return date.getTime();
    }


    /**
     * 格式化Date yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String formatDateToStringComplex(Date date) {
        if (null == date) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 小写的mm表示的是分钟
        return sdf.format(date);
    }

}
