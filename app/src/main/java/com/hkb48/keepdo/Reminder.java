package com.hkb48.keepdo;

import java.io.Serializable;
import java.util.Calendar;

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

    public Reminder(boolean enabled, long timeInMillis) {
        this.enabled = enabled;
        setTimeInMillis(timeInMillis);
    }

    public boolean getEnabled() {
        return enabled;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public int getMinute() {
        return minute;
    }

    public long getTimeInMillis() {
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, hourOfDay);
        time.set(Calendar.MINUTE, minute);
        return time.getTimeInMillis();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    private void setTimeInMillis(long milliseconds) {
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(milliseconds);
        hourOfDay = time.get(Calendar.HOUR_OF_DAY);
        minute = time.get(Calendar.MINUTE);
    }
}
