package com.hkb48.keepdo;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Task implements Serializable {
	private long identifier;
	private String name;
    private Recurrence recurrence;
    private boolean isChecked;

    public Task(String name, Recurrence recurrence) {
    	this.name = name;
    	this.recurrence = recurrence;
    	this.isChecked = false;
    	this.identifier = Long.MIN_VALUE;
	}

	public long getTaskID() {
		return identifier;
	}
    
	public String getName() {
		return name;
	}

    public Recurrence getRecurrence() {
        return recurrence;
    }
    
    public boolean ifChecked() {
    	return this.isChecked;
    }
    
    protected void setTaskID(long id) {
    	this.identifier = id;
    }

    protected void setChecked(boolean checked) {
    	this.isChecked = checked;
    }
}