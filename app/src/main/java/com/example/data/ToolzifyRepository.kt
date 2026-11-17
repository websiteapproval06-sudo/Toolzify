package com.example.data

import com.example.data.local.*
import com.example.data.preferences.PreferencesRepository
import com.example.data.preferences.UserPreferences
import com.example.ui.theme.ThemeAccent
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ToolzifyRepository(
    private val dao: ToolzifyDao,
    private val prefs: PreferencesRepository
) {
    val userPreferences: Flow<UserPreferences> = prefs.preferencesFlow

    val todos: Flow<List<TodoEntity>> = dao.observeTodos()
    val notes: Flow<List<NoteEntity>> = dao.observeNotes()
    val checklists: Flow<List<ChecklistEntity>> = dao.observeChecklists()
    val checklistItems: Flow<List<ChecklistItemEntity>> = dao.observeAllChecklistItems()
    val habits: Flow<List<HabitEntity>> = dao.observeHabits()
    val dailyGoals: Flow<List<DailyGoalEntity>> = dao.observeDailyGoals()
    val shoppingItems: Flow<List<ShoppingItemEntity>> = dao.observeShoppingItems()
    val expenses: Flow<List<ExpenseEntity>> = dao.observeExpenses()
    val ramadanEntries: Flow<List<RamadanEntryEntity>> = dao.observeRamadanEntries()
    val toolUsage: Flow<List<ToolUsageEntity>> = dao.observeToolUsage()

    fun waterLogsToday(dateKey: String = LocalDate.now().toString()): Flow<List<WaterLogEntity>> =
        dao.observeWaterLogsForDate(dateKey)

    // Tool Usage & Favorites
    suspend fun recordToolOpened(toolId: String) {
        val current = dao.getToolUsage(toolId)
        val updated = if (current == null) {
            ToolUsageEntity(
                toolId = toolId,
                isFavorite = false,
                useCount = 1,
                lastUsedAt = System.currentTimeMillis()
            )
        } else {
            current.copy(
                useCount = current.useCount + 1,
                lastUsedAt = System.currentTimeMillis()
            )
        }
        dao.upsertToolUsage(updated)
    }

    suspend fun toggleFavorite(toolId: String) {
        val current = dao.getToolUsage(toolId)
        val updated = if (current == null) {
            ToolUsageEntity(toolId = toolId, isFavorite = true, useCount = 0, lastUsedAt = 0L)
        } else {
            current.copy(isFavorite = !current.isFavorite)
        }
        dao.upsertToolUsage(updated)
    }

    // Todos & Planner
    suspend fun addTodo(title: String, notes: String, category: String, priority: Int, timeSlot: String, dateKey: String) {
        dao.insertTodo(
            TodoEntity(
                title = title.trim(),
                notes = notes.trim(),
                category = category,
                priority = priority,
                timeSlot = timeSlot.trim(),
                dateKey = dateKey
            )
        )
    }

    suspend fun toggleTodo(todo: TodoEntity) = dao.updateTodo(todo.copy(isCompleted = !todo.isCompleted))
    suspend fun deleteTodo(todo: TodoEntity) = dao.deleteTodo(todo)
    suspend fun clearCompletedTodos() = dao.clearCompletedTodos()

    // Notes
    suspend fun saveNote(id: Long = 0, title: String, content: String, tag: String, isPinned: Boolean = false) {
        dao.insertNote(
            NoteEntity(
                id = id,
                title = title.trim(),
                content = content.trim(),
                tag = tag,
                isPinned = isPinned,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleNotePin(note: NoteEntity) = dao.updateNote(note.copy(isPinned = !note.isPinned))
    suspend fun deleteNote(note: NoteEntity) = dao.deleteNote(note)

    // Checklists
    suspend fun createChecklist(title: String): Long = dao.insertChecklist(ChecklistEntity(title = title.trim()))
    suspend fun deleteChecklist(checklist: ChecklistEntity) = dao.deleteChecklist(checklist)
    suspend fun addChecklistItem(checklistId: Long, text: String) =
        dao.insertChecklistItem(ChecklistItemEntity(checklistId = checklistId, text = text.trim()))
    suspend fun toggleChecklistItem(item: ChecklistItemEntity) =
        dao.updateChecklistItem(item.copy(isChecked = !item.isChecked))
    suspend fun deleteChecklistItem(item: ChecklistItemEntity) = dao.deleteChecklistItem(item)
    suspend fun resetChecklist(checklistId: Long) = dao.resetChecklistItems(checklistId)

    // Habits
    suspend fun addHabit(title: String, targetPerDay: Int) {
        dao.insertHabit(HabitEntity(title = title.trim(), targetPerDay = targetPerDay.coerceIn(1, 20)))
    }

    suspend fun incrementHabit(habit: HabitEntity) {
        val today = LocalDate.now().toString()
        val baseCount = if (habit.lastCompletedDate == today) habit.completedTodayCount else 0
        val newCount = (baseCount + 1).coerceAtMost(habit.targetPerDay)
        val reachedTargetNow = newCount >= habit.targetPerDay && (habit.lastCompletedDate != today || baseCount < habit.targetPerDay)

        val newStreak = if (reachedTargetNow) {
            val yesterday = LocalDate.now().minusDays(1).toString()
            if (habit.lastCompletedDate == yesterday || habit.lastCompletedDate == today) {
                habit.currentStreak + 1
            } else {
                1
            }
        } else {
            habit.currentStreak
        }
        val newBest = maxOf(habit.bestStreak, newStreak)

        dao.updateHabit(
            habit.copy(
                completedTodayCount = newCount,
                currentStreak = newStreak,
                bestStreak = newBest,
                lastCompletedDate = today
            )
        )
    }

    suspend fun resetHabitToday(habit: HabitEntity) {
        dao.updateHabit(habit.copy(completedTodayCount = 0))
    }

    suspend fun deleteHabit(habit: HabitEntity) = dao.deleteHabit(habit)

    // Daily Goals
    suspend fun addDailyGoal(title: String, target: Int, unit: String) {
        dao.insertDailyGoal(
            DailyGoalEntity(
                title = title.trim(),
                currentValue = 0,
                targetValue = target.coerceAtLeast(1),
                unit = unit.trim().ifEmpty { "units" },
                dateKey = LocalDate.now().toString()
            )
        )
    }

    suspend fun updateDailyGoalValue(goal: DailyGoalEntity, delta: Int) {
        val next = (goal.currentValue + delta).coerceIn(0, goal.targetValue * 2)
        dao.updateDailyGoal(goal.copy(currentValue = next))
    }

    suspend fun deleteDailyGoal(goal: DailyGoalEntity) = dao.deleteDailyGoal(goal)

    // Shopping List
    suspend fun addShoppingItem(name: String, qty: String, category: String, price: Double) {
        dao.insertShoppingItem(
            ShoppingItemEntity(
                name = name.trim(),
                quantity = qty.trim().ifEmpty { "1" },
                category = category,
                estimatedPrice = price.coerceAtLeast(0.0)
            )
        )
    }

    suspend fun toggleShoppingItem(item: ShoppingItemEntity) =
        dao.updateShoppingItem(item.copy(isPurchased = !item.isPurchased))

    suspend fun deleteShoppingItem(item: ShoppingItemEntity) = dao.deleteShoppingItem(item)
    suspend fun clearPurchasedShopping() = dao.clearPurchasedShoppingItems()

    // Expenses
    suspend fun addExpense(title: String, amount: Double, category: String, isIncome: Boolean, note: String) {
        dao.insertExpense(
            ExpenseEntity(
                title = title.trim(),
                amount = amount.coerceAtLeast(0.0),
                category = category,
                isIncome = isIncome,
                note = note.trim()
            )
        )
    }

    suspend fun deleteExpense(expense: ExpenseEntity) = dao.deleteExpense(expense)

    // Water Intake
    suspend fun logWater(amountMl: Int) {
        dao.insertWaterLog(
            WaterLogEntity(
                dateKey = LocalDate.now().toString(),
                amountMl = amountMl
            )
        )
    }

    suspend fun deleteWaterLog(log: WaterLogEntity) = dao.deleteWaterLog(log)

    // Ramadan Planner
    suspend fun saveRamadanEntry(entry: RamadanEntryEntity) = dao.upsertRamadanEntry(entry)

    // Preferences
    suspend fun setThemeMode(mode: ThemeMode) = prefs.setThemeMode(mode)
    suspend fun setThemeAccent(accent: ThemeAccent) = prefs.setThemeAccent(accent)
    suspend fun setAppLanguage(code: String) = prefs.setAppLanguage(code)
    suspend fun setHapticsEnabled(enabled: Boolean) = prefs.setHapticsEnabled(enabled)
    suspend fun setAnimationsEnabled(enabled: Boolean) = prefs.setAnimationsEnabled(enabled)
    suspend fun setShowIslamicTools(show: Boolean) = prefs.setShowIslamicTools(show)
    suspend fun setOnboardingCompleted(done: Boolean) = prefs.setOnboardingCompleted(done)
    suspend fun setDailyWaterTarget(ml: Int) = prefs.setDailyWaterTarget(ml)
    suspend fun setMonthlyBudgetLimit(limit: Double) = prefs.setMonthlyBudgetLimit(limit)
    suspend fun setSelectedCityId(cityId: String) = prefs.setSelectedCityId(cityId)
    suspend fun setPrayerMethodId(methodId: String) = prefs.setPrayerMethodId(methodId)
    suspend fun updateTasbeeh(count: Int, target: Int, phraseIndex: Int) =
        prefs.updateTasbeeh(count, target, phraseIndex)

    suspend fun clearAllLocalData() {
        dao.wipeTodos()
        dao.wipeNotes()
        dao.wipeChecklists()
        dao.wipeHabits()
        dao.wipeDailyGoals()
        dao.wipeShopping()
        dao.wipeExpenses()
        dao.wipeWaterLogs()
        dao.wipeRamadan()
        dao.wipeToolUsage()
        prefs.clearAllPreferences()
    }
}
