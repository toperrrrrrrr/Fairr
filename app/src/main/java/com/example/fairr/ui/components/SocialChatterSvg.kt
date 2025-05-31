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
    primaryColor: Color = TextOnDark.copy(alpha = 0.8f),
    secondaryColor: Color = TextOnDark.copy(alpha = 0.6f),
    accentColor: Color = TextOnDark.copy(alpha = 0.4f)
) {
    Canvas(
        modifier = modifier.size(200.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // Scale factor to fit the original SVG (609x495) into our canvas
        val scaleX = canvasWidth / 609f
        val scaleY = canvasHeight / 495f
        val scaleValue = minOf(scaleX, scaleY)
        
        // Center the drawing
        val offsetX = (canvasWidth - 609f * scaleValue) / 2f
        val offsetY = (canvasHeight - 495f * scaleValue) / 2f
        
        translate(offsetX, offsetY) {
            scale(scaleValue, scaleValue, pivot = center) {
                drawSocialChatterIllustration(primaryColor, secondaryColor, accentColor)
            }
        }
    }
}

private fun DrawScope.drawSocialChatterIllustration(
    primaryColor: Color,
    secondaryColor: Color, 
    accentColor: Color
) {
    // Main laptop/tablet screen
    drawRoundRect(
        color = primaryColor,
        topLeft = androidx.compose.ui.geometry.Offset(147.659f, 6f),
        size = androidx.compose.ui.geometry.Size(325.128f, 310.809f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
    
    // Screen content bars
    drawRoundRect(
        color = secondaryColor,
        topLeft = androidx.compose.ui.geometry.Offset(220.103f, 150.086f),
        size = androidx.compose.ui.geometry.Size(180.588f, 28.422f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
    )
    
    drawRoundRect(
        color = secondaryColor,
        topLeft = androidx.compose.ui.geometry.Offset(220.103f, 194.799f),
        size = androidx.compose.ui.geometry.Size(180.588f, 28.423f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
    )
    
    // User avatar circle
    drawCircle(
        color = primaryColor,
        radius = 45.1f,
        center = androidx.compose.ui.geometry.Offset(310.397f, 78.162f)
    )
    
    // User icon elements
    drawCircle(
        color = accentColor,
        radius = 6.82f,
        center = androidx.compose.ui.geometry.Offset(310.137f, 69.5617f)
    )
    
    // Left floating elements (representing social interaction)
    drawPath(
        path = Path().apply {
            // Left floating shape
            moveTo(24.2629f, 275.388f)
            quadraticBezierTo(24.2629f, 255f, 43.1536f, 256.498f)
            quadraticBezierTo(62.0443f, 255f, 62.0443f, 275.388f)
            close()
        },
        color = accentColor
    )
    
    // Right floating elements
    drawPath(
        path = Path().apply {
            // Right floating shape
            moveTo(584.052f, 275.388f)
            quadraticBezierTo(584.052f, 255f, 565.161f, 256.498f)
            quadraticBezierTo(546.27f, 255f, 546.27f, 275.388f)
            close()
        },
        color = accentColor
    )
    
    // People silhouettes (simplified)
    for (i in 0..4) {
        val x = 230f + i * 20f
        val y = 420f + (i % 2) * 10f
        
        // Head
        drawCircle(
            color = secondaryColor,
            radius = 8f,
            center = androidx.compose.ui.geometry.Offset(x, y)
        )
        
        // Body (simplified rectangle)
        drawRoundRect(
            color = secondaryColor,
            topLeft = androidx.compose.ui.geometry.Offset(x - 6f, y + 8f),
            size = androidx.compose.ui.geometry.Size(12f, 20f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
    }
    
    // Chat bubbles or connection lines (simplified)
    for (i in 0..2) {
        val startX = 100f + i * 80f
        val startY = 300f
        val endX = startX + 40f
        val endY = startY + 30f
        
        drawLine(
            color = accentColor.copy(alpha = 0.5f),
            start = androidx.compose.ui.geometry.Offset(startX, startY),
            end = androidx.compose.ui.geometry.Offset(endX, endY),
            strokeWidth = 2f
        )
    }
    
    // Additional decorative elements
    repeat(6) { i ->
        val angle = i * 60f * (kotlin.math.PI / 180f).toFloat()
        val radius = 150f
        val centerX = 300f
        val centerY = 200f
        
        val x = centerX + kotlin.math.cos(angle) * radius
        val y = centerY + kotlin.math.sin(angle) * radius
        
        drawCircle(
            color = accentColor.copy(alpha = 0.3f),
            radius = 4f,
            center = androidx.compose.ui.geometry.Offset(x, y)
        )
    }
} 