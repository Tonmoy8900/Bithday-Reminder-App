package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.authStateFlow
import com.example.ui.AuthScreen
import com.example.ui.BirthdayPulseApp
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = FirebaseAuth.getInstance()) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(
            onAuthSuccess = { }
        )
    } else {
        val displayName = user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: "Team Member"
        BirthdayPulseApp(
            currentUserId = user.uid,
            currentUserName = displayName,
            currentUserEmail = user.email ?: "",
            onSignedOut = { }
        )
    }
}
