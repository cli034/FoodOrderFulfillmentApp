package com.example.csschallenge.di

import android.content.Context
import com.example.csschallenge.data.database.AppDatabase
import com.example.csschallenge.data.database.OrderEventDao
import com.example.csschallenge.data.database.createDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return createDatabase(context)
    }

    @Provides
    fun provideOrderEventDao(database: AppDatabase): OrderEventDao {
        return database.orderEventDao()
    }
}