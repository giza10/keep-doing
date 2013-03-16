package com.hkb48.keepdo;

import java.util.Calendar;
import java.util.Date;

public class DateChangeTime {
    public static Date getDate() {
        return getDateCalendar().getTime();
    }

    public static Calendar getDateCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
        int nowMinutes = calendar.get(Calendar.MINUTE);

        String dateChangeTime = Settings.getDateChangeTime();
        String[] time_para = dateChangeTime.split(":");
        int hour = Integer.parseInt(time_para[0]);
        int minutes = Integer.parseInt(time_para[1]);

        nowHour += 24;
        if ((nowHour * 60 + nowMinutes) < (hour * 60 + minutes)) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
