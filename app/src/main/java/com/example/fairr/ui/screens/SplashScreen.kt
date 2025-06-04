package com.example.fairr.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fairr.R
import com.example.fairr.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    // Animation states
    var isVisible by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "Fade animation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "Scale animation"
    )

    // Start animations and handle timeout
    LaunchedEffect(Unit) {
        isVisible = true
        delay(2000) // Show splash for 2 seconds
        onTimeout()
    }

    // Full screen background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Animated logo container
        Box(
            modifier = Modifier
                .size(160.dp)
                .graphicsLayer(
                    alpha = alphaAnim,
                    scaleX = scale,
                    scaleY = scale
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.fairr),
                contentDescription = "Fairr Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        
        // App name with fade animation
        Text(
            text = "Fairr",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .graphicsLayer(alpha = alphaAnim)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    FairrTheme {
        SplashScreen(onTimeout = {})
    }
}

