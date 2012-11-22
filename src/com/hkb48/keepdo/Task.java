package com.hkb48.keepdo;

public class Task {
	public String name;
    public Recurrence recurrence;

    public Task(String name, Recurrence recurrence) {
    	this.name = name;
    	this.recurrence = recurrence;
	}

	public String getName() {
		return name;
	}

    public Recurrence getRecurrence() {
        return recurrence;
    }
}
