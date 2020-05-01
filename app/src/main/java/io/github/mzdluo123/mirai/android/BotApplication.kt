package io.github.mzdluo123.mirai.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log

class BotApplication : Application() {
    companion object{
        const val SERVICE_NOTIFICATION = "service"
        const val CAPTCHA_NOTIFICATION = "captcha"
        lateinit var context:Context
        private set
    }
    override fun onCreate() {
        super.onCreate()
        context = this
        // 设置工作目录为内部存储
        Log.d("app", getExternalFilesDirs("external")[0].absolutePath)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = "bot通知"
            val descriptionText = "接受bot的信息"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(SERVICE_NOTIFICATION, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val captchaChannel = NotificationChannel(CAPTCHA_NOTIFICATION,"需输入验证码",NotificationManager.IMPORTANCE_HIGH)

            notificationManager.createNotificationChannel(mChannel)
            notificationManager.createNotificationChannel(captchaChannel)
        }

    }
}