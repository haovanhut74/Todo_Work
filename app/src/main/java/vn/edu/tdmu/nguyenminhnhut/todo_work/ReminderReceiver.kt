package vn.edu.tdmu.nguyenminhnhut.todo_work

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            // Kiểm tra quyền trước khi gửi thông báo
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {

                val notificationBuilder = NotificationCompat.Builder(context, "todo_channel")
                    .setSmallIcon(R.drawable.baseline_circle_notifications_24)
                    .setContentTitle("Task Reminder")
                    .setContentText("It's time to complete your task!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(1, notificationBuilder.build())
            }
        }
    }
}