package com.hkb48.keepdo;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Task implements Serializable {
    public static final long INVALID_TASKID = Long.MIN_VALUE;

	private long identifier;
	private String name;
	private String context;
	private Recurrence recurrence;
    private Reminder reminder;

    public Task(String name, String context, Recurrence recurrence) {
        this.identifier = INVALID_TASKID;
        this.name = name;
        this.context = context;
        this.recurrence = recurrence;
        this.reminder = new Reminder();
	}

	public long getTaskID() {
		return identifier;
	}

	public String getName() {
		return name;
	}

	public String getContext() {
		return context;
	}

	public Recurrence getRecurrence() {
        return recurrence;
    }

    public Reminder getReminder() {
        return reminder;
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

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
    }
}