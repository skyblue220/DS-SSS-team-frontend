package com.sss.healthcare

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val medName = intent.getStringExtra("medication_name") ?: "지정된 약"
        val channelId = "medication_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // 💡 [경고 해결] 현재 프로젝트의 minSdk가 26(오레오) 이상이므로,
        // 버전 체크(SDK_INT >= 26) 없이 알림 채널을 바로 만듭니다.
        val channel = NotificationChannel(
            channelId, "복약 알림",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        // 💡 [경고 해결] 문자열 템플릿 안에서 쓸데없는 중괄호("${medName}" -> "$medName")를 제거했습니다.
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("💊 $medName 복용 시간입니다!")
            .setContentText("설정하신 복약 시간이 되었습니다. 잊지 말고 복용하세요.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // 💡 [해결책 2] 버전에 따라 분기하지 않고 '무조건' 권한 검사를 하도록 플랫하게 짰습니다.
        // ContextCompat을 쓰면 낮은 버전의 안드로이드에서도 에러 없이 안전하게 실행됩니다.
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(medName.hashCode(), notification)
        }
    }
}