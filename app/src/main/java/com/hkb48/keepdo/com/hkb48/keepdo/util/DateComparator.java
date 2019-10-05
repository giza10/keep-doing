package com.hkb48.keepdo.com.hkb48.keepdo.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateComparator {
    public static boolean equals(Date a, Date b) {
        Date compA = truncate(a);
        Date compB = truncate(b);

        return compA.equals(compB);
    }

    private static Date truncate(Date datetime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(datetime);

        return new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE))
                .getTime();
    }
}
