package com.example.csschallenge.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [OrderEventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderEventDao(): OrderEventDao

    companion object {
        const val ORDER_EVENTS_DB_NAME = "order_events_database"
    }
}

fun createDatabase(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        AppDatabase.ORDER_EVENTS_DB_NAME,
    ).build()
}