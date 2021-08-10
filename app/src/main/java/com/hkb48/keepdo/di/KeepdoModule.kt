package com.hkb48.keepdo.di

import android.content.Context
import com.hkb48.keepdo.TaskRepository
import com.hkb48.keepdo.db.TaskDatabase
import com.hkb48.keepdo.db.dao.TaskCompletionDao
import com.hkb48.keepdo.db.dao.TaskDao
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
    fun provideTaskCompletionDao(database: TaskDatabase): TaskCompletionDao {
        return database.taskCompletionDao()
    }

    @Singleton
    @Provides
    fun provideTaskRepository(
        taskDao: TaskDao,
        taskCompletionDao: TaskCompletionDao
    ): TaskRepository {
        return TaskRepository(taskDao, taskCompletionDao)
    }
}