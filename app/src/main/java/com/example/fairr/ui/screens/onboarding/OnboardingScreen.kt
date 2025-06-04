package com.example.fairr.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.luminance
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
import com.google.accompanist.systemuicontroller.rememberSystemUiController
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
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colorScheme.background.luminance() > 0.5f

    DisposableEffect(systemUiController, useDarkIcons) {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
        onDispose {}
    }

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
            accentColor = Primary
        ),
        OnboardingPage(
            title = "Secure Group",
            subtitle = "Collaboration", 
            description = "Create groups for any occasion and manage shared expenses with friends, family, and colleagues",
            icon = Icons.Default.Group,
            accentColor = SuccessGreen
        )
    )
    
    val pagerState = rememberPagerState(pageCount = { pages.size })
    var isAutoScrolling by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(pagerState.currentPage, isAutoScrolling) {
        if (isAutoScrolling) {
            delay(4000)
            val nextPage = (pagerState.currentPage + 1) % pages.size
            try {
                pagerState.animateScrollToPage(nextPage)
            } catch (e: Exception) {
                // Handle any animation conflicts gracefully
            }
        }
    }
    
    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            isAutoScrolling = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Geometric background elements
        GeometricBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars))
            
            // Header with skip option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fairr",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                TextButton(
                    onClick = onGetStarted
                ) {
                    Text(
                        text = "Skip",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
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
            
            Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
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
                            Primary.copy(alpha = 0.08f),
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
                    color = SuccessGreen.copy(alpha = 0.06f),
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
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .aspectRatio(0.85f),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Modern illustration container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.surface
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = page.title,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Modern typography with better spacing
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = page.title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = page.subtitle,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = page.description,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ModernPageIndicators(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        repeat(pageCount) { index ->
            val width = if (index == currentPage) 32.dp else 12.dp
            
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(
                        width = width,
                        height = 12.dp
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (index == currentPage) 
                            MaterialTheme.colorScheme.primary
                        else 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
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
        targetValue = if (isLastPage) 280.dp else 56.dp,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "buttonWidth"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    OutlinedButton(
                        onClick = onPrevious,
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Previous",
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer { rotationZ = 180f }
                        )
                    }
                }
                
                if (!showPrevious) {
                    Spacer(modifier = Modifier.size(56.dp))
                }
                
                // Next button
                Button(
                    onClick = onNext,
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.size(56.dp))
            }
        } else {
            // Get Started button
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .width(buttonWidth)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Get Started",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
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

