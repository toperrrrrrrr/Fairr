package com.example.fairr.ui.screens.support

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.theme.*
import com.example.fairr.ui.components.FairrTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            com.example.fairr.ui.components.FairrTopAppBar(
                title = "Privacy Policy",
                navController = navController
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your Privacy Matters",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Last updated: January 2025",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Introduction
            item {
                PrivacySection(
                    title = "Introduction",
                    content = "Fairr is committed to protecting your privacy and ensuring the security of your personal information. This Privacy Policy explains how we collect, use, and safeguard your data when you use our expense sharing application."
                )
            }

            // Information We Collect
            item {
                PrivacySection(
                    title = "Information We Collect",
                    content = """
• **Account Information**: Name, email address, and profile picture
• **Financial Data**: Expense amounts, payment records, and group sharing details
• **Usage Data**: App interactions, feature usage, and performance analytics
• **Device Information**: Device type, operating system, and app version
• **Communication Data**: Messages and interactions within groups

We only collect information necessary to provide our expense sharing services.
                    """.trimIndent()
                )
            }

            // How We Use Your Information
            item {
                PrivacySection(
                    title = "How We Use Your Information",
                    content = """
• **Service Delivery**: Process expenses, calculate balances, and manage groups
• **Account Management**: Maintain your profile and authenticate access
• **Communication**: Send notifications about expenses and group activities
• **Improvement**: Analyze usage patterns to enhance app functionality
• **Security**: Detect and prevent fraudulent activities
• **Legal Compliance**: Meet regulatory requirements and resolve disputes

We never sell your personal information to third parties.
                    """.trimIndent()
                )
            }

            // Data Sharing
            item {
                PrivacySection(
                    title = "Data Sharing and Disclosure",
                    content = """
**Within Groups**: Expense data is shared with group members as necessary for expense tracking.

**Service Providers**: We may share data with trusted third-party services for:
• Cloud storage and backup
• Payment processing
• Analytics and app performance
• Customer support

**Legal Requirements**: We may disclose information when required by law or to protect our rights and users' safety.

**Business Transfers**: In the event of a merger or acquisition, user data may be transferred as part of business assets.
                    """.trimIndent()
                )
            }

            // Data Security
            item {
                PrivacySection(
                    title = "Data Security",
                    content = """
We implement industry-standard security measures to protect your information:

• **Encryption**: All data is encrypted in transit and at rest
• **Access Controls**: Strict authentication and authorization protocols
• **Regular Audits**: Continuous monitoring and security assessments
• **Secure Infrastructure**: Cloud services with enterprise-grade security
• **Data Minimization**: We collect only what's necessary for our services

While we strive to protect your data, no system is 100% secure. Please use strong passwords and keep your account information confidential.
                    """.trimIndent()
                )
            }

            // Your Rights
            item {
                PrivacySection(
                    title = "Your Privacy Rights",
                    content = """
You have the following rights regarding your personal data:

• **Access**: Request a copy of your personal information
• **Correction**: Update or correct inaccurate data
• **Deletion**: Request deletion of your account and data
• **Portability**: Export your data in a machine-readable format
• **Restriction**: Limit how we process your information
• **Objection**: Object to certain types of data processing

To exercise these rights, contact us through the app's support feature or email privacy@fairr.app.
                    """.trimIndent()
                )
            }

            // Data Retention
            item {
                PrivacySection(
                    title = "Data Retention",
                    content = """
We retain your information for as long as necessary to provide our services:

• **Active Accounts**: Data is retained while your account is active
• **Deleted Accounts**: Most data is deleted within 30 days of account deletion
• **Legal Requirements**: Some data may be retained longer for legal compliance
• **Group Data**: Shared expense data remains accessible to group members even if you leave

You can export your data before deleting your account through the Export Data feature.
                    """.trimIndent()
                )
            }

            // Children's Privacy
            item {
                PrivacySection(
                    title = "Children's Privacy",
                    content = "Fairr is not intended for users under 13 years of age. We do not knowingly collect personal information from children under 13. If you believe we have collected information from a child under 13, please contact us immediately so we can delete such information."
                )
            }

            // International Users
            item {
                PrivacySection(
                    title = "International Users",
                    content = "Fairr is operated from the United States. If you are accessing our services from outside the US, please be aware that your information may be transferred to, stored, and processed in the US where our servers are located and our central database is operated."
                )
            }

            // Changes to Privacy Policy
            item {
                PrivacySection(
                    title = "Changes to This Privacy Policy",
                    content = "We may update this Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy on this page and updating the 'Last updated' date. Continued use of our services after any modifications indicates your acceptance of the updated Privacy Policy."
                )
            }

            // Contact Information
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Contact Us",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "If you have any questions about this Privacy Policy or our privacy practices, please contact us:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Email: privacy@fairr.app\nSupport: Use the 'Help Center' in the app\nAddress: Fairr Privacy Team, [Your Address]",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PrivacySection(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PrivacyPolicyScreenPreview() {
    FairrTheme {
        PrivacyPolicyScreen(navController = rememberNavController())
    }
} 