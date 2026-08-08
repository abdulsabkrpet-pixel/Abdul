package com.example.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser

@Composable
fun LogoutComponent(
    user: FirebaseUser?,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("logout_component_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User Account",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp).testTag("user_avatar_icon")
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Signed In",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = user?.email ?: user?.displayName ?: "Authenticated User",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            OutlinedButton(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("logout_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Sign Out Icon",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Confirm Sign Out",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to sign out of your Firebase account?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onSignOut()
                    },
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    modifier = Modifier.testTag("cancel_logout_button")
                ) {
                    Text("Cancel")
                }
            },
            modifier = Modifier.testTag("logout_dialog")
        )
    }
}

/**
 * Handler function to perform Firebase sign-out via AuthManager and execute callback.
 */
fun performLogout(authManager: AuthManager, onLoggedOut: () -> Unit) {
    authManager.signOut()
    onLoggedOut()
}
