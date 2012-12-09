package com.hkb48.keepdo;

import android.provider.BaseColumns;

public final class Database {
	private Database() {}
	
	// Task table
	public static final class TasksToday implements BaseColumns {

        private TasksToday() {}

        public static final String TASKS_TABLE_NAME = "table_tasks";
        
        public static final String TASK_NAME = "task_name";
        public static final String FREQUENCY_MON = "mon_frequency";
        public static final String FREQUENCY_TUE = "tue_frequency";
        public static final String FREQUENCY_WEN = "wen_frequency";
        public static final String FREQUENCY_THR = "thr_frequency";
        public static final String FREQUENCY_FRI = "fri_frequency";
        public static final String FREQUENCY_SAT = "sat_frequency";
        public static final String FREQUENCY_SUN = "sun_frequency";
        
        public static final String DEFAULT_SORT_ORDER = "task_name ASC";
    }
    

    // Task Completion table
    public static final class TaskCompletions implements BaseColumns {

        private TaskCompletions() {}
        
        public static final String TASK_COMPLETION_TABLE_NAME = "table_completions";

        public static final String TASK_NAME_ID = "task_id";
        public static final String TASK_COMPLETION_DATE = "completion_date";
        
        public static final String DEFAULT_SORT_ORDER = "task_id ASC";
    }
}