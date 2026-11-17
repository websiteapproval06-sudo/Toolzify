package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ToolzifyRepository
import com.example.data.local.*
import com.example.data.preferences.PreferencesRepository
import com.example.data.preferences.UserPreferences
import com.example.model.ToolDefinition
import com.example.model.ToolRegistry
import com.example.ui.theme.ThemeAccent
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ToolzifyViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ToolzifyDatabase.getInstance(application)
    private val prefsRepo = PreferencesRepository(application)
    val repository = ToolzifyRepository(database.toolzifyDao(), prefsRepo)

    val preferences: StateFlow<UserPreferences> = repository.userPreferences
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    val todos: StateFlow<List<TodoEntity>> = repository.todos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.notes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val checklists: StateFlow<List<ChecklistEntity>> = repository.checklists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val checklistItems: StateFlow<List<ChecklistItemEntity>> = repository.checklistItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<HabitEntity>> = repository.habits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyGoals: StateFlow<List<DailyGoalEntity>> = repository.dailyGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shoppingItems: StateFlow<List<ShoppingItemEntity>> = repository.shoppingItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val waterLogsToday: StateFlow<List<WaterLogEntity>> = repository.waterLogsToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ramadanEntries: StateFlow<List<RamadanEntryEntity>> = repository.ramadanEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val toolUsageMap: StateFlow<Map<String, ToolUsageEntity>> = repository.toolUsage
        .map { list -> list.associateBy { it.toolId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val favoriteTools: StateFlow<List<ToolDefinition>> = combine(toolUsageMap, preferences) { usage, prefs ->
        ToolRegistry.allTools.filter { tool ->
            (prefs.showIslamicTools || !tool.category.isOptional) && (usage[tool.id]?.isFavorite == true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTools: StateFlow<List<ToolDefinition>> = combine(toolUsageMap, preferences) { usage, prefs ->
        usage.values
            .filter { it.lastUsedAt > 0L }
            .sortedByDescending { it.lastUsedAt }
            .mapNotNull { ToolRegistry.findById(it.toolId) }
            .filter { prefs.showIslamicTools || !it.category.isOptional }
            .take(8)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onToolOpened(toolId: String) {
        viewModelScope.launch { repository.recordToolOpened(toolId) }
    }

    fun toggleFavorite(toolId: String) {
        viewModelScope.launch { repository.toggleFavorite(toolId) }
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repository.setThemeMode(mode) }
    fun setThemeAccent(accent: ThemeAccent) = viewModelScope.launch { repository.setThemeAccent(accent) }
    fun setAppLanguage(code: String) = viewModelScope.launch { repository.setAppLanguage(code) }
    fun setHapticsEnabled(enabled: Boolean) = viewModelScope.launch { repository.setHapticsEnabled(enabled) }
    fun setAnimationsEnabled(enabled: Boolean) = viewModelScope.launch { repository.setAnimationsEnabled(enabled) }
    fun setShowIslamicTools(show: Boolean) = viewModelScope.launch { repository.setShowIslamicTools(show) }
    fun completeOnboarding() = viewModelScope.launch { repository.setOnboardingCompleted(true) }
    fun setDailyWaterTarget(ml: Int) = viewModelScope.launch { repository.setDailyWaterTarget(ml) }
    fun setMonthlyBudgetLimit(limit: Double) = viewModelScope.launch { repository.setMonthlyBudgetLimit(limit) }
    fun setSelectedCityId(cityId: String) = viewModelScope.launch { repository.setSelectedCityId(cityId) }
    fun setPrayerMethodId(methodId: String) = viewModelScope.launch { repository.setPrayerMethodId(methodId) }
    fun updateTasbeeh(count: Int, target: Int, phraseIndex: Int) =
        viewModelScope.launch { repository.updateTasbeeh(count, target, phraseIndex) }

    fun addTodo(title: String, notes: String = "", category: String = "Personal", priority: Int = 1, timeSlot: String = "", dateKey: String = "") =
        viewModelScope.launch { repository.addTodo(title, notes, category, priority, timeSlot, dateKey) }

    fun toggleTodo(todo: TodoEntity) = viewModelScope.launch { repository.toggleTodo(todo) }
    fun deleteTodo(todo: TodoEntity) = viewModelScope.launch { repository.deleteTodo(todo) }
    fun clearCompletedTodos() = viewModelScope.launch { repository.clearCompletedTodos() }

    fun saveNote(id: Long = 0, title: String, content: String, tag: String, isPinned: Boolean = false) =
        viewModelScope.launch { repository.saveNote(id, title, content, tag, isPinned) }

    fun toggleNotePin(note: NoteEntity) = viewModelScope.launch { repository.toggleNotePin(note) }
    fun deleteNote(note: NoteEntity) = viewModelScope.launch { repository.deleteNote(note) }

    fun createChecklistAndAdd(title: String, firstItem: String?) = viewModelScope.launch {
        val id = repository.createChecklist(title)
        if (!firstItem.isNullOrBlank()) repository.addChecklistItem(id, firstItem)
    }

    fun deleteChecklist(checklist: ChecklistEntity) = viewModelScope.launch { repository.deleteChecklist(checklist) }
    fun addChecklistItem(checklistId: Long, text: String) =
        viewModelScope.launch { repository.addChecklistItem(checklistId, text) }
    fun toggleChecklistItem(item: ChecklistItemEntity) =
        viewModelScope.launch { repository.toggleChecklistItem(item) }
    fun deleteChecklistItem(item: ChecklistItemEntity) =
        viewModelScope.launch { repository.deleteChecklistItem(item) }
    fun resetChecklist(checklistId: Long) = viewModelScope.launch { repository.resetChecklist(checklistId) }

    fun addHabit(title: String, targetPerDay: Int) =
        viewModelScope.launch { repository.addHabit(title, targetPerDay) }
    fun incrementHabit(habit: HabitEntity) = viewModelScope.launch { repository.incrementHabit(habit) }
    fun resetHabitToday(habit: HabitEntity) = viewModelScope.launch { repository.resetHabitToday(habit) }
    fun deleteHabit(habit: HabitEntity) = viewModelScope.launch { repository.deleteHabit(habit) }

    fun addDailyGoal(title: String, target: Int, unit: String) =
        viewModelScope.launch { repository.addDailyGoal(title, target, unit) }
    fun updateDailyGoalValue(goal: DailyGoalEntity, delta: Int) =
        viewModelScope.launch { repository.updateDailyGoalValue(goal, delta) }
    fun deleteDailyGoal(goal: DailyGoalEntity) = viewModelScope.launch { repository.deleteDailyGoal(goal) }

    fun addShoppingItem(name: String, qty: String, category: String, price: Double) =
        viewModelScope.launch { repository.addShoppingItem(name, qty, category, price) }
    fun toggleShoppingItem(item: ShoppingItemEntity) =
        viewModelScope.launch { repository.toggleShoppingItem(item) }
    fun deleteShoppingItem(item: ShoppingItemEntity) =
        viewModelScope.launch { repository.deleteShoppingItem(item) }
    fun clearPurchasedShopping() = viewModelScope.launch { repository.clearPurchasedShopping() }

    fun addExpense(title: String, amount: Double, category: String, isIncome: Boolean, note: String = "") =
        viewModelScope.launch { repository.addExpense(title, amount, category, isIncome, note) }
    fun deleteExpense(expense: ExpenseEntity) = viewModelScope.launch { repository.deleteExpense(expense) }

    fun logWater(amountMl: Int) = viewModelScope.launch { repository.logWater(amountMl) }
    fun deleteWaterLog(log: WaterLogEntity) = viewModelScope.launch { repository.deleteWaterLog(log) }

    fun saveRamadanEntry(entry: RamadanEntryEntity) = viewModelScope.launch { repository.saveRamadanEntry(entry) }

    fun clearAllLocalData() = viewModelScope.launch { repository.clearAllLocalData() }
}
