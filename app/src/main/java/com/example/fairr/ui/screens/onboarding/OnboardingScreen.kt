package com.example.fairr.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.theme.*
import kotlinx.coroutines.delay

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    onGetStarted: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Split Expenses",
            description = "Easily split bills, rent, groceries, and more with friends and family",
            icon = Icons.Default.Receipt
        ),
        OnboardingPage(
            title = "Track Balances",
            description = "Keep track of who owes what and settle up with just a few taps",
            icon = Icons.Default.TrendingUp
        ),
        OnboardingPage(
            title = "Group Management",
            description = "Create groups for different occasions and manage expenses together",
            icon = Icons.Default.Group
        )
    )
    
    val pagerState = rememberPagerState(pageCount = { pages.size })
    
    // Auto-advance pages
    LaunchedEffect(pagerState.currentPage) {
        delay(3000)
        if (pagerState.currentPage < pages.size - 1) {
            pagerState.animateScrollToPage(pagerState.currentPage + 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageContent(
                page = pages[page],
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Page indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 24.dp)
        ) {
            repeat(pages.size) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == pagerState.currentPage) 24.dp else 8.dp, 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage) DarkGreen 
                            else PlaceholderText.copy(alpha = 0.3f)
                        )
                )
            }
        }
        
        // Get Started Button
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkGreen,
                contentColor = PureWhite
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Get Started",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon illustration
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(
                    DarkGreen.copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = page.title,
                modifier = Modifier.size(80.dp),
                tint = DarkGreen
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = page.description,
            fontSize = 16.sp,
            color = TextSecondary,
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
            navController = rememberNavController(),
            onGetStarted = {}
        )
    }
} 