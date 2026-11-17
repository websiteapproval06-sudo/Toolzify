package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.ExpressionEvaluator
import com.example.ui.components.CopyTextButton
import com.example.ui.components.LuxuryCard
import com.example.ui.components.rememberHapticTrigger
import com.example.ui.theme.LocalToolzifyColors
import com.example.ui.theme.MonoLargeStyle
import com.example.ui.theme.MonoMediumStyle
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.pow

@Composable
fun StandardCalculatorScreen() {
    CalculatorBody(scientific = false)
}

@Composable
fun ScientificCalculatorScreen() {
    CalculatorBody(scientific = true)
}

@Composable
private fun CalculatorBody(scientific: Boolean) {
    val ext = LocalToolzifyColors.current
    val triggerHaptic = rememberHapticTrigger()
    var expression by remember { mutableStateOf("") }
    var useDegrees by remember { mutableStateOf(true) }
    var history by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    val livePreview = remember(expression, useDegrees) {
        if (expression.isBlank()) ""
        else ExpressionEvaluator.evaluate(expression, useDegrees)
            .map { ExpressionEvaluator.formatResult(it) }
            .getOrElse { "" }
    }

    fun appendToken(token: String) {
        triggerHaptic()
        expression += token
    }

    fun evaluateNow() {
        triggerHaptic()
        if (expression.isBlank()) return
        val res = ExpressionEvaluator.evaluate(expression, useDegrees)
        res.onSuccess { value ->
            val formatted = ExpressionEvaluator.formatResult(value)
            history = (listOf(expression to formatted) + history).take(12)
            expression = formatted
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // History & Display
        Column(modifier = Modifier.weight(1f)) {
            if (history.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(history) { (expr, ans) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { expression = ans }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                "$expr = $ans",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            LuxuryCard(
                containerColor = ext.creamSurface,
                contentPadding = PaddingValues(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (scientific) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = useDegrees,
                            onClick = { useDegrees = !useDegrees },
                            label = { Text(if (useDegrees) "DEG" else "RAD") }
                        )
                        CopyTextButton(textToCopy = livePreview.ifEmpty { expression }, label = "Copy")
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text = expression.ifEmpty { "0" },
                    style = MonoLargeStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calc_display")
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (livePreview.isNotEmpty() && livePreview != expression) "= $livePreview" else "",
                    style = MonoMediumStyle,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scientific function strip
        if (scientific) {
            val sciRows = listOf(
                listOf("sin(" to "sin", "cos(" to "cos", "tan(" to "tan", "√(" to "√", "^" to "xʸ"),
                listOf("ln(" to "ln", "log(" to "log", "π" to "π", "e" to "e", "!" to "n!")
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sciRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { (token, label) ->
                            FilledTonalButton(
                                onClick = { appendToken(token) },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Text(label, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Standard Keypad
        val rows = listOf(
            listOf("C", "(", ")", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("%", "0", ".", "=")
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        triggerHaptic()
                        if (expression.isNotEmpty()) expression = expression.dropLast(1)
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = "Backspace")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Backspace")
                }
            }
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { key ->
                        val isEquals = key == "="
                        val isOperator = key in listOf("÷", "×", "−", "+", "(", ")", "%")
                        val isClear = key == "C"

                        val bg = when {
                            isEquals -> MaterialTheme.colorScheme.primary
                            isClear -> MaterialTheme.colorScheme.errorContainer
                            isOperator -> ext.goldContainer
                            else -> ext.cardSurface
                        }
                        val fg = when {
                            isEquals -> Color.White
                            isClear -> MaterialTheme.colorScheme.error
                            isOperator -> ext.onGoldContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Surface(
                            onClick = {
                                when (key) {
                                    "C" -> {
                                        triggerHaptic()
                                        expression = ""
                                    }
                                    "=" -> evaluateNow()
                                    else -> appendToken(key)
                                }
                            },
                            shape = RoundedCornerShape(18.dp),
                            color = bg,
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .weight(1f)
                                .height(if (scientific) 52.dp else 60.dp)
                                .testTag("calc_key_$key")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = fg,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PercentageCalculatorScreen() {
    var mode by remember { mutableIntStateOf(0) }
    var inputA by remember { mutableStateOf("25") }
    var inputB by remember { mutableStateOf("200") }

    val a = inputA.toDoubleOrNull() ?: 0.0
    val b = inputB.toDoubleOrNull() ?: 0.0

    val resultText = when (mode) {
        0 -> {
            val res = (a / 100.0) * b
            "${ExpressionEvaluator.formatResult(res)}"
        }
        1 -> {
            if (b == 0.0) "Cannot divide by 0"
            else "${ExpressionEvaluator.formatResult((a / b) * 100.0)}%"
        }
        else -> {
            if (a == 0.0) "Cannot divide by 0"
            else {
                val pct = ((b - a) / a) * 100.0
                val prefix = if (pct >= 0) "+" else ""
                "$prefix${ExpressionEvaluator.formatResult(pct)}%"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val modes = listOf("X% of Y", "X is what % of Y", "% Change (X → Y)")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(modes.indices.toList()) { idx ->
                FilterChip(
                    selected = mode == idx,
                    onClick = { mode = idx },
                    label = { Text(modes[idx]) }
                )
            }
        }

        LuxuryCard {
            OutlinedTextField(
                value = inputA,
                onValueChange = { inputA = it },
                label = {
                    Text(
                        when (mode) {
                            0 -> "Percentage (%)"
                            1 -> "Part Value (X)"
                            else -> "Initial Value (X)"
                        }
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = inputB,
                onValueChange = { inputB = it },
                label = {
                    Text(
                        when (mode) {
                            0 -> "Of Total Value (Y)"
                            1 -> "Total Value (Y)"
                            else -> "Final Value (Y)"
                        }
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
            Text("Result", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(resultText, style = MonoLargeStyle, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            CopyTextButton(textToCopy = resultText)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeCalculatorScreen() {
    var birthDate by remember { mutableStateOf(LocalDate.of(1998, 5, 15)) }
    var showPicker by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val valid = !birthDate.isAfter(today)
    val period = if (valid) Period.between(birthDate, today) else Period.ZERO
    val totalDays = if (valid) ChronoUnit.DAYS.between(birthDate, today) else 0L
    val totalWeeks = totalDays / 7L

    val nextBirthday = run {
        val candidate = birthDate.withYear(today.year)
        if (candidate.isBefore(today) || candidate.isEqual(today)) {
            birthDate.withYear(today.year + 1)
        } else candidate
    }
    val daysUntilBirthday = ChronoUnit.DAYS.between(today, nextBirthday)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LuxuryCard(onClick = { showPicker = true }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Date of Birth", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(birthDate.toString(), style = MonoMediumStyle)
                }
                FilledTonalIconButton(onClick = { showPicker = true }) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = "Pick birth date")
                }
            }
        }

        if (!valid) {
            Text("Birth date cannot be in the future.", color = MaterialTheme.colorScheme.error)
        } else {
            LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
                Text("Exact Age", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "${period.years} Years, ${period.months} Months, ${period.days} Days",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LuxuryCard(modifier = Modifier.weight(1f)) {
                    Text("Total Days", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("%,d".format(totalDays), style = MonoMediumStyle, color = MaterialTheme.colorScheme.primary)
                }
                LuxuryCard(modifier = Modifier.weight(1f)) {
                    Text("Total Weeks", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("%,d".format(totalWeeks), style = MonoMediumStyle, color = MaterialTheme.colorScheme.primary)
                }
            }

            LuxuryCard(containerColor = LocalToolzifyColors.current.goldContainer.copy(alpha = 0.55f)) {
                Text("Next Birthday Countdown", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "$daysUntilBirthday days remaining (${nextBirthday.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = birthDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        birthDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    showPicker = false
                }) { Text("Select") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = state) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateDifferenceScreen() {
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(30)) }
    var pickingStart by remember { mutableStateOf<Boolean?>(null) }

    val orderedStart = if (startDate.isAfter(endDate)) endDate else startDate
    val orderedEnd = if (startDate.isAfter(endDate)) startDate else endDate
    val period = Period.between(orderedStart, orderedEnd)
    val totalDays = ChronoUnit.DAYS.between(orderedStart, orderedEnd)
    val weekdays = remember(orderedStart, orderedEnd) {
        if (totalDays > 50000) 0L
        else {
            var count = 0L
            var cur = orderedStart
            while (cur.isBefore(orderedEnd)) {
                if (cur.dayOfWeek != DayOfWeek.SATURDAY && cur.dayOfWeek != DayOfWeek.SUNDAY) count++
                cur = cur.plusDays(1)
            }
            count
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LuxuryCard(onClick = { pickingStart = true }) {
            Text("Start Date", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(startDate.toString(), style = MonoMediumStyle)
        }
        LuxuryCard(onClick = { pickingStart = false }) {
            Text("End Date", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(endDate.toString(), style = MonoMediumStyle)
        }
        LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
            Text("Duration", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "${period.years}y ${period.months}m ${period.days}d",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total Calendar Days: $totalDays", style = MaterialTheme.typography.bodyLarge)
            Text("Total Weeks: ${totalDays / 7} weeks, ${totalDays % 7} days", style = MaterialTheme.typography.bodyMedium)
            if (totalDays <= 50000) {
                Text("Business Days (Mon–Fri): $weekdays", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    pickingStart?.let { isStart ->
        val initial = if (isStart) startDate else endDate
        val state = rememberDatePickerState(
            initialSelectedDateMillis = initial.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { pickingStart = null },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        if (isStart) startDate = picked else endDate = picked
                    }
                    pickingStart = null
                }) { Text("Select") }
            },
            dismissButton = { TextButton(onClick = { pickingStart = null }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }
}

@Composable
fun DiscountCalculatorScreen() {
    var priceStr by remember { mutableStateOf("120.00") }
    var discountStr by remember { mutableStateOf("25") }
    var taxStr by remember { mutableStateOf("0") }

    val price = priceStr.toDoubleOrNull() ?: 0.0
    val discountPct = (discountStr.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0)
    val taxPct = (taxStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)

    val savings = price * (discountPct / 100.0)
    val discounted = price - savings
    val taxAmount = discounted * (taxPct / 100.0)
    val finalPrice = discounted + taxAmount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LuxuryCard {
            OutlinedTextField(
                value = priceStr,
                onValueChange = { priceStr = it },
                label = { Text("Original Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = discountStr,
                onValueChange = { discountStr = it },
                label = { Text("Discount (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("10", "15", "20", "25", "30", "50")) { pct ->
                    AssistChip(onClick = { discountStr = pct }, label = { Text("$pct%") })
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = taxStr,
                onValueChange = { taxStr = it },
                label = { Text("Optional Sales Tax (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
            Text("Final Price", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(String.format("%.2f", finalPrice), style = MonoLargeStyle)
            Spacer(modifier = Modifier.height(8.dp))
            Text("You Save: ${String.format("%.2f", savings)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            if (taxAmount > 0) {
                Text("Tax Included: ${String.format("%.2f", taxAmount)}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun GstCalculatorScreen() {
    var amountStr by remember { mutableStateOf("1000") }
    var gstRate by remember { mutableStateOf(18.0) }
    var isExclusive by remember { mutableStateOf(true) }

    val amount = amountStr.toDoubleOrNull() ?: 0.0
    val netAmount: Double
    val gstAmount: Double
    val totalAmount: Double

    if (isExclusive) {
        netAmount = amount
        gstAmount = amount * (gstRate / 100.0)
        totalAmount = netAmount + gstAmount
    } else {
        totalAmount = amount
        netAmount = amount / (1.0 + gstRate / 100.0)
        gstAmount = totalAmount - netAmount
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(selected = isExclusive, onClick = { isExclusive = true }, label = { Text("Add GST (+)") })
            FilterChip(selected = !isExclusive, onClick = { isExclusive = false }, label = { Text("Remove GST (-)") })
        }

        LuxuryCard {
            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text(if (isExclusive) "Base Amount" else "Total Amount (GST Included)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("GST Rate: ${gstRate.toInt()}%", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf(3.0, 5.0, 12.0, 18.0, 28.0)) { slab ->
                    FilterChip(
                        selected = gstRate == slab,
                        onClick = { gstRate = slab },
                        label = { Text("${slab.toInt()}%") }
                    )
                }
            }
        }

        LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Net Price")
                Text(String.format("%.2f", netAmount), fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("CGST (${gstRate / 2}%)")
                Text(String.format("%.2f", gstAmount / 2.0))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SGST (${gstRate / 2}%)")
                Text(String.format("%.2f", gstAmount / 2.0))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Amount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(String.format("%.2f", totalAmount), style = MonoMediumStyle, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private enum class UnitCategory(val title: String, val units: List<Pair<String, Double>>) {
    LENGTH("Length", listOf("Meter (m)" to 1.0, "Kilometer (km)" to 1000.0, "Centimeter (cm)" to 0.01, "Millimeter (mm)" to 0.001, "Mile (mi)" to 1609.344, "Yard (yd)" to 0.9144, "Foot (ft)" to 0.3048, "Inch (in)" to 0.0254)),
    WEIGHT("Weight", listOf("Kilogram (kg)" to 1.0, "Gram (g)" to 0.001, "Pound (lb)" to 0.45359237, "Ounce (oz)" to 0.0283495, "Metric Ton (t)" to 1000.0)),
    TEMPERATURE("Temperature", listOf("Celsius (°C)" to 1.0, "Fahrenheit (°F)" to 2.0, "Kelvin (K)" to 3.0)),
    VOLUME("Volume", listOf("Liter (L)" to 1.0, "Milliliter (mL)" to 0.001, "Gallon US" to 3.78541, "Cup US" to 0.236588, "Fluid Oz" to 0.0295735)),
    SPEED("Speed", listOf("m/s" to 1.0, "km/h" to 0.277778, "mph" to 0.44704, "Knots" to 0.514444)),
    DATA("Data", listOf("Byte (B)" to 1.0, "Kilobyte (KB)" to 1024.0, "Megabyte (MB)" to 1048576.0, "Gigabyte (GB)" to 1073741824.0, "Terabyte (TB)" to 1099511627776.0))
}

@Composable
fun UnitConverterScreen() {
    var selectedCat by remember { mutableStateOf(UnitCategory.LENGTH) }
    var fromIdx by remember(selectedCat) { mutableIntStateOf(0) }
    var toIdx by remember(selectedCat) { mutableIntStateOf(1) }
    var inputStr by remember { mutableStateOf("1") }

    val inputVal = inputStr.toDoubleOrNull() ?: 0.0
    val converted = remember(selectedCat, fromIdx, toIdx, inputVal) {
        val units = selectedCat.units
        val f = units[fromIdx]
        val t = units[toIdx]
        if (selectedCat == UnitCategory.TEMPERATURE) {
            val celsius = when (fromIdx) {
                0 -> inputVal
                1 -> (inputVal - 32.0) * 5.0 / 9.0
                else -> inputVal - 273.15
            }
            when (toIdx) {
                0 -> celsius
                1 -> celsius * 9.0 / 5.0 + 32.0
                else -> celsius + 273.15
            }
        } else {
            (inputVal * f.second) / t.second
        }
    }

    val formattedOut = ExpressionEvaluator.formatResult(converted)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(UnitCategory.entries) { cat ->
                FilterChip(selected = selectedCat == cat, onClick = { selectedCat = cat }, label = { Text(cat.title) })
            }
        }

        LuxuryCard {
            OutlinedTextField(
                value = inputStr,
                onValueChange = { inputStr = it },
                label = { Text("Value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("From", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(selectedCat.units.indices.toList()) { idx ->
                    FilterChip(
                        selected = fromIdx == idx,
                        onClick = { fromIdx = idx },
                        label = { Text(selectedCat.units[idx].first) }
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                IconButton(onClick = {
                    val tmp = fromIdx
                    fromIdx = toIdx
                    toIdx = tmp
                }) {
                    Icon(Icons.Rounded.SwapVert, contentDescription = "Swap units")
                }
            }
            Text("To", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(selectedCat.units.indices.toList()) { idx ->
                    FilterChip(
                        selected = toIdx == idx,
                        onClick = { toIdx = idx },
                        label = { Text(selectedCat.units[idx].first) }
                    )
                }
            }
        }

        LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
            Text(selectedCat.units[toIdx].first, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(formattedOut, style = MonoLargeStyle)
            Spacer(modifier = Modifier.height(8.dp))
            CopyTextButton(textToCopy = formattedOut)
        }
    }
}

@Composable
fun BmiCalculatorScreen() {
    var isMetric by remember { mutableStateOf(true) }
    var weightStr by remember { mutableStateOf("68") }
    var heightStr by remember { mutableStateOf("172") }

    val w = weightStr.toDoubleOrNull() ?: 0.0
    val h = heightStr.toDoubleOrNull() ?: 0.0

    val bmi = remember(isMetric, w, h) {
        if (w <= 0 || h <= 0) 0.0
        else if (isMetric) {
            w / (h / 100.0).pow(2)
        } else {
            (w / h.pow(2)) * 703.0
        }
    }

    val (category, color) = when {
        bmi <= 0 -> "Enter values" to MaterialTheme.colorScheme.onSurfaceVariant
        bmi < 18.5 -> "Underweight" to Color(0xFF3A7CA5)
        bmi < 25.0 -> "Healthy Weight" to Color(0xFF1E6B47)
        bmi < 30.0 -> "Overweight" to Color(0xFFC59433)
        else -> "Obese" to Color(0xFFB33A3A)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = isMetric, onClick = { isMetric = true; weightStr = "68"; heightStr = "172" }, label = { Text("Metric (kg / cm)") })
            FilterChip(selected = !isMetric, onClick = { isMetric = false; weightStr = "150"; heightStr = "68" }, label = { Text("Imperial (lb / in)") })
        }

        LuxuryCard {
            OutlinedTextField(
                value = weightStr,
                onValueChange = { weightStr = it },
                label = { Text(if (isMetric) "Weight (kg)" else "Weight (lb)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = heightStr,
                onValueChange = { heightStr = it },
                label = { Text(if (isMetric) "Height (cm)" else "Height (inches)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Body Mass Index", style = MaterialTheme.typography.labelLarge)
                    Text(String.format("%.1f", bmi), style = MonoLargeStyle, color = color)
                    Text(category, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.size(92.dp), contentAlignment = Alignment.Center) {
                    val norm = ((bmi - 12.0) / 28.0).coerceIn(0.0, 1.0).toFloat()
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(Color.Gray.copy(alpha = 0.2f), 135f, 270f, false, style = Stroke(10.dp.toPx(), cap = StrokeCap.Round))
                        drawArc(color, 135f, 270f * norm, false, style = Stroke(10.dp.toPx(), cap = StrokeCap.Round))
                    }
                    Text(String.format("%.1f", bmi), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Calculated strictly on-device. Nothing is stored or transmitted.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
