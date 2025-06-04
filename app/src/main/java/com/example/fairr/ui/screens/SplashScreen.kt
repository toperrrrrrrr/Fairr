package com.example.fairr.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairr.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    // Animation for the logo
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "Logo scale animation"
    )

    // Handle splash timeout
    LaunchedEffect(Unit) {
        delay(2500) // Show splash for 2.5 seconds
        onTimeout()
    }

    // Full screen dark background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        // Animated logo
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .background(
                    color = NeutralWhite,
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "F",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBackground
            )
        }
        
        // App name below logo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = 100.dp)
        ) {
            Text(
                text = "Fairr",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = NeutralWhite
            )
            Text(
                text = "Split expenses fairly",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = TextOnDark.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    FairrTheme {
        SplashScreen(onTimeout = {})
    }
}

