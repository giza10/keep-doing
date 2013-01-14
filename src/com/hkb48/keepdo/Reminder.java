package com.hkb48.keepdo;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Reminder implements Serializable {
    boolean enabled;
    int hour;
    int minute;

    public Reminder(boolean enabled, int hour, int minute) {
        this.enabled = enabled;
        this.hour = hour;
        this.minute = minute;
    }

    boolean getEnabled() {
        return enabled;
    }

    int getHour() {
        return hour;
    }

    int getMinute() {
        return minute;
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    void setHour(int hour) {
        this.hour = hour;
    }

    void setMinute(int minute) {
        this.minute = minute;
    }
}
