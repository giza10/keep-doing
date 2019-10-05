package com.hkb48.keepdo;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Task implements Serializable {
    static final long INVALID_TASKID = Long.MIN_VALUE;

    private long identifier;
    private String name;
    private String context;
    private Recurrence recurrence;
    private Reminder reminder;
    private long order;

    public Task(String name, String context, Recurrence recurrence) {
        this.identifier = INVALID_TASKID;
        this.name = name;
        this.context = context;
        this.recurrence = recurrence;
        this.reminder = new Reminder();
        this.order = INVALID_TASKID;
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

    long getOrder() {
        return order;
    }

    void setTaskID(long id) {
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

    public void setContext(String context) {
        this.context = context;
    }

    void setOrder(long order) {
        this.order = order;
    }
}