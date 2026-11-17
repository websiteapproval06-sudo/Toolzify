package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class ToolzifyExtendedColors(
    val gold: Color,
    val goldContainer: Color,
    val onGoldContainer: Color,
    val creamSurface: Color,
    val cardSurface: Color,
    val cardBorder: Color,
    val success: Color,
    val warning: Color,
    val isDark: Boolean
)

val LocalToolzifyColors = staticCompositionLocalOf {
    ToolzifyExtendedColors(
        gold = BurnishedGold,
        goldContainer = SoftGoldContainer,
        onGoldContainer = OnSoftGold,
        creamSurface = CreamSurface,
        cardSurface = WarmCardWhite,
        cardBorder = WarmBorder,
        success = ForestGreenLight,
        warning = BurnishedGold,
        isDark = false
    )
}

val ToolzifyShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun ToolzifyTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accent: ThemeAccent = ThemeAccent.FOREST_GOLD,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) {
        val darkPrimary = when (accent) {
            ThemeAccent.FOREST_GOLD -> MintEmeraldDark
            ThemeAccent.EMERALD_JADE -> Color(0xFF6CD6BA)
            ThemeAccent.TERRACOTTA_SAND -> Color(0xFFE79D82)
            ThemeAccent.MIDNIGHT_NAVY -> Color(0xFF8AB4F8)
        }
        val darkPrimaryContainer = when (accent) {
            ThemeAccent.FOREST_GOLD -> DeepForestContainerDark
            ThemeAccent.EMERALD_JADE -> Color(0xFF144239)
            ThemeAccent.TERRACOTTA_SAND -> Color(0xFF4F2418)
            ThemeAccent.MIDNIGHT_NAVY -> Color(0xFF1B3358)
        }
        darkColorScheme(
            primary = darkPrimary,
            onPrimary = OnMintDark,
            primaryContainer = darkPrimaryContainer,
            onPrimaryContainer = InkLight,
            secondary = WarmGoldDark,
            onSecondary = Color(0xFF281C02),
            secondaryContainer = GoldContainerDark,
            onSecondaryContainer = ChampagneGold,
            tertiary = accent.accent,
            background = ObsidianBackground,
            onBackground = InkLight,
            surface = ObsidianSurface,
            onSurface = InkLight,
            surfaceVariant = ObsidianCard,
            onSurfaceVariant = InkMutedLight,
            outline = ObsidianBorder,
            error = Color(0xFFEF8C8C),
            onError = Color(0xFF3E0A0A)
        )
    } else {
        val lightPrimaryContainer = when (accent) {
            ThemeAccent.FOREST_GOLD -> SageMintContainer
            ThemeAccent.EMERALD_JADE -> Color(0xFFCDEEE5)
            ThemeAccent.TERRACOTTA_SAND -> Color(0xFFF6DDD4)
            ThemeAccent.MIDNIGHT_NAVY -> Color(0xFFD7E4F8)
        }
        lightColorScheme(
            primary = accent.primary,
            onPrimary = Color.White,
            primaryContainer = lightPrimaryContainer,
            onPrimaryContainer = accent.primary,
            secondary = accent.accent,
            onSecondary = Color.White,
            secondaryContainer = SoftGoldContainer,
            onSecondaryContainer = OnSoftGold,
            tertiary = ForestGreenLight,
            background = IvoryBackground,
            onBackground = InkPrimary,
            surface = CreamSurface,
            onSurface = InkPrimary,
            surfaceVariant = SandContainer,
            onSurfaceVariant = InkSecondary,
            outline = WarmBorder,
            error = ErrorCrimson,
            onError = Color.White,
            errorContainer = ErrorContainerLight
        )
    }

    val extendedColors = if (isDark) {
        ToolzifyExtendedColors(
            gold = WarmGoldDark,
            goldContainer = GoldContainerDark,
            onGoldContainer = ChampagneGold,
            creamSurface = ObsidianSurface,
            cardSurface = ObsidianCard,
            cardBorder = ObsidianBorder,
            success = MintEmeraldDark,
            warning = WarmGoldDark,
            isDark = true
        )
    } else {
        ToolzifyExtendedColors(
            gold = accent.accent,
            goldContainer = SoftGoldContainer,
            onGoldContainer = OnSoftGold,
            creamSurface = CreamSurface,
            cardSurface = WarmCardWhite,
            cardBorder = WarmBorder,
            success = accent.primary,
            warning = accent.accent,
            isDark = false
        )
    }

    CompositionLocalProvider(LocalToolzifyColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ToolzifyTypography,
            shapes = ToolzifyShapes,
            content = content
        )
    }
}
