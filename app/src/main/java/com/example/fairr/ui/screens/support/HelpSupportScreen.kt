package com.example.fairr.ui.screens.support

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import android.content.Intent
import android.content.Context
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.example.fairr.navigation.Screen
import com.example.fairr.ui.components.FairrEmptyState
import com.example.fairr.ui.components.FairrFilterChip
import com.example.fairr.ui.theme.*
import com.example.fairr.ui.components.FairrTopAppBar

data class HelpCategory(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val description: String,
    val articles: List<HelpArticle>
)

data class HelpArticle(
    val id: String,
    val title: String,
    val content: String,
    val isPopular: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    navController: NavController
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var expandedArticle by remember { mutableStateOf<String?>(null) }
    
    val categories = remember { getHelpCategories() }
    val categoryFilters = listOf("All") + categories.map { it.title }
    
    val filteredArticles = remember(selectedCategory, searchQuery, categories) {
        val allArticles = if (selectedCategory == "All") {
            categories.flatMap { it.articles }
        } else {
            categories.find { it.title == selectedCategory }?.articles ?: emptyList()
        }
        
        if (searchQuery.isBlank()) {
            allArticles
        } else {
            allArticles.filter { 
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.content.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            com.example.fairr.ui.components.FairrTopAppBar(
                title = "Help & Support",
                navController = navController
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Help,
                            contentDescription = "Help",
                            tint = DarkGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "How can we help you?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Find answers to common questions and get support",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            // Quick Actions
            item {
                QuickActionsSection()
            }
            
            // Search Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search help articles") },
                        leadingIcon = { 
                            Icon(Icons.Default.Search, contentDescription = "Search") 
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            focusedLabelColor = DarkGreen
                        )
                    )
                }
            }
            
            // Category Filters
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categoryFilters) { category ->
                        FairrFilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = category,
                            leadingIcon = when (category) {
                                "All" -> Icons.AutoMirrored.Filled.List
                                "Getting Started" -> Icons.AutoMirrored.Filled.Help
                                "Expenses" -> Icons.AutoMirrored.Filled.ReceiptLong
                                "Groups" -> Icons.Default.Groups
                                "Payments" -> Icons.Default.Payments
                                "Account" -> Icons.Default.AccountCircle
                                else -> null
                            }
                        )
                    }
                }
            }
            
            // Help Articles
            if (filteredArticles.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = "No articles found",
                                tint = TextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = if (searchQuery.isNotBlank()) "No Articles Found" else "Explore Help Topics",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = if (searchQuery.isNotBlank()) 
                                    "Try a different search term or browse categories below" 
                                else 
                                    "Browse our help categories or search for specific topics",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            
                            if (searchQuery.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OutlinedButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Primary
                                    ),
                                    border = BorderStroke(1.dp, Primary)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Clear Search")
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredArticles) { article ->
                    HelpArticleCard(
                        article = article,
                        isExpanded = expandedArticle == article.id,
                        onToggleExpanded = { 
                            expandedArticle = if (expandedArticle == article.id) null else article.id
                        }
                    )
                }
            }
            
            // Contact Support Section
            item {
                ContactSupportSection(context, navController)
            }
            
            // Additional spacing at bottom
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun QuickActionsSection() {
    Column {
        Text(
            text = "Quick Actions",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.VideoLibrary,
                title = "Video Tutorials",
                description = "Watch step-by-step guides",
                modifier = Modifier.weight(1f),
                onClick = { /* Navigate to tutorials */ }
            )
            
            QuickActionCard(
                icon = Icons.AutoMirrored.Filled.Chat,
                title = "Live Chat",
                description = "Chat with our support team",
                modifier = Modifier.weight(1f),
                onClick = { /* Open live chat */ }
            )
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = DarkGreen,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = description,
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun HelpArticleCard(
    article: HelpArticle,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpanded() }
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = article.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (article.isPopular) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = DarkGreen.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Popular",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = DarkGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TextSecondary
                )
            }
            
            if (isExpanded) {
                HorizontalDivider(
                    color = PlaceholderText.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                
                Text(
                    text = article.content,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun ContactSupportSection(
    context: android.content.Context,
    navController: NavController
) {
    Column {
        Text(
            text = "Still Need Help?",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NeutralWhite)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Contact Our Support Team",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                Text(
                    text = "We're here to help you with any questions or issues you might have.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { 
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@fairr.app")
                                putExtra(Intent.EXTRA_SUBJECT, "Fairr App Support Request")
                                putExtra(Intent.EXTRA_TEXT, "Hi Fairr Support Team,\n\nI need help with:\n\n")
                            }
                            context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = DarkGreen
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkGreen)
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = "Email",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Email Us")
                    }
                    
                    Button(
                        onClick = { 
                            // For now, redirect to Contact Support screen
                            navController.navigate(Screen.ContactSupport.route)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkGreen
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Chat",
                            modifier = Modifier.size(16.dp),
                            tint = NeutralWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Live Chat", color = NeutralWhite)
                    }
                }
            }
        }
    }
}

private fun getHelpCategories(): List<HelpCategory> {
    return listOf(
        HelpCategory(
            id = "getting_started",
            title = "Getting Started",
            icon = Icons.AutoMirrored.Filled.Help,
            description = "Learn the basics of Fairr",
            articles = listOf(
                HelpArticle(
                    id = "create_account",
                    title = "How to create an account",
                    content = "To create a Fairr account:\n\n1. Download the Fairr app from the App Store or Google Play\n2. Open the app and tap 'Sign Up'\n3. Enter your email address and create a secure password\n4. Verify your email address\n5. Complete your profile information\n\nOnce your account is created, you can start creating groups and adding expenses right away!",
                    isPopular = true
                ),
                HelpArticle(
                    id = "first_group",
                    title = "Creating your first group",
                    content = "Creating a group is easy:\n\n1. Tap the '+' button on the Groups screen\n2. Enter a group name (like 'Weekend Trip' or 'Roommates')\n3. Add a description (optional)\n4. Invite members by email or sharing an invite code\n5. Start adding expenses!\n\nYour group members will receive notifications to join and can start adding expenses immediately.",
                    isPopular = true
                )
            )
        ),
        HelpCategory(
            id = "expenses",
            title = "Expenses",
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            description = "Managing expenses and receipts",
            articles = listOf(
                HelpArticle(
                    id = "add_expense",
                    title = "How to add an expense",
                    content = "Adding an expense is simple:\n\n1. Open your group\n2. Tap the '+' button\n3. Enter the expense amount\n4. Add a description\n5. Choose who paid\n6. Select how to split the expense\n7. Add receipt photos (optional)\n8. Tap 'Save'\n\nThe expense will be automatically added to the group and all members will be notified."
                ),
                HelpArticle(
                    id = "split_methods",
                    title = "Different ways to split expenses",
                    content = "Fairr offers several splitting methods:\n\n• Equal Split: Divides the expense equally among selected members\n• Percentage Split: Each member pays a specific percentage\n• Exact Amounts: Specify exact amounts for each member\n• Pay for Yourself: Each member selects what they consumed\n\nChoose the method that works best for your expense type and group preferences."
                ),
                HelpArticle(
                    id = "receipt_scanning",
                    title = "Using receipt scanning",
                    content = "Our smart receipt scanning feature helps you add expenses quickly:\n\n1. When adding an expense, tap the camera icon\n2. Take a photo of your receipt or select from gallery\n3. Our AI will automatically extract:\n   - Total amount\n   - Merchant name\n   - Date\n   - Individual items (when possible)\n4. Review and edit the extracted information\n5. Save the expense\n\nThis feature works best with clear, well-lit photos of receipts.",
                    isPopular = true
                )
            )
        ),
        HelpCategory(
            id = "groups",
            title = "Groups",
            icon = Icons.Default.Groups,
            description = "Group management and settings",
            articles = listOf(
                HelpArticle(
                    id = "invite_members",
                    title = "Inviting members to a group",
                    content = "There are several ways to invite members:\n\n1. Email Invitation:\n   - Go to group settings\n   - Tap 'Invite Members'\n   - Enter email addresses\n   - Send invitations\n\n2. Share Invite Code:\n   - Generate an invite code in group settings\n   - Share the code via text, email, or social media\n   - Members can join by entering the code\n\n3. QR Code:\n   - Show the QR code from group settings\n   - Members can scan it to join instantly"
                ),
                HelpArticle(
                    id = "group_settings",
                    title = "Managing group settings",
                    content = "Group administrators can manage various settings:\n\n• Group Name & Description: Edit basic group information\n• Member Permissions: Control who can add expenses\n• Currency: Set the group's default currency\n• Notifications: Configure group notification settings\n• Privacy: Set group visibility and join permissions\n• Archive/Delete: Archive completed groups or delete permanently\n\nAccess these settings through the group menu or settings icon."
                )
            )
        ),
        HelpCategory(
            id = "payments",
            title = "Payments",
            icon = Icons.Default.Payments,
            description = "Settling up and payments",
            articles = listOf(
                HelpArticle(
                    id = "settle_up",
                    title = "How to settle up with group members",
                    content = "Fairr calculates who owes what and provides optimal payment suggestions:\n\n1. View your group's 'Balances' tab\n2. See who owes money and who should receive payments\n3. Tap 'Settle Up' to see payment recommendations\n4. Choose a payment method:\n   - Record cash payment\n   - Use integrated payment apps\n   - Bank transfer\n5. Mark payments as completed\n\nOur algorithm minimizes the number of transactions needed to settle all debts.",
                    isPopular = true
                ),
                HelpArticle(
                    id = "payment_methods",
                    title = "Available payment methods",
                    content = "Fairr integrates with popular payment services:\n\n• Venmo: Send money directly through the app\n• PayPal: Transfer funds securely\n• Cash App: Quick peer-to-peer payments\n• Zelle: Bank-to-bank transfers\n• Cash: Record cash payments\n• Bank Transfer: Traditional bank transfers\n\nYou can also record manual payments and add payment confirmation details."
                )
            )
        ),
        HelpCategory(
            id = "account",
            title = "Account",
            icon = Icons.Default.AccountCircle,
            description = "Account settings and security",
            articles = listOf(
                HelpArticle(
                    id = "change_password",
                    title = "Changing your password",
                    content = "To change your password:\n\n1. Go to Settings > Account\n2. Tap 'Change Password'\n3. Enter your current password\n4. Enter your new password\n5. Confirm your new password\n6. Tap 'Save Changes'\n\nMake sure your new password is strong and unique. We recommend using a mix of letters, numbers, and symbols."
                ),
                HelpArticle(
                    id = "delete_account",
                    title = "Deleting your account",
                    content = "If you need to delete your account:\n\n1. Settle all outstanding balances in your groups\n2. Leave all groups you're a member of\n3. Go to Settings > Account > Delete Account\n4. Confirm the deletion\n\nPlease note: This action is permanent and cannot be undone. All your data will be deleted and cannot be recovered."
                )
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
fun HelpSupportScreenPreview() {
    FairrTheme {
        HelpSupportScreen(navController = rememberNavController())
    }
} 
