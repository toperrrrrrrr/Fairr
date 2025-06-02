package com.example.fairr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import com.example.fairr.ui.theme.*

@Composable
fun SocialChatterSvg(
    modifier: Modifier = Modifier,
    primaryColor: Color = Primary,
    secondaryColor: Color = LightGray,
    accentColor: Color = LightGray
) {
    Canvas(
        modifier = modifier.size(240.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // Scale factor to fit the original SVG (584x659) into our canvas
        val scaleX = canvasWidth / 584f
        val scaleY = canvasHeight / 659f
        val scaleValue = minOf(scaleX, scaleY)
        
        // Center the drawing
        val offsetX = (canvasWidth - 584f * scaleValue) / 2f
        val offsetY = (canvasHeight - 659f * scaleValue) / 2f
        
        translate(offsetX, offsetY) {
            scale(scaleValue, scaleValue, pivot = center) {
                drawAnalyticsIllustration(primaryColor, secondaryColor, accentColor)
            }
        }
    }
}

private fun DrawScope.drawAnalyticsIllustration(
    primaryColor: Color,
    secondaryColor: Color, 
    accentColor: Color
) {
    // Main phone/tablet frame
    drawRoundRect(
        color = secondaryColor,
        topLeft = androidx.compose.ui.geometry.Offset(156f, 0f),
        size = androidx.compose.ui.geometry.Size(272f, 571.419f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(43.871f, 43.871f)
    )
    
    // Phone screen area
    drawRoundRect(
        color = Color.White,
        topLeft = androidx.compose.ui.geometry.Offset(166.968f, 10.9677f),
        size = androidx.compose.ui.geometry.Size(250.064f, 549.484f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(32.9f, 32.9f)
    )
    
    // Top notch/speaker
    drawRoundRect(
        color = primaryColor.copy(alpha = 0.2f),
        topLeft = androidx.compose.ui.geometry.Offset(258f, 21.9355f),
        size = androidx.compose.ui.geometry.Size(69.419f, 15.355f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(7.677f, 7.677f)
    )
    
    // Chart area background
    drawRoundRect(
        color = Color.White,
        topLeft = androidx.compose.ui.geometry.Offset(188f, 167f),
        size = androidx.compose.ui.geometry.Size(209f, 235f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
    )
    
    // Analytics bars (chart visualization)
    val barData = listOf(
        Pair(369f, 150f), Pair(355f, 129f), Pair(313f, 111f), Pair(299f, 97f),
        Pair(285f, 85f), Pair(257f, 78f), Pair(243f, 63f), Pair(229f, 50f),
        Pair(215f, 37f), Pair(271f, 59f), Pair(327f, 92f), Pair(341f, 108f)
    )
    
    barData.forEachIndexed { index, (x, height) ->
        val barHeight = height - 230f + (index * 2) // Vary heights
        drawRect(
            color = primaryColor,
            topLeft = androidx.compose.ui.geometry.Offset(x, 380f - barHeight),
            size = androidx.compose.ui.geometry.Size(3f, barHeight)
        )
    }
    
    // Bottom platform/base
    drawRoundRect(
        color = accentColor,
        topLeft = androidx.compose.ui.geometry.Offset(121f, 571f),
        size = androidx.compose.ui.geometry.Size(360f, 88f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
    
    // Data visualization elements (scattered rectangles representing data points)
    val dataPoints = listOf(
        Triple(106f, 637f, 94f), Triple(6f, 637f, 94f), Triple(206f, 637f, 94f),
        Triple(56f, 615f, 94f), Triple(106f, 593f, 94f), Triple(156f, 615f, 94f)
    )
    
    dataPoints.forEach { (x, y, width) ->
        drawRoundRect(
            color = secondaryColor,
            topLeft = androidx.compose.ui.geometry.Offset(x, y),
            size = androidx.compose.ui.geometry.Size(width, 22f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
    }
    
    // Left circular element (user/profile indicator)
    drawCircle(
        color = primaryColor,
        radius = 61.5f,
        center = androidx.compose.ui.geometry.Offset(61.5f, 362.5f)
    )
    
    // Right circular element (settings/profile indicator)
    drawCircle(
        color = primaryColor,
        radius = 61.5f,
        center = androidx.compose.ui.geometry.Offset(509.5f, 208.5f)
    )
    
    // "S" letter in left circle (for Split/Share)
    drawTextIndicator(61.5f, 362.5f, primaryColor)
    
    // "S" letter in right circle (for Settings/Stats)
    drawTextIndicator(509.5f, 208.5f, primaryColor)
    
    // Laptop/computer screen (secondary device)
    drawRoundRect(
        color = primaryColor,
        topLeft = androidx.compose.ui.geometry.Offset(237f, 457f),
        size = androidx.compose.ui.geometry.Size(342f, 201.536f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.357f, 20.357f)
    )
    
    // Screen separator line
    drawLine(
        color = secondaryColor,
        start = androidx.compose.ui.geometry.Offset(237f, 483.464f),
        end = androidx.compose.ui.geometry.Offset(579f, 483.464f),
        strokeWidth = 2f
    )
    
    // Keyboard keys simulation (data representation)
    val keyPositions = listOf(
        Pair(279.75f, 562.857f), Pair(346.929f, 562.857f),
        Pair(414.107f, 562.857f), Pair(481.286f, 562.857f)
    )
    
    keyPositions.forEach { (x, y) ->
        drawRoundRect(
            color = accentColor,
            topLeft = androidx.compose.ui.geometry.Offset(x, y),
            size = androidx.compose.ui.geometry.Size(49.875f, 14.25f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(7.125f, 7.125f)
        )
    }
    
    // Connected circles (representing data flow/sharing)
    drawConnectedCircles(477.214f, 601.536f, primaryColor, accentColor)
}

private fun DrawScope.drawTextIndicator(centerX: Float, centerY: Float, color: Color) {
    // Simple "S" shape indicator (simplified for performance)
    val path = Path().apply {
        moveTo(centerX - 15f, centerY - 20f)
        lineTo(centerX + 15f, centerY - 20f)
        lineTo(centerX + 15f, centerY - 5f)
        lineTo(centerX - 15f, centerY - 5f)
        lineTo(centerX - 15f, centerY + 5f)
        lineTo(centerX + 15f, centerY + 5f)
        lineTo(centerX + 15f, centerY + 20f)
        lineTo(centerX - 15f, centerY + 20f)
        close()
    }
    
    drawPath(
        path = path,
        color = Color.White
    )
}

private fun DrawScope.drawConnectedCircles(centerX: Float, centerY: Float, primary: Color, accent: Color) {
    // Left circle
    drawCircle(
        color = accent,
        radius = 20.832f,
        center = androidx.compose.ui.geometry.Offset(centerX - 11.583f, centerY + 18.321f)
    )
    
    // Right circle
    drawCircle(
        color = accent,
        radius = 20.832f,
        center = androidx.compose.ui.geometry.Offset(centerX + 31.082f, centerY + 18.321f)
    )
    
    // Intersection area
    drawRect(
        color = accent.copy(alpha = 0.5f),
        topLeft = androidx.compose.ui.geometry.Offset(centerX, centerY),
        size = androidx.compose.ui.geometry.Size(40.714f, 50.892f)
    )
} 
