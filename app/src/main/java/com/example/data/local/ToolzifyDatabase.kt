package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TodoEntity::class,
        NoteEntity::class,
        ChecklistEntity::class,
        ChecklistItemEntity::class,
        HabitEntity::class,
        DailyGoalEntity::class,
        ShoppingItemEntity::class,
        ExpenseEntity::class,
        WaterLogEntity::class,
        RamadanEntryEntity::class,
        ToolUsageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ToolzifyDatabase : RoomDatabase() {
    abstract fun toolzifyDao(): ToolzifyDao

    companion object {
        @Volatile
        private var INSTANCE: ToolzifyDatabase? = null

        fun getInstance(context: Context): ToolzifyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ToolzifyDatabase::class.java,
                    "toolzify_local.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
