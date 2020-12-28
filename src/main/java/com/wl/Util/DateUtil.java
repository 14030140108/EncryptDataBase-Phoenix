package com.wl.Util;

import com.wl.beans.RectNode;
import com.wl.stcoder.STCodeTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static Date parseDate(String time) throws ParseException {
        SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simple.parse(time);
    }



    /**
     * 生成STCodeTime
     *
     * @param time format : 2020-12-13 11:03:23
     * @return STCodeTime
     * @throws ParseException 解析异常
     */
    public static STCodeTime transSTC(String time) throws ParseException {
        String start_time = time.substring(0, 4) + "-01-01 00:00:00";
        Date periodStart = DateUtil.parseDate(start_time);
        double minutes = (DateUtil.parseDate(time).getTime() - periodStart.getTime()) / 60000D;
        return new STCodeTime(periodStart, minutes);
    }

    public static STCodeTime transSTC(RectNode rectNode) throws ParseException {
        String time = rectNode.getTime();
        return transSTC(time);
    }

    public static boolean isLeapYear(Date d) {
        int year = Integer.parseInt(String.format("%tY", d));
        return year % 4 == 0 && year % 100 != 0 || year % 400 == 0;
    }
}
