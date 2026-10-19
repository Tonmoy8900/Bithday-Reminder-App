package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.BirthdayContact
import com.example.util.SongPlaybackState

@Composable
fun MusicalSongBannerCard(
    mailIdName: String,
    userEmail: String,
    songState: SongPlaybackState,
    onPlayForMailUser: () -> Unit,
    onStopSong: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("musical_song_banner_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = if (songState.isPlaying) {
                            listOf(Color(0xFF5A224B), Color(0xFF34154F))
                        } else {
                            listOf(Color(0xFF28153E), Color(0xFF1D102E))
                        }
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (songState.isPlaying) Color(0xFFFFCA58) else Color(0x33FFCA58),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (songState.isPlaying) {
                                        Color(0xFFFFCA58)
                                    } else {
                                        MaterialTheme.colorScheme.primaryContainer
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = if (songState.isPlaying) {
                                    Color(0xFF3E2700)
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (songState.isPlaying) {
                                    "Now Singing: Happy Birthday ${songState.celebrantName}!"
                                } else {
                                    "Birthday Song Studio • Hi, $mailIdName"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (songState.isPlaying) {
                                    songState.currentLyricLine
                                } else {
                                    "Mail ID: ${userEmail.ifBlank { mailIdName }} • Tap any person's Song button to sing!"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFFDEA1),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    if (songState.isPlaying) {
                        Button(
                            onClick = onStopSong,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF8A75),
                                contentColor = Color(0xFF2C0B06)
                            ),
                            shape = CircleShape,
                            modifier = Modifier.testTag("stop_song_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Stop,
                                contentDescription = "Stop birthday song",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop", style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        FilledTonalButton(
                            onClick = onPlayForMailUser,
                            shape = CircleShape,
                            modifier = Modifier.testTag("play_mail_user_song_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play Happy Birthday song for Mail ID name",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sing \"$mailIdName\"",
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NextCelebrationSpotlightCard(
    contact: BirthdayContact,
    isPlayingThisPerson: Boolean,
    onPlaySong: (BirthdayContact) -> Unit,
    onWishSmsByMobile: (BirthdayContact) -> Unit,
    onWishWhatsAppByMobile: (BirthdayContact) -> Unit,
    onCallMobile: (BirthdayContact) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysLeft = contact.daysUntilNextBirthday()
    val turningAge = contact.turningAge()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("spotlight_hero_card"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF3B1A5A),
                            Color(0xFF241038),
                            Color(0xFF4A203B)
                        )
                    )
                )
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color(0x22FFCA58),
                    radius = size.width * 0.35f,
                    center = Offset(size.width * 0.9f, size.height * 0.15f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (daysLeft == 0) Color(0xFFFF8A75) else Color(0x33FFCA58)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = if (daysLeft == 0) Color(0xFF2C0B06) else Color(0xFFFFCA58),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (daysLeft == 0) {
                                    "TODAY'S SPECIAL BIRTHDAY 🎉"
                                } else {
                                    "NEXT UPCOMING EVENT • IN $daysLeft DAYS"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (daysLeft == 0) Color(0xFF2C0B06) else Color(0xFFFFDEA1)
                            )
                        }
                    }

                    Text(
                        text = contact.monthName,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFFCA58),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFFCA58), Color(0xFFFF8A75))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.initials(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF2B1200),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "DOB: ${contact.birthDate} (${contact.monthName}) • Turning $turningAge",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFFDEA1)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PhoneAndroid,
                                contentDescription = null,
                                tint = Color(0xFFFF8A75),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Mobile No: ${contact.phone.ifBlank { "Not set (Add in Admin)" }}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onPlaySong(contact) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFCA58),
                            contentColor = Color(0xFF3E2700)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.15f)
                            .testTag("spotlight_play_song_button")
                    ) {
                        Icon(
                            imageVector = if (isPlayingThisPerson) Icons.Filled.Stop else Icons.Filled.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlayingThisPerson) "Stop Song" else "Sing Birthday Song",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }

                    FilledTonalButton(
                        onClick = { onWishSmsByMobile(contact) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("spotlight_wish_mobile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Sms,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Wish by Mobile",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }

                    IconButton(
                        onClick = { onWishWhatsAppByMobile(contact) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x33FFFFFF))
                            .testTag("spotlight_whatsapp_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "WhatsApp Wish by Mobile No",
                            tint = Color(0xFFFFCA58)
                        )
                    }

                    if (contact.phone.isNotBlank()) {
                        IconButton(
                            onClick = { onCallMobile(contact) },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x33FFFFFF))
                                .testTag("spotlight_call_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = "Call Mobile No",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BirthdayContactCard(
    contact: BirthdayContact,
    isAdminMode: Boolean,
    isPlayingThisPerson: Boolean,
    onPlaySong: (BirthdayContact) -> Unit,
    onWishSmsByMobile: (BirthdayContact) -> Unit,
    onWishWhatsAppByMobile: (BirthdayContact) -> Unit,
    onCallMobile: (BirthdayContact) -> Unit,
    onAdminEdit: (BirthdayContact) -> Unit,
    onAdminDelete: (BirthdayContact) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysLeft = contact.daysUntilNextBirthday()
    val turningAge = contact.turningAge()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("birthday_item_card_${contact.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (daysLeft == 0) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.initials(),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (daysLeft == 0) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = contact.monthName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "DOB: ${contact.birthDate} • Turning $turningAge",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhoneAndroid,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = contact.phone.ifBlank { "No mobile number" },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        daysLeft == 0 -> MaterialTheme.colorScheme.secondary
                        daysLeft <= 14 -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = if (daysLeft == 0) "TODAY 🎉" else "In ${daysLeft}d",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            daysLeft == 0 -> MaterialTheme.colorScheme.onSecondary
                            daysLeft <= 14 -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Clean, User-Friendly Action Row: 1. Play Song ("Happy Birthday [Name]"), 2. Wish by Mobile No
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { onPlaySong(contact) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("song_button_${contact.id}")
                ) {
                    Icon(
                        imageVector = if (isPlayingThisPerson) Icons.Filled.Stop else Icons.Filled.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPlayingThisPerson) "Stop Song" else "🎵 Sing Song",
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = { onWishSmsByMobile(contact) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("sms_button_${contact.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Sms,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Wish by Mobile",
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = { onWishWhatsAppByMobile(contact) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("wa_button_${contact.id}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "WhatsApp Wish",
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (contact.phone.isNotBlank()) {
                    IconButton(
                        onClick = { onCallMobile(contact) },
                        modifier = Modifier.testTag("call_button_${contact.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Call Mobile Number",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (isAdminMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Admin Master Actions:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { onAdminEdit(contact) },
                        modifier = Modifier.testTag("edit_button_${contact.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Master Edit birthday",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { onAdminDelete(contact) },
                        modifier = Modifier.testTag("delete_button_${contact.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Master Delete birthday",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BirthdayEditorDialog(
    initialContact: BirthdayContact?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        day: Int,
        month: Int,
        year: Int,
        relationship: String,
        phone: String,
        template: String,
        autoSend: Boolean,
        notifyAll: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialContact?.name ?: "") }
    var dayText by remember { mutableStateOf((initialContact?.birthDay ?: 1L).toString()) }
    var monthText by remember { mutableStateOf((initialContact?.birthMonth ?: 1L).toString()) }
    var yearText by remember { mutableStateOf((initialContact?.birthYear ?: 1995L).toString()) }
    var relationship by remember { mutableStateOf(initialContact?.relationship ?: "Colleague") }
    var phone by remember { mutableStateOf(initialContact?.phone ?: "") }
    var template by remember {
        mutableStateOf(
            initialContact?.autoMessageTemplate
                ?: "Wishing you a very Happy Birthday! May this special day bring joy, health, and celebration!"
        )
    }
    var autoSend by remember { mutableStateOf(initialContact?.autoSendEnabled ?: true) }
    var notifyAll by remember { mutableStateOf(initialContact?.notifyAllUsers ?: true) }

    val relationships = listOf("Colleague", "Friend", "Family", "Team Lead")

    AlertDialog(
        onDismissRequest = onDismiss,
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
                Text(
                    text = if (initialContact == null) "Master Admin: Add Birthday" else "Master Admin: Edit Record",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Employee / Friend Full Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("birthday_name_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number (e.g. +91 98301 10001)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("birthday_phone_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dayText,
                        onValueChange = { dayText = it.filter { ch -> ch.isDigit() }.take(2) },
                        label = { Text("Day (DD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("birthday_day_input")
                    )
                    OutlinedTextField(
                        value = monthText,
                        onValueChange = { monthText = it.filter { ch -> ch.isDigit() }.take(2) },
                        label = { Text("Month (MM)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("birthday_month_input")
                    )
                    OutlinedTextField(
                        value = yearText,
                        onValueChange = { yearText = it.filter { ch -> ch.isDigit() }.take(4) },
                        label = { Text("Year (YYYY)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("birthday_year_input")
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    relationships.forEach { rel ->
                        FilterChip(
                            selected = relationship.equals(rel, ignoreCase = true),
                            onClick = { relationship = rel },
                            label = { Text(rel) }
                        )
                    }
                }

                OutlinedTextField(
                    value = template,
                    onValueChange = { template = it },
                    label = { Text("Birthday Wish Message") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("birthday_template_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Master Auto-Send on Birthday", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Automatically sends wish on their special day",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoSend,
                        onCheckedChange = { autoSend = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Intimate All Installed App Users", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Notifies all installed app users on their birthday",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = notifyAll,
                        onCheckedChange = { notifyAll = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val d = dayText.toIntOrNull() ?: 1
                    val m = monthText.toIntOrNull() ?: 1
                    val y = yearText.toIntOrNull() ?: 1995
                    onSave(name, d, m, y, relationship, phone, template, autoSend, notifyAll)
                },
                modifier = Modifier.testTag("save_birthday_button")
            ) {
                Text("Save Master Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
