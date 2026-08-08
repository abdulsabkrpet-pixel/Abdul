package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.auth.AuthManager
import com.example.auth.LogoutComponent
import com.example.auth.performLogout
import com.example.ui.UserContentSection
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        MainScreen()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
  val context = LocalContext.current
  val authManager = remember { AuthManager(context) }
  val coroutineScope = rememberCoroutineScope()

  var currentUser by remember { mutableStateOf(authManager.currentUser) }
  var isLoading by remember { mutableStateOf(false) }
  var statusMessage by remember { mutableStateOf("Ready for authentication") }

  Scaffold(
    modifier = Modifier.fillMaxSize().testTag("main_scaffold"),
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(id = R.string.app_name),
            fontWeight = FontWeight.Bold
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.testTag("top_app_bar")
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(24.dp)
        .verticalScroll(rememberScrollState())
        .testTag("main_content_column"),
      verticalArrangement = Arrangement.spacedBy(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
          .testTag("status_banner")
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Verified Icon",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp).testTag("verified_icon")
          )
          Spacer(modifier = Modifier.width(16.dp))
          Column {
            Text(
              text = "Signed Build Ready",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
              text = "Firebase Auth & Credential Manager initialized",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
          }
        }
      }

      // Authentication Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("auth_card"),
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
              imageVector = Icons.Default.Lock,
              contentDescription = "Auth",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "Google Sign-In Flow",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          if (currentUser != null) {
            LogoutComponent(
              user = currentUser,
              onSignOut = {
                performLogout(authManager) {
                  currentUser = null
                  statusMessage = "Signed out successfully"
                }
              }
            )
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Text(
                text = "Authenticate securely using Android Credential Manager and Firebase Auth.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              if (isLoading) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center,
                  modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                  CircularProgressIndicator(modifier = Modifier.size(24.dp))
                  Spacer(modifier = Modifier.width(12.dp))
                  Text("Signing in with Google...")
                }
              } else {
                Button(
                  onClick = {
                    isLoading = true
                    statusMessage = "Launching Credential Manager..."
                    coroutineScope.launch {
                      val result = authManager.signInWithGoogle(
                        webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"
                      )
                      isLoading = false
                      if (result.isSuccess) {
                        currentUser = result.getOrNull()
                        statusMessage = "Sign-in successful!"
                      } else {
                        statusMessage = "Sign-in state: ${result.exceptionOrNull()?.localizedMessage ?: "Configuration required"}"
                      }
                    }
                  },
                  modifier = Modifier.fillMaxWidth().testTag("google_sign_in_button")
                ) {
                  Text("Sign in with Google")
                }
              }

              Text(
                text = statusMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
              )
            }
          }
        }
      }

      UserContentSection()

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("build_details_card"),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Text(
            text = "Build Information",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          InfoRow(
            icon = Icons.Default.VerifiedUser,
            label = "Package Name",
            value = BuildConfig.APPLICATION_ID
          )

          InfoRow(
            icon = Icons.Default.Info,
            label = "Build Type",
            value = BuildConfig.BUILD_TYPE
          )
        }
      }
    }
  }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth()
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(24.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
      )
      Text(
        text = value,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}


