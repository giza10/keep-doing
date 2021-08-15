package com.hkb48.keepdo.di

import android.content.Context
import com.hkb48.keepdo.*
import com.hkb48.keepdo.db.BackupManager
import com.hkb48.keepdo.db.TaskDatabase
import com.hkb48.keepdo.db.dao.DoneHistoryDao
import com.hkb48.keepdo.db.dao.TaskDao
import com.hkb48.keepdo.db.dao.TaskWithDoneHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KeepdoModule {
    @Singleton
    @Provides
    fun provideTaskDatabase(@ApplicationContext context: Context): TaskDatabase {
        return TaskDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideTaskDao(database: TaskDatabase): TaskDao {
        return database.taskDao()
    }

    @Singleton
    @Provides
    fun provideDoneHistoryDao(database: TaskDatabase): DoneHistoryDao {
        return database.doneHistoryDao()
    }

    @Singleton
    @Provides
    fun provideTaskWithDoneHistoryDao(database: TaskDatabase): TaskWithDoneHistoryDao {
        return database.taskWithDoneHistoryDao()
    }

    @Singleton
    @Provides
    fun provideTaskRepository(
        taskDao: TaskDao,
        doneHistoryDao: DoneHistoryDao,
        taskWithDoneHistoryDao: TaskWithDoneHistoryDao
    ): TaskRepository {
        return TaskRepository(taskDao, doneHistoryDao, taskWithDoneHistoryDao)
    }

    @Singleton
    @Provides
    fun provideReminderManager(
        @ApplicationContext context: Context,
        repository: TaskRepository
    ): ReminderManager {
        return ReminderManager(context, repository)
    }

    @Singleton
    @Provides
    fun provideDateChangeTimeManager(
        @ApplicationContext context: Context,
        reminderManager: ReminderManager
    ): DateChangeTimeManager {
        return DateChangeTimeManager(context, reminderManager)
    }

    @Singleton
    @Provides
    fun provideNotificationController(
        @ApplicationContext context: Context,
        reminderManager: ReminderManager
    ): NotificationController {
        return NotificationController(context, reminderManager)
    }

    @Singleton
    @Provides
    fun provideBackupManager(
        @ApplicationContext context: Context,
        database: TaskDatabase
    ): BackupManager {
        return BackupManager(context, database)
    }

    @Singleton
    @Provides
    fun provideCheckSoundPlayer(
        @ApplicationContext context: Context
    ): CheckSoundPlayer {
        return CheckSoundPlayer(context)
    }
}