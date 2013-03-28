package com.hkb48.keepdo;

import java.util.Calendar;
import java.util.Date;

public class DateChangeTimeUtil {
    public static Date getDateTime() {
        return getDateTimeCalendar().getTime();
    }

    public static Date getDate() {
        Calendar calendar = getDateTimeCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Calendar getDateTimeCalendar() {
        Calendar realTime = Calendar.getInstance();
        realTime.setTimeInMillis(System.currentTimeMillis());
        return getDateTimeCalendar(realTime);
    }

    public static Calendar getDateTimeCalendar(Calendar realTime) {
        DateChangeTime dateChangeTime = getDateChangeTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(realTime.getTimeInMillis());
        calendar.add(Calendar.HOUR_OF_DAY, -dateChangeTime.hourOfDay);
        calendar.add(Calendar.MINUTE, -dateChangeTime.minute);
        return calendar;
    }

    public static DateChangeTime getDateChangeTime() {
        String dateChangeTimeStr = Settings.getDateChangeTime();
        String[] time_para = dateChangeTimeStr.split(":");
        DateChangeTime dateChangeTime = new DateChangeTime();
        dateChangeTime.hourOfDay = Integer.parseInt(time_para[0]) - 24;
        dateChangeTime.minute = Integer.parseInt(time_para[1]);
        return dateChangeTime;
    }

    public static class DateChangeTime {
        int hourOfDay;
        int minute;
    }
}
