package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BirthdayContact
import com.example.data.model.CardThemeOption
import com.example.data.model.GreetingPost
import com.example.data.model.PreloadedRoster
import com.example.data.repository.BirthdayRepository
import com.google.firebase.Timestamp
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class CelebrationBannerEvent(
    val celebrantName: String,
    val message: String,
    val isAutomated: Boolean
)

enum class AppRoleMode {
    USER_WISH_AND_VIEW,
    ADMIN_MASTER_CONTROL
}

class BirthdayViewModel(
    private val repository: BirthdayRepository,
    val currentUserId: String,
    val currentUserName: String,
    val currentUserEmail: String
) : ViewModel() {

    val isBootstrappedAdminAccount: Boolean =
        currentUserEmail.equals(MASTER_ADMIN_EMAIL, ignoreCase = true)

    private val _isAdminAuthorized = MutableStateFlow(isBootstrappedAdminAccount)
    val isAdminAuthorized: StateFlow<Boolean> = _isAdminAuthorized.asStateFlow()

    private val _roleMode = MutableStateFlow(
        if (isBootstrappedAdminAccount) AppRoleMode.ADMIN_MASTER_CONTROL else AppRoleMode.USER_WISH_AND_VIEW
    )
    val roleMode: StateFlow<AppRoleMode> = _roleMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMonthFilter = MutableStateFlow<Int?>(null)
    val selectedMonthFilter: StateFlow<Int?> = _selectedMonthFilter.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _celebrationEvent = MutableStateFlow<CelebrationBannerEvent?>(null)
    val celebrationEvent: StateFlow<CelebrationBannerEvent?> = _celebrationEvent.asStateFlow()

    private val _isSeeding = MutableStateFlow(false)
    val isSeeding: StateFlow<Boolean> = _isSeeding.asStateFlow()

    private var hasCheckedInitialSeed = false
    private val autoTriggeredIdsInSession = mutableSetOf<String>()

    val birthdaysState: StateFlow<UiState<List<BirthdayContact>>> =
        repository.observeBirthdays(currentUserId)
            .map<List<BirthdayContact>, UiState<List<BirthdayContact>>> { list ->
                val sorted = list.sortedWith(
                    compareBy<BirthdayContact> { it.daysUntilNextBirthday() }
                        .thenBy { it.birthMonth }
                        .thenBy { it.birthDay }
                )
                if (!hasCheckedInitialSeed && list.isEmpty()) {
                    hasCheckedInitialSeed = true
                    triggerInitialRosterSeed()
                } else if (list.isNotEmpty()) {
                    hasCheckedInitialSeed = true
                    checkAndRunAutoPilot(sorted)
                }
                UiState.Success(sorted)
            }
            .catch { error ->
                Log.w(TAG, "Error observing birthdays", error)
                emit(UiState.Error(error.message ?: "Failed to load birthdays"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val greetingsState: StateFlow<UiState<List<GreetingPost>>> =
        repository.observeGreetings(currentUserId)
            .map<List<GreetingPost>, UiState<List<GreetingPost>>> { list ->
                val sorted = list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                UiState.Success(sorted)
            }
            .catch { error ->
                Log.w(TAG, "Error observing greetings", error)
                emit(UiState.Error(error.message ?: "Failed to load greetings wall"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    fun switchRoleMode(targetMode: AppRoleMode) {
        if (targetMode == AppRoleMode.ADMIN_MASTER_CONTROL && !_isAdminAuthorized.value) {
            _statusBanner.value = "Enter Admin Passcode (1234) in Admin Control tab to unlock Master Control."
            return
        }
        _roleMode.value = targetMode
        _statusBanner.value = if (targetMode == AppRoleMode.ADMIN_MASTER_CONTROL) {
            "Switched to Admin Master Control mode"
        } else {
            "Switched to Standard User mode (Wish & View Only)"
        }
    }

    fun unlockAdminControlWithPin(pin: String): Boolean {
        return if (pin.trim() == DEFAULT_ADMIN_PIN || isBootstrappedAdminAccount) {
            _isAdminAuthorized.value = true
            _roleMode.value = AppRoleMode.ADMIN_MASTER_CONTROL
            _statusBanner.value = "Admin Master Control unlocked! All master tasks are now active."
            true
        } else {
            _statusBanner.value = "Invalid Admin PIN. Use 1234 to unlock Master Admin Control."
            false
        }
    }

    fun lockToUserMode() {
        _roleMode.value = AppRoleMode.USER_WISH_AND_VIEW
        _statusBanner.value = "Locked to User Mode (Wish & View Only)."
    }

    private fun ensureMasterAdminActive(): Boolean {
        if (_roleMode.value != AppRoleMode.ADMIN_MASTER_CONTROL) {
            _statusBanner.value = "Restricted: Only Admin Control can perform master data tasks."
            return false
        }
        return true
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectMonthFilter(month: Int?) {
        _selectedMonthFilter.value = month
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    private fun triggerInitialRosterSeed() {
        viewModelScope.launch {
            _isSeeding.value = true
            val result = repository.seedEmployeeRosterIfEmpty()
            _isSeeding.value = false
            result.onSuccess { count ->
                if (count > 0) {
                    _statusBanner.value = "Master Roster initialized with $count employee birthdays!"
                }
            }
        }
    }

    fun syncPreloadedRoster() {
        if (!ensureMasterAdminActive()) return
        viewModelScope.launch {
            _isSeeding.value = true
            val result = repository.seedEmployeeRosterForce()
            _isSeeding.value = false
            result.onSuccess { count ->
                _statusBanner.value = if (count > 0) {
                    "Admin Master Sync: Added $count colleagues from master roster!"
                } else {
                    "All 12 colleagues in Master Roster are already synced."
                }
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to sync master roster"
            }
        }
    }

    private fun checkAndRunAutoPilot(contacts: List<BirthdayContact>) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR).toLong()
        val todaysDueContacts = contacts.filter { contact ->
            contact.id.isNotBlank() &&
                contact.daysUntilNextBirthday() == 0 &&
                contact.autoSendEnabled &&
                contact.lastWishedYear < currentYear &&
                contact.id !in autoTriggeredIdsInSession
        }

        if (todaysDueContacts.isEmpty()) return

        viewModelScope.launch {
            for (contact in todaysDueContacts) {
                autoTriggeredIdsInSession.add(contact.id)
                dispatchAutomatedBirthdayGreeting(contact, currentYear, isSimulation = false)
            }
        }
    }

    fun triggerMasterAutoBroadcast(contact: BirthdayContact) {
        if (!ensureMasterAdminActive()) return
        viewModelScope.launch {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR).toLong()
            dispatchAutomatedBirthdayGreeting(contact, currentYear, isSimulation = true)
        }
    }

    private suspend fun dispatchAutomatedBirthdayGreeting(
        contact: BirthdayContact,
        currentYear: Long,
        isSimulation: Boolean
    ) {
        val messageText = contact.autoMessageTemplate.ifBlank {
            "Wishing you a very Happy Birthday, ${contact.name}! Have a fantastic year ahead!"
        }
        val senderLabel = if (contact.notifyAllUsers) {
            "Admin Master Auto-Pilot • All-User Intimation"
        } else {
            "Admin Master Auto-Pilot • Automated Wish"
        }
        val post = GreetingPost(
            userId = currentUserId,
            birthdayId = contact.id.ifBlank { "bday_manual" },
            recipientName = contact.name,
            senderName = senderLabel,
            message = messageText,
            cardTheme = CardThemeOption.CONFETTI_GOLD.key,
            deliveryChannel = "auto_broadcast",
            likesCount = 1L
        )
        val createRes = repository.createGreeting(post)
        if (createRes.isSuccess) {
            if (contact.id.isNotBlank() && contact.createdAt != null) {
                repository.updateBirthday(contact.copy(lastWishedYear = currentYear))
            }
            _celebrationEvent.value = CelebrationBannerEvent(
                celebrantName = contact.name,
                message = messageText,
                isAutomated = !isSimulation
            )
            _statusBanner.value = if (contact.notifyAllUsers) {
                "Master Auto-Wish sent to ${contact.name} & intimated all installed app users!"
            } else {
                "Master Automated birthday message sent to ${contact.name}!"
            }
        } else {
            _statusBanner.value = createRes.exceptionOrNull()?.localizedMessage
                ?: "Failed to send automated greeting"
        }
    }

    fun addOrUpdateBirthday(
        existing: BirthdayContact?,
        name: String,
        birthDay: Int,
        birthMonth: Int,
        birthYear: Int,
        relationship: String,
        phone: String,
        autoMessageTemplate: String,
        autoSendEnabled: Boolean,
        notifyAllUsers: Boolean
    ) {
        if (!ensureMasterAdminActive()) return
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            _statusBanner.value = "Please enter a name"
            return
        }
        val safeDay = birthDay.coerceIn(1, 31)
        val safeMonth = birthMonth.coerceIn(1, 12)
        val safeYear = birthYear.coerceIn(1900, 2100)
        val formattedDate = "%02d-%02d-%04d".format(safeDay, safeMonth, safeYear)
        val monthName = PreloadedRoster.monthNameFor(safeMonth)
        val defaultTemplate = autoMessageTemplate.trim().ifBlank {
            "Wishing you a very Happy Birthday, $trimmedName! May your special day bring joy and celebration!"
        }

        viewModelScope.launch {
            if (existing == null || existing.id.isBlank()) {
                val newContact = BirthdayContact(
                    userId = currentUserId,
                    name = trimmedName,
                    birthDate = formattedDate,
                    birthDay = safeDay.toLong(),
                    birthMonth = safeMonth.toLong(),
                    birthYear = safeYear.toLong(),
                    monthName = monthName,
                    relationship = relationship.trim().ifBlank { "Colleague" },
                    phone = phone.trim().take(30),
                    autoMessageTemplate = defaultTemplate.take(500),
                    autoSendEnabled = autoSendEnabled,
                    notifyAllUsers = notifyAllUsers,
                    lastWishedYear = 0L
                )
                repository.createBirthday(newContact)
                    .onSuccess { _statusBanner.value = "Master Record Added: $trimmedName ($formattedDate)" }
                    .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to add birthday" }
            } else {
                val updated = existing.copy(
                    name = trimmedName,
                    birthDate = formattedDate,
                    birthDay = safeDay.toLong(),
                    birthMonth = safeMonth.toLong(),
                    birthYear = safeYear.toLong(),
                    monthName = monthName,
                    relationship = relationship.trim().ifBlank { "Colleague" },
                    phone = phone.trim().take(30),
                    autoMessageTemplate = defaultTemplate.take(500),
                    autoSendEnabled = autoSendEnabled,
                    notifyAllUsers = notifyAllUsers
                )
                repository.updateBirthday(updated)
                    .onSuccess { _statusBanner.value = "Master Record Updated: ${updated.name}" }
                    .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to update birthday" }
            }
        }
    }

    fun toggleAutoSend(contact: BirthdayContact, enabled: Boolean) {
        if (!ensureMasterAdminActive()) return
        if (contact.id.isBlank() || contact.createdAt == null) return
        viewModelScope.launch {
            repository.updateBirthday(contact.copy(autoSendEnabled = enabled))
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to update master setting" }
        }
    }

    fun toggleNotifyAllUsers(contact: BirthdayContact, enabled: Boolean) {
        if (!ensureMasterAdminActive()) return
        if (contact.id.isBlank() || contact.createdAt == null) return
        viewModelScope.launch {
            repository.updateBirthday(contact.copy(notifyAllUsers = enabled))
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to update master setting" }
        }
    }

    fun deleteBirthday(contact: BirthdayContact) {
        if (!ensureMasterAdminActive()) return
        if (contact.id.isBlank()) return
        viewModelScope.launch {
            repository.deleteBirthday(contact.id)
                .onSuccess { _statusBanner.value = "Master Record Deleted: ${contact.name}" }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to delete birthday" }
        }
    }

    // Allowed for ALL Users (Wish & View)
    fun postPersonalizedGreeting(
        contact: BirthdayContact,
        senderName: String,
        message: String,
        cardTheme: CardThemeOption,
        deliveryChannel: String = "personalized_wall"
    ) {
        val cleanMsg = message.trim()
        if (cleanMsg.isEmpty()) {
            _statusBanner.value = "Please write a personalized birthday wish"
            return
        }
        viewModelScope.launch {
            val post = GreetingPost(
                userId = currentUserId,
                birthdayId = contact.id.ifBlank { "bday_custom" },
                recipientName = contact.name,
                senderName = senderName.trim().ifBlank { currentUserName },
                message = cleanMsg.take(500),
                cardTheme = cardTheme.key,
                deliveryChannel = deliveryChannel,
                likesCount = 1L
            )
            repository.createGreeting(post)
                .onSuccess {
                    _statusBanner.value = "Birthday wish posted for ${contact.name}!"
                }
                .onFailure {
                    _statusBanner.value = it.localizedMessage ?: "Failed to post wish"
                }
        }
    }

    // Allowed for ALL Users (Wish & View)
    fun cheerGreeting(greeting: GreetingPost) {
        if (greeting.id.isBlank() || greeting.createdAt == null) return
        viewModelScope.launch {
            repository.cheerGreeting(greeting)
        }
    }

    // Master Moderation: Restricted to Admin Control
    fun deleteGreeting(greeting: GreetingPost) {
        if (!ensureMasterAdminActive()) return
        if (greeting.id.isBlank()) return
        viewModelScope.launch {
            repository.deleteGreeting(greeting.id)
                .onSuccess { _statusBanner.value = "Admin Moderation: Removed greeting card" }
        }
    }

    companion object {
        private const val TAG = "BirthdayVM"
        private const val STOP_TIMEOUT_MILLIS = 5000L
        const val MASTER_ADMIN_EMAIL = "tddas111@gmail.com"
        const val DEFAULT_ADMIN_PIN = "1234"
    }
}
