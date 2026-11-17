package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.LocalAppSettings
import com.example.ui.theme.LocalToolzifyColors
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashScreen(onFinished: () -> Unit) {
    val ext = LocalToolzifyColors.current
    val animationsEnabled = LocalAppSettings.current.animationsEnabled

    var started by remember { mutableStateOf(!animationsEnabled) }
    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.65f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "splashLogoScale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = if (animationsEnabled) 650 else 0),
        label = "splashAlpha"
    )
    val arcSweep by animateFloatAsState(
        targetValue = if (started) 360f else 0f,
        animationSpec = tween(durationMillis = if (animationsEnabled) 1050 else 0, easing = FastOutSlowInEasing),
        label = "splashArc"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "floatingOrbs")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "floatY"
    )

    LaunchedEffect(Unit) {
        started = true
        delay(if (animationsEnabled) 1400L else 400L)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        ext.creamSurface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_hero_botanical),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (ext.isDark) 0.08f else 0.28f)
        )

        // Subtle floating golden & emerald decorative orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = ext.gold.copy(alpha = 0.14f),
                radius = 70.dp.toPx(),
                center = Offset(size.width * 0.18f, size.height * 0.22f + floatY)
            )
            drawCircle(
                color = Color(0xFF154734).copy(alpha = 0.08f),
                radius = 95.dp.toPx(),
                center = Offset(size.width * 0.85f, size.height * 0.78f - floatY)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .alpha(contentAlpha)
        ) {
            Box(
                modifier = Modifier.size(136.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = ext.gold,
                        startAngle = -90f,
                        sweepAngle = arcSweep,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.ic_toolzify_emblem),
                    contentDescription = "Toolzify Logo",
                    modifier = Modifier
                        .size(108.dp)
                        .scale(logoScale)
                        .clip(RoundedCornerShape(28.dp))
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Toolzify",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Make Your Everyday Easier",
                style = MaterialTheme.typography.titleMedium,
                color = ext.gold
            )
            Spacer(modifier = Modifier.height(28.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "100% Offline & Private",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val ext = LocalToolzifyColors.current
    var page by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Toolzify",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(
                    onClick = onComplete,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("onboarding_skip_button")
                ) {
                    Text("Skip", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            AnimatedContent(
                targetState = page,
                transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(200)) },
                label = "onboardingPage"
            ) { currentPage ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(max = 480.dp)
                ) {
                    val imgRes = if (currentPage == 0) R.drawable.img_onboarding_tools else R.drawable.img_onboarding_privacy
                    val title = if (currentPage == 0) {
                        "Your Everyday Tools,\nOne Beautiful App"
                    } else {
                        "Zero Data Collection.\nAlways Private."
                    }
                    val desc = if (currentPage == 0) {
                        "39 thoughtfully crafted calculators, timers, planners, text utilities, and mindful tools ready whenever you need them."
                    } else {
                        "No accounts, no sign-ups, no cloud tracking. Every note, task, habit, and calculation stays securely on your device."
                    }

                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = ext.cardSurface,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                    ) {
                        Image(
                            painter = painterResource(id = imgRes),
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(2) { idx ->
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (idx == page) 28.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (idx == page) MaterialTheme.colorScheme.primary else ext.cardBorder)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (page == 0) page = 1 else onComplete()
                },
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_next_button")
            ) {
                Text(
                    text = if (page == 0) "Continue" else "Get Started",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
