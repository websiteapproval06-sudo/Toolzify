package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.components.CopyTextButton
import com.example.ui.components.LuxuryCard
import com.example.ui.components.rememberHapticTrigger
import com.example.ui.theme.LocalToolzifyColors
import com.example.ui.theme.MonoLargeStyle
import com.example.ui.theme.MonoMediumStyle
import java.security.SecureRandom
import kotlin.math.ceil

@Composable
fun WordCounterScreen() {
    var text by remember { mutableStateOf("") }
    val words = remember(text) {
        if (text.isBlank()) emptyList() else text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    }
    val sentences = remember(text) {
        if (text.isBlank()) 0 else text.split(Regex("[.!?]+")).count { it.isNotBlank() }
    }
    val paragraphs = remember(text) {
        if (text.isBlank()) 0 else text.split(Regex("\\n+")).count { it.isNotBlank() }
    }
    val readMin = ceil(words.size / 200.0).toInt().coerceAtLeast(if (words.isEmpty()) 0 else 1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Words", words.size.toString(), Modifier.weight(1f))
            StatTile("Sentences", sentences.toString(), Modifier.weight(1f))
            StatTile("Paragraphs", paragraphs.toString(), Modifier.weight(1f))
            StatTile("Read Time", "${readMin}m", Modifier.weight(1f))
        }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Paste or type your text") },
            minLines = 8,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("word_counter_input")
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CopyTextButton(textToCopy = text)
            OutlinedButton(onClick = { text = "" }, enabled = text.isNotEmpty(), shape = RoundedCornerShape(14.dp)) {
                Text("Clear")
            }
        }
    }
}

@Composable
fun CharacterCounterScreen() {
    var text by remember { mutableStateOf("") }
    val withSpaces = text.length
    val withoutSpaces = text.count { !it.isWhitespace() }
    val lines = if (text.isEmpty()) 0 else text.lines().size
    val utf8Bytes = text.toByteArray(Charsets.UTF_8).size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Chars", withSpaces.toString(), Modifier.weight(1f))
            StatTile("No Spaces", withoutSpaces.toString(), Modifier.weight(1f))
            StatTile("Lines", lines.toString(), Modifier.weight(1f))
            StatTile("Bytes", utf8Bytes.toString(), Modifier.weight(1f))
        }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter text to inspect") },
            minLines = 8,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        )

        LuxuryCard {
            Text("Platform Length Guide", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Text("SMS Segment (160): $withSpaces / 160", style = MaterialTheme.typography.bodySmall)
            Text("Short Post (280): $withSpaces / 280", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun CaseConverterScreen() {
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }

    fun convert(mode: String) {
        val words = input.trim().split(Regex("[\\s_-]+")).filter { it.isNotEmpty() }
        output = when (mode) {
            "UPPER" -> input.uppercase()
            "lower" -> input.lowercase()
            "Title Case" -> words.joinToString(" ") { w -> w.lowercase().replaceFirstChar { it.uppercase() } }
            "Sentence case" -> input.lowercase().replaceFirstChar { it.uppercase() }
            "camelCase" -> words.mapIndexed { i, w ->
                val lw = w.lowercase()
                if (i == 0) lw else lw.replaceFirstChar { it.uppercase() }
            }.joinToString("")
            "snake_case" -> words.joinToString("_") { it.lowercase() }
            "kebab-case" -> words.joinToString("-") { it.lowercase() }
            else -> input
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Source Text") },
            minLines = 4,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        )

        val modes = listOf("UPPER", "lower", "Title Case", "Sentence case", "camelCase", "snake_case", "kebab-case")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(modes) { mode ->
                FilledTonalButton(onClick = { convert(mode) }, shape = RoundedCornerShape(14.dp)) {
                    Text(mode)
                }
            }
        }

        LuxuryCard(containerColor = LocalToolzifyColors.current.creamSurface) {
            Text("Converted Result", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(output.ifEmpty { "Tap a style above to transform your text." }, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(12.dp))
            CopyTextButton(textToCopy = output)
        }
    }
}

@Composable
fun RemoveExtraSpacesScreen() {
    var input by remember { mutableStateOf("") }
    var collapseSpaces by remember { mutableStateOf(true) }
    var trimLines by remember { mutableStateOf(true) }
    var removeBlankLines by remember { mutableStateOf(true) }
    var singleLine by remember { mutableStateOf(false) }

    val cleaned = remember(input, collapseSpaces, trimLines, removeBlankLines, singleLine) {
        var result = input
        if (trimLines) {
            result = result.lines().joinToString("\n") { it.trim() }
        }
        if (collapseSpaces) {
            result = result.replace(Regex("[ \\t]+"), " ")
        }
        if (removeBlankLines) {
            result = result.lines().filter { it.isNotBlank() }.joinToString("\n")
        }
        if (singleLine) {
            result = result.replace(Regex("\\s+"), " ").trim()
        }
        result
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Messy Text") },
            minLines = 5,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        )

        LuxuryCard {
            ToggleOption("Collapse multiple spaces", collapseSpaces) { collapseSpaces = it }
            ToggleOption("Trim line start & end", trimLines) { trimLines = it }
            ToggleOption("Remove blank lines", removeBlankLines) { removeBlankLines = it }
            ToggleOption("Join into single line", singleLine) { singleLine = it }
        }

        LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)) {
            Text("Cleaned Output", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(cleaned.ifEmpty { "Cleaned text appears here automatically." }, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(12.dp))
            CopyTextButton(textToCopy = cleaned)
        }
    }
}

@Composable
fun TextReverserScreen() {
    var input by remember { mutableStateOf("") }
    var mode by remember { mutableIntStateOf(0) }

    val reversed = remember(input, mode) {
        when (mode) {
            0 -> input.reversed()
            1 -> input.trim().split(Regex("\\s+")).reversed().joinToString(" ")
            else -> input.split(" ").joinToString(" ") { it.reversed() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Input Text") },
            minLines = 4,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = mode == 0, onClick = { mode = 0 }, label = { Text("Full Flip") })
            FilterChip(selected = mode == 1, onClick = { mode = 1 }, label = { Text("Word Order") })
            FilterChip(selected = mode == 2, onClick = { mode = 2 }, label = { Text("Each Word") })
        }
        LuxuryCard(containerColor = LocalToolzifyColors.current.creamSurface) {
            Text(reversed.ifEmpty { "Reversed output appears here." }, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(12.dp))
            CopyTextButton(textToCopy = reversed)
        }
    }
}

@Composable
fun PasswordGeneratorScreen() {
    val triggerHaptic = rememberHapticTrigger()
    var length by remember { mutableFloatStateOf(16f) }
    var useUpper by remember { mutableStateOf(true) }
    var useLower by remember { mutableStateOf(true) }
    var useDigits by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }
    var password by remember { mutableStateOf("") }

    fun generate() {
        triggerHaptic()
        val upper = "ABCDEFGHJKLMNPQRSTUVWXYZ"
        val lower = "abcdefghijkmnopqrstuvwxyz"
        val digits = "23456789"
        val symbols = "!@#\$%^&*-_=+?"
        val pool = buildString {
            if (useUpper) append(upper)
            if (useLower) append(lower)
            if (useDigits) append(digits)
            if (useSymbols) append(symbols)
        }.ifEmpty { lower }

        val rng = SecureRandom()
        password = (1..length.toInt()).map { pool[rng.nextInt(pool.length)] }.joinToString("")
    }

    LaunchedEffect(Unit) { generate() }

    val strengthScore = listOf(useUpper, useLower, useDigits, useSymbols).count { it } +
        if (length >= 16) 2 else if (length >= 12) 1 else 0
    val (strengthLabel, strengthColor) = when {
        strengthScore >= 5 -> "Very Strong" to Color(0xFF1E6B47)
        strengthScore >= 3 -> "Strong" to Color(0xFFC59433)
        else -> "Moderate" to Color(0xFFB33A3A)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
            Text("Generated Offline (SecureRandom)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(password, style = MonoMediumStyle, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Strength: $strengthLabel", color = strengthColor, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalIconButton(onClick = { generate() }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Regenerate")
                    }
                    CopyTextButton(textToCopy = password)
                }
            }
        }

        LuxuryCard {
            Text("Length: ${length.toInt()} characters", style = MaterialTheme.typography.titleSmall)
            Slider(
                value = length,
                onValueChange = { length = it },
                valueRange = 8f..40f
            )
            ToggleOption("Uppercase (A–Z)", useUpper) { useUpper = it }
            ToggleOption("Lowercase (a–z)", useLower) { useLower = it }
            ToggleOption("Numbers (2–9)", useDigits) { useDigits = it }
            ToggleOption("Symbols (!@#\$...)", useSymbols) { useSymbols = it }
        }

        Button(
            onClick = { generate() },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Rounded.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate New Password")
        }
    }
}

@Composable
fun RandomNumberGeneratorScreen() {
    val triggerHaptic = rememberHapticTrigger()
    var minStr by remember { mutableStateOf("1") }
    var maxStr by remember { mutableStateOf("100") }
    var countStr by remember { mutableStateOf("1") }
    var results by remember { mutableStateOf(listOf(42)) }

    fun roll(min: Int, max: Int, count: Int) {
        triggerHaptic()
        val low = minOf(min, max)
        val high = maxOf(min, max)
        val rng = SecureRandom()
        val span = (high - low + 1).coerceAtLeast(1)
        results = List(count.coerceIn(1, 20)) { low + rng.nextInt(span) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
            Text("Generated Output", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedContent(targetState = results, label = "rng") { nums ->
                Text(nums.joinToString(", "), style = MonoLargeStyle)
            }
        }

        LuxuryCard {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = minStr,
                    onValueChange = { minStr = it },
                    label = { Text("Min") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = maxStr,
                    onValueChange = { maxStr = it },
                    label = { Text("Max") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = countStr,
                    onValueChange = { countStr = it },
                    label = { Text("Count") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(0.8f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { minStr = "1"; maxStr = "6"; countStr = "1"; roll(1, 6, 1) }, label = { Text("Dice (1–6)") })
                AssistChip(onClick = { minStr = "0"; maxStr = "1"; countStr = "1"; roll(0, 1, 1) }, label = { Text("Coin (0/1)") })
                AssistChip(onClick = { minStr = "1"; maxStr = "100"; countStr = "1"; roll(1, 100, 1) }, label = { Text("1–100") })
            }
        }

        Button(
            onClick = {
                roll(
                    minStr.toIntOrNull() ?: 1,
                    maxStr.toIntOrNull() ?: 100,
                    countStr.toIntOrNull() ?: 1
                )
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Rounded.Casino, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate")
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    LuxuryCard(modifier = modifier, contentPadding = PaddingValues(12.dp)) {
        Text(value, style = MonoMediumStyle, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ToggleOption(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}
