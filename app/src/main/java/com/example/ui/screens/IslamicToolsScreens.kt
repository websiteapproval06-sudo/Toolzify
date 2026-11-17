package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.RamadanEntryEntity
import com.example.domain.IslamicAstroEngine
import com.example.domain.PrayerCalcMethod
import com.example.ui.ToolzifyViewModel
import com.example.ui.components.AnimatedProgressRing
import com.example.ui.components.CopyTextButton
import com.example.ui.components.LuxuryCard
import com.example.ui.components.rememberHapticTrigger
import com.example.ui.theme.LocalToolzifyColors
import com.example.ui.theme.MonoLargeStyle
import com.example.ui.theme.MonoMediumStyle
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.TimeZone
import kotlin.math.roundToInt

@SuppressLint("MissingPermission")
private fun readLastCoarseCoordinates(context: Context): Pair<Double, Double>? {
    return try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        loc?.let { it.latitude to it.longitude }
    } catch (_: Exception) {
        null
    }
}

@Composable
fun PrayerTimesScreen(viewModel: ToolzifyViewModel) {
    val context = LocalContext.current
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val ext = LocalToolzifyColors.current

    var showCityDialog by remember { mutableStateOf(false) }
    var customCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var locationNote by remember { mutableStateOf<String?>(null) }

    val locLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val coords = readLastCoarseCoordinates(context)
            if (coords != null) {
                customCoords = coords
                locationNote = "Using device coarse coordinates (${String.format("%.2f", coords.first)}, ${String.format("%.2f", coords.second)})"
            } else {
                locationNote = "No cached coarse location available; using selected city."
            }
        } else {
            locationNote = "Location permission declined; manual city selection active."
        }
    }

    val city = IslamicAstroEngine.findCity(prefs.selectedCityId)
    val method = PrayerCalcMethod.fromId(prefs.prayerMethodId)
    val lat = customCoords?.first ?: city.latitude
    val lon = customCoords?.second ?: city.longitude
    val utcOffset = if (customCoords != null) {
        TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 3600000.0
    } else city.utcOffsetHours

    val times = remember(lat, lon, utcOffset, method) {
        IslamicAstroEngine.calculatePrayerTimes(LocalDate.now(), lat, lon, utcOffset, method)
    }

    val nowHours = LocalTime.now().hour + LocalTime.now().minute / 60.0
    val nextPrayer = times.prayerMinutes.firstOrNull { it.second > nowHours }?.first ?: "Fajr"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (customCoords != null) "Device Location" else "${city.name}, ${city.country}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text("Next: $nextPrayer • ${LocalDate.now()}", style = MaterialTheme.typography.bodySmall)
                }
                FilledTonalButton(onClick = { showCityDialog = true }) {
                    Text("City")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            val coords = readLastCoarseCoordinates(context)
                            if (coords != null) {
                                customCoords = coords
                                locationNote = "Using device coarse coordinates"
                            } else {
                                locationNote = "No cached location found; using ${city.name}"
                            }
                        } else {
                            locLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Use Device Location")
                }
                if (customCoords != null) {
                    TextButton(onClick = { customCoords = null; locationNote = null }) {
                        Text("Reset to City")
                    }
                }
            }
            locationNote?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(PrayerCalcMethod.entries) { m ->
                FilterChip(
                    selected = method == m,
                    onClick = { viewModel.setPrayerMethodId(m.id) },
                    label = { Text(m.label) }
                )
            }
        }

        val rows = listOf(
            "Fajr" to times.fajr,
            "Sunrise" to times.sunrise,
            "Dhuhr" to times.dhuhr,
            "Asr" to times.asr,
            "Maghrib" to times.maghrib,
            "Isha" to times.isha
        )

        rows.forEach { (name, time) ->
            val isNext = name == nextPrayer
            LuxuryCard(
                containerColor = if (isNext) ext.goldContainer else ext.cardSurface,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isNext) Icons.Rounded.NotificationsActive else Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = if (isNext) ext.gold else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium)
                    }
                    Text(time, style = MonoMediumStyle, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showCityDialog) {
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = { Text("Select City (Offline)") },
            text = {
                LazyColumn(modifier = Modifier.height(340.dp)) {
                    items(IslamicAstroEngine.cities) { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    customCoords = null
                                    locationNote = null
                                    viewModel.setSelectedCityId(c.id)
                                    showCityDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${c.name}, ${c.country}", style = MaterialTheme.typography.bodyLarge)
                            if (c.id == city.id) {
                                Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showCityDialog = false }) { Text("Close") } }
        )
    }
}

@Composable
fun QiblaCompassScreen(viewModel: ToolzifyViewModel) {
    val context = LocalContext.current
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val ext = LocalToolzifyColors.current

    val city = IslamicAstroEngine.findCity(prefs.selectedCityId)
    var customCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var azimuthDeg by remember { mutableFloatStateOf(0f) }
    var sensorAvailable by remember { mutableStateOf(true) }

    val lat = customCoords?.first ?: city.latitude
    val lon = customCoords?.second ?: city.longitude
    val qiblaBearing = remember(lat, lon) { IslamicAstroEngine.calculateQiblaBearing(lat, lon).toFloat() }
    val distanceKm = remember(lat, lon) { IslamicAstroEngine.calculateDistanceToMakkahKm(lat, lon).roundToInt() }

    val locLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            customCoords = readLastCoarseCoordinates(context)
        }
    }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationSensor == null) {
            sensorAvailable = false
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val rotationMatrix = FloatArray(9)
                    val orientation = FloatArray(3)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    azimuthDeg = (deg + 360f) % 360f
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    val needleAngle by animateFloatAsState(
        targetValue = qiblaBearing - azimuthDeg,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "qiblaAngle"
    )
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(if (customCoords != null) "Device Coordinates" else "${city.name}, ${city.country}", style = MaterialTheme.typography.titleMedium)
                    Text("Qibla Bearing: ${qiblaBearing.roundToInt()}° • Distance: %,d km".format(distanceKm), style = MaterialTheme.typography.bodySmall)
                }
                FilledTonalIconButton(onClick = {
                    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (granted) customCoords = readLastCoarseCoordinates(context)
                    else locLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                }) {
                    Icon(Icons.Rounded.MyLocation, contentDescription = "Use coarse location")
                }
            }
        }

        Box(
            modifier = Modifier.size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f
                drawCircle(color = ext.creamSurface, radius = radius)
                drawCircle(color = ext.cardBorder, radius = radius, style = androidx.compose.ui.graphics.drawscope.Stroke(4.dp.toPx()))
                drawCircle(color = ext.gold.copy(alpha = 0.25f), radius = radius * 0.78f, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()))

                rotate(degrees = needleAngle) {
                    val path = Path().apply {
                        moveTo(center.x, center.y - radius * 0.74f)
                        lineTo(center.x - 16.dp.toPx(), center.y)
                        lineTo(center.x + 16.dp.toPx(), center.y)
                        close()
                    }
                    drawPath(path, color = primaryColor)
                    drawCircle(color = ext.gold, radius = 10.dp.toPx(), center = Offset(center.x, center.y))
                }
            }
            Text(
                "Qibla",
                style = MaterialTheme.typography.labelLarge,
                color = ext.gold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp)
            )
        }

        if (!sensorAvailable) {
            Text(
                "Compass magnetometer sensor unavailable on this device. Bearing shown is ${qiblaBearing.roundToInt()}° clockwise from True North.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                "Hold phone flat away from metal objects for accurate live heading.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TasbeehCounterScreen(viewModel: ToolzifyViewModel) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val triggerHaptic = rememberHapticTrigger()
    val ext = LocalToolzifyColors.current

    val phrases = listOf(
        Triple("سُبْحَانَ اللَّهِ", "SubhanAllah", "Glory be to Allah"),
        Triple("الْحَمْدُ لِلَّهِ", "Alhamdulillah", "All praise is due to Allah"),
        Triple("اللَّهُ أَكْبَرُ", "Allahu Akbar", "Allah is the Greatest"),
        Triple("أَسْتَغْفِرُ اللَّهَ", "Astaghfirullah", "I seek forgiveness from Allah"),
        Triple("لَا إِلَٰهَ إِلَّا اللَّهُ", "La ilaha illallah", "There is no deity worthy of worship except Allah")
    )

    val count = prefs.tasbeehCount
    val target = prefs.tasbeehTarget.coerceAtLeast(1)
    val phraseIdx = prefs.tasbeehPhraseIndex.coerceIn(0, phrases.lastIndex)
    val phrase = phrases[phraseIdx]
    val progress = (count % target).toFloat() / target

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(phrases.indices.toList()) { idx ->
                FilterChip(
                    selected = phraseIdx == idx,
                    onClick = { viewModel.updateTasbeeh(count, target, idx) },
                    label = { Text(phrases[idx].second) }
                )
            }
        }

        LuxuryCard(containerColor = ext.goldContainer.copy(alpha = 0.45f), modifier = Modifier.fillMaxWidth()) {
            Text(phrase.first, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(4.dp))
            Text(phrase.second, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text(phrase.third, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(230.dp)
                .clip(CircleShape)
                .clickable {
                    triggerHaptic()
                    viewModel.updateTasbeeh(count + 1, target, phraseIdx)
                }
        ) {
            AnimatedProgressRing(
                progress = if (count > 0 && count % target == 0) 1f else progress,
                size = 230.dp,
                strokeWidth = 14.dp,
                activeColor = ext.gold
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(count.toString(), style = MonoLargeStyle, fontWeight = FontWeight.Bold)
                    Text("Target: $target • Tap anywhere", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(33, 99, 100).forEach { t ->
                FilterChip(
                    selected = target == t,
                    onClick = { viewModel.updateTasbeeh(count, t, phraseIdx) },
                    label = { Text("Target $t") }
                )
            }
            OutlinedButton(onClick = { viewModel.updateTasbeeh(0, target, phraseIdx) }) {
                Text("Reset")
            }
        }
    }
}

@Composable
fun DailyDuasScreen() {
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val categories = remember { IslamicAstroEngine.dailyDuas.map { it.category }.distinct() }

    val filtered = remember(search, selectedCategory) {
        IslamicAstroEngine.dailyDuas.filter { dua ->
            (selectedCategory == null || dua.category == selectedCategory) &&
                (search.isBlank() || dua.title.contains(search, true) || dua.translation.contains(search, true))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search daily duas...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = selectedCategory == null, onClick = { selectedCategory = null }, label = { Text("All") }) }
            items(categories) { cat ->
                FilterChip(selected = selectedCategory == cat, onClick = { selectedCategory = cat }, label = { Text(cat) })
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filtered, key = { it.id }) { dua ->
                LuxuryCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(dua.title, style = MaterialTheme.typography.titleMedium)
                        Surface(shape = RoundedCornerShape(50), color = LocalToolzifyColors.current.goldContainer) {
                            Text(dua.category, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(dua.arabic, fontSize = 22.sp, lineHeight = 34.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(dua.transliteration, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(dua.translation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    CopyTextButton(textToCopy = "${dua.arabic}\n${dua.transliteration}\n${dua.translation}")
                }
            }
        }
    }
}

@Composable
fun RamadanPlannerScreen(viewModel: ToolzifyViewModel) {
    val entries by viewModel.ramadanEntries.collectAsStateWithLifecycle()
    val entryMap = remember(entries) { entries.associateBy { it.dayNumber } }
    var selectedDay by remember { mutableIntStateOf(1) }

    val current = entryMap[selectedDay] ?: RamadanEntryEntity(dayNumber = selectedDay)
    val totalPages = entries.sumOf { it.quranPages }
    val quranProgress = (totalPages / 604f).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)) {
            Text("Quran Reading Progress", style = MaterialTheme.typography.titleMedium)
            Text("$totalPages / 604 pages completed", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { quranProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
            )
        }

        Text("Select Ramadan Day", style = MaterialTheme.typography.titleSmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items((1..30).toList()) { day ->
                FilterChip(
                    selected = selectedDay == day,
                    onClick = { selectedDay = day },
                    label = { Text("Day $day") }
                )
            }
        }

        LuxuryCard {
            Text("Day $selectedDay Checklist", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Suhoor Completed")
                Checkbox(
                    checked = current.suhoorDone,
                    onCheckedChange = { viewModel.saveRamadanEntry(current.copy(suhoorDone = it)) }
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Iftar & Gratitude")
                Checkbox(
                    checked = current.iftarDone,
                    onCheckedChange = { viewModel.saveRamadanEntry(current.copy(iftarDone = it)) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Quran Pages Today: ${current.quranPages}", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = {
                        viewModel.saveRamadanEntry(current.copy(quranPages = (current.quranPages - 2).coerceAtLeast(0)))
                    }) { Text("-2") }
                    Button(onClick = {
                        viewModel.saveRamadanEntry(current.copy(quranPages = current.quranPages + 4))
                    }) { Text("+4") }
                }
            }
        }

        var reflection by remember(selectedDay, current.reflection) { mutableStateOf(current.reflection) }
        LuxuryCard {
            OutlinedTextField(
                value = reflection,
                onValueChange = { reflection = it },
                label = { Text("Charity & Daily Reflection (Local Only)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.saveRamadanEntry(current.copy(reflection = reflection)) },
                shape = RoundedCornerShape(14.dp)
            ) { Text("Save Day $selectedDay") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslamicDateConverterScreen() {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showPicker by remember { mutableStateOf(false) }

    val hijri = remember(selectedDate) { HijrahDate.from(selectedDate) }
    val hijriMonths = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Ula", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )
    val hDay = hijri.get(ChronoField.DAY_OF_MONTH)
    val hMonth = hijri.get(ChronoField.MONTH_OF_YEAR)
    val hYear = hijri.get(ChronoField.YEAR)
    val monthName = hijriMonths.getOrElse(hMonth - 1) { "Month $hMonth" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LuxuryCard(onClick = { showPicker = true }) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Gregorian Date", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(selectedDate.toString(), style = MonoMediumStyle)
                }
                FilledTonalIconButton(onClick = { showPicker = true }) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = "Pick date")
                }
            }
        }

        LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
            Text("Hijri (Islamic) Date", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            Text("$hDay $monthName $hYear AH", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Numeric: $hYear-${String.format("%02d", hMonth)}-${String.format("%02d", hDay)} AH", style = MonoMediumStyle)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = { selectedDate = LocalDate.now() }, label = { Text("Today") })
            AssistChip(onClick = { selectedDate = selectedDate.minusDays(1) }, label = { Text("-1 Day") })
            AssistChip(onClick = { selectedDate = selectedDate.plusDays(1) }, label = { Text("+1 Day") })
        }
    }

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        selectedDate = java.time.Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    showPicker = false
                }) { Text("Select") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }
}
