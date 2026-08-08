package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ContentSyncRepository
import com.example.data.local.AppDatabase
import kotlinx.coroutines.launch

@Composable
fun UserContentSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val syncRepository = remember { ContentSyncRepository(db.userContentDao()) }
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    val contentList by remember(searchQuery) {
        syncRepository.searchContent(searchQuery)
    }.collectAsState(initial = emptyList())

    var titleInput by remember { mutableStateOf("") }
    var bodyInput by remember { mutableStateOf("") }
    var isSyncing by remember { mutableStateOf(false) }
    var syncStatusMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("room_database_card"),
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
                    imageVector = Icons.Default.Storage,
                    contentDescription = "Database",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp).testTag("room_db_icon")
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Offline & Network Sync (Room + Retrofit)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Room database caches Retrofit network data for seamless offline access",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // Sync Action Button
            OutlinedButton(
                onClick = {
                    isSyncing = true
                    syncStatusMessage = null
                    coroutineScope.launch {
                        val result = syncRepository.syncRemoteData()
                        isSyncing = false
                        if (result.isSuccess) {
                            syncStatusMessage = "Synced ${result.getOrNull()} items from remote API to Room cache!"
                        } else {
                            syncStatusMessage = "Sync failed: ${result.exceptionOrNull()?.localizedMessage ?: "Offline"}. Showing cached data."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sync_network_button"),
                enabled = !isSyncing
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Synchronizing Network Data...")
                } else {
                    Icon(imageVector = Icons.Default.CloudSync, contentDescription = "Sync Network")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sync Network Data to Room")
                }
            }

            syncStatusMessage?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("sync_status_message")
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Local Item Title") },
                    modifier = Modifier.fillMaxWidth().testTag("content_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = bodyInput,
                    onValueChange = { bodyInput = it },
                    label = { Text("Content Body") },
                    modifier = Modifier.fillMaxWidth().testTag("content_body_input")
                )

                Button(
                    onClick = {
                        if (titleInput.isNotBlank()) {
                            val title = titleInput
                            val body = bodyInput
                            coroutineScope.launch {
                                syncRepository.addLocalContent(title, body)
                                titleInput = ""
                                bodyInput = ""
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_content_button"),
                    enabled = titleInput.isNotBlank()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Local Content to Room")
                }
            }

            // Real-Time Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Room Database") },
                placeholder = { Text("Filter items in real-time...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("room_search_bar_input")
            )

            if (contentList.isNotEmpty()) {
                Text(
                    text = if (searchQuery.isBlank()) "Cached Offline Items (${contentList.size})" else "Filtered Items (${contentList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    contentList.forEach { item ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("content_item_${item.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (item.body.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.body,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            syncRepository.deleteContent(item)
                                        }
                                    },
                                    modifier = Modifier.testTag("delete_content_${item.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Item",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (searchQuery.isNotBlank()) {
                Text(
                    text = "No matching items found for \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.testTag("no_search_results_text")
                )
            }
        }
    }
}
