package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Calendar

@IgnoreExtraProperties
data class BirthdayContact(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val birthDate: String = "01-01-1990",
    val birthDay: Long = 1L,
    val birthMonth: Long = 1L,
    val birthYear: Long = 1990L,
    val monthName: String = "January",
    val relationship: String = "Colleague",
    val phone: String = "",
    val autoMessageTemplate: String = "",
    val autoSendEnabled: Boolean = true,
    val notifyAllUsers: Boolean = true,
    val lastWishedYear: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun daysUntilNextBirthday(today: Calendar = Calendar.getInstance()): Int {
        val currentMonth = today.get(Calendar.MONTH) + 1
        val currentDay = today.get(Calendar.DAY_OF_MONTH)
        val bMonth = birthMonth.toInt().coerceIn(1, 12)
        val bDay = birthDay.toInt().coerceIn(1, 31)

        if (bMonth == currentMonth && bDay == currentDay) {
            return 0
        }

        val target = (today.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.MONTH, bMonth - 1)
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, bDay.coerceAtMost(maxDay))
        }
        val start = (today.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.before(start)) {
            target.add(Calendar.YEAR, 1)
            val maxDay = target.getActualMaximum(Calendar.DAY_OF_MONTH)
            target.set(Calendar.DAY_OF_MONTH, bDay.coerceAtMost(maxDay))
        }
        val diffMillis = target.timeInMillis - start.timeInMillis
        return (diffMillis / (1000L * 60L * 60L * 24L)).toInt().coerceAtLeast(0)
    }

    fun turningAge(today: Calendar = Calendar.getInstance()): Int {
        val currentYear = today.get(Calendar.YEAR)
        val currentMonth = today.get(Calendar.MONTH) + 1
        val currentDay = today.get(Calendar.DAY_OF_MONTH)
        val bMonth = birthMonth.toInt()
        val bDay = birthDay.toInt()
        val nextBirthdayYear = if (bMonth < currentMonth || (bMonth == currentMonth && bDay < currentDay)) {
            currentYear + 1
        } else {
            currentYear
        }
        return (nextBirthdayYear - birthYear.toInt()).coerceAtLeast(1)
    }

    fun initials(): String {
        val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> "${parts.first().first().uppercaseChar()}${parts.last().first().uppercaseChar()}"
            parts.size == 1 -> parts.first().take(2).uppercase()
            else -> "BP"
        }
    }
}

@IgnoreExtraProperties
data class GreetingPost(
    @DocumentId val id: String = "",
    val userId: String = "",
    val birthdayId: String = "",
    val recipientName: String = "",
    val senderName: String = "",
    val message: String = "",
    val cardTheme: String = "confetti_gold",
    val deliveryChannel: String = "personalized_wall",
    val likesCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class CardThemeOption(
    val key: String,
    val label: String,
    val subtitle: String
) {
    CONFETTI_GOLD("confetti_gold", "Champagne Gold", "Warm golden sparkles & royal plum"),
    MIDNIGHT_SPARKLE("midnight_sparkle", "Midnight Starlight", "Deep indigo & stardust glow"),
    ROSE_FESTIVE("rose_festive", "Coral & Rose", "Vibrant sunset celebration"),
    EMERALD_GLOW("emerald_glow", "Emerald Jubilee", "Rich botanical gold & jade");

    companion object {
        fun fromKey(key: String): CardThemeOption =
            entries.find { it.key == key } ?: CONFETTI_GOLD
    }
}

object PreloadedRoster {
    val MONTHS = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    fun monthNameFor(monthNumber: Int): String =
        MONTHS.getOrElse((monthNumber - 1).coerceIn(0, 11)) { "January" }

    fun defaultEmployeeRoster(userId: String): List<BirthdayContact> = listOf(
        BirthdayContact(
            userId = userId,
            name = "Kamalesh Vishwakarma",
            birthDate = "01-01-1980",
            birthDay = 1L,
            birthMonth = 1L,
            birthYear = 1980L,
            monthName = "January",
            relationship = "Colleague",
            phone = "+91 98301 10001",
            autoMessageTemplate = "Wishing you a joyful Happy Birthday, Kamalesh! May this New Year and new chapter bring you health, happiness, and continued success!",
            autoSendEnabled = true,
            notifyAllUsers = true
        ),
        BirthdayContact(
            userId = userId,
            name = "Sayan Banerjee",
            birthDate = "07-01-1995",
            birthDay = 7L,
            birthMonth = 1L,
            birthYear = 1995L,
            monthName = "January",
            relationship = "Colleague",
            phone = "+91 98301 10007",
            autoMessageTemplate = "Happy Birthday Sayan! Wishing you an inspiring year ahead filled with great milestones and celebration!",
            autoSendEnabled = true,
            notifyAllUsers = true
        ),
        BirthdayContact(
            userId = userId,
            name = "Akhilesh Kumar Pandey",
            birthDate = "01-02-1983",
            birthDay = 1L,
            birthMonth = 2L,
            birthYear = 1983L,
            monthName = "February",
            relationship = "Colleague",
            phone = "+91 98301 20001",
            autoMessageTemplate = "Warmest Birthday Wishes to you, Akhilesh! Thank you for being such a wonderful pillar of our team!",
            autoSendEnabled = true,
            notifyAllUsers = true
        ),
        BirthdayContact(
            userId = userId,
            name = "Bipin Pandey",
            birthDate = "02-02-1968",
            birthDay = 2L,
            birthMonth = 2L,
            birthYear = 1968L,
            monthName = "February",
            relationship = "Colleague",
            phone = "+91 98301 20002",
            autoMessageTemplate = "Wishing you a very Happy Birthday, Bipin Ji! May the year ahead bring you wonderful health, peace, and prosperity.",
            autoSendEnabled = true,
            notifyAllUsers = true
        ),
        BirthdayContact(
            userId = userId,
            name = "Sanju Ghosh",
            birthDate = "04-02-1998",
            birthDay = 4L,
            birthMonth = 2L,
            birthYear = 1998L,
            monthName = "February",
            relationship = "Colleague",
            phone = "+91 98301 20004",
            autoMessageTemplate = "Happy Birthday Sanju! Hope your special day is packed with laughter, cake, and great memories!",
            autoSendEnabled = true,
            notifyAllUsers = true
        ),
        BirthdayContact(
            userId = userId,
            name = "Sourav Bairagya",
            birthDate = "17-02-1996",
            birthDay = 17L,
            birthMonth = 2L,
            birthYear = 1996L,
            monthName = "February",
            relationship = "Colleague",
            phone = "+91 98301 20017",
            autoMessageTemplate = "Many Happy Returns of the Day, Sourav! Wishing you boundless energy and success in everything you do!",
            autoSendEnabled = true,
            notifyAllUsers = true
        ),
        BirthdayContact(
            userId = userId,
            name = "Milan Kumar Ghosh",
            birthDate = "07-04-1989",
            birthDay = 7L,
            birthMonth = 4L,
            birthYear = 1989L,
            monthName = "April",
            relationship = "Colleague",
            phone = "+91 98301 40007",
            autoMessageTemplate = "Happy Birthday Milan! Wishing you a fantastic celebration and a rewarding year ahead!",
            autoSendEnabled = true,
            notifyAllUsers = true
        ),
        BirthdayContact(
            userId = userId,
            name = "Gopinath Bauri",
            birthDate = "10-04-1987",
            birthDay = 10L,
            birthMonth = 4L,
            birthYear = 1987L,
            monthName = "April",
            relationship = "Colleague",
            phone = "+91 98301 40010",
            autoMessageTemplate = "Wishing you a very Happy Birthday, Gopinath! Have a wonderful day surrounded by friends and family!",
            autoSendEnabled = true,
            notifyAllUsers = true
        ),
        BirthdayContact(
            userId = userId,
            name = "Uday Datta",
            birthDate = "15-04-1994",
            birthDay = 15L,
            birthMonth = 4L,
            birthYear = 1994L,
            monthName = "April",
            relationship = "Colleague",
            phone = "+91 98301 40015",
            autoMessageTemplate = "Happy Birthday Uday! May this year bring exciting new adventures and achievements your way!",
            autoSendEnabled = true,
            notifyAllUsers = true
        ),
        BirthdayContact(
            userId = userId,
            name = "Sudarshan Parida",
            birthDate = "16-04-1964",
            birthDay = 16L,
            birthMonth = 4L,
            birthYear = 1964L,
            monthName = "April",
            relationship = "Colleague",
            phone = "+91 98301 40016",
            autoMessageTemplate = "Respectful and warmest Birthday Wishes to you, Sudarshan Sir! Wishing you wonderful health and happiness always!",
            autoSendEnabled = true,
            notifyAllUsers = true
        ),
        BirthdayContact(
            userId = userId,
            name = "Vinay Kurolya",
            birthDate = "24-04-1994",
            birthDay = 24L,
            birthMonth = 4L,
            birthYear = 1994L,
            monthName = "April",
            relationship = "Colleague",
            phone = "+91 98301 40024",
            autoMessageTemplate = "Happy Birthday Vinay! Have an awesome celebration and a brilliant year ahead!",
            autoSendEnabled = true,
            notifyAllUsers = true
        ),
        BirthdayContact(
            userId = userId,
            name = "Alip Lodh",
            birthDate = "01-05-1993",
            birthDay = 1L,
            birthMonth = 5L,
            birthYear = 1993L,
            monthName = "May",
            relationship = "Colleague",
            phone = "+91 98301 50001",
            autoMessageTemplate = "Wishing you a very Happy Birthday, Alip! May your special day be filled with joy and celebration!",
            autoSendEnabled = true,
            notifyAllUsers = true
        )
    )
}
