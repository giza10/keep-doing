package com.hkb48.keepdo;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Task implements Serializable {
	private long identifier;
	private String name;
    private Recurrence recurrence;

    public Task(String name, Recurrence recurrence) {
    	this.name = name;
    	this.recurrence = recurrence;
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

    protected void setTaskID(long id) {
    	this.identifier = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRecurrence(Recurrence recurrence) {
        this.recurrence = recurrence;
    }
}