package com.hkb48.keepdo;

import android.net.Uri;
import android.provider.BaseColumns;

final class Database {
    public static final String AUTHORITY = "com.hkb48.keepdo.keepdoprovider";

	private Database() {}

	// Task table
	public static final class TasksToday implements BaseColumns {

        private TasksToday() {}

        // Incoming URI matches the main table URI pattern
        public static final int TABLE_LIST = 10;
        // Incoming URI matches the main table row ID URI pattern
        public static final int TABLE_ID = 20;

        public static final String TABLE_NAME = "table_tasks";
        public static final String TABLE_URI = "table_task_uri";
        public static final Uri CONTENT_URI =  Uri.parse("content://" + AUTHORITY + "/" + TABLE_URI);

        public static final String TASK_NAME = "task_name";
        public static final String FREQUENCY_MON = "mon_frequency";
        public static final String FREQUENCY_TUE = "tue_frequency";
        public static final String FREQUENCY_WEN = "wen_frequency";
        public static final String FREQUENCY_THR = "thr_frequency";
        public static final String FREQUENCY_FRI = "fri_frequency";
        public static final String FREQUENCY_SAT = "sat_frequency";
        public static final String FREQUENCY_SUN = "sun_frequency";
        public static final String TASK_CONTEXT = "task_context";

        public static final String REMINDER_ENABLED = "reminder_enabled";
        public static final String REMINDER_TIME = "reminder_time";
        public static final String TASK_LIST_ORDER = "task_list_order";

        public static final String DEFAULT_SORT_ORDER = "_id ASC";
    }

    // Task Completion table
    public static final class TaskCompletion implements BaseColumns {

        private TaskCompletion() {}
        
        // Incoming URI matches the main table URI pattern
        public static final int TABLE_LIST = 30;
        // Incoming URI matches the main table row ID URI pattern
        public static final int TABLE_ID = 40;

        public static final String TABLE_NAME = "table_completions";
        public static final String TABLE_URI = "table_completionuri"; 

        public static final Uri CONTENT_URI =  Uri.parse("content://" + AUTHORITY + "/" + TABLE_URI);

        public static final String TASK_NAME_ID = "task_id";
        public static final String TASK_COMPLETION_DATE = "completion_date";
        
        public static final String DEFAULT_SORT_ORDER = "task_id ASC";
    }
}