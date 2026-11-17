package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolzifyDao {

    // Todos & Daily Planner
    @Query("SELECT * FROM todos ORDER BY isCompleted ASC, priority DESC, createdAt DESC")
    fun observeTodos(): Flow<List<TodoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity)

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)

    @Query("DELETE FROM todos WHERE isCompleted = 1")
    suspend fun clearCompletedTodos()

    // Notes
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun observeNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    // Checklists
    @Query("SELECT * FROM checklists ORDER BY createdAt DESC")
    fun observeChecklists(): Flow<List<ChecklistEntity>>

    @Query("SELECT * FROM checklist_items ORDER BY id ASC")
    fun observeAllChecklistItems(): Flow<List<ChecklistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklist(checklist: ChecklistEntity): Long

    @Delete
    suspend fun deleteChecklist(checklist: ChecklistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItem(item: ChecklistItemEntity)

    @Update
    suspend fun updateChecklistItem(item: ChecklistItemEntity)

    @Delete
    suspend fun deleteChecklistItem(item: ChecklistItemEntity)

    @Query("UPDATE checklist_items SET isChecked = 0 WHERE checklistId = :checklistId")
    suspend fun resetChecklistItems(checklistId: Long)

    // Habits
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun observeHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    // Daily Goals
    @Query("SELECT * FROM daily_goals ORDER BY id DESC")
    fun observeDailyGoals(): Flow<List<DailyGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyGoal(goal: DailyGoalEntity)

    @Update
    suspend fun updateDailyGoal(goal: DailyGoalEntity)

    @Delete
    suspend fun deleteDailyGoal(goal: DailyGoalEntity)

    // Shopping List
    @Query("SELECT * FROM shopping_items ORDER BY isPurchased ASC, id DESC")
    fun observeShoppingItems(): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItemEntity)

    @Update
    suspend fun updateShoppingItem(item: ShoppingItemEntity)

    @Delete
    suspend fun deleteShoppingItem(item: ShoppingItemEntity)

    @Query("DELETE FROM shopping_items WHERE isPurchased = 1")
    suspend fun clearPurchasedShoppingItems()

    // Expenses & Budget
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun observeExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    // Water Intake
    @Query("SELECT * FROM water_logs WHERE dateKey = :dateKey ORDER BY timestamp DESC")
    fun observeWaterLogsForDate(dateKey: String): Flow<List<WaterLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLogEntity)

    @Delete
    suspend fun deleteWaterLog(log: WaterLogEntity)

    // Ramadan Planner
    @Query("SELECT * FROM ramadan_entries ORDER BY dayNumber ASC")
    fun observeRamadanEntries(): Flow<List<RamadanEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRamadanEntry(entry: RamadanEntryEntity)

    // Tool Usage & Favorites
    @Query("SELECT * FROM tool_usage")
    fun observeToolUsage(): Flow<List<ToolUsageEntity>>

    @Query("SELECT * FROM tool_usage WHERE toolId = :toolId LIMIT 1")
    suspend fun getToolUsage(toolId: String): ToolUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertToolUsage(usage: ToolUsageEntity)

    // Clear All Data
    @Query("DELETE FROM todos") suspend fun wipeTodos()
    @Query("DELETE FROM notes") suspend fun wipeNotes()
    @Query("DELETE FROM checklists") suspend fun wipeChecklists()
    @Query("DELETE FROM habits") suspend fun wipeHabits()
    @Query("DELETE FROM daily_goals") suspend fun wipeDailyGoals()
    @Query("DELETE FROM shopping_items") suspend fun wipeShopping()
    @Query("DELETE FROM expenses") suspend fun wipeExpenses()
    @Query("DELETE FROM water_logs") suspend fun wipeWaterLogs()
    @Query("DELETE FROM ramadan_entries") suspend fun wipeRamadan()
    @Query("DELETE FROM tool_usage") suspend fun wipeToolUsage()
}
