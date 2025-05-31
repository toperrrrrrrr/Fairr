package com.example.fairr.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.theme.*

@Composable
fun AccountVerificationScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    onVerificationComplete: () -> Unit
) {
    var verificationCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary) // Dark background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Section with Back Button and Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Back button
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextOnDark,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Title text
                Text(
                    text = "Account",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnDark
                )
                
                Text(
                    text = "Verification",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnDark
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Content Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Icon/Illustration placeholder
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = TextOnDark.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Verification icon representation
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        repeat(3) { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                repeat(3) { 
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                color = TextOnDark.copy(alpha = 0.4f),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                    )
                                }
                            }
                            if (row < 2) Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                Text(
                    text = "Verify your\naccount!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnDark,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )
                
                Text(
                    text = "Enter the 6-digit verification\ncode sent to your email",
                    fontSize = 16.sp,
                    color = TextOnDark.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Verification Code Input
                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = { 
                        if (it.length <= 6) {
                            verificationCode = it
                        }
                    },
                    placeholder = {
                        Text(
                            text = "Enter 6-digit code",
                            color = TextOnDark.copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextOnDark,
                        unfocusedTextColor = TextOnDark,
                        cursorColor = TextOnDark,
                        focusedBorderColor = TextOnDark.copy(alpha = 0.5f),
                        unfocusedBorderColor = TextOnDark.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Resend code
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Didn't receive code? ",
                        color = TextOnDark.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Resend",
                        color = TextOnDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            // Handle resend code
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Verify Button
                Button(
                    onClick = {
                        if (verificationCode.length == 6) {
                            isLoading = true
                            // Simulate verification
                            onVerificationComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TextOnDark,
                        contentColor = Primary
                    ),
                    shape = RoundedCornerShape(28.dp),
                    enabled = !isLoading && verificationCode.length == 6
                ) {
                    Text(
                        text = if (isLoading) "Verifying..." else "Verify Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountVerificationScreenPreview() {
    FairrTheme {
        AccountVerificationScreen(
            navController = rememberNavController(),
            onNavigateBack = {},
            onVerificationComplete = {}
        )
    }
} 