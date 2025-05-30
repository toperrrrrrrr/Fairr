package com.example.fairr.ui.screens.support

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    navController: NavController
) {
    // FAQ Data
    val faqItems = remember {
        listOf(
            FAQItem(
                question = "How do I create a new group?",
                answer = "Tap the '+' button on the home screen and select 'Create Group'. Enter the group name, description, and currency, then invite members by sharing the group code."
            ),
            FAQItem(
                question = "How do I join an existing group?",
                answer = "Get the invite code from a group member, then tap 'Join Group' from the home screen and enter the code."
            ),
            FAQItem(
                question = "How are expenses split?",
                answer = "You can choose from equal split, percentage-based, or custom amounts. The app automatically calculates who owes what to whom."
            ),
            FAQItem(
                question = "How do I settle up with someone?",
                answer = "Go to your group, tap on the member you want to settle with, and select 'Settle Up'. Choose your payment method and mark as paid."
            ),
            FAQItem(
                question = "Can I edit or delete expenses?",
                answer = "Yes, tap on any expense to view details, then use the menu button to edit or delete. Only the person who added the expense can modify it."
            ),
            FAQItem(
                question = "Is my data secure?",
                answer = "Yes, all your data is encrypted and secure. We never share your personal information with third parties."
            )
        )
    }

    var selectedFAQ by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Help & Support",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureWhite
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Header Card
            item {
                HelpHeaderCard(
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Quick Actions
            item {
                Text(
                    text = "Quick Actions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                QuickActionsCard(
                    onContactSupport = { /* TODO */ },
                    onViewGuides = { /* TODO */ },
                    onReportBug = { /* TODO */ },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // FAQ Section
            item {
                Text(
                    text = "Frequently Asked Questions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            items(faqItems) { faq ->
                FAQCard(
                    faq = faq,
                    isExpanded = selectedFAQ == faq.question,
                    onToggle = { 
                        selectedFAQ = if (selectedFAQ == faq.question) null else faq.question
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Contact Section
            item {
                Text(
                    text = "Still Need Help?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                ContactCard(
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun HelpHeaderCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkGreen.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(DarkGreen.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Help,
                    contentDescription = "Help",
                    tint = DarkGreen,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "How can we help you?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Find answers to common questions or get in touch with our support team",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun QuickActionsCard(
    onContactSupport: () -> Unit,
    onViewGuides: () -> Unit,
    onReportBug: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Column {
            QuickActionItem(
                icon = Icons.Default.ContactSupport,
                title = "Contact Support",
                subtitle = "Get help from our team",
                onClick = onContactSupport
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = PlaceholderText.copy(alpha = 0.2f)
            )
            
            QuickActionItem(
                icon = Icons.Default.MenuBook,
                title = "User Guides",
                subtitle = "Step-by-step tutorials",
                onClick = onViewGuides
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = PlaceholderText.copy(alpha = 0.2f)
            )
            
            QuickActionItem(
                icon = Icons.Default.BugReport,
                title = "Report a Bug",
                subtitle = "Help us improve the app",
                onClick = onReportBug
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(DarkBlue.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = DarkBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = PlaceholderText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQCard(
    faq: FAQItem,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onToggle,
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = PlaceholderText,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = faq.answer,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun ContactCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Get in Touch",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            ContactItem(
                icon = Icons.Default.Email,
                title = "Email Support",
                subtitle = "support@fairshare.com",
                onClick = { /* TODO: Open email */ }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ContactItem(
                icon = Icons.Default.Chat,
                title = "Live Chat",
                subtitle = "Available 9 AM - 6 PM EST",
                onClick = { /* TODO: Open chat */ }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "We typically respond within 24 hours",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(DarkGreen.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = DarkGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

// Data classes
data class FAQItem(
    val question: String,
    val answer: String
)

@Preview(showBackground = true)
@Composable
fun HelpSupportScreenPreview() {
    FairrTheme {
        HelpSupportScreen(
            navController = rememberNavController()
        )
    }
} 