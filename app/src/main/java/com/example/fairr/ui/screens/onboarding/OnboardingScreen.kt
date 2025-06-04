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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.systemBarsPadding
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.R
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
            accentColor = MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            title = "Real-time",
            subtitle = "Balance Tracking",
            description = "Keep track of who owes what with instant updates and crystal-clear balance calculations",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            accentColor = MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            title = "Secure Group",
            subtitle = "Collaboration", 
            description = "Create groups for any occasion and manage shared expenses with friends, family, and colleagues",
            icon = Icons.Default.Group,
            accentColor = MaterialTheme.colorScheme.tertiary
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
                // Handle animation conflicts gracefully
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
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding() // Handle system bars
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Header with logo and skip option
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.fairr),
                    contentDescription = "Fairr Logo",
                    modifier = Modifier.size(32.dp),
                    contentScale = ContentScale.Fit
                )
                
                TextButton(
                    onClick = onGetStarted,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Skip",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Pager content
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Page indicators
            Row(
                modifier = Modifier
                    .height(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { index ->
                    val isSelected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(
                                width = if (isSelected) 24.dp else 8.dp,
                                height = 8.dp
                            )
                            .clip(CircleShape)
                            .background(
                                if (isSelected) 
                                    MaterialTheme.colorScheme.primary
                                else 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Navigation button
            Button(
                onClick = {
                    if (pagerState.currentPage == pages.size - 1) {
                        onGetStarted()
                    } else {
                        isAutoScrolling = false
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun ModernOnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = page.accentColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(32.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = page.accentColor
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Title and subtitle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = page.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = page.subtitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = page.description,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
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

