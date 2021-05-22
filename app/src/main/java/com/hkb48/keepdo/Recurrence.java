package com.hkb48.keepdo;

import java.io.Serializable;
import java.util.Calendar;

@SuppressWarnings("serial")
public class Recurrence implements Serializable {
    private final boolean monday;
    private final boolean tuesday;
    private final boolean wednesday;
    private final boolean thursday;
    private final boolean friday;
    private final boolean saturday;
    private final boolean sunday;

    Recurrence(boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday, boolean sunday) {
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
    }

    boolean getMonday() {
        return this.monday;
    }

    boolean getTuesday() {
        return this.tuesday;
    }

    boolean getWednesday() {
        return this.wednesday;
    }

    boolean getThurday() {
        return this.thursday;
    }

    boolean getFriday() {
        return this.friday;
    }

    boolean getSaturday() {
        return this.saturday;
    }

    boolean getSunday() {
        return this.sunday;
    }

    public boolean isValidDay(int week) {
        switch (week) {
            case Calendar.SUNDAY:
                return sunday;
            case Calendar.MONDAY:
                return monday;
            case Calendar.TUESDAY:
                return tuesday;
            case Calendar.WEDNESDAY:
                return wednesday;
            case Calendar.THURSDAY:
                return thursday;
            case Calendar.FRIDAY:
                return friday;
            case Calendar.SATURDAY:
                return saturday;
            default:
                return false;
        }
    }
}