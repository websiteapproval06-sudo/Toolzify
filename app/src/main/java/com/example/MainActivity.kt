package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.StringsCatalog
import com.example.model.ToolIds
import com.example.model.ToolRegistry
import com.example.ui.ToolzifyViewModel
import com.example.ui.components.AppRuntimeSettings
import com.example.ui.components.LocalAppSettings
import com.example.ui.components.ToolzifyToolTopBar
import com.example.ui.screens.*
import com.example.ui.theme.ToolzifyTheme

private sealed interface AppScreen {
    data object Splash : AppScreen
    data object Onboarding : AppScreen
    data object Main : AppScreen
    data object Privacy : AppScreen
    data class ToolDetail(val toolId: String) : AppScreen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToolzifyApp()
        }
    }
}

@Composable
fun ToolzifyApp(viewModel: ToolzifyViewModel = viewModel()) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val usageMap by viewModel.toolUsageMap.collectAsStateWithLifecycle()

    val strings = remember(prefs.appLanguage) { StringsCatalog.forCode(prefs.appLanguage) }
    val layoutDirection = if (strings.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }

    val runtimeSettings = remember(strings, prefs.animationsEnabled, prefs.hapticsEnabled) {
        AppRuntimeSettings(
            strings = strings,
            animationsEnabled = prefs.animationsEnabled,
            hapticsEnabled = prefs.hapticsEnabled
        )
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalAppSettings provides runtimeSettings
    ) {
        ToolzifyTheme(
            themeMode = prefs.themeMode,
            accent = prefs.themeAccent
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "rootNavigation"
            ) { screen ->
                when (screen) {
                    AppScreen.Splash -> {
                        AnimatedSplashScreen(
                            onFinished = {
                                currentScreen = if (prefs.onboardingCompleted) AppScreen.Main else AppScreen.Onboarding
                            }
                        )
                    }
                    AppScreen.Onboarding -> {
                        OnboardingScreen(
                            onComplete = {
                                viewModel.completeOnboarding()
                                currentScreen = AppScreen.Main
                            }
                        )
                    }
                    AppScreen.Main -> {
                        MainShell(
                            selectedTab = selectedTab,
                            onSelectTab = { selectedTab = it },
                            viewModel = viewModel,
                            onOpenTool = { toolId ->
                                viewModel.onToolOpened(toolId)
                                currentScreen = AppScreen.ToolDetail(toolId)
                            },
                            onOpenPrivacy = {
                                currentScreen = AppScreen.Privacy
                            }
                        )
                    }
                    AppScreen.Privacy -> {
                        BackHandler { currentScreen = AppScreen.Main }
                        PrivacyInfoScreen(onBack = { currentScreen = AppScreen.Main })
                    }
                    is AppScreen.ToolDetail -> {
                        BackHandler { currentScreen = AppScreen.Main }
                        val tool = ToolRegistry.findById(screen.toolId)
                        val isFav = usageMap[screen.toolId]?.isFavorite == true

                        Scaffold(
                            containerColor = MaterialTheme.colorScheme.background,
                            topBar = {
                                ToolzifyToolTopBar(
                                    title = tool?.name ?: "Tool",
                                    subtitle = tool?.category?.title,
                                    isFavorite = isFav,
                                    onBack = { currentScreen = AppScreen.Main },
                                    onToggleFavorite = { viewModel.toggleFavorite(screen.toolId) }
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Box(modifier = Modifier.widthIn(max = 760.dp)) {
                                    ToolContentRouter(toolId = screen.toolId, viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolContentRouter(toolId: String, viewModel: ToolzifyViewModel) {
    when (toolId) {
        // Calculators
        ToolIds.STANDARD_CALC -> StandardCalculatorScreen()
        ToolIds.SCIENTIFIC_CALC -> ScientificCalculatorScreen()
        ToolIds.PERCENTAGE_CALC -> PercentageCalculatorScreen()
        ToolIds.AGE_CALC -> AgeCalculatorScreen()
        ToolIds.DATE_DIFF_CALC -> DateDifferenceScreen()
        ToolIds.DISCOUNT_CALC -> DiscountCalculatorScreen()
        ToolIds.GST_CALC -> GstCalculatorScreen()
        ToolIds.UNIT_CONVERTER -> UnitConverterScreen()
        ToolIds.BMI_CALC -> BmiCalculatorScreen()

        // Productivity
        ToolIds.TODO_LIST -> TodoListScreen(viewModel)
        ToolIds.DAILY_PLANNER -> DailyPlannerToolScreen(viewModel)
        ToolIds.NOTES -> NotesScreen(viewModel)
        ToolIds.CHECKLIST_MAKER -> ChecklistMakerScreen(viewModel)
        ToolIds.HABIT_TRACKER -> HabitTrackerScreen(viewModel)
        ToolIds.POMODORO_TIMER -> PomodoroTimerScreen()
        ToolIds.STOPWATCH -> StopwatchScreen()
        ToolIds.COUNTDOWN_TIMER -> CountdownTimerScreen()
        ToolIds.DAILY_GOALS -> DailyGoalsScreen(viewModel)

        // Text Tools
        ToolIds.WORD_COUNTER -> WordCounterScreen()
        ToolIds.CHAR_COUNTER -> CharacterCounterScreen()
        ToolIds.CASE_CONVERTER -> CaseConverterScreen()
        ToolIds.REMOVE_SPACES -> RemoveExtraSpacesScreen()
        ToolIds.TEXT_REVERSER -> TextReverserScreen()
        ToolIds.PASSWORD_GEN -> PasswordGeneratorScreen()
        ToolIds.RANDOM_NUMBER -> RandomNumberGeneratorScreen()

        // Everyday Utilities
        ToolIds.QR_GENERATOR -> QrGeneratorScreen()
        ToolIds.QR_SCANNER -> QrScannerScreen()
        ToolIds.IMAGE_RESIZER -> ImageResizerScreen()
        ToolIds.SHOPPING_LIST -> ShoppingListScreen(viewModel)
        ToolIds.EXPENSE_TRACKER -> ExpenseTrackerScreen(viewModel)
        ToolIds.SAVINGS_CALC -> SavingsCalculatorScreen()
        ToolIds.WATER_INTAKE -> WaterIntakeScreen(viewModel)
        ToolIds.BUDGET_PLANNER -> BudgetPlannerScreen(viewModel)

        // Optional Islamic Tools
        ToolIds.PRAYER_TIMES -> PrayerTimesScreen(viewModel)
        ToolIds.QIBLA_COMPASS -> QiblaCompassScreen(viewModel)
        ToolIds.TASBEEH_COUNTER -> TasbeehCounterScreen(viewModel)
        ToolIds.DAILY_DUAS -> DailyDuasScreen()
        ToolIds.RAMADAN_PLANNER -> RamadanPlannerScreen(viewModel)
        ToolIds.ISLAMIC_DATE_CONV -> IslamicDateConverterScreen()
    }
}
