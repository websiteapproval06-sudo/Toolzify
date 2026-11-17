package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.MediaUtils
import com.example.ui.ToolzifyViewModel
import com.example.ui.components.AnimatedProgressRing
import com.example.ui.components.CopyTextButton
import com.example.ui.components.EmptyStateView
import com.example.ui.components.LuxuryCard
import com.example.ui.components.rememberHapticTrigger
import com.example.ui.theme.LocalToolzifyColors
import com.example.ui.theme.MonoLargeStyle
import com.example.ui.theme.MonoMediumStyle
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors
import kotlin.math.pow

@Composable
fun QrGeneratorScreen() {
    val context = LocalContext.current
    var content by remember { mutableStateOf("https://toolzify.local") }
    var useGreenTint by remember { mutableStateOf(true) }
    var statusMsg by remember { mutableStateOf<String?>(null) }

    val qrBitmap = remember(content, useGreenTint) {
        MediaUtils.generateQrBitmap(
            content = content,
            sizePx = 680,
            foregroundColor = if (useGreenTint) AndroidColor.parseColor("#154734") else AndroidColor.BLACK
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Text, URL, Phone, or Wi-Fi payload") },
            minLines = 2,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = useGreenTint, onClick = { useGreenTint = true }, label = { Text("Forest Ink") })
            FilterChip(selected = !useGreenTint, onClick = { useGreenTint = false }, label = { Text("Classic Black") })
        }

        if (qrBitmap != null) {
            LuxuryCard(containerColor = Color.White, contentPadding = PaddingValues(18.dp)) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Generated QR Code",
                    modifier = Modifier.size(230.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val saved = MediaUtils.saveBitmapToGallery(
                            context,
                            qrBitmap,
                            "toolzify_qr_${System.currentTimeMillis()}"
                        )
                        statusMsg = if (saved) "Saved to Pictures/Toolzify" else "Unable to save image"
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save PNG")
                }
                OutlinedButton(
                    onClick = {
                        MediaUtils.shareBitmap(context, qrBitmap, "toolzify_qr")
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share")
                }
            }
        }
        statusMsg?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
fun QrScannerScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var cameraActive by remember { mutableStateOf(false) }
    var scannedText by remember { mutableStateOf<String?>(null) }
    var scanError by remember { mutableStateOf<String?>(null) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) cameraActive = true
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bmp = MediaUtils.loadBitmapFromUri(context, uri)
            val decoded = bmp?.let { MediaUtils.decodeQrFromBitmap(it) }
            if (decoded != null) {
                scannedText = decoded
                scanError = null
            } else {
                scanError = "No QR code found in the selected photo."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LuxuryCard {
            Text("Offline QR & Barcode Scanner", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Decodes QR codes 100% locally on your device. Camera permission is only requested if you enable live scanning.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        if (hasCameraPermission) cameraActive = !cameraActive
                        else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (cameraActive) "Stop Camera" else "Enable Camera")
                }
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("From Photo")
                }
            }
        }

        if (cameraActive && hasCameraPermission) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
            ) {
                val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
                DisposableEffect(Unit) {
                    onDispose { cameraExecutor.shutdown() }
                }
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val provider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            val reader = MultiFormatReader()
                            val analysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build().apply {
                                    setAnalyzer(cameraExecutor) { imageProxy ->
                                        try {
                                            val plane = imageProxy.planes[0]
                                            val buffer = plane.buffer
                                            val bytes = ByteArray(buffer.remaining())
                                            buffer.get(bytes)
                                            val source = PlanarYUVLuminanceSource(
                                                bytes,
                                                imageProxy.width,
                                                imageProxy.height,
                                                0,
                                                0,
                                                imageProxy.width,
                                                imageProxy.height,
                                                false
                                            )
                                            val result = reader.decodeWithState(BinaryBitmap(HybridBinarizer(source)))
                                            scannedText = result.text
                                            scanError = null
                                        } catch (_: Exception) {
                                        } finally {
                                            imageProxy.close()
                                        }
                                    }
                                }
                            try {
                                provider.unbindAll()
                                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                            } catch (_: Exception) {
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        scanError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        scannedText?.let { decoded ->
            LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)) {
                Text("Scanned Result", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))
                Text(decoded, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))
                CopyTextButton(textToCopy = decoded)
            }
        }
    }
}

@Composable
fun ImageResizerScreen() {
    val context = LocalContext.current
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scalePercent by remember { mutableFloatStateOf(50f) }
    var jpegQuality by remember { mutableFloatStateOf(85f) }
    var statusMsg by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            sourceBitmap = MediaUtils.loadBitmapFromUri(context, uri)
            statusMsg = null
        }
    }

    val resizedBitmap = remember(sourceBitmap, scalePercent) {
        sourceBitmap?.let { src ->
            val factor = scalePercent / 100f
            val w = (src.width * factor).toInt().coerceAtLeast(32)
            val h = (src.height * factor).toInt().coerceAtLeast(32)
            Bitmap.createScaledBitmap(src, w, h, true)
        }
    }

    val estKb = remember(resizedBitmap, jpegQuality) {
        resizedBitmap?.let { bmp ->
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, jpegQuality.toInt(), baos)
            baos.size() / 1024
        } ?: 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Button(
            onClick = {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pick Photo (Zero Permissions)")
        }

        if (sourceBitmap == null) {
            EmptyStateView(
                icon = Icons.Rounded.PhotoSizeSelectLarge,
                title = "Resize & Compress Offline",
                description = "Pick any photo via Android's private Photo Picker to shrink dimensions & file size locally."
            )
        } else {
            val orig = sourceBitmap!!
            val out = resizedBitmap!!
            LuxuryCard {
                Text("Original: ${orig.width} × ${orig.height} px", style = MaterialTheme.typography.bodySmall)
                Text("Resized: ${out.width} × ${out.height} px (~${estKb} KB)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Scale: ${scalePercent.toInt()}%", style = MaterialTheme.typography.labelLarge)
                Slider(value = scalePercent, onValueChange = { scalePercent = it }, valueRange = 10f..100f)
                Text("JPEG Quality: ${jpegQuality.toInt()}%", style = MaterialTheme.typography.labelLarge)
                Slider(value = jpegQuality, onValueChange = { jpegQuality = it }, valueRange = 20f..100f)
            }

            Image(
                bitmap = out.asImageBitmap(),
                contentDescription = "Resized Preview",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(18.dp))
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val saved = MediaUtils.saveBitmapToGallery(
                            context,
                            out,
                            "toolzify_resized_${System.currentTimeMillis()}",
                            Bitmap.CompressFormat.JPEG,
                            jpegQuality.toInt()
                        )
                        statusMsg = if (saved) "Saved resized JPEG to Pictures/Toolzify" else "Save failed"
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Save") }
                OutlinedButton(
                    onClick = {
                        MediaUtils.shareBitmap(context, out, "toolzify_resized", Bitmap.CompressFormat.JPEG, jpegQuality.toInt())
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Share") }
            }
            statusMsg?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        }
    }
}

@Composable
fun ShoppingListScreen(viewModel: ToolzifyViewModel) {
    val items by viewModel.shoppingItems.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("1") }
    var priceStr by remember { mutableStateOf("") }

    val totalEst = items.filter { !it.isPurchased }.sumOf { it.estimatedPrice }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LuxuryCard {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Qty") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Est. Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addShoppingItem(name, qty, "Groceries", priceStr.toDoubleOrNull() ?: 0.0)
                        name = ""
                        priceStr = ""
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Add to List") }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Remaining Est: ${String.format("%.2f", totalEst)}", style = MaterialTheme.typography.titleSmall)
            if (items.any { it.isPurchased }) {
                TextButton(onClick = { viewModel.clearPurchasedShopping() }) { Text("Clear Checked") }
            }
        }

        if (items.isEmpty()) {
            EmptyStateView(icon = Icons.Rounded.ShoppingBag, title = "Shopping List Empty", description = "Add groceries or errands above.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items, key = { it.id }) { item ->
                    LuxuryCard(onClick = { viewModel.toggleShoppingItem(item) }, contentPadding = PaddingValues(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = item.isPurchased, onCheckedChange = { viewModel.toggleShoppingItem(item) })
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${item.name} (${item.quantity})",
                                    textDecoration = if (item.isPurchased) TextDecoration.LineThrough else TextDecoration.None,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                if (item.estimatedPrice > 0) {
                                    Text(String.format("%.2f", item.estimatedPrice), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            IconButton(onClick = { viewModel.deleteShoppingItem(item) }) {
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
fun ExpenseTrackerScreen(viewModel: ToolzifyViewModel) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var isIncome by remember { mutableStateOf(false) }

    val totalExpense = expenses.filter { !it.isIncome }.sumOf { it.amount }
    val totalIncome = expenses.filter { it.isIncome }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LuxuryCard(modifier = Modifier.weight(1f), containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)) {
                Text("Income", style = MaterialTheme.typography.labelMedium)
                Text(String.format("%.2f", totalIncome), style = MonoMediumStyle, color = MaterialTheme.colorScheme.primary)
            }
            LuxuryCard(modifier = Modifier.weight(1f), containerColor = LocalToolzifyColors.current.goldContainer.copy(alpha = 0.55f)) {
                Text("Spent", style = MaterialTheme.typography.labelMedium)
                Text(String.format("%.2f", totalExpense), style = MonoMediumStyle)
            }
        }

        LuxuryCard {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !isIncome, onClick = { isIncome = false }, label = { Text("Expense") })
                FilterChip(selected = isIncome, onClick = { isIncome = true }, label = { Text("Income") })
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.weight(1.3f))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf("Food", "Transport", "Bills", "Shopping", "Health", "Salary")) { cat ->
                    FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull()
                    if (title.isNotBlank() && amt != null && amt > 0) {
                        viewModel.addExpense(title, amt, category, isIncome)
                        title = ""
                        amountStr = ""
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Log Transaction") }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(expenses, key = { it.id }) { exp ->
                LuxuryCard(contentPadding = PaddingValues(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(exp.title, style = MaterialTheme.typography.titleSmall)
                            Text(exp.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = (if (exp.isIncome) "+" else "-") + String.format("%.2f", exp.amount),
                                style = MonoMediumStyle,
                                color = if (exp.isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            IconButton(onClick = { viewModel.deleteExpense(exp) }) {
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
fun SavingsCalculatorScreen() {
    var initialStr by remember { mutableStateOf("1000") }
    var monthlyStr by remember { mutableStateOf("200") }
    var rateStr by remember { mutableStateOf("6.5") }
    var yearsStr by remember { mutableStateOf("5") }

    val initial = initialStr.toDoubleOrNull() ?: 0.0
    val monthly = monthlyStr.toDoubleOrNull() ?: 0.0
    val annualRate = (rateStr.toDoubleOrNull() ?: 0.0) / 100.0
    val years = (yearsStr.toIntOrNull() ?: 1).coerceIn(1, 60)

    val monthlyRate = annualRate / 12.0
    val months = years * 12
    val futureInitial = initial * (1.0 + monthlyRate).pow(months)
    val futureMonthly = if (monthlyRate == 0.0) monthly * months else monthly * (((1.0 + monthlyRate).pow(months) - 1.0) / monthlyRate)
    val totalFuture = futureInitial + futureMonthly
    val totalDeposited = initial + (monthly * months)
    val totalInterest = (totalFuture - totalDeposited).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LuxuryCard {
            OutlinedTextField(value = initialStr, onValueChange = { initialStr = it }, label = { Text("Initial Deposit") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = monthlyStr, onValueChange = { monthlyStr = it }, label = { Text("Monthly Contribution") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = rateStr, onValueChange = { rateStr = it }, label = { Text("Annual Rate (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = yearsStr, onValueChange = { yearsStr = it }, label = { Text("Years") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f))
            }
        }

        LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
            Text("Projected Balance ($years Years)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(String.format("%,.2f", totalFuture), style = MonoLargeStyle)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total Contributed: ${String.format("%,.2f", totalDeposited)}", style = MaterialTheme.typography.bodyMedium)
            Text("Compound Interest Earned: ${String.format("%,.2f", totalInterest)}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun WaterIntakeScreen(viewModel: ToolzifyViewModel) {
    val logs by viewModel.waterLogsToday.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val triggerHaptic = rememberHapticTrigger()

    val totalMl = logs.sumOf { it.amountMl }
    val targetMl = prefs.dailyWaterTargetMl
    val progress = (totalMl.toFloat() / targetMl.coerceAtLeast(1)).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedProgressRing(progress = progress, size = 200.dp, strokeWidth = 16.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Text("$totalMl ml", style = MonoMediumStyle, fontWeight = FontWeight.Bold)
                Text("of $targetMl ml", style = MaterialTheme.typography.bodySmall)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(150, 250, 500).forEach { ml ->
                Button(
                    onClick = {
                        triggerHaptic()
                        viewModel.logWater(ml)
                    },
                    shape = RoundedCornerShape(16.dp)
                ) { Text("+$ml ml") }
            }
        }

        LuxuryCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Daily Target: $targetMl ml", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = { viewModel.setDailyWaterTarget(targetMl - 250) }) { Text("-250") }
                    OutlinedButton(onClick = { viewModel.setDailyWaterTarget(targetMl + 250) }) { Text("+250") }
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(logs, key = { it.id }) { log ->
                LuxuryCard(contentPadding = PaddingValues(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("+${log.amountMl} ml logged today", style = MaterialTheme.typography.bodyMedium)
                        IconButton(onClick = { viewModel.deleteWaterLog(log) }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetPlannerScreen(viewModel: ToolzifyViewModel) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    var limitInput by remember(prefs.monthlyBudgetLimit) { mutableStateOf(prefs.monthlyBudgetLimit.toInt().toString()) }

    val spent = expenses.filter { !it.isIncome }.sumOf { it.amount }
    val budget = prefs.monthlyBudgetLimit.coerceAtLeast(1.0)
    val remaining = (budget - spent)
    val ratio = (spent / budget).toFloat().coerceIn(0f, 1f)
    val byCat = expenses.filter { !it.isIncome }.groupBy { it.category }.mapValues { e -> e.value.sumOf { it.amount } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LuxuryCard {
            Text("Monthly Budget Limit", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = limitInput,
                    onValueChange = { limitInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    limitInput.toDoubleOrNull()?.let { viewModel.setMonthlyBudgetLimit(it) }
                }) { Text("Set Cap") }
            }
        }

        LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Spent: ${String.format("%.2f", spent)}", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (remaining >= 0) "Left: ${String.format("%.2f", remaining)}" else "Over: ${String.format("%.2f", -remaining)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (remaining >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { ratio },
                color = if (remaining >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
            )
        }

        LuxuryCard {
            Text("Spending by Category", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (byCat.isEmpty()) {
                Text("Log expenses in Expense Tracker to see your category breakdown here.", style = MaterialTheme.typography.bodySmall)
            } else {
                byCat.forEach { (cat, sum) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(cat)
                        Text(String.format("%.2f", sum), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
