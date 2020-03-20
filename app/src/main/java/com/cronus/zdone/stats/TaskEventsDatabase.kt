package com.cronus.zdone.stats

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Database(entities = [TaskEvent::class], version = 2)
@TypeConverters(Converters::class)
abstract class TaskEventsDatabase : RoomDatabase() {
    abstract fun taskEventsDao(): TaskEventsDao
}

private object Converters {

    @TypeConverter
    @JvmStatic
    fun fromString(name: String) = TaskUpdateType.valueOf(name)

    @TypeConverter
    @JvmStatic
    fun fromUpdateType(taskUpdateType: TaskUpdateType) = taskUpdateType.name

}