package com.cresup.app.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Base Backgrounds ─────────────────────────────────────────────────────────
val Background = Color(0xFF050505)
val BackgroundSecondary = Color(0xFF111111)
val Surface = Color(0xFF111111)
val SurfaceVariant = Color(0xFF1C1C1C)

// ─── Cards ───────────────────────────────────────────────────────────────────
val CardBackground = Color(0xFF151515)
val CardBorder = Color(0xFF262626)

// ─── Brand — Lime Green (Tailwind lime-400 → lime-600) ───────────────────────
val NeonGreen = Color(0xFFA3E635)       // lime-400  — primary accent
val NeonGreenDim = Color(0xFF84CC16)    // lime-500
val GreenDark = Color(0xFF65A30D)       // lime-600
val GreenDeep = Color(0xFF4D7C0F)       // lime-700

// Aliases for backward compatibility
val NeonBlue = GreenDark
val NeonBlueDim = GreenDeep
val NeonCyan = NeonGreenDim

// Brand gradient
val PrimaryGradientStart = GreenDark
val PrimaryGradientEnd = NeonGreen

// ─── Semantic ─────────────────────────────────────────────────────────────────
val Success = Color(0xFF22C55E)         // green-500
val Danger = Color(0xFFEF4444)          // red-500
val Warning = Color(0xFFFACC15)         // yellow-400

val AccentYellow = Warning
val AccentOrange = Color(0xFFF97316)    // orange-500
val AccentRed = Danger
val AccentPink = Color(0xFFEC4899)      // pink-500

// ─── Text ─────────────────────────────────────────────────────────────────────
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFA1A1AA)   // zinc-400
val TextMuted = Color(0xFF71717A)       // zinc-500

// ─── Glass ───────────────────────────────────────────────────────────────────
val GlassBackground = Color(0x0AFFFFFF)
val GlassBorder = Color(0x18FFFFFF)

// ─── Category Colors ─────────────────────────────────────────────────────────
val ColorFood = Color(0xFFF97316)       // orange-500
val ColorDelivery = Color(0xFFEF4444)   // red-500
val ColorTransport = Color(0xFF3B82F6)  // blue-500
val ColorStudies = Color(0xFF8B5CF6)    // violet-500
val ColorGym = Color(0xFF22C55E)        // green-500
val ColorLeisure = Color(0xFFFACC15)    // yellow-400
val ColorStreaming = Color(0xFFEC4899)  // pink-500
val ColorInvestments = Color(0xFFA3E635) // lime-400
val ColorShopping = Color(0xFFF59E0B)  // amber-500

// ─── Level Colors ─────────────────────────────────────────────────────────────
val LevelRookie = Color(0xFF71717A)     // zinc-500
val LevelBuilder = Color(0xFF84CC16)   // lime-500
val LevelPro = Color(0xFFA3E635)       // lime-400
val LevelElite = Color(0xFFFACC15)     // yellow-400
