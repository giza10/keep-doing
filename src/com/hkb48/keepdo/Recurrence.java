
package com.hkb48.keepdo;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Recurrence implements Serializable {
	private boolean monday;
	private boolean tuesday;
	private boolean wednesday;
	private boolean thursday;
	private boolean friday;
	private boolean saturday;
	private boolean sunday;

	public Recurrence (boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday, boolean sunday) {
		this.monday = monday;
		this.tuesday = tuesday;
		this.wednesday = wednesday;
		this.thursday = thursday;
		this.friday = friday;
		this.saturday = saturday;
		this.sunday = sunday;
	}
	
	public boolean getMonday(){
		return this.monday;
	}
	
	public boolean getTuesday(){
		return this.tuesday;
	}

	public boolean getWednesday(){
		return this.wednesday;
	}
	
	public boolean getThurday() {
		return this.thursday;
	}
	
	public boolean getFriday() {
		return this.friday;
	}
	
	public boolean getSaturday() {
		return this.saturday;
	}
	
	public boolean getSunday() {
		return this.sunday;
	}
	
	private void setMonday(boolean setValue){
		this.monday = setValue;
	}
	
	private void setTuesday(boolean setValue){
		this.tuesday = setValue;
	}
	
	private void setWednesday(boolean setValue){
		this.wednesday = setValue;
	}
	
	private void setThurday(boolean setValue){
		this.thursday = setValue;
	}
	
	private void setFriday(boolean setValue){
		this.friday = setValue;
	}
	
	private void setSaturday(boolean setValue){
		this.saturday = setValue;
	}

	private void setSunday(boolean setValue){
		this.sunday = setValue;
	}
}