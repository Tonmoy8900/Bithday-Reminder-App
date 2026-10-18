package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Campaign
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import com.example.data.model.GreetingPost
import com.example.data.model.PreloadedRoster
import com.example.data.repository.BirthdayRepository
import com.example.util.BirthdayNotificationHelper
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
                    currentUserName = currentUserName,
                    currentUserEmail = currentUserEmail
                )
            }
        }
    )

    val birthdaysState by viewModel.birthdaysState.collectAsStateWithLifecycle()
    val greetingsState by viewModel.greetingsState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonthFilter.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val celebrationEvent by viewModel.celebrationEvent.collectAsStateWithLifecycle()
    val isSeeding by viewModel.isSeeding.collectAsStateWithLifecycle()
    val roleMode by viewModel.roleMode.collectAsStateWithLifecycle()
    val isAdminAuthorized by viewModel.isAdminAuthorized.collectAsStateWithLifecycle()

    val isAdminMode = roleMode == AppRoleMode.ADMIN_MASTER_CONTROL

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var editingContact by remember { mutableStateOf<BirthdayContact?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var greetingDialogTarget by remember { mutableStateOf<BirthdayContact?>(null) }
    var showComposeGreetingDialog by remember { mutableStateOf(false) }

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
    }

    if (selectedTab != 0) {
        BackHandler {
            selectedTab = 0
        }
    }

    val allContacts = (birthdaysState as? UiState.Success)?.data ?: emptyList()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 700.dp

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
                                color = if (isAdminMode) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isAdminMode) {
                                            Icons.Filled.AdminPanelSettings
                                        } else {
                                            Icons.Filled.Cake
                                        },
                                        contentDescription = null,
                                        tint = if (isAdminMode) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.secondary
                                        },
                                        modifier = Modifier.size(20.dp)
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
                                    text = if (isAdminMode) {
                                        "ADMIN CONTROL • Master Management"
                                    } else {
                                        "USER MODE • Wish & View Only"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isAdminMode) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.secondary
                                    },
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
                                    selectedTab = 2
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
                                text = if (isAdminMode) "Admin Mode" else "User Mode",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        IconButton(
                            onClick = {
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
            bottomBar = {
                if (!isExpandedScreen) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Filled.Cake else Icons.Outlined.Cake,
                                    contentDescription = stringResource(R.string.tab_upcoming)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_upcoming)) },
                            modifier = Modifier.testTag("nav_tab_upcoming")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 1) Icons.Filled.Campaign else Icons.Outlined.Campaign,
                                    contentDescription = stringResource(R.string.tab_greetings_wall)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_greetings_wall)) },
                            modifier = Modifier.testTag("nav_tab_greetings")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 2) {
                                        Icons.Filled.AdminPanelSettings
                                    } else {
                                        Icons.Outlined.AdminPanelSettings
                                    },
                                    contentDescription = stringResource(R.string.tab_automation)
                                )
                            },
                            label = { Text(stringResource(R.string.tab_automation)) },
                            modifier = Modifier.testTag("nav_tab_automation")
                        )
                    }
                }
            },
            floatingActionButton = {
                // In User Mode (or Tabs 0/1), users ONLY send wishes.
                // Master Birthday creation is exclusively in Admin Control!
                val showMasterAddFab = selectedTab == 2 && isAdminMode
                ExtendedFloatingActionButton(
                    onClick = {
                        if (showMasterAddFab) {
                            editingContact = null
                            showAddEditDialog = true
                        } else {
                            greetingDialogTarget = allContacts.firstOrNull()
                            showComposeGreetingDialog = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (showMasterAddFab) Icons.Filled.Add else Icons.Filled.Celebration,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            if (showMasterAddFab) {
                                stringResource(R.string.add_birthday)
                            } else {
                                stringResource(R.string.send_greeting)
                            }
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("primary_fab")
                )
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    NavigationRail(modifier = Modifier.fillMaxHeight()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        NavigationRailItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Filled.Cake, contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_upcoming)) }
                        )
                        NavigationRailItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Filled.Campaign, contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_greetings_wall)) }
                        )
                        NavigationRailItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Filled.AdminPanelSettings, contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_automation)) }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 840.dp)
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

                        when (selectedTab) {
                            0 -> UpcomingBirthdaysTab(
                                birthdaysState = birthdaysState,
                                isAdminMode = isAdminMode,
                                searchQuery = searchQuery,
                                selectedMonth = selectedMonth,
                                onSearchChange = viewModel::updateSearchQuery,
                                onMonthSelect = viewModel::selectMonthFilter,
                                onCelebrateNow = { contact ->
                                    viewModel.triggerMasterAutoBroadcast(contact)
                                },
                                onComposeCard = { contact ->
                                    greetingDialogTarget = contact
                                    showComposeGreetingDialog = true
                                },
                                onShareSms = { contact ->
                                    BirthdayNotificationHelper.sendDirectSmsGreeting(
                                        context = context,
                                        phone = contact.phone,
                                        recipientName = contact.name,
                                        message = contact.autoMessageTemplate
                                    )
                                },
                                onAdminEdit = { contact ->
                                    editingContact = contact
                                    showAddEditDialog = true
                                },
                                onAdminDelete = viewModel::deleteBirthday
                            )

                            1 -> GreetingsWallTab(
                                greetingsState = greetingsState,
                                isAdminMode = isAdminMode,
                                onCheer = viewModel::cheerGreeting,
                                onShareExternal = { post ->
                                    BirthdayNotificationHelper.sharePersonalizedGreeting(
                                        context = context,
                                        recipientName = post.recipientName,
                                        message = post.message
                                    )
                                },
                                onAdminDelete = viewModel::deleteGreeting,
                                onOpenComposer = {
                                    greetingDialogTarget = allContacts.firstOrNull()
                                    showComposeGreetingDialog = true
                                }
                            )

                            2 -> AdminControlCenterTab(
                                contacts = allContacts,
                                isAdminMode = isAdminMode,
                                isAdminAuthorized = isAdminAuthorized,
                                currentUserEmail = currentUserEmail,
                                isSeeding = isSeeding,
                                onUnlockWithPin = viewModel::unlockAdminControlWithPin,
                                onSwitchRoleMode = viewModel::switchRoleMode,
                                onAddMasterBirthday = {
                                    editingContact = null
                                    showAddEditDialog = true
                                },
                                onSyncMasterRoster = viewModel::syncPreloadedRoster,
                                onToggleAutoSend = viewModel::toggleAutoSend,
                                onToggleNotifyAll = viewModel::toggleNotifyAllUsers,
                                onSimulateAutoWish = viewModel::triggerMasterAutoBroadcast,
                                onEditRecord = { contact ->
                                    editingContact = contact
                                    showAddEditDialog = true
                                },
                                onDeleteRecord = viewModel::deleteBirthday
                            )
                        }
                    }
                }
            }
        }
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

    if (showComposeGreetingDialog) {
        ComposeGreetingDialog(
            contacts = allContacts,
            initialContact = greetingDialogTarget,
            defaultSenderName = currentUserName,
            onDismiss = { showComposeGreetingDialog = false },
            onPostAndShare = { contact, sender, message, theme, alsoShareExternal ->
                viewModel.postPersonalizedGreeting(
                    contact = contact,
                    senderName = sender,
                    message = message,
                    cardTheme = theme
                )
                showComposeGreetingDialog = false
                if (alsoShareExternal) {
                    BirthdayNotificationHelper.sharePersonalizedGreeting(
                        context = context,
                        recipientName = contact.name,
                        message = message
                    )
                } else {
                    selectedTab = 1
                }
            }
        )
    }
}

@Composable
private fun UpcomingBirthdaysTab(
    birthdaysState: UiState<List<BirthdayContact>>,
    isAdminMode: Boolean,
    searchQuery: String,
    selectedMonth: Int?,
    onSearchChange: (String) -> Unit,
    onMonthSelect: (Int?) -> Unit,
    onCelebrateNow: (BirthdayContact) -> Unit,
    onComposeCard: (BirthdayContact) -> Unit,
    onShareSms: (BirthdayContact) -> Unit,
    onAdminEdit: (BirthdayContact) -> Unit,
    onAdminDelete: (BirthdayContact) -> Unit
) {
    when (birthdaysState) {
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
                    text = birthdaysState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        is UiState.Success -> {
            val allContacts = birthdaysState.data
            val filteredContacts = remember(allContacts, searchQuery, selectedMonth) {
                allContacts.filter { contact ->
                    val matchesMonth = selectedMonth == null || contact.birthMonth.toInt() == selectedMonth
                    val q = searchQuery.trim().lowercase()
                    val matchesSearch = q.isEmpty() ||
                        contact.name.lowercase().contains(q) ||
                        contact.monthName.lowercase().contains(q) ||
                        contact.birthDate.contains(q) ||
                        contact.relationship.lowercase().contains(q)
                    matchesMonth && matchesSearch
                }
            }

            val monthCounts = remember(allContacts) {
                allContacts.groupingBy { it.birthMonth.toInt() }.eachCount()
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(key = "role_banner") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isAdminMode) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isAdminMode) {
                                    Icons.Filled.AdminPanelSettings
                                } else {
                                    Icons.Filled.Person
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isAdminMode) {
                                    "Admin Control Active: You can view, send wishes, or manage master records."
                                } else {
                                    "User Mode (Wish & View Only): Browse upcoming birthdays and send personalized wishes. Master changes require Admin Control."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (allContacts.isNotEmpty()) {
                    item(key = "spotlight_hero") {
                        NextCelebrationSpotlightCard(
                            contact = allContacts.first(),
                            totalBirthdaysCount = allContacts.size,
                            autoEnabledCount = allContacts.count { it.autoSendEnabled },
                            isAdminMode = isAdminMode,
                            onCelebrateNow = onCelebrateNow,
                            onComposeCard = onComposeCard,
                            onShareSms = onShareSms
                        )
                    }
                }

                item(key = "search_and_month_filters") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            placeholder = { Text(stringResource(R.string.search_hint)) },
                            leadingIcon = {
                                Icon(Icons.Filled.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchChange("") }) {
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
                                    onClick = { onMonthSelect(null) },
                                    label = { Text("All (${allContacts.size})") },
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
                                            onMonthSelect(if (selectedMonth == monthNum) null else monthNum)
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
                                    text = "No matching birthdays found",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try clearing your search filter.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            onComposeCard = onComposeCard,
                            onShareSms = onShareSms,
                            onAdminEdit = onAdminEdit,
                            onAdminDelete = onAdminDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GreetingsWallTab(
    greetingsState: UiState<List<GreetingPost>>,
    isAdminMode: Boolean,
    onCheer: (GreetingPost) -> Unit,
    onShareExternal: (GreetingPost) -> Unit,
    onAdminDelete: (GreetingPost) -> Unit,
    onOpenComposer: () -> Unit
) {
    var filterChannel by rememberSaveable { mutableStateOf("all") }

    when (greetingsState) {
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
                    text = greetingsState.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        is UiState.Success -> {
            val allGreetings = greetingsState.data
            val filtered = remember(allGreetings, filterChannel) {
                when (filterChannel) {
                    "auto" -> allGreetings.filter { it.deliveryChannel == "auto_broadcast" }
                    "cards" -> allGreetings.filter { it.deliveryChannel != "auto_broadcast" }
                    else -> allGreetings
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(key = "wall_header") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Campaign,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "All-User Intimation & Celebration Wall",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "All installed app users are intimated here on special days and can share personalized birthday wishes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = filterChannel == "all",
                                    onClick = { filterChannel = "all" },
                                    label = { Text("All (${allGreetings.size})") }
                                )
                                FilterChip(
                                    selected = filterChannel == "auto",
                                    onClick = { filterChannel = "auto" },
                                    label = { Text("Master Intimations") }
                                )
                                FilterChip(
                                    selected = filterChannel == "cards",
                                    onClick = { filterChannel = "cards" },
                                    label = { Text("User Wishes") }
                                )
                            }
                        }
                    }
                }

                if (filtered.isEmpty()) {
                    item(key = "empty_greetings") {
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
                                    imageVector = Icons.Filled.Celebration,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Celebration Wishes Yet",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap 'Send Wish' to share a personalized birthday greeting card with everyone!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onOpenComposer,
                                    modifier = Modifier.testTag("empty_wall_create_greeting_button")
                                ) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Send Personalized Wish")
                                }
                            }
                        }
                    }
                } else {
                    items(
                        items = filtered,
                        key = { it.id.ifBlank { it.recipientName + it.message } }
                    ) { post ->
                        GreetingWallCard(
                            greeting = post,
                            isAdminMode = isAdminMode,
                            onCheer = onCheer,
                            onShareExternal = onShareExternal,
                            onAdminDelete = onAdminDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminControlCenterTab(
    contacts: List<BirthdayContact>,
    isAdminMode: Boolean,
    isAdminAuthorized: Boolean,
    currentUserEmail: String,
    isSeeding: Boolean,
    onUnlockWithPin: (String) -> Boolean,
    onSwitchRoleMode: (AppRoleMode) -> Unit,
    onAddMasterBirthday: () -> Unit,
    onSyncMasterRoster: () -> Unit,
    onToggleAutoSend: (BirthdayContact, Boolean) -> Unit,
    onToggleNotifyAll: (BirthdayContact, Boolean) -> Unit,
    onSimulateAutoWish: (BirthdayContact) -> Unit,
    onEditRecord: (BirthdayContact) -> Unit,
    onDeleteRecord: (BirthdayContact) -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    val autoEnabledCount = contacts.count { it.autoSendEnabled }
    val broadcastCount = contacts.count { it.notifyAllUsers }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "admin_access_header") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAdminMode) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isAdminMode) {
                                    Icons.Filled.VerifiedUser
                                } else {
                                    Icons.Filled.Lock
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = if (isAdminMode) {
                                        "Master Admin Control Active"
                                    } else {
                                        "Admin Control Locked (User Mode Active)"
                                    },
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "Account: ${currentUserEmail.ifBlank { "Signed-In User" }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "All master tasks—adding/editing/deleting employee birthdays, syncing the 12-member roster, configuring automated messages, and broadcasting all-user intimations—are controlled exclusively here.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isAdminMode) {
                        if (isAdminAuthorized) {
                            Button(
                                onClick = { onSwitchRoleMode(AppRoleMode.ADMIN_MASTER_CONTROL) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("activate_admin_mode_button")
                            ) {
                                Icon(Icons.Filled.LockOpen, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Switch to Admin Master Control")
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = pinInput,
                                    onValueChange = { pinInput = it.filter { ch -> ch.isDigit() }.take(6) },
                                    label = { Text("Master Admin PIN (1234)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("admin_pin_input")
                                )
                                Button(
                                    onClick = {
                                        if (onUnlockWithPin(pinInput)) {
                                            pinInput = ""
                                        }
                                    },
                                    modifier = Modifier.testTag("unlock_admin_button")
                                ) {
                                    Icon(Icons.Filled.LockOpen, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Unlock")
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onAddMasterBirthday,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_add_birthday_button")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Master Record")
                            }
                            FilledTonalButton(
                                onClick = onSyncMasterRoster,
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

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { onSwitchRoleMode(AppRoleMode.USER_WISH_AND_VIEW) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("switch_to_user_view_button")
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Preview as Standard User (Wish & View Only)")
                        }
                    }
                }
            }
        }

        if (!isAdminMode) {
            item(key = "locked_notice") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Master Controls Restricted to Admin",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "In User Mode, you can view all upcoming birthdays and post wishes on the Greetings Wall. Switch to Admin Control above to manage master birthday records and automation rules.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            item(key = "master_stats_bar") {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Master Roster (${contacts.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$autoEnabledCount Auto-Wish • $broadcastCount Intimate All",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            items(
                items = contacts,
                key = { "master_${it.id.ifBlank { it.name }}" }
            ) { contact ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "DOB: ${contact.birthDate} (${contact.monthName}) • ${contact.relationship}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row {
                                IconButton(
                                    onClick = { onEditRecord(contact) },
                                    modifier = Modifier.testTag("master_edit_${contact.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Edit Master Record",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteRecord(contact) },
                                    modifier = Modifier.testTag("master_delete_${contact.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = "Delete Master Record",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Template: \"${contact.autoMessageTemplate}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Master Auto-Send on Birthday",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = contact.autoSendEnabled,
                                onCheckedChange = { onToggleAutoSend(contact, it) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Intimate All Installed App Users",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = contact.notifyAllUsers,
                                onCheckedChange = { onToggleNotifyAll(contact, it) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        FilledTonalButton(
                            onClick = { onSimulateAutoWish(contact) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_autowish_button_${contact.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Celebration,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Dispatch Master Auto-Wish & Intimate All Users Now",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
