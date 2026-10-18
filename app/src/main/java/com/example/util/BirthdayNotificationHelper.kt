package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.R

object BirthdayNotificationHelper {
    private const val CHANNEL_ID = "birthday_pulse_alerts"
    private const val CHANNEL_NAME = "Birthday Intimations & Auto-Wishes"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Automated birthday reminders and community celebration intimations"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun showBirthdayIntimationNotification(
        context: Context,
        celebrantName: String,
        message: String,
        notificationId: Int = celebrantName.hashCode()
    ) {
        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("It's $celebrantName's Birthday Today!")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch ( ignored: SecurityException) {
        }
    }

    fun sharePersonalizedGreeting(
        context: Context,
        recipientName: String,
        message: String
    ) {
        val shareText = "🎉 Happy Birthday $recipientName! 🎂\n\n$message\n\n— Shared via Birthday Pulse"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Happy Birthday $recipientName!")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        val chooser = Intent.createChooser(intent, "Share Birthday Greeting for $recipientName").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun sendDirectSmsGreeting(
        context: Context,
        phone: String,
        recipientName: String,
        message: String
    ) {
        if (phone.isBlank()) {
            sharePersonalizedGreeting(context, recipientName, message)
            return
        }
        val smsUri = Uri.parse("smsto:${Uri.encode(phone)}")
        val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(smsIntent)
        } catch (e: Exception) {
            sharePersonalizedGreeting(context, recipientName, message)
        }
    }
}
