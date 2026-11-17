package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategory(val id: String, val title: String, val icon: ImageVector, val isOptional: Boolean = false) {
    CALCULATORS("calculators", "Calculators", Icons.Rounded.Calculate),
    PRODUCTIVITY("productivity", "Productivity", Icons.Rounded.TaskAlt),
    TEXT("text", "Text Tools", Icons.Rounded.TextFields),
    UTILITIES("utilities", "Everyday Utilities", Icons.Rounded.Handyman),
    ISLAMIC("islamic", "Islamic Tools", Icons.Rounded.Mosque, isOptional = true)
}

data class ToolDefinition(
    val id: String,
    val name: String,
    val shortDesc: String,
    val category: ToolCategory,
    val icon: ImageVector,
    val isFeatured: Boolean = false,
    val isPopular: Boolean = false,
    val keywords: List<String> = emptyList()
)

object ToolIds {
    // Calculators (9)
    const val STANDARD_CALC = "standard_calc"
    const val SCIENTIFIC_CALC = "scientific_calc"
    const val PERCENTAGE_CALC = "percentage_calc"
    const val AGE_CALC = "age_calc"
    const val DATE_DIFF_CALC = "date_diff_calc"
    const val DISCOUNT_CALC = "discount_calc"
    const val GST_CALC = "gst_calc"
    const val UNIT_CONVERTER = "unit_converter"
    const val BMI_CALC = "bmi_calc"

    // Productivity (9)
    const val TODO_LIST = "todo_list"
    const val DAILY_PLANNER = "daily_planner"
    const val NOTES = "notes"
    const val CHECKLIST_MAKER = "checklist_maker"
    const val HABIT_TRACKER = "habit_tracker"
    const val POMODORO_TIMER = "pomodoro_timer"
    const val STOPWATCH = "stopwatch"
    const val COUNTDOWN_TIMER = "countdown_timer"
    const val DAILY_GOALS = "daily_goals"

    // Text Tools (7)
    const val WORD_COUNTER = "word_counter"
    const val CHAR_COUNTER = "char_counter"
    const val CASE_CONVERTER = "case_converter"
    const val REMOVE_SPACES = "remove_spaces"
    const val TEXT_REVERSER = "text_reverser"
    const val PASSWORD_GEN = "password_gen"
    const val RANDOM_NUMBER = "random_number"

    // Everyday Utilities (8)
    const val QR_GENERATOR = "qr_generator"
    const val QR_SCANNER = "qr_scanner"
    const val IMAGE_RESIZER = "image_resizer"
    const val SHOPPING_LIST = "shopping_list"
    const val EXPENSE_TRACKER = "expense_tracker"
    const val SAVINGS_CALC = "savings_calc"
    const val WATER_INTAKE = "water_intake"
    const val BUDGET_PLANNER = "budget_planner"

    // Optional Islamic Tools (6)
    const val PRAYER_TIMES = "prayer_times"
    const val QIBLA_COMPASS = "qibla_compass"
    const val TASBEEH_COUNTER = "tasbeeh_counter"
    const val DAILY_DUAS = "daily_duas"
    const val RAMADAN_PLANNER = "ramadan_planner"
    const val ISLAMIC_DATE_CONV = "islamic_date_conv"
}

object ToolRegistry {
    val allTools: List<ToolDefinition> = listOf(
        // A. Calculators
        ToolDefinition(ToolIds.STANDARD_CALC, "Calculator", "Fast daily arithmetic with history tape", ToolCategory.CALCULATORS, Icons.Rounded.Calculate, isFeatured = true, isPopular = true, keywords = listOf("math", "add", "multiply")),
        ToolDefinition(ToolIds.SCIENTIFIC_CALC, "Scientific Calc", "Trig, logs, roots, powers & radians", ToolCategory.CALCULATORS, Icons.Rounded.Functions, keywords = listOf("sin", "cos", "log", "science")),
        ToolDefinition(ToolIds.PERCENTAGE_CALC, "Percentage Calc", "Find % of value, ratio & % change", ToolCategory.CALCULATORS, Icons.Rounded.Percent, isPopular = true),
        ToolDefinition(ToolIds.AGE_CALC, "Age Calculator", "Exact age & next birthday countdown", ToolCategory.CALCULATORS, Icons.Rounded.Cake, keywords = listOf("birthday", "years")),
        ToolDefinition(ToolIds.DATE_DIFF_CALC, "Date Difference", "Days, weeks & weekdays between dates", ToolCategory.CALCULATORS, Icons.Rounded.DateRange),
        ToolDefinition(ToolIds.DISCOUNT_CALC, "Discount Calc", "Sale price, tax & instant savings", ToolCategory.CALCULATORS, Icons.Rounded.LocalOffer, isPopular = true),
        ToolDefinition(ToolIds.GST_CALC, "GST Calculator", "Add or extract GST with CGST/SGST", ToolCategory.CALCULATORS, Icons.Rounded.ReceiptLong),
        ToolDefinition(ToolIds.UNIT_CONVERTER, "Unit Converter", "Length, mass, temp, speed & data", ToolCategory.CALCULATORS, Icons.Rounded.SwapHoriz, isFeatured = true, isPopular = true),
        ToolDefinition(ToolIds.BMI_CALC, "BMI Calculator", "Offline body mass & healthy range", ToolCategory.CALCULATORS, Icons.Rounded.MonitorWeight),

        // B. Productivity
        ToolDefinition(ToolIds.TODO_LIST, "To-Do List", "Prioritized daily tasks stored locally", ToolCategory.PRODUCTIVITY, Icons.Rounded.CheckCircle, isFeatured = true, isPopular = true),
        ToolDefinition(ToolIds.DAILY_PLANNER, "Daily Planner", "Time-blocked schedule for today", ToolCategory.PRODUCTIVITY, Icons.Rounded.EventNote),
        ToolDefinition(ToolIds.NOTES, "Notes", "Pin, tag & search private offline notes", ToolCategory.PRODUCTIVITY, Icons.Rounded.EditNote, isPopular = true),
        ToolDefinition(ToolIds.CHECKLIST_MAKER, "Checklist Maker", "Reusable lists for travel & errands", ToolCategory.PRODUCTIVITY, Icons.Rounded.Checklist),
        ToolDefinition(ToolIds.HABIT_TRACKER, "Habit Tracker", "Build streaks with mindful daily reps", ToolCategory.PRODUCTIVITY, Icons.Rounded.LocalFireDepartment, isFeatured = true),
        ToolDefinition(ToolIds.POMODORO_TIMER, "Pomodoro Timer", "Focused 25/5 work & break intervals", ToolCategory.PRODUCTIVITY, Icons.Rounded.Timer, isPopular = true),
        ToolDefinition(ToolIds.STOPWATCH, "Stopwatch", "Millisecond timer with lap tracking", ToolCategory.PRODUCTIVITY, Icons.Rounded.AvTimer),
        ToolDefinition(ToolIds.COUNTDOWN_TIMER, "Countdown Timer", "Custom timers with preset shortcuts", ToolCategory.PRODUCTIVITY, Icons.Rounded.HourglassBottom),
        ToolDefinition(ToolIds.DAILY_GOALS, "Daily Goals", "Track custom numeric targets each day", ToolCategory.PRODUCTIVITY, Icons.Rounded.Flag),

        // C. Text Tools
        ToolDefinition(ToolIds.WORD_COUNTER, "Word Counter", "Words, sentences & reading time", ToolCategory.TEXT, Icons.Rounded.FormatListNumbered, isPopular = true),
        ToolDefinition(ToolIds.CHAR_COUNTER, "Character Counter", "Characters, lines & UTF-8 bytes", ToolCategory.TEXT, Icons.Rounded.Pin),
        ToolDefinition(ToolIds.CASE_CONVERTER, "Case Converter", "UPPER, Title, camelCase & snake_case", ToolCategory.TEXT, Icons.Rounded.TextFormat),
        ToolDefinition(ToolIds.REMOVE_SPACES, "Clean Spaces", "Trim, collapse spaces & blank lines", ToolCategory.TEXT, Icons.Rounded.CleaningServices),
        ToolDefinition(ToolIds.TEXT_REVERSER, "Text Reverser", "Flip full text, words or characters", ToolCategory.TEXT, Icons.Rounded.Flip),
        ToolDefinition(ToolIds.PASSWORD_GEN, "Password Gen", "Cryptographic offline password builder", ToolCategory.TEXT, Icons.Rounded.Password, isFeatured = true),
        ToolDefinition(ToolIds.RANDOM_NUMBER, "Random Number", "Custom ranges, dice & coin flip", ToolCategory.TEXT, Icons.Rounded.Casino),

        // D. Everyday Utilities
        ToolDefinition(ToolIds.QR_GENERATOR, "QR Generator", "Create, save & share offline QR codes", ToolCategory.UTILITIES, Icons.Rounded.QrCode2, isFeatured = true, isPopular = true),
        ToolDefinition(ToolIds.QR_SCANNER, "QR Scanner", "Scan QR via camera or photo picker", ToolCategory.UTILITIES, Icons.Rounded.QrCodeScanner),
        ToolDefinition(ToolIds.IMAGE_RESIZER, "Image Resizer", "Shrink & compress photos offline", ToolCategory.UTILITIES, Icons.Rounded.PhotoSizeSelectLarge),
        ToolDefinition(ToolIds.SHOPPING_LIST, "Shopping List", "Organize groceries & estimated spend", ToolCategory.UTILITIES, Icons.Rounded.ShoppingBag),
        ToolDefinition(ToolIds.EXPENSE_TRACKER, "Expense Tracker", "Log income & spending privately", ToolCategory.UTILITIES, Icons.Rounded.AccountBalanceWallet, isPopular = true),
        ToolDefinition(ToolIds.SAVINGS_CALC, "Savings Calc", "Compound interest growth projection", ToolCategory.UTILITIES, Icons.Rounded.Savings),
        ToolDefinition(ToolIds.WATER_INTAKE, "Water Tracker", "Daily hydration ring & quick log", ToolCategory.UTILITIES, Icons.Rounded.WaterDrop, isFeatured = true),
        ToolDefinition(ToolIds.BUDGET_PLANNER, "Budget Planner", "Monthly cap vs actual expense view", ToolCategory.UTILITIES, Icons.Rounded.PieChart),

        // E. Optional Islamic Tools
        ToolDefinition(ToolIds.PRAYER_TIMES, "Prayer Times", "Offline solar calculation & city list", ToolCategory.ISLAMIC, Icons.Rounded.Schedule, isFeatured = true),
        ToolDefinition(ToolIds.QIBLA_COMPASS, "Qibla Compass", "Live magnetic bearing to Makkah", ToolCategory.ISLAMIC, Icons.Rounded.Explore),
        ToolDefinition(ToolIds.TASBEEH_COUNTER, "Tasbeeh Counter", "Tactile dhikr beads with haptic pulse", ToolCategory.ISLAMIC, Icons.Rounded.Spa, isPopular = true),
        ToolDefinition(ToolIds.DAILY_DUAS, "Daily Duas", "Authentic Arabic, transliteration & meaning", ToolCategory.ISLAMIC, Icons.Rounded.MenuBook),
        ToolDefinition(ToolIds.RAMADAN_PLANNER, "Ramadan Planner", "30-day Suhoor, Quran & charity log", ToolCategory.ISLAMIC, Icons.Rounded.NightsStay),
        ToolDefinition(ToolIds.ISLAMIC_DATE_CONV, "Hijri Converter", "Gregorian & Islamic Hijri date sync", ToolCategory.ISLAMIC, Icons.Rounded.CalendarMonth)
    )

    fun findById(id: String): ToolDefinition? = allTools.find { it.id == id }

    val dailyTips: List<String> = listOf(
        "Use the Password Generator offline to craft 16+ character passphrases without ever touching the cloud.",
        "Tap the heart icon inside any tool to pin it directly to your Home and Favorites tabs.",
        "Batch similar errands into reusable Checklist templates for groceries, trips, and weekly reviews.",
        "Stay hydrated: logging just 250ml of water every 2 hours effortlessly reaches a 2.5L daily goal.",
        "Use the Pomodoro Timer's 25-minute sprints to defeat procrastination on deep work tasks.",
        "Need to shrink a photo before sending? Image Resizer works offline using Android's zero-permission Photo Picker.",
        "Everything you save in Toolzify stays 100% on your device—no accounts, no cloud sync, zero tracking."
    )
}
