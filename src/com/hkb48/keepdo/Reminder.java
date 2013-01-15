package com.hkb48.keepdo;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Reminder implements Serializable {
    private boolean enabled;
    private int hourOfDay;
    private int minute;

    public Reminder() {
        this.enabled = false;
        this.hourOfDay = 0;
        this.minute = 0;
    }

    public Reminder(boolean enabled, int hourOfDay, int minute) {
        this.enabled = enabled;
        this.hourOfDay = hourOfDay;
        this.minute = minute;
    }

    boolean getEnabled() {
        return enabled;
    }

    int getHourOfDay() {
        return hourOfDay;
    }

    int getMinute() {
        return minute;
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    void setMinute(int minute) {
        this.minute = minute;
    }
}
