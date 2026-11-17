package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.ToolUsageEntity
import com.example.data.preferences.UserPreferences
import com.example.model.ToolCategory
import com.example.model.ToolDefinition
import com.example.model.ToolIds
import com.example.model.ToolRegistry
import com.example.ui.ToolzifyViewModel
import com.example.ui.components.*
import com.example.ui.theme.LocalToolzifyColors
import java.time.LocalDate

enum class BottomTab(
    val id: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", Icons.Rounded.Home, Icons.Outlined.Home),
    CATEGORIES("categories", Icons.Rounded.GridView, Icons.Outlined.GridView),
    FAVORITES("favorites", Icons.Rounded.Favorite, Icons.Outlined.FavoriteBorder),
    PLANNER("planner", Icons.Rounded.CalendarToday, Icons.Outlined.CalendarToday),
    SETTINGS("settings", Icons.Rounded.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainShell(
    selectedTab: BottomTab,
    onSelectTab: (BottomTab) -> Unit,
    viewModel: ToolzifyViewModel,
    onOpenTool: (String) -> Unit,
    onOpenPrivacy: () -> Unit
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val strings = LocalAppSettings.current.strings
    val ext = LocalToolzifyColors.current
    val triggerHaptic = rememberHapticTrigger()

    fun tabLabel(tab: BottomTab): String = when (tab) {
        BottomTab.HOME -> strings.navHome
        BottomTab.CATEGORIES -> strings.navCategories
        BottomTab.FAVORITES -> strings.navFavorites
        BottomTab.PLANNER -> strings.navPlanner
        BottomTab.SETTINGS -> strings.navSettings
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!isWide) {
                    NavigationBar(
                        containerColor = ext.cardSurface,
                        tonalElevation = 4.dp,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        BottomTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    triggerHaptic()
                                    onSelectTab(tab)
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tabLabel(tab)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tabLabel(tab),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.id}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWide) {
                    NavigationRail(
                        containerColor = ext.cardSurface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        BottomTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    triggerHaptic()
                                    onSelectTab(tab)
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tabLabel(tab)
                                    )
                                },
                                label = { Text(tabLabel(tab)) },
                                modifier = Modifier.testTag("nav_rail_${tab.id}")
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tabContent",
                        modifier = Modifier.widthIn(max = 900.dp)
                    ) { tab ->
                        when (tab) {
                            BottomTab.HOME -> HomeDashboardScreen(
                                prefs = prefs,
                                viewModel = viewModel,
                                onOpenTool = onOpenTool,
                                onBrowseCategories = { onSelectTab(BottomTab.CATEGORIES) }
                            )
                            BottomTab.CATEGORIES -> CategoriesScreen(
                                prefs = prefs,
                                viewModel = viewModel,
                                onOpenTool = onOpenTool
                            )
                            BottomTab.FAVORITES -> FavoritesScreen(
                                viewModel = viewModel,
                                onOpenTool = onOpenTool,
                                onBrowseCategories = { onSelectTab(BottomTab.CATEGORIES) }
                            )
                            BottomTab.PLANNER -> PlannerDashboardScreen(
                                viewModel = viewModel,
                                onOpenTool = onOpenTool
                            )
                            BottomTab.SETTINGS -> SettingsScreen(
                                prefs = prefs,
                                viewModel = viewModel,
                                onOpenPrivacy = onOpenPrivacy
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeDashboardScreen(
    prefs: UserPreferences,
    viewModel: ToolzifyViewModel,
    onOpenTool: (String) -> Unit,
    onBrowseCategories: () -> Unit
) {
    val strings = LocalAppSettings.current.strings
    val ext = LocalToolzifyColors.current
    val usageMap by viewModel.toolUsageMap.collectAsStateWithLifecycle()
    val favorites by viewModel.favoriteTools.collectAsStateWithLifecycle()
    val recents by viewModel.recentTools.collectAsStateWithLifecycle()
    val todos by viewModel.todos.collectAsStateWithLifecycle()
    val waterLogs by viewModel.waterLogsToday.collectAsStateWithLifecycle()
    val triggerHaptic = rememberHapticTrigger()

    var searchQuery by remember { mutableStateOf("") }

    val availableTools = remember(prefs.showIslamicTools) {
        ToolRegistry.allTools.filter { prefs.showIslamicTools || !it.category.isOptional }
    }

    val filteredTools = remember(searchQuery, availableTools) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) emptyList()
        else availableTools.filter {
            it.name.lowercase().contains(q) ||
                it.shortDesc.lowercase().contains(q) ||
                it.category.title.lowercase().contains(q) ||
                it.keywords.any { k -> k.contains(q) }
        }
    }

    val pendingTasksCount = remember(todos) { todos.count { !it.isCompleted } }
    val todayWaterMl = remember(waterLogs) { waterLogs.sumOf { it.amountMl } }
    val waterRatio = (todayWaterMl.toFloat() / prefs.dailyWaterTargetMl.coerceAtLeast(1)).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Brand & Hero Botanical Banner
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = ext.cardSurface,
                border = BorderStroke(1.dp, ext.cardBorder),
                shadowElevation = if (ext.isDark) 0.dp else 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_botanical),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(if (ext.isDark) 0.14f else 0.42f)
                    )
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_toolzify_emblem),
                                    contentDescription = "Toolzify",
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Toolzify",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = ext.goldContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = ext.gold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Offline",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ext.onGoldContainer
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = strings.greeting,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Animated Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.searchHint) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
                trailingIcon = {
                    AnimatedVisibility(visible = searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ext.cardSurface,
                    unfocusedContainerColor = ext.cardSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = ext.cardBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_search_field")
            )
        }

        if (searchQuery.isNotBlank()) {
            item {
                Text(
                    text = "${filteredTools.size} matching tools",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (filteredTools.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Rounded.SearchOff,
                        title = "No tools matched \"$searchQuery\"",
                        description = "Try searching for calculator, timer, notes, QR, or habit."
                    )
                }
            } else {
                items(filteredTools.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pair.forEach { tool ->
                            ToolGridCard(
                                tool = tool,
                                isFavorite = usageMap[tool.id]?.isFavorite == true,
                                onOpen = { onOpenTool(tool.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(tool.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            // Today at a Glance & Quick Actions (Replaces Daily Tip)
            item {
                LuxuryCard(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.testTag("home_today_glance_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Today at a Glance",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$pendingTasksCount tasks left • ${todayWaterMl}ml / ${prefs.dailyWaterTargetMl}ml water",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        FilledTonalButton(
                            onClick = {
                                triggerHaptic()
                                viewModel.logWater(250)
                            },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("home_quick_water_button")
                        ) {
                            Icon(
                                Icons.Rounded.WaterDrop,
                                contentDescription = "Log 250ml water",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+250ml", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { waterRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val quickActions = listOf(
                            Triple(ToolIds.STANDARD_CALC, "Calculator", Icons.Rounded.Calculate),
                            Triple(ToolIds.TODO_LIST, "Tasks ($pendingTasksCount)", Icons.Rounded.CheckCircle),
                            Triple(ToolIds.NOTES, "Quick Note", Icons.Rounded.EditNote),
                            Triple(ToolIds.POMODORO_TIMER, "Focus 25m", Icons.Rounded.Timer),
                            Triple(ToolIds.QR_GENERATOR, "QR Code", Icons.Rounded.QrCode2)
                        )
                        items(quickActions) { (toolId, label, icon) ->
                            AssistChip(
                                onClick = {
                                    triggerHaptic()
                                    onOpenTool(toolId)
                                },
                                label = { Text(label) },
                                leadingIcon = {
                                    Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            )
                        }
                    }
                }
            }

            // Tool Categories Quick Access
            item {
                SectionTitleRow(title = strings.categories, actionText = "All", onAction = onBrowseCategories)
                Spacer(modifier = Modifier.height(10.dp))
                val visibleCategories = ToolCategory.entries.filter { prefs.showIslamicTools || !it.isOptional }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(visibleCategories) { cat ->
                        val catLabel = when (cat) {
                            ToolCategory.CALCULATORS -> strings.catCalculators
                            ToolCategory.PRODUCTIVITY -> strings.catProductivity
                            ToolCategory.TEXT -> strings.catText
                            ToolCategory.UTILITIES -> strings.catUtilities
                            ToolCategory.ISLAMIC -> strings.catIslamic
                        }
                        Surface(
                            onClick = onBrowseCategories,
                            shape = RoundedCornerShape(16.dp),
                            color = ext.cardSurface,
                            border = BorderStroke(1.dp, ext.cardBorder),
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(cat.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(catLabel, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            // Recently Used Tools (if any)
            if (recents.isNotEmpty()) {
                item {
                    SectionTitleRow(title = strings.recentlyUsed)
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(recents, key = { it.id }) { tool ->
                            CompactHorizontalToolPill(
                                tool = tool,
                                onClick = { onOpenTool(tool.id) }
                            )
                        }
                    }
                }
            }

            // Favorite Tools (if any)
            if (favorites.isNotEmpty()) {
                item {
                    SectionTitleRow(title = strings.favoriteTools)
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(favorites, key = { it.id }) { tool ->
                            CompactHorizontalToolPill(
                                tool = tool,
                                isGoldAccent = true,
                                onClick = { onOpenTool(tool.id) }
                            )
                        }
                    }
                }
            }

            // Featured Tools Carousel
            item {
                SectionTitleRow(title = strings.featuredTools)
                Spacer(modifier = Modifier.height(10.dp))
                val featured = availableTools.filter { it.isFeatured }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    itemsIndexed(featured, key = { _, t -> t.id }) { idx, tool ->
                        StaggeredEntrance(index = idx) {
                            ToolGridCard(
                                tool = tool,
                                isFavorite = usageMap[tool.id]?.isFavorite == true,
                                onOpen = { onOpenTool(tool.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(tool.id) },
                                modifier = Modifier.width(172.dp)
                            )
                        }
                    }
                }
            }

            // Popular Tools Grid
            item {
                SectionTitleRow(title = strings.popularTools)
            }
            val popular = availableTools.filter { it.isPopular }
            items(popular.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { tool ->
                        ToolGridCard(
                            tool = tool,
                            isFavorite = usageMap[tool.id]?.isFavorite == true,
                            onOpen = { onOpenTool(tool.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(tool.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CompactHorizontalToolPill(
    tool: ToolDefinition,
    isGoldAccent: Boolean = false,
    onClick: () -> Unit
) {
    val ext = LocalToolzifyColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (isGoldAccent) ext.goldContainer.copy(alpha = 0.65f) else ext.cardSurface,
        border = BorderStroke(1.dp, if (isGoldAccent) ext.gold.copy(alpha = 0.45f) else ext.cardBorder),
        modifier = Modifier.minimumInteractiveComponentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = tool.name,
                tint = if (isGoldAccent) ext.gold else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tool.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SectionTitleRow(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionText, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun CategoriesScreen(
    prefs: UserPreferences,
    viewModel: ToolzifyViewModel,
    onOpenTool: (String) -> Unit
) {
    val strings = LocalAppSettings.current.strings
    val usageMap by viewModel.toolUsageMap.collectAsStateWithLifecycle()
    val categories = remember(prefs.showIslamicTools) {
        ToolCategory.entries.filter { prefs.showIslamicTools || !it.isOptional }
    }
    var selectedCategory by remember(categories) { mutableStateOf<ToolCategory?>(null) }

    val toolsToShow = remember(selectedCategory, prefs.showIslamicTools) {
        ToolRegistry.allTools.filter { tool ->
            (prefs.showIslamicTools || !tool.category.isOptional) &&
                (selectedCategory == null || tool.category == selectedCategory)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = strings.categories,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All (${toolsToShow.size})") }
                    )
                }
                items(categories) { cat ->
                    val label = when (cat) {
                        ToolCategory.CALCULATORS -> strings.catCalculators
                        ToolCategory.PRODUCTIVITY -> strings.catProductivity
                        ToolCategory.TEXT -> strings.catText
                        ToolCategory.UTILITIES -> strings.catUtilities
                        ToolCategory.ISLAMIC -> strings.catIslamic
                    }
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(label) },
                        leadingIcon = {
                            Icon(cat.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.testTag("category_chip_${cat.id}")
                    )
                }
            }
        }

        val grouped = toolsToShow.groupBy { it.category }
        grouped.forEach { (category, list) ->
            item(key = "header_${category.id}") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Icon(category.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (category.isOptional) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = LocalToolzifyColors.current.goldContainer
                        ) {
                            Text(
                                "Optional",
                                style = MaterialTheme.typography.labelSmall,
                                color = LocalToolzifyColors.current.onGoldContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            items(list.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { tool ->
                        ToolGridCard(
                            tool = tool,
                            isFavorite = usageMap[tool.id]?.isFavorite == true,
                            onOpen = { onOpenTool(tool.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(tool.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    viewModel: ToolzifyViewModel,
    onOpenTool: (String) -> Unit,
    onBrowseCategories: () -> Unit
) {
    val strings = LocalAppSettings.current.strings
    val favorites by viewModel.favoriteTools.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = strings.favoriteTools,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Your pinned everyday shortcuts, saved locally",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (favorites.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Rounded.FavoriteBorder,
                    title = strings.emptyFavoritesTitle,
                    description = strings.emptyFavoritesBody,
                    actionLabel = "Explore Tools",
                    onAction = onBrowseCategories
                )
            }
        } else {
            items(favorites.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { tool ->
                        ToolGridCard(
                            tool = tool,
                            isFavorite = true,
                            onOpen = { onOpenTool(tool.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(tool.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun PlannerDashboardScreen(
    viewModel: ToolzifyViewModel,
    onOpenTool: (String) -> Unit
) {
    val ext = LocalToolzifyColors.current
    val todos by viewModel.todos.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val goals by viewModel.dailyGoals.collectAsStateWithLifecycle()
    val waterLogs by viewModel.waterLogsToday.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()

    val totalWaterMl = waterLogs.sumOf { it.amountMl }
    val waterProgress = (totalWaterMl.toFloat() / prefs.dailyWaterTargetMl.coerceAtLeast(1)).coerceIn(0f, 1f)
    val pendingTodos = todos.filter { !it.isCompleted }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Daily Planner",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = LocalDate.now().toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Quick Overview Ring Card
        item {
            LuxuryCard(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Today's Snapshot", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "${pendingTodos.size} tasks remaining • ${habits.size} active habits",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(onClick = { onOpenTool(ToolIds.TODO_LIST) }) {
                                Text("To-Do")
                            }
                            FilledTonalButton(onClick = { onOpenTool(ToolIds.POMODORO_TIMER) }) {
                                Text("Focus")
                            }
                        }
                    }
                    AnimatedProgressRing(
                        progress = waterProgress,
                        size = 86.dp,
                        strokeWidth = 9.dp
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text("${(waterProgress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Launch Productivity Grid
        item {
            Text("Productivity Suite", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            val plannerTools = listOf(
                ToolIds.DAILY_PLANNER,
                ToolIds.HABIT_TRACKER,
                ToolIds.NOTES,
                ToolIds.CHECKLIST_MAKER,
                ToolIds.DAILY_GOALS,
                ToolIds.WATER_INTAKE
            ).mapNotNull { ToolRegistry.findById(it) }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                plannerTools.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { tool ->
                            LuxuryCard(
                                onClick = { onOpenTool(tool.id) },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(tool.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(tool.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top Pending Tasks Preview
        item {
            SectionTitleRow(
                title = "Upcoming Tasks (${pendingTodos.size})",
                actionText = "Open List",
                onAction = { onOpenTool(ToolIds.TODO_LIST) }
            )
        }

        if (pendingTodos.isEmpty()) {
            item {
                LuxuryCard(containerColor = ext.goldContainer.copy(alpha = 0.4f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("All caught up! Add a task in To-Do List or Daily Planner.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            items(pendingTodos.take(5), key = { it.id }) { todo ->
                LuxuryCard(
                    onClick = { viewModel.toggleTodo(todo) },
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = todo.isCompleted, onCheckedChange = { viewModel.toggleTodo(todo) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(todo.title, style = MaterialTheme.typography.titleSmall)
                            if (todo.timeSlot.isNotBlank()) {
                                Text(todo.timeSlot, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        // Active Goals Preview
        if (goals.isNotEmpty()) {
            item {
                SectionTitleRow(
                    title = "Today's Goals",
                    actionText = "Manage",
                    onAction = { onOpenTool(ToolIds.DAILY_GOALS) }
                )
            }
            items(goals.take(3), key = { it.id }) { goal ->
                LuxuryCard(contentPadding = PaddingValues(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(goal.title, style = MaterialTheme.typography.titleSmall)
                        Text("${goal.currentValue} / ${goal.targetValue} ${goal.unit}", style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (goal.currentValue.toFloat() / goal.targetValue.coerceAtLeast(1)).coerceIn(0f, 1f) },
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
