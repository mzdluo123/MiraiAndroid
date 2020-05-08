package io.github.mzdluo123.mirai.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class BotApplication : Application() {
    companion object {
        const val SERVICE_NOTIFICATION = "service"
        const val CAPTCHA_NOTIFICATION = "captcha"
        const val OFFLINE_NOTIFICATION = "offline"
        lateinit var context: BotApplication
            private set
    }


    override fun onCreate() {
        super.onCreate()
        context = this
        val processName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getProcessName()
        } else {
            myGetProcessName()
        }
        // 防止服务进程多次初始化
        if (processName?.isEmpty() == false && processName == packageName) {

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            // Create the NotificationChannel
            val mChannel = NotificationChannel(
                SERVICE_NOTIFICATION, "状态通知",
                NotificationManager.IMPORTANCE_DEFAULT)
            mChannel.description = "Mirai正在运行的通知"

            val captchaChannel = NotificationChannel(
                CAPTCHA_NOTIFICATION, "验证码通知",
                NotificationManager.IMPORTANCE_HIGH)
            captchaChannel.description = "登录需要输入验证码时的通知"

            val offlineChannel = NotificationChannel(
                OFFLINE_NOTIFICATION, "离线通知",
                NotificationManager.IMPORTANCE_HIGH)
            captchaChannel.description = "Mirai因网络原因离线的通知"

            notificationManager.createNotificationChannel(mChannel)
            notificationManager.createNotificationChannel(captchaChannel)
            notificationManager.createNotificationChannel(offlineChannel)

        }
    }

    private fun myGetProcessName(): String? {
        return try {
            val file = File("/proc/" + Process.myPid() + "/cmdline")
            val mBufferedReader = BufferedReader(FileReader(file))
            val processName: String = mBufferedReader.readLine().trim()
            mBufferedReader.close()
            processName
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}