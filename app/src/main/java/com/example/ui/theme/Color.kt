package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Brand Palette: Warm Ivory, Deep Forest Green, Burnished Gold
val IvoryBackground = Color(0xFFFBF8F1)
val CreamSurface = Color(0xFFF4EDE0)
val WarmCardWhite = Color(0xFFFFFEFA)
val SandContainer = Color(0xFFECE2CE)
val WarmBorder = Color(0xFFE2D7C1)

val ForestGreen = Color(0xFF154734)
val ForestGreenLight = Color(0xFF216149)
val SageMintContainer = Color(0xFFD5E8DD)
val OnSageMint = Color(0xFF0C2E20)

val BurnishedGold = Color(0xFFC59433)
val ChampagneGold = Color(0xFFE7C878)
val SoftGoldContainer = Color(0xFFFAF0D4)
val OnSoftGold = Color(0xFF4A3306)

val InkPrimary = Color(0xFF1A241E)
val InkSecondary = Color(0xFF536158)
val ErrorCrimson = Color(0xFFB33A3A)
val ErrorContainerLight = Color(0xFFFBE7E7)

// Dark Mode Palette: Deep Obsidian Forest & Warm Gold
val ObsidianBackground = Color(0xFF0D1813)
val ObsidianSurface = Color(0xFF14241D)
val ObsidianCard = Color(0xFF1B3026)
val ObsidianBorder = Color(0xFF2A4538)

val MintEmeraldDark = Color(0xFF7AD1A8)
val OnMintDark = Color(0xFF06291A)
val DeepForestContainerDark = Color(0xFF1A4332)
val WarmGoldDark = Color(0xFFE5C16B)
val GoldContainerDark = Color(0xFF3E3012)
val InkLight = Color(0xFFF2EFE7)
val InkMutedLight = Color(0xFFB1BEB5)

// Additional Theme Accent Presets
enum class ThemeAccent(val id: String, val label: String, val primary: Color, val accent: Color) {
    FOREST_GOLD("forest_gold", "Forest & Gold", Color(0xFF154734), Color(0xFFC59433)),
    EMERALD_JADE("emerald_jade", "Botanical Jade", Color(0xFF0F5C4D), Color(0xFF2F9D82)),
    TERRACOTTA_SAND("terracotta_sand", "Warm Terracotta", Color(0xFF8C3F2B), Color(0xFFD08C46)),
    MIDNIGHT_NAVY("midnight_navy", "Midnight Brass", Color(0xFF1B365D), Color(0xFFC89B3C));

    companion object {
        fun fromId(id: String): ThemeAccent = entries.find { it.id == id } ?: FOREST_GOLD
    }
}

enum class ThemeMode(val id: String, val label: String) {
    SYSTEM("system", "System"),
    LIGHT("light", "Light"),
    DARK("dark", "Dark");

    companion object {
        fun fromId(id: String): ThemeMode = entries.find { it.id == id } ?: SYSTEM
    }
}
