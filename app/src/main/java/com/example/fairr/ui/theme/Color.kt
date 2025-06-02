package com.example.fairr.ui.theme

import androidx.compose.ui.graphics.Color

// Monochromatic Design System - Based on Modern UI Patterns
val PureBlack = Color(0xFF000000)
val PureWhite = Color(0xFFFFFFFF)
val SoftBlack = Color(0xFF1A1A1A)
val CharcoalGray = Color(0xFF2D2D2D)
val MediumGray = Color(0xFF6B6B6B)
val LightGray = Color(0xFFF5F5F5)
val UltraLightGray = Color(0xFFFAFAFA)

// Accent Colors (WCAG 2.1 AA Compliant)
val AccentGreen = Color(0xFF00A94C) // Adjusted for better contrast
val AccentBlue = Color(0xFF0066CC)  // Adjusted for better contrast
val AccentRed = Color(0xFFE53935)   // Adjusted for better contrast
val AccentOrange = Color(0xFFE65100) // Adjusted for better contrast

// Primary Theme Colors
val Primary = PureBlack
val PrimaryVariant = SoftBlack
val Secondary = CharcoalGray
val SecondaryVariant = MediumGray

// Background Colors
val BackgroundPrimary = PureWhite
val BackgroundSecondary = UltraLightGray
val Surface = PureWhite
val SurfaceVariant = LightGray

// Text Colors (WCAG 2.1 AA Compliant)
val TextPrimary = PureBlack
val TextSecondary = Color(0xFF666666) // Adjusted for better contrast
val TextTertiary = Color(0xFF757575)  // Adjusted for better contrast
val TextOnDark = PureWhite

// Status Colors (With Semantic Variations)
val SuccessGreen = AccentGreen
val SuccessGreenLight = Color(0xFFE8F5E9)
val SuccessGreenDark = Color(0xFF00843D)

val ErrorRed = AccentRed
val ErrorRedLight = Color(0xFFFFEBEE)
val ErrorRedDark = Color(0xFFC62828)

val WarningOrange = AccentOrange
val WarningOrangeLight = Color(0xFFFFF3E0)
val WarningOrangeDark = Color(0xFFE65100)

val InfoBlue = AccentBlue
val InfoBlueLight = Color(0xFFE3F2FD)
val InfoBlueDark = Color(0xFF0D47A1)

// Legacy colors for compatibility (updated to match new scheme)
val DarkGreen = SuccessGreen
val LightGreen = SuccessGreenLight
val DarkBlue = InfoBlue
val LightBlue = InfoBlueLight
val DarkBackground = SoftBlack
val LightBackground = UltraLightGray
val PlaceholderText = TextSecondary

// Card and Component Colors
val CardBackground = PureWhite
val CardBorder = Color(0xFFE0E0E0)
val DividerColor = LightGray
val IconTint = CharcoalGray
val ButtonBackground = PureBlack
val ButtonText = PureWhite

// Divider and border colors
val BorderColor = LightGray

// Modern Design System Additions
val NeutralWhite = PureWhite
val NeutralBlack = PureBlack
val NeutralLight = UltraLightGray
val NeutralGray = MediumGray
val NeutralDark = CharcoalGray

// Component specific colors
val InputBackground = UltraLightGray
val InputBorder = LightGray
val InputFocused = PureBlack
val ButtonSecondary = LightGray
val ButtonSecondaryText = PureBlack

// Navigation Colors
val NavBackground = BackgroundPrimary
val NavSelected = Primary
val NavUnselected = TextSecondary

// Semantic Color Extensions for Better UX
object FairrColors {
    // Button Colors
    val ButtonPrimary = PureBlack
    val ButtonPrimaryText = PureWhite
    val ButtonSecondary = LightGray
    val ButtonSecondaryText = PureBlack
    val ButtonOutline = CardBorder
    val ButtonDisabled = Color(0xFFE0E0E0)
    val ButtonDisabledText = Color(0xFF9E9E9E)
    
    // Text Hierarchy (WCAG Compliant)
    val TextError = ErrorRed
    val TextSuccess = SuccessGreen
    val TextWarning = WarningOrange
    val TextInfo = InfoBlue
    val TextPlaceholder = Color(0xFF9E9E9E)
    val TextDisabled = Color(0xFFBDBDBD)
    
    // Surface Colors
    val SurfaceElevated = PureWhite
    val SurfaceDepressed = Color(0xFFF8F8F8)
    val SurfaceHighlight = Color(0xFFF0F0F0)
    
    // Interactive States
    val StatePressed = Color(0x1A000000)  // 10% black
    val StateHovered = Color(0x0A000000)  // 4% black
    val StateFocused = Color(0x1F000000)  // 12% black
    val StateSelected = Color(0x1F000000)  // 12% black
    val StateDisabled = Color(0x61000000)  // 38% black
    
    // Category Colors (for expense categories)
    val CategoryColors = listOf(
        AccentBlue,
        AccentGreen, 
        AccentOrange,
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF673AB7), // Deep Purple
        Color(0xFF3F51B5), // Indigo
        Color(0xFF009688), // Teal
        Color(0xFF795548), // Brown
        Color(0xFF607D8B)  // Blue Grey
    )
    
    // Gradient Colors
    val GradientStart = PureBlack
    val GradientEnd = CharcoalGray
    
    // Shadow Colors
    val ShadowLight = Color(0x1A000000) // 10% opacity
    val ShadowMedium = Color(0x33000000) // 20% opacity
    val ShadowHeavy = Color(0x4D000000) // 30% opacity
}

// Component-specific semantic colors
object ComponentColors {
    // Status colors with semantic meaning
    val Success = SuccessGreen
    val SuccessLight = SuccessGreenLight
    val SuccessDark = SuccessGreenDark
    
    val Error = ErrorRed
    val ErrorLight = ErrorRedLight
    val ErrorDark = ErrorRedDark
    
    val Warning = WarningOrange
    val WarningLight = WarningOrangeLight
    val WarningDark = WarningOrangeDark
    
    val Info = InfoBlue
    val InfoLight = InfoBlueLight
    val InfoDark = InfoBlueDark
    
    // Avatar and icon backgrounds
    val AvatarBackground = Primary.copy(alpha = 0.1f)
    val IconBackgroundSuccess = Success.copy(alpha = 0.1f)
    val IconBackgroundError = Error.copy(alpha = 0.1f)
    val IconBackgroundInfo = Info.copy(alpha = 0.1f)
    val IconBackgroundWarning = Warning.copy(alpha = 0.1f)
    
    // Progress indicators
    val ProgressDefault = Success
    val ProgressWarning = Warning
    val ProgressError = Error
    val ProgressTrack = TextSecondary.copy(alpha = 0.1f)
    
    // Card colors
    val CardBorderDefault = LightGray
    val CardBorderFocused = Primary
    val CardBackgroundElevated = Surface
    val CardBackgroundPressed = Surface.copy(alpha = 0.9f)
    
    // Input fields
    val InputBorderDefault = LightGray
    val InputBorderFocused = Primary
    val InputBorderError = Error
    val InputBackground = BackgroundSecondary
    
    // Text field colors
    val TextFieldBorderFocused = Primary
    val TextFieldBorderUnfocused = TextSecondary.copy(alpha = 0.3f)
    val TextFieldLabelFocused = Primary
    val TextFieldError = Error
    
    // Interactive states
    val StatePressed = Color(0x1A000000)  // 10% black
    val StateHovered = Color(0x0A000000)  // 4% black
    val StateFocused = Color(0x1F000000)  // 12% black
    val StateSelected = Color(0x1F000000)  // 12% black
    val StateDisabled = Color(0x61000000)  // 38% black
}
