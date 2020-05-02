package io.github.mzdluo123.mirai.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Process
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class BotApplication : Application() {
    companion object {
        const val SERVICE_NOTIFICATION = "service"
        const val CAPTCHA_NOTIFICATION = "captcha"
        lateinit var context: Context
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
        if (processName?.isEmpty() == false && processName.equals(packageName)) {

            // Create the NotificationChannel
            val name = "bot通知"
            val descriptionText = "接受bot的信息"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(SERVICE_NOTIFICATION, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val captchaChannel = NotificationChannel(
                CAPTCHA_NOTIFICATION, "需输入验证码",
                NotificationManager.IMPORTANCE_HIGH
            )
            captchaChannel.description = "当你需要输入验证码才能登录时，你将会收到这条通知"

            notificationManager.createNotificationChannel(mChannel)
            notificationManager.createNotificationChannel(captchaChannel)

        }
    }

    private fun myGetProcessName(): String? {
        return try {
            val file = File("/proc/" + Process.myPid() + "/" + "cmdline")
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