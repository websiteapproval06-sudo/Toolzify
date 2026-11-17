package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ChecklistEntity
import com.example.data.local.NoteEntity
import com.example.domain.MediaUtils
import com.example.ui.ToolzifyViewModel
import com.example.ui.components.AnimatedProgressRing
import com.example.ui.components.EmptyStateView
import com.example.ui.components.LuxuryCard
import com.example.ui.components.rememberHapticTrigger
import com.example.ui.theme.LocalToolzifyColors
import com.example.ui.theme.MonoLargeStyle
import com.example.ui.theme.MonoMediumStyle
import kotlinx.coroutines.delay
import java.time.LocalDate

@Composable
fun TodoListScreen(viewModel: ToolzifyViewModel) {
    val todos by viewModel.todos.collectAsStateWithLifecycle()
    val triggerHaptic = rememberHapticTrigger()
    var newTitle by remember { mutableStateOf("") }
    var newPriority by remember { mutableIntStateOf(1) }
    var filterMode by remember { mutableIntStateOf(0) } // 0=All, 1=Active, 2=Done

    val filtered = remember(todos, filterMode) {
        when (filterMode) {
            1 -> todos.filter { !it.isCompleted }
            2 -> todos.filter { it.isCompleted }
            else -> todos
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LuxuryCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    placeholder = { Text("Add a new task...") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("todo_input")
                )
                FilledIconButton(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            triggerHaptic()
                            viewModel.addTodo(
                                title = newTitle,
                                priority = newPriority,
                                dateKey = LocalDate.now().toString()
                            )
                            newTitle = ""
                        }
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add task")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Low" to 0, "Medium" to 1, "High" to 2).forEach { (label, prio) ->
                    FilterChip(
                        selected = newPriority == prio,
                        onClick = { newPriority = prio },
                        label = { Text(label) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All" to 0, "Active" to 1, "Done" to 2).forEach { (lbl, idx) ->
                    FilterChip(selected = filterMode == idx, onClick = { filterMode = idx }, label = { Text(lbl) })
                }
            }
            if (todos.any { it.isCompleted }) {
                TextButton(onClick = { viewModel.clearCompletedTodos() }) {
                    Text("Clear Done")
                }
            }
        }

        if (filtered.isEmpty()) {
            EmptyStateView(
                icon = Icons.Rounded.TaskAlt,
                title = "No Tasks Here",
                description = "Add tasks above. Everything is saved offline in your local Room database."
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered, key = { it.id }) { todo ->
                    LuxuryCard(
                        onClick = {
                            triggerHaptic()
                            viewModel.toggleTodo(todo)
                        },
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = todo.isCompleted,
                                onCheckedChange = {
                                    triggerHaptic()
                                    viewModel.toggleTodo(todo)
                                }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = todo.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )
                                val prioLabel = when (todo.priority) {
                                    2 -> "High Priority"
                                    0 -> "Low Priority"
                                    else -> "Medium Priority"
                                }
                                Text(prioLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { viewModel.deleteTodo(todo) }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyPlannerToolScreen(viewModel: ToolzifyViewModel) {
    val todos by viewModel.todos.collectAsStateWithLifecycle()
    val triggerHaptic = rememberHapticTrigger()
    var taskTitle by remember { mutableStateOf("") }
    var timeSlot by remember { mutableStateOf("09:00 AM") }
    var selectedDateOffset by remember { mutableIntStateOf(0) } // 0=Today, 1=Tomorrow

    val targetDate = LocalDate.now().plusDays(selectedDateOffset.toLong()).toString()
    val dayTasks = remember(todos, targetDate) {
        todos.filter { it.dateKey == targetDate || it.dateKey.isBlank() }
    }
    val completedCount = dayTasks.count { it.isCompleted }
    val progress = if (dayTasks.isEmpty()) 0f else completedCount.toFloat() / dayTasks.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = selectedDateOffset == 0, onClick = { selectedDateOffset = 0 }, label = { Text("Today") })
            FilterChip(selected = selectedDateOffset == 1, onClick = { selectedDateOffset = 1 }, label = { Text("Tomorrow") })
        }

        LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(targetDate, style = MaterialTheme.typography.titleLarge)
                    Text("$completedCount of ${dayTasks.size} scheduled items done", style = MaterialTheme.typography.bodyMedium)
                }
                AnimatedProgressRing(progress = progress, size = 64.dp, strokeWidth = 7.dp) {
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        LuxuryCard {
            OutlinedTextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                label = { Text("Scheduled Block") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf("08:00 AM", "09:00 AM", "11:00 AM", "02:00 PM", "05:00 PM", "08:00 PM")) { slot ->
                    FilterChip(selected = timeSlot == slot, onClick = { timeSlot = slot }, label = { Text(slot) })
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (taskTitle.isNotBlank()) {
                        triggerHaptic()
                        viewModel.addTodo(
                            title = taskTitle,
                            timeSlot = timeSlot,
                            dateKey = targetDate
                        )
                        taskTitle = ""
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Add Time Block") }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(dayTasks, key = { it.id }) { item ->
                LuxuryCard(
                    onClick = { viewModel.toggleTodo(item) },
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(10.dp), color = LocalToolzifyColors.current.goldContainer) {
                            Text(
                                text = item.timeSlot.ifBlank { "Anytime" },
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            modifier = Modifier.weight(1f)
                        )
                        Checkbox(checked = item.isCompleted, onCheckedChange = { viewModel.toggleTodo(item) })
                    }
                }
            }
        }
    }
}

@Composable
fun NotesScreen(viewModel: ToolzifyViewModel) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    var editingNote by remember { mutableStateOf<NoteEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    val filtered = remember(notes, search) {
        val q = search.trim().lowercase()
        if (q.isEmpty()) notes else notes.filter {
            it.title.lowercase().contains(q) || it.content.lowercase().contains(q)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingNote = null
                    showEditor = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "New Note", tint = Color.White)
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search private notes...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (filtered.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Rounded.EditNote,
                    title = "No Notes Yet",
                    description = "Tap + to jot down ideas, journal entries, or reminders. Stored 100% locally."
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtered, key = { it.id }) { note ->
                        LuxuryCard(onClick = {
                            editingNote = note
                            showEditor = true
                        }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(note.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                Row {
                                    IconButton(onClick = { viewModel.toggleNotePin(note) }) {
                                        Icon(
                                            Icons.Rounded.PushPin,
                                            contentDescription = "Pin",
                                            tint = if (note.isPinned) LocalToolzifyColors.current.gold else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deleteNote(note) }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete")
                                    }
                                }
                            }
                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        var title by remember(editingNote) { mutableStateOf(editingNote?.title ?: "") }
        var content by remember(editingNote) { mutableStateOf(editingNote?.content ?: "") }
        var tag by remember(editingNote) { mutableStateOf(editingNote?.tag ?: "General") }

        AlertDialog(
            onDismissRequest = { showEditor = false },
            title = { Text(if (editingNote == null) "New Note" else "Edit Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true)
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Content") }, minLines = 4)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("General", "Work", "Ideas", "Personal")) { t ->
                            FilterChip(selected = tag == t, onClick = { tag = t }, label = { Text(t) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank() || content.isNotBlank()) {
                        viewModel.saveNote(
                            id = editingNote?.id ?: 0L,
                            title = title.ifBlank { "Quick Note" },
                            content = content,
                            tag = tag,
                            isPinned = editingNote?.isPinned ?: false
                        )
                    }
                    showEditor = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditor = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ChecklistMakerScreen(viewModel: ToolzifyViewModel) {
    val checklists by viewModel.checklists.collectAsStateWithLifecycle()
    val allItems by viewModel.checklistItems.collectAsStateWithLifecycle()
    var newListName by remember { mutableStateOf("") }
    var selectedList by remember { mutableStateOf<ChecklistEntity?>(null) }
    var newItemText by remember { mutableStateOf("") }

    LaunchedEffect(checklists) {
        if (selectedList == null && checklists.isNotEmpty()) {
            selectedList = checklists.first()
        } else if (selectedList != null && checklists.none { it.id == selectedList?.id }) {
            selectedList = checklists.firstOrNull()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LuxuryCard {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    placeholder = { Text("New checklist (e.g. Travel Bag)...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    if (newListName.isNotBlank()) {
                        viewModel.createChecklistAndAdd(newListName, null)
                        newListName = ""
                    }
                }) { Text("Create") }
            }
        }

        if (checklists.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(checklists, key = { it.id }) { list ->
                    FilterChip(
                        selected = selectedList?.id == list.id,
                        onClick = { selectedList = list },
                        label = { Text(list.title) }
                    )
                }
            }
        }

        val active = selectedList
        if (active == null) {
            EmptyStateView(
                icon = Icons.Rounded.Checklist,
                title = "No Checklists Yet",
                description = "Create reusable checklists for travel packing, morning routines, or project launches."
            )
        } else {
            val itemsForActive = allItems.filter { it.checklistId == active.id }
            val checkedCount = itemsForActive.count { it.isChecked }
            LuxuryCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(active.title, style = MaterialTheme.typography.titleLarge)
                        Text("$checkedCount / ${itemsForActive.size} checked", style = MaterialTheme.typography.bodySmall)
                    }
                    Row {
                        TextButton(onClick = { viewModel.resetChecklist(active.id) }) { Text("Reset") }
                        IconButton(onClick = { viewModel.deleteChecklist(active) }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete checklist")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        placeholder = { Text("Add checklist item...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    FilledIconButton(onClick = {
                        if (newItemText.isNotBlank()) {
                            viewModel.addChecklistItem(active.id, newItemText)
                            newItemText = ""
                        }
                    }) { Icon(Icons.Rounded.Add, contentDescription = "Add item") }
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(itemsForActive, key = { it.id }) { item ->
                    LuxuryCard(
                        onClick = { viewModel.toggleChecklistItem(item) },
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = item.isChecked, onCheckedChange = { viewModel.toggleChecklistItem(item) })
                            Text(
                                item.text,
                                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.deleteChecklistItem(item) }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Remove")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HabitTrackerScreen(viewModel: ToolzifyViewModel) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val triggerHaptic = rememberHapticTrigger()
    var habitName by remember { mutableStateOf("") }
    var targetPerDay by remember { mutableIntStateOf(1) }
    val today = LocalDate.now().toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LuxuryCard {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    placeholder = { Text("New habit (e.g. Read 15 mins)...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    if (habitName.isNotBlank()) {
                        triggerHaptic()
                        viewModel.addHabit(habitName, targetPerDay)
                        habitName = ""
                    }
                }) { Text("Add") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Daily target:", style = MaterialTheme.typography.labelLarge)
                listOf(1, 2, 3, 5).forEach { t ->
                    FilterChip(selected = targetPerDay == t, onClick = { targetPerDay = t }, label = { Text("${t}x") })
                }
            }
        }

        if (habits.isEmpty()) {
            EmptyStateView(
                icon = Icons.Rounded.LocalFireDepartment,
                title = "Build Mindful Streaks",
                description = "Add daily habits above and tap to log progress with animated streak tracking."
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(habits, key = { it.id }) { habit ->
                    val todayCount = if (habit.lastCompletedDate == today) habit.completedTodayCount else 0
                    val done = todayCount >= habit.targetPerDay
                    val progress = todayCount.toFloat() / habit.targetPerDay.coerceAtLeast(1)

                    LuxuryCard(
                        containerColor = if (done) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else LocalToolzifyColors.current.cardSurface
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(habit.title, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = LocalToolzifyColors.current.gold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Streak: ${habit.currentStreak}d (Best: ${habit.bestStreak}d)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalButton(
                                    onClick = {
                                        triggerHaptic()
                                        viewModel.incrementHabit(habit)
                                    },
                                    enabled = !done
                                ) {
                                    Text(if (done) "Done ✓" else "$todayCount/${habit.targetPerDay} +1")
                                }
                                IconButton(onClick = { viewModel.deleteHabit(habit) }) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PomodoroTimerScreen() {
    val context = LocalContext.current
    val triggerHaptic = rememberHapticTrigger()

    var focusMinutes by remember { mutableIntStateOf(25) }
    var breakMinutes by remember { mutableIntStateOf(5) }
    var isFocusPhase by remember { mutableStateOf(true) }
    var remainingSeconds by remember { mutableIntStateOf(25 * 60) }
    var running by remember { mutableStateOf(false) }
    var completedSessions by remember { mutableIntStateOf(0) }
    var notifyOnComplete by remember { mutableStateOf(false) }

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notifyOnComplete = granted
    }

    LaunchedEffect(running, isFocusPhase) {
        while (running && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
        if (running && remainingSeconds == 0) {
            running = false
            triggerHaptic()
            if (isFocusPhase) completedSessions++
            if (notifyOnComplete) {
                MediaUtils.showTimerFinishedNotification(
                    context,
                    if (isFocusPhase) "Focus Session Complete!" else "Break Finished!",
                    if (isFocusPhase) "Great work. Time for a short break." else "Ready for your next focus sprint."
                )
            }
            isFocusPhase = !isFocusPhase
            remainingSeconds = (if (isFocusPhase) focusMinutes else breakMinutes) * 60
        }
    }

    val totalSec = (if (isFocusPhase) focusMinutes else breakMinutes) * 60
    val progress = 1f - (remainingSeconds.toFloat() / totalSec.coerceAtLeast(1))
    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = isFocusPhase,
                onClick = {
                    running = false
                    isFocusPhase = true
                    remainingSeconds = focusMinutes * 60
                },
                label = { Text("Focus (${focusMinutes}m)") }
            )
            FilterChip(
                selected = !isFocusPhase,
                onClick = {
                    running = false
                    isFocusPhase = false
                    remainingSeconds = breakMinutes * 60
                },
                label = { Text("Break (${breakMinutes}m)") }
            )
        }

        AnimatedProgressRing(
            progress = progress,
            size = 240.dp,
            strokeWidth = 14.dp,
            activeColor = if (isFocusPhase) MaterialTheme.colorScheme.primary else LocalToolzifyColors.current.gold
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (isFocusPhase) "FOCUS" else "BREAK",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    String.format("%02d:%02d", mins, secs),
                    style = MonoLargeStyle,
                    fontWeight = FontWeight.Bold
                )
                Text("Sessions: $completedSessions", style = MaterialTheme.typography.labelMedium)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    triggerHaptic()
                    running = !running
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(52.dp)
            ) {
                Icon(if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (running) "Pause" else "Start")
            }
            OutlinedButton(
                onClick = {
                    running = false
                    remainingSeconds = (if (isFocusPhase) focusMinutes else breakMinutes) * 60
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(52.dp)
            ) {
                Icon(Icons.Rounded.Replay, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset")
            }
        }

        LuxuryCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Completion Notification", style = MaterialTheme.typography.titleSmall)
                Switch(
                    checked = notifyOnComplete,
                    onCheckedChange = { checked ->
                        if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                            if (granted) notifyOnComplete = true
                            else notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            notifyOnComplete = checked
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StopwatchScreen() {
    val triggerHaptic = rememberHapticTrigger()
    var running by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var laps by remember { mutableStateOf<List<Long>>(emptyList()) }

    LaunchedEffect(running) {
        var last = System.currentTimeMillis()
        while (running) {
            delay(35L)
            val now = System.currentTimeMillis()
            elapsedMs += (now - last)
            last = now
        }
    }

    fun formatMs(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        val centi = (ms % 1000) / 10
        return String.format("%02d:%02d.%02d", min, sec, centi)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LuxuryCard(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = formatMs(elapsedMs),
                style = MonoLargeStyle,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    triggerHaptic()
                    running = !running
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(50.dp)
            ) {
                Text(if (running) "Pause" else if (elapsedMs > 0) "Resume" else "Start")
            }
            FilledTonalButton(
                onClick = {
                    triggerHaptic()
                    laps = listOf(elapsedMs) + laps
                },
                enabled = running,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(50.dp)
            ) { Text("Lap") }
            OutlinedButton(
                onClick = {
                    running = false
                    elapsedMs = 0L
                    laps = emptyList()
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(50.dp)
            ) { Text("Reset") }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(laps.indices.toList()) { idx ->
                LuxuryCard(contentPadding = PaddingValues(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Lap #${laps.size - idx}", style = MaterialTheme.typography.titleSmall)
                        Text(formatMs(laps[idx]), style = MonoMediumStyle)
                    }
                }
            }
        }
    }
}

@Composable
fun CountdownTimerScreen() {
    val triggerHaptic = rememberHapticTrigger()
    var initialSeconds by remember { mutableIntStateOf(300) }
    var remainingSeconds by remember { mutableIntStateOf(300) }
    var running by remember { mutableStateOf(false) }
    var finishedBanner by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        while (running && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
        if (running && remainingSeconds == 0) {
            running = false
            finishedBanner = true
            triggerHaptic()
        }
    }

    val progress = if (initialSeconds == 0) 0f else remainingSeconds.toFloat() / initialSeconds
    val h = remainingSeconds / 3600
    val m = (remainingSeconds % 3600) / 60
    val s = remainingSeconds % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedProgressRing(progress = progress, size = 210.dp, strokeWidth = 12.dp) {
            Text(String.format("%02d:%02d:%02d", h, m, s), style = MonoLargeStyle)
        }

        if (finishedBanner) {
            Surface(shape = RoundedCornerShape(14.dp), color = LocalToolzifyColors.current.goldContainer) {
                Text(
                    "Time is up!",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf(60 to "1m", 180 to "3m", 300 to "5m", 600 to "10m", 900 to "15m", 1800 to "30m")) { (sec, label) ->
                FilterChip(
                    selected = initialSeconds == sec,
                    onClick = {
                        running = false
                        finishedBanner = false
                        initialSeconds = sec
                        remainingSeconds = sec
                    },
                    label = { Text(label) }
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    finishedBanner = false
                    running = !running
                },
                enabled = remainingSeconds > 0,
                shape = RoundedCornerShape(16.dp)
            ) { Text(if (running) "Pause" else "Start") }
            OutlinedButton(
                onClick = {
                    running = false
                    finishedBanner = false
                    remainingSeconds = initialSeconds
                },
                shape = RoundedCornerShape(16.dp)
            ) { Text("Reset") }
        }
    }
}

@Composable
fun DailyGoalsScreen(viewModel: ToolzifyViewModel) {
    val goals by viewModel.dailyGoals.collectAsStateWithLifecycle()
    val triggerHaptic = rememberHapticTrigger()
    var title by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("10") }
    var unit by remember { mutableStateOf("pages") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LuxuryCard {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Goal (e.g. Read Book)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = { Text("Target") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        triggerHaptic()
                        viewModel.addDailyGoal(title, targetStr.toIntOrNull() ?: 10, unit)
                        title = ""
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Add Daily Goal") }
        }

        if (goals.isEmpty()) {
            EmptyStateView(
                icon = Icons.Rounded.Flag,
                title = "No Daily Goals Yet",
                description = "Track reading pages, steps, study hours, or push-ups with quick +/- steppers."
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(goals, key = { it.id }) { goal ->
                    val progress = goal.currentValue.toFloat() / goal.targetValue.coerceAtLeast(1)
                    LuxuryCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(goal.title, style = MaterialTheme.typography.titleMedium)
                                Text("${goal.currentValue} / ${goal.targetValue} ${goal.unit}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.updateDailyGoalValue(goal, -1) }) {
                                    Icon(Icons.Rounded.RemoveCircleOutline, contentDescription = "Decrease")
                                }
                                IconButton(onClick = { viewModel.updateDailyGoalValue(goal, +1) }) {
                                    Icon(Icons.Rounded.AddCircle, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { viewModel.deleteDailyGoal(goal) }) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }
        }
    }
}
