package com.cronus.zdone.stats

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskEventsDao {

    @Query("SELECT * FROM task_events WHERE timestamp_completed_millis > :timestamp")
    fun getTaskEventsSince(timestamp: Long): Flow<List<TaskEvent>>

    @Query("SELECT * FROM task_events")
    fun getTaskEvents(): Flow<List<TaskEvent>>

    @Insert
    fun addTaskEvent(taskEvent: TaskEvent)

    @Update
    fun updateEvent(taskEvent: TaskEvent)

    @Delete
    fun deleteEvent(taskEvent: TaskEvent)

}