package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.CardThemeOption
import com.example.data.model.GreetingPost
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun NextCelebrationSpotlightCard(
    contact: BirthdayContact,
    totalBirthdaysCount: Int,
    autoEnabledCount: Int,
    isAdminMode: Boolean,
    onCelebrateNow: (BirthdayContact) -> Unit,
    onComposeCard: (BirthdayContact) -> Unit,
    onShareSms: (BirthdayContact) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysLeft = contact.daysUntilNextBirthday()
    val turningAge = contact.turningAge()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("spotlight_hero_card"),
        shape = RoundedCornerShape(28.dp),
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
                drawCircle(
                    color = Color(0x1AFF8A75),
                    radius = size.width * 0.25f,
                    center = Offset(size.width * 0.1f, size.height * 0.85f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
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
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = if (daysLeft == 0) Color(0xFF2C0B06) else Color(0xFFFFCA58),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (daysLeft == 0) {
                                    "TODAY'S BIRTHDAY SPOTLIGHT"
                                } else {
                                    "UP NEXT • IN $daysLeft DAYS"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (daysLeft == 0) Color(0xFF2C0B06) else Color(0xFFFFDEA1)
                            )
                        }
                    }

                    Text(
                        text = "$autoEnabledCount/$totalBirthdaysCount Auto-Wishes On",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD6C2E0)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${contact.monthName} ${contact.birthDay} (${contact.birthDate}) • Turning $turningAge",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFFDEA1)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Campaign,
                                contentDescription = null,
                                tint = Color(0xFFFF8A75),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (contact.notifyAllUsers) {
                                    "Master Admin intimates all installed users"
                                } else {
                                    "Master birthday reminder active"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE5D4EE)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x26FFFFFF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"${contact.autoMessageTemplate}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF6EDF8),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onComposeCard(contact) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFCA58),
                            contentColor = Color(0xFF3E2700)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("spotlight_send_card_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Send Wish Card",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }

                    if (isAdminMode) {
                        FilledTonalButton(
                            onClick = { onCelebrateNow(contact) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("spotlight_celebrate_now_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Master Broadcast",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(
                        onClick = { onShareSms(contact) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x33FFFFFF))
                            .testTag("spotlight_share_sms_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Sms,
                            contentDescription = "Send SMS or WhatsApp Wish",
                            tint = Color.White
                        )
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
    onComposeCard: (BirthdayContact) -> Unit,
    onShareSms: (BirthdayContact) -> Unit,
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
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
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

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "DOB: ${contact.birthDate} • Turning $turningAge • ${contact.relationship}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (contact.autoSendEnabled) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Master Auto-Message On",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (contact.notifyAllUsers) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Campaign,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "All-User Intimation",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Actions (Wish & View) vs Admin Control Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = { onComposeCard(contact) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("card_button_${contact.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Cake,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Wish Card", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = { onShareSms(contact) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("sms_button_${contact.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Wish", style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (isAdminMode) {
                    Row {
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
}

@Composable
fun GreetingWallCard(
    greeting: GreetingPost,
    isAdminMode: Boolean,
    onCheer: (GreetingPost) -> Unit,
    onShareExternal: (GreetingPost) -> Unit,
    onAdminDelete: (GreetingPost) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientColors = when (greeting.cardTheme) {
        "midnight_sparkle" -> listOf(Color(0xFF1A237E), Color(0xFF311B92))
        "rose_festive" -> listOf(Color(0xFF880E4F), Color(0xFF4A148C))
        "emerald_glow" -> listOf(Color(0xFF004D40), Color(0xFF1B5E20))
        else -> listOf(Color(0xFF4A2508), Color(0xFF2E1538))
    }

    val isBroadcast = greeting.deliveryChannel == "auto_broadcast"
    val dateFormatter = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val timestampText = greeting.createdAt?.toDate()?.let { dateFormatter.format(it) } ?: "Just now"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("greeting_wall_card_${greeting.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .border(
                    width = 1.dp,
                    color = Color(0x33FFCA58),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isBroadcast) Color(0x33FF8A75) else Color(0x33FFCA58)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isBroadcast) Icons.Filled.Campaign else Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFCA58),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBroadcast) {
                                    "MASTER ADMIN • ALL-USER INTIMATION"
                                } else {
                                    "USER BIRTHDAY WISH CARD"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFDEA1),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = timestampText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD6C2E0)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "For ${greeting.recipientName} 🎂",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "\"${greeting.message}\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFF6EDF8)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "From ${greeting.senderName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFFCA58)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { onCheer(greeting) },
                        shape = CircleShape,
                        modifier = Modifier.testTag("cheer_greeting_button_${greeting.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Celebrate cheer",
                            tint = Color(0xFFFF8A75),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Wish & Cheer • ${greeting.likesCount}")
                    }

                    Row {
                        IconButton(
                            onClick = { onShareExternal(greeting) },
                            modifier = Modifier.testTag("share_greeting_button_${greeting.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share greeting card",
                                tint = Color.White
                            )
                        }
                        if (isAdminMode) {
                            IconButton(
                                onClick = { onAdminDelete(greeting) },
                                modifier = Modifier.testTag("delete_greeting_button_${greeting.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Admin Remove greeting card",
                                    tint = Color(0xFFFF8A75)
                                )
                            }
                        }
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
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (for SMS/WhatsApp)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("birthday_phone_input")
                )

                OutlinedTextField(
                    value = template,
                    onValueChange = { template = it },
                    label = { Text("Master Automated Birthday Message") },
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
                            "Broadcasts birthday alert to all users on the Wall",
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

@Composable
fun ComposeGreetingDialog(
    contacts: List<BirthdayContact>,
    initialContact: BirthdayContact?,
    defaultSenderName: String,
    onDismiss: () -> Unit,
    onPostAndShare: (
        contact: BirthdayContact,
        senderName: String,
        message: String,
        theme: CardThemeOption,
        alsoShareExternal: Boolean
    ) -> Unit
) {
    val safeContacts = if (contacts.isEmpty() && initialContact != null) {
        listOf(initialContact)
    } else {
        contacts
    }
    var selectedIndex by remember {
        val idx = initialContact?.let { target ->
            safeContacts.indexOfFirst { it.id == target.id || it.name == target.name }
        } ?: 0
        mutableIntStateOf(idx.coerceAtLeast(0))
    }
    val activeContact = safeContacts.getOrElse(selectedIndex) {
        initialContact ?: BirthdayContact(name = "Friend")
    }

    var senderName by remember { mutableStateOf(defaultSenderName) }
    var message by remember(activeContact.id) {
        mutableStateOf(
            activeContact.autoMessageTemplate.ifBlank {
                "Wishing you a very Happy Birthday, ${activeContact.name}! Have a joyful celebration!"
            }
        )
    }
    var selectedTheme by remember { mutableStateOf(CardThemeOption.CONFETTI_GOLD) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Send Birthday Wish",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Celebrating: ${activeContact.name} (${activeContact.monthName} ${activeContact.birthDay})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                if (safeContacts.size > 1 && initialContact == null) {
                    Text(
                        text = "Select celebrant:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        safeContacts.take(3).forEachIndexed { idx, person ->
                            FilterChip(
                                selected = selectedIndex == idx,
                                onClick = { selectedIndex = idx },
                                label = {
                                    Text(
                                        person.name.substringBefore(" "),
                                        maxLines = 1
                                    )
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = senderName,
                    onValueChange = { senderName = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("greeting_sender_input")
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Your Birthday Wish") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("greeting_message_input")
                )

                Text(
                    text = "Greeting Card Style",
                    style = MaterialTheme.typography.labelLarge
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CardThemeOption.entries.forEach { option ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (selectedTheme == option) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTheme = option }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = option.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                FilterChip(
                                    selected = selectedTheme == option,
                                    onClick = { selectedTheme = option },
                                    label = { Text(if (selectedTheme == option) "Selected" else "Pick") },
                                    colors = FilterChipDefaults.filterChipColors()
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        onPostAndShare(activeContact, senderName, message, selectedTheme, true)
                    },
                    modifier = Modifier.testTag("post_and_share_external_button")
                ) {
                    Text("Wish + Share")
                }
                Button(
                    onClick = {
                        onPostAndShare(activeContact, senderName, message, selectedTheme, false)
                    },
                    modifier = Modifier.testTag("post_greeting_wall_button")
                ) {
                    Text("Post Wish")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
