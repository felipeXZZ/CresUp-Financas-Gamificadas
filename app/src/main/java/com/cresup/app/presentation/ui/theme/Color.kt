package com.cresup.app.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// Dark Premium Palette
val Background = Color(0xFF0A0A0A)
val BackgroundSecondary = Color(0xFF111111)
val Surface = Color(0xFF181818)
val SurfaceVariant = Color(0xFF202020)

// Logo gradient: dark green → lime green (cores exatas da logo CresUp)
val NeonGreen = Color(0xFFA8E040)       // verde limão (extremo claro do gradiente)
val NeonGreenDim = Color(0xFF7DC828)    // verde médio
val GreenDark = Color(0xFF33B81A)       // verde escuro (extremo escuro do gradiente)
val GreenDeep = Color(0xFF267A12)       // verde profundo (hover/pressed)

// Mantido como alias para compatibilidade com código existente
val NeonBlue = GreenDark
val NeonBlueDim = GreenDeep
val NeonCyan = NeonGreenDim

// Gradiente primário da marca (espelha a logo)
val PrimaryGradientStart = GreenDark    // #33B81A
val PrimaryGradientEnd = NeonGreen      // #A8E040

val AccentYellow = Color(0xFFFFD700)
val AccentOrange = Color(0xFFFF6B35)
val AccentRed = Color(0xFFFF4757)
val AccentPink = Color(0xFFFF6B9D)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B0B0)
val TextMuted = Color(0xFF6B6B6B)

val CardBackground = Color(0xFF1A1A1A)
val CardBorder = Color(0xFF2A2A2A)

val GlassBackground = Color(0x1AFFFFFF)
val GlassBorder = Color(0x26FFFFFF)

// Category Colors
val ColorFood = Color(0xFFFF6B35)
val ColorDelivery = Color(0xFFFF4757)
val ColorTransport = Color(0xFF7DC828)
val ColorStudies = Color(0xFFA8E040)
val ColorGym = Color(0xFF33B81A)
val ColorLeisure = Color(0xFFFFD700)
val ColorStreaming = Color(0xFFFF6B9D)
val ColorInvestments = Color(0xFF5DBF28)
val ColorShopping = Color(0xFFFF9F43)

// XP / Level Colors
val LevelRookie = Color(0xFF8B8B8B)
val LevelBuilder = Color(0xFF7DC828)
val LevelPro = Color(0xFFA8E040)
val LevelElite = Color(0xFFFFD700)
