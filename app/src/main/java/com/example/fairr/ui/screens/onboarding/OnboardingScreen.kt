package com.example.fairr.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.components.*
import com.example.fairr.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color = Primary
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Smart Expense",
            subtitle = "Management",
            description = "Effortlessly split bills, track expenses, and manage group finances with precision and ease",
            icon = Icons.Default.Receipt,
            accentColor = Primary
        ),
        OnboardingPage(
            title = "Real-time",
            subtitle = "Balance Tracking",
            description = "Keep track of who owes what with instant updates and crystal-clear balance calculations",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            accentColor = AccentBlue
        ),
        OnboardingPage(
            title = "Secure Group",
            subtitle = "Collaboration", 
            description = "Create groups for any occasion and manage shared expenses with friends, family, and colleagues",
            icon = Icons.Default.Group,
            accentColor = AccentGreen
        )
    )
    
    val pagerState = rememberPagerState(pageCount = { pages.size })
    var isAutoScrolling by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope() // Proper coroutine scope
    
    // Improved auto-advance with better timing
    LaunchedEffect(pagerState.currentPage, isAutoScrolling) {
        if (isAutoScrolling) {
            delay(4000) // Longer delay for better UX
            val nextPage = (pagerState.currentPage + 1) % pages.size
            try {
                pagerState.animateScrollToPage(nextPage)
            } catch (e: Exception) {
                // Handle any animation conflicts gracefully
            }
        }
    }
    
    // Stop auto-scroll when user interacts
    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            isAutoScrolling = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        // Geometric background elements
        GeometricBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Header with skip option
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FairShare",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                TextButton(
                    onClick = onGetStarted
                ) {
                    Text(
                        text = "Skip",
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                pageSpacing = 24.dp
            ) { page ->
                ModernOnboardingPageContent(
                    page = pages[page],
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Modern page indicators
            ModernPageIndicators(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            // Animated navigation buttons
            AnimatedNavigationButtons(
                currentPage = pagerState.currentPage,
                totalPages = pages.size,
                onPrevious = {
                    isAutoScrolling = false
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                onNext = {
                    isAutoScrolling = false
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                onGetStarted = onGetStarted
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GeometricBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Large circle - top right
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-100).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Primary.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // Medium circle - bottom left
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = 60.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentBlue.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // Small accent shape - center right
        Box(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 30.dp)
                .background(
                    color = AccentGreen.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(30.dp)
                )
        )
    }
}

@Composable
fun ModernOnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Modern icon container
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            page.accentColor.copy(alpha = 0.15f),
                            page.accentColor.copy(alpha = 0.05f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = page.accentColor.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = page.title,
                    modifier = Modifier.size(36.dp),
                    tint = page.accentColor
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Modern typography
        Text(
            text = page.title,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = page.subtitle,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = page.accentColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = page.description,
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun ModernPageIndicators(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(
                        width = if (index == currentPage) 24.dp else 8.dp,
                        height = 8.dp
                    )
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (index == currentPage) Primary 
                        else Primary.copy(alpha = 0.2f)
                    )
                    .clickable {
                        // Allow manual page selection
                    }
            )
        }
    }
}

@Composable
fun AnimatedNavigationButtons(
    currentPage: Int,
    totalPages: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onGetStarted: () -> Unit
) {
    val isLastPage = currentPage == totalPages - 1
    
    val showPrevious by remember(currentPage) {
        derivedStateOf { currentPage > 0 && !isLastPage }
    }
    
    // Animation values for center expansion
    val buttonWidth by animateDpAsState(
        targetValue = if (isLastPage) 260.dp else 56.dp,
        animationSpec = tween(
            durationMillis = 350,
            easing = EaseInOutCubic
        ),
        label = "buttonWidth"
    )
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (!isLastPage) {
            // Navigation buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous button with fade animation
                AnimatedVisibility(
                    visible = showPrevious,
                    enter = fadeIn(
                        animationSpec = tween(200)
                    ) + scaleIn(
                        animationSpec = tween(200),
                        initialScale = 0.8f
                    ),
                    exit = fadeOut(
                        animationSpec = tween(200)
                    ) + scaleOut(
                        animationSpec = tween(200),
                        targetScale = 0.8f
                    )
                ) {
                    OutlinedButton(
                        onClick = onPrevious,
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Previous",
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer {
                                    rotationZ = 180f
                                }
                        )
                    }
                }
                
                if (!showPrevious) {
                    Spacer(modifier = Modifier.size(56.dp))
                }
                
                // Centered Next button
                FloatingActionButton(
                    onClick = onNext,
                    containerColor = Primary,
                    contentColor = TextOnDark,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Invisible spacer for balance
                Spacer(modifier = Modifier.size(56.dp))
            }
        } else {
            // Get Started button that grows from center
            ModernButton(
                text = "Get Started",
                onClick = onGetStarted,
                modifier = Modifier
                    .width(buttonWidth)
                    .height(56.dp),
                icon = Icons.AutoMirrored.Filled.ArrowForward
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    FairrTheme {
        OnboardingScreen(
            onGetStarted = {}
        )
    }
} 