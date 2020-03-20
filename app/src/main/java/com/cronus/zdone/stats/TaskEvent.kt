package com.cronus.zdone.stats

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.net.IDN

@Entity(tableName = "task_events")
data class TaskEvent(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    @ColumnInfo(name = "task_id")
    val taskID: String,
    @ColumnInfo(name = "task_name")
    val taskName: String,
    @ColumnInfo(name = "task_result")
    val taskResult: TaskUpdateType,
    @ColumnInfo(name = "expected_duration")
    val expectedDurationSecs: Long,
    @ColumnInfo(name = "duration_secs")
    val durationSecs: Long,
    @ColumnInfo(name = "timestamp_completed_millis")
    val completedAtMillis: Long
)

enum class TaskUpdateType {
    COMPLETED,
    DEFERRED,
}
