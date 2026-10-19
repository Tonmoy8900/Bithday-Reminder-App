package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.auth.signOutUser
import com.example.data.model.BirthdayContact
import com.example.data.model.PreloadedRoster
import com.example.data.repository.BirthdayRepository
import com.example.util.BirthdayNotificationHelper
import com.example.util.BirthdaySongPlayer
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayPulseApp(
    currentUserId: String,
    currentUserName: String,
    currentUserEmail: String,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val songPlayer = remember { BirthdaySongPlayer(context, scope) }

    DisposableEffect(Unit) {
        onDispose {
            songPlayer.release()
        }
    }

    val mailIdDisplayName = remember(currentUserName, currentUserEmail) {
        if (currentUserEmail.isNotBlank()) {
            val rawPrefix = currentUserEmail.substringBefore("@")
                .replace(".", " ")
                .replace("_", " ")
                .trim()
            if (currentUserName.isNotBlank() && !currentUserName.contains("@")) {
                currentUserName
            } else {
                rawPrefix.split(" ").filter { it.isNotBlank() }
                    .joinToString(" ") { it.replaceFirstChar { ch -> ch.uppercase() } }
                    .ifBlank { currentUserName }
            }
        } else {
            currentUserName.ifBlank { "Friend" }
        }
    }

    val viewModel: BirthdayViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                BirthdayViewModel(
                    repository = BirthdayRepository(db),
                    currentUserId = currentUserId,
                    currentUserName = mailIdDisplayName,
                    currentUserEmail = currentUserEmail
                )
            }
        }
    )

    val birthdaysState by viewModel.birthdaysState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonthFilter.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val celebrationEvent by viewModel.celebrationEvent.collectAsStateWithLifecycle()
    val isSeeding by viewModel.isSeeding.collectAsStateWithLifecycle()
    val roleMode by viewModel.roleMode.collectAsStateWithLifecycle()
    val isAdminAuthorized by viewModel.isAdminAuthorized.collectAsStateWithLifecycle()
    val songState by songPlayer.state.collectAsStateWithLifecycle()

    val isAdminMode = roleMode == AppRoleMode.ADMIN_MASTER_CONTROL

    var editingContact by remember { mutableStateOf<BirthdayContact?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        BirthdayNotificationHelper.ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(celebrationEvent) {
        val event = celebrationEvent ?: return@LaunchedEffect
        BirthdayNotificationHelper.showBirthdayIntimationNotification(
            context = context,
            celebrantName = event.celebrantName,
            message = event.message
        )
        songPlayer.playHappyBirthdaySong(
            celebrantName = event.celebrantName,
            wishedByMailName = mailIdDisplayName
        )
    }

    if (isAdminMode && !viewModel.isBootstrappedAdminAccount) {
        BackHandler {
            viewModel.switchRoleMode(AppRoleMode.USER_WISH_AND_VIEW)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Cake,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(R.string.developer_credit),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            if (isAdminMode) {
                                viewModel.switchRoleMode(AppRoleMode.USER_WISH_AND_VIEW)
                            } else if (isAdminAuthorized) {
                                viewModel.switchRoleMode(AppRoleMode.ADMIN_MASTER_CONTROL)
                            } else {
                                pinInput = ""
                                showAdminPinDialog = true
                            }
                        },
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("role_mode_switcher_button")
                    ) {
                        Icon(
                            imageVector = if (isAdminMode) {
                                Icons.Filled.AdminPanelSettings
                            } else {
                                Icons.Filled.Person
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAdminMode) "Admin Control" else "User View",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    IconButton(
                        onClick = {
                            songPlayer.stop()
                            signOutUser(
                                credentialManager = credentialManager,
                                onSignOutComplete = onSignedOut,
                                scope = scope
                            )
                        },
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(R.string.sign_out)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (isAdminMode) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editingContact = null
                        showAddEditDialog = true
                    },
                    icon = {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    },
                    text = {
                        Text(stringResource(R.string.add_birthday))
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("primary_fab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp)
            ) {
                AnimatedVisibility(visible = statusBanner != null) {
                    statusBanner?.let { msg ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .testTag("status_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                IconButton(onClick = { viewModel.clearStatusBanner() }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Dismiss notification"
                                    )
                                }
                            }
                        }
                    }
                }

                when (val state = birthdaysState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    is UiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    is UiState.Success -> {
                        val allContacts = state.data
                        val filteredContacts = remember(allContacts, searchQuery, selectedMonth) {
                            allContacts.filter { contact ->
                                val matchesMonth = selectedMonth == null || contact.birthMonth.toInt() == selectedMonth
                                val q = searchQuery.trim().lowercase()
                                val matchesSearch = q.isEmpty() ||
                                    contact.name.lowercase().contains(q) ||
                                    contact.monthName.lowercase().contains(q) ||
                                    contact.birthDate.contains(q) ||
                                    contact.phone.contains(q)
                                matchesMonth && matchesSearch
                            }
                        }

                        val monthCounts = remember(allContacts) {
                            allContacts.groupingBy { it.birthMonth.toInt() }.eachCount()
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 96.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // 1. Musical Birthday Song Studio Banner ("Happy Birthday [Name] as per Mail ID Name")
                            item(key = "musical_song_banner") {
                                MusicalSongBannerCard(
                                    mailIdName = mailIdDisplayName,
                                    userEmail = currentUserEmail,
                                    songState = songState,
                                    onPlayForMailUser = {
                                        songPlayer.playHappyBirthdaySong(
                                            celebrantName = mailIdDisplayName,
                                            wishedByMailName = mailIdDisplayName
                                        )
                                    },
                                    onStopSong = { songPlayer.stop() }
                                )
                            }

                            // 2. Admin Master Control Toolbar (ONLY visible when Admin Mode is active)
                            if (isAdminMode) {
                                item(key = "admin_master_bar") {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.AdminPanelSettings,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "Admin Master Control Active",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Manage master birthday records, mobile numbers, and automated wishes. Tap 'User View' in the top bar to preview the standard user experience.",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        editingContact = null
                                                        showAddEditDialog = true
                                                    },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("admin_add_birthday_button")
                                                ) {
                                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Add Person")
                                                }
                                                FilledTonalButton(
                                                    onClick = { viewModel.syncPreloadedRoster() },
                                                    enabled = !isSeeding,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("admin_sync_roster_button")
                                                ) {
                                                    Icon(Icons.Filled.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(if (isSeeding) "Syncing…" else "Sync 12 Roster")
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Next Upcoming Birthday Spotlight
                            if (allContacts.isNotEmpty()) {
                                val nextPerson = allContacts.first()
                                item(key = "spotlight_hero") {
                                    NextCelebrationSpotlightCard(
                                        contact = nextPerson,
                                        isPlayingThisPerson = songState.isPlaying &&
                                            songState.celebrantName.equals(nextPerson.name, ignoreCase = true),
                                        onPlaySong = { person ->
                                            if (songState.isPlaying &&
                                                songState.celebrantName.equals(person.name, ignoreCase = true)
                                            ) {
                                                songPlayer.stop()
                                            } else {
                                                songPlayer.playHappyBirthdaySong(
                                                    celebrantName = person.name,
                                                    wishedByMailName = mailIdDisplayName
                                                )
                                            }
                                        },
                                        onWishSmsByMobile = { person ->
                                            songPlayer.playHappyBirthdaySong(
                                                celebrantName = person.name,
                                                wishedByMailName = mailIdDisplayName
                                            )
                                            BirthdayNotificationHelper.sendDirectSmsGreeting(
                                                context = context,
                                                phone = person.phone,
                                                recipientName = person.name,
                                                message = person.autoMessageTemplate,
                                                senderMailName = mailIdDisplayName
                                            )
                                        },
                                        onWishWhatsAppByMobile = { person ->
                                            BirthdayNotificationHelper.sendWhatsAppGreetingByMobile(
                                                context = context,
                                                phone = person.phone,
                                                recipientName = person.name,
                                                message = person.autoMessageTemplate,
                                                senderMailName = mailIdDisplayName
                                            )
                                        },
                                        onCallMobile = { person ->
                                            BirthdayNotificationHelper.dialMobileNumber(context, person.phone)
                                        }
                                    )
                                }
                            }

                            // 4. Clean Search & Month Filters
                            item(key = "search_and_month_filters") {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = viewModel::updateSearchQuery,
                                        placeholder = { Text(stringResource(R.string.search_hint)) },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Search, contentDescription = null)
                                        },
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                                    Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(18.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("search_birthdays_input")
                                    )

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        item {
                                            FilterChip(
                                                selected = selectedMonth == null,
                                                onClick = { viewModel.selectMonthFilter(null) },
                                                label = { Text("All Upcoming (${allContacts.size})") },
                                                modifier = Modifier.testTag("month_filter_all")
                                            )
                                        }
                                        items(PreloadedRoster.MONTHS.indices.toList()) { index ->
                                            val monthNum = index + 1
                                            val count = monthCounts[monthNum] ?: 0
                                            if (count > 0 || selectedMonth == monthNum) {
                                                FilterChip(
                                                    selected = selectedMonth == monthNum,
                                                    onClick = {
                                                        viewModel.selectMonthFilter(
                                                            if (selectedMonth == monthNum) null else monthNum
                                                        )
                                                    },
                                                    label = {
                                                        Text("${PreloadedRoster.MONTHS[index]} ($count)")
                                                    },
                                                    modifier = Modifier.testTag("month_filter_$monthNum")
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 5. Upcoming Birthday Event Cards
                            if (filteredContacts.isEmpty()) {
                                item(key = "empty_birthdays") {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(28.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Cake,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(44.dp)
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "No matching upcoming events",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(
                                    items = filteredContacts,
                                    key = { it.id.ifBlank { it.name } }
                                ) { contact ->
                                    BirthdayContactCard(
                                        contact = contact,
                                        isAdminMode = isAdminMode,
                                        isPlayingThisPerson = songState.isPlaying &&
                                            songState.celebrantName.equals(contact.name, ignoreCase = true),
                                        onPlaySong = { person ->
                                            if (songState.isPlaying &&
                                                songState.celebrantName.equals(person.name, ignoreCase = true)
                                            ) {
                                                songPlayer.stop()
                                            } else {
                                                songPlayer.playHappyBirthdaySong(
                                                    celebrantName = person.name,
                                                    wishedByMailName = mailIdDisplayName
                                                )
                                            }
                                        },
                                        onWishSmsByMobile = { person ->
                                            songPlayer.playHappyBirthdaySong(
                                                celebrantName = person.name,
                                                wishedByMailName = mailIdDisplayName
                                            )
                                            BirthdayNotificationHelper.sendDirectSmsGreeting(
                                                context = context,
                                                phone = person.phone,
                                                recipientName = person.name,
                                                message = person.autoMessageTemplate,
                                                senderMailName = mailIdDisplayName
                                            )
                                        },
                                        onWishWhatsAppByMobile = { person ->
                                            BirthdayNotificationHelper.sendWhatsAppGreetingByMobile(
                                                context = context,
                                                phone = person.phone,
                                                recipientName = person.name,
                                                message = person.autoMessageTemplate,
                                                senderMailName = mailIdDisplayName
                                            )
                                        },
                                        onCallMobile = { person ->
                                            BirthdayNotificationHelper.dialMobileNumber(context, person.phone)
                                        },
                                        onAdminEdit = { person ->
                                            editingContact = person
                                            showAddEditDialog = true
                                        },
                                        onAdminDelete = viewModel::deleteBirthday
                                    )
                                }
                            }

                            // 6. Developer Credit Footer
                            item(key = "developer_credit_footer") {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .testTag("developer_credit_card")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 14.dp, horizontal = 16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = stringResource(R.string.developer_credit),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Global Birthday Reminder & Musical Wish Platform",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdminPinDialog) {
        AlertDialog(
            onDismissRequest = { showAdminPinDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AdminPanelSettings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Unlock Admin Master Control")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Standard users can view upcoming events and wish by mobile number. Enter Admin PIN (1234) to manage master records.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it.filter { ch -> ch.isDigit() }.take(6) },
                        label = { Text("Admin PIN (1234)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pin_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.unlockAdminControlWithPin(pinInput)) {
                            showAdminPinDialog = false
                        }
                    },
                    modifier = Modifier.testTag("unlock_admin_button")
                ) {
                    Icon(Icons.Filled.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Unlock Admin")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminPinDialog = false }) {
                    Text("Stay in User View")
                }
            }
        )
    }

    if (showAddEditDialog && isAdminMode) {
        BirthdayEditorDialog(
            initialContact = editingContact,
            onDismiss = { showAddEditDialog = false },
            onSave = { name, day, month, year, rel, phone, template, autoSend, notifyAll ->
                viewModel.addOrUpdateBirthday(
                    existing = editingContact,
                    name = name,
                    birthDay = day,
                    birthMonth = month,
                    birthYear = year,
                    relationship = rel,
                    phone = phone,
                    autoMessageTemplate = template,
                    autoSendEnabled = autoSend,
                    notifyAllUsers = notifyAll
                )
                showAddEditDialog = false
            }
        )
    }
}
