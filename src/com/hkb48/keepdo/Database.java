package com.hkb48.keepdo;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Database {
    public static final String AUTHORITY = " com.hkb48.keepdo.keepdoprovider";

	private Database() {}

	// Task table
	public static final class TasksToday implements BaseColumns {

        private TasksToday() {}

        // The incoming URI matches the main table URI pattern
        public static final int TABLE_LIST = 10;
        // The incoming URI matches the main table row ID URI pattern
        public static final int TABLE_ID = 20;

        public static final String TABLE_NAME = "table_tasks";
        public static final String TABLE_URI = "table_taskrui";
        public static final Uri CONTENT_URI =  Uri.parse("content://" + AUTHORITY + "/" + "tasktblurl");
        
        public static final String TASK_NAME = "task_name";
        public static final String FREQUENCY_MON = "mon_frequency";
        public static final String FREQUENCY_TUE = "tue_frequency";
        public static final String FREQUENCY_WEN = "wen_frequency";
        public static final String FREQUENCY_THR = "thr_frequency";
        public static final String FREQUENCY_FRI = "fri_frequency";
        public static final String FREQUENCY_SAT = "sat_frequency";
        public static final String FREQUENCY_SUN = "sun_frequency";
        // TODO : Temporary added for reminder test (by kuramitsu)
        public static final String REMINDER_ENABLED = "reminder_enabled";
        public static final String REMINDER_TIME_HOUR = "reminder_time_hour";
        public static final String REMINDER_TIME_MINUTE = "reminder_time_minute";
        
        public static final String DEFAULT_SORT_ORDER = "task_name ASC";
    }
    

    // Task Completion table
    public static final class TaskCompletions implements BaseColumns {

        private TaskCompletions() {}
        
        // The incoming URI matches the main table URI pattern
        public static final int TABLE_LIST = 30;
        // The incoming URI matches the main table row ID URI pattern
        public static final int TABLE_ID = 40;

        public static final String TABLE_NAME = "table_completions";
        public static final String TABLE_URI = "table_completionuri"; 

        public static final Uri CONTENT_URI =  Uri.parse("content://" + AUTHORITY + "/" + "completiontblurl");

        public static final String TASK_NAME_ID = "task_id";
        public static final String TASK_COMPLETION_DATE = "completion_date";
        
        public static final String DEFAULT_SORT_ORDER = "task_id ASC";
    }
}