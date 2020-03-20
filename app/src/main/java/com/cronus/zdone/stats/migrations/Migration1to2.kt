package com.cronus.zdone.stats.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migration1to2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE task_events ADD COLUMN expected_duration INTEGER NOT NULL DEFAULT 0")
    }
}