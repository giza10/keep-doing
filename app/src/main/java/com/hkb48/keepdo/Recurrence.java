
package com.hkb48.keepdo;

import java.io.Serializable;
import java.util.Calendar;

@SuppressWarnings("serial")
public class Recurrence implements Serializable {
	private boolean monday;
	private boolean tuesday;
	private boolean wednesday;
	private boolean thursday;
	private boolean friday;
	private boolean saturday;
	private boolean sunday;

    Recurrence (boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday, boolean sunday) {
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
    }
	
	boolean getMonday(){
		return this.monday;
	}
	
	boolean getTuesday(){
		return this.tuesday;
	}

	boolean getWednesday(){
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
        switch(week) {
        case Calendar.SUNDAY: return sunday;
        case Calendar.MONDAY: return monday;
        case Calendar.TUESDAY: return tuesday;
        case Calendar.WEDNESDAY: return wednesday;
        case Calendar.THURSDAY: return thursday;
        case Calendar.FRIDAY: return friday;
        case Calendar.SATURDAY: return saturday;
        default: return false;
        }
    }

    public void setMonday(boolean setValue){
        this.monday = setValue;
    }

    public void setTuesday(boolean setValue){
        this.tuesday = setValue;
    }

    public void setWednesday(boolean setValue){
        this.wednesday = setValue;
    }

    public void setThurday(boolean setValue){
        this.thursday = setValue;
    }

    public void setFriday(boolean setValue){
        this.friday = setValue;
    }

    public void setSaturday(boolean setValue){
        this.saturday = setValue;
    }

    public void setSunday(boolean setValue){
        this.sunday = setValue;
    }
}