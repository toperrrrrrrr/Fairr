package com.example.fairr.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.components.*
import com.example.fairr.ui.theme.*

@Composable
fun WelcomeScreen(
    navController: NavController,
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary) // Dark background like in the image
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // SVG Illustration
            SocialChatterSvg(
                modifier = Modifier.size(200.dp),
                primaryColor = TextOnDark.copy(alpha = 0.8f),
                secondaryColor = TextOnDark.copy(alpha = 0.6f),
                accentColor = TextOnDark.copy(alpha = 0.4f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Title
            Text(
                text = "Social Chatter",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextOnDark,
                textAlign = TextAlign.Center
            )
            
            // Subtitle
            Text(
                text = "Team.",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextOnDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description
            Text(
                text = "Lorem Ipsum is simply dummy text of\nthe printing and typesetting industry.\nLorem Ipsum",
                fontSize = 16.sp,
                color = TextOnDark.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Login Button
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TextOnDark,
                    contentColor = Primary
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Login",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sign Up Button
            OutlinedButton(
                onClick = onNavigateToSignUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextOnDark
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, TextOnDark),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Sign up",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    FairrTheme {
        WelcomeScreen(
            navController = rememberNavController(),
            onNavigateToLogin = {},
            onNavigateToSignUp = {}
        )
    }
} 