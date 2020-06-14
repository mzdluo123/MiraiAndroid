package io.github.mzdluo123.mirai.android

import android.app.ActivityManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.Process
import io.github.mzdluo123.mirai.android.crash.MiraiAndroidReportSenderFactory
import io.github.mzdluo123.mirai.android.receiver.PushMsgReceiver
import org.acra.ACRA
import org.acra.config.CoreConfigurationBuilder
import org.acra.config.ToastConfigurationBuilder
import org.acra.data.StringFormat

class BotApplication : Application() {
    companion object {
        const val SERVICE_NOTIFICATION = "service"
        const val CAPTCHA_NOTIFICATION = "captcha"
        const val OFFLINE_NOTIFICATION = "offline"
        lateinit var context: BotApplication
            private set

        internal fun getSettingPreference(): SharedPreferences {
            return context.getSharedPreferences("setting", Context.MODE_PRIVATE)
        }

    }


    override fun onCreate() {
        super.onCreate()
        context = this
        val processName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            getProcessName()
        else
            myGetProcessName()

        // 防止服务进程多次初始化
        if (processName?.isEmpty() == false && processName == packageName) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // 只在8.0系统上注册通知通道，防止程序崩溃
                val statusChannel = NotificationChannel(
                    SERVICE_NOTIFICATION, "状态通知",
                    NotificationManager.IMPORTANCE_MIN
                )

                statusChannel.description = "Mirai正在运行的通知"

                val captchaChannel = NotificationChannel(
                    CAPTCHA_NOTIFICATION, "验证码通知",
                    NotificationManager.IMPORTANCE_HIGH
                )
                captchaChannel.description = "登录需要输入验证码时的通知"

                val offlineChannel = NotificationChannel(
                    OFFLINE_NOTIFICATION, "离线通知",
                    NotificationManager.IMPORTANCE_HIGH
                )
                offlineChannel.description = "Mirai因各种原因离线的通知"

                notificationManager.createNotificationChannel(statusChannel)
                notificationManager.createNotificationChannel(captchaChannel)
                notificationManager.createNotificationChannel(offlineChannel)
            }
        }
    }


    //崩溃事件注册
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        ACRA.init(this, CoreConfigurationBuilder(this).apply {
            setBuildConfigClass(BuildConfig::class.java)
                .setReportFormat(StringFormat.JSON)
            setReportSenderFactoryClasses(MiraiAndroidReportSenderFactory::class.java)
//            getPluginConfigurationBuilder(ToastConfigurationBuilder::class.java)
//                .setResText(R.string.acra_toast_text)
//                .setEnabled(true)
            //不知道为什么开启的时候总是显示这个，先暂时禁用
        })
    }

    private fun myGetProcessName(): String? {
        val pid = Process.myPid()
        for (appProcess in (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).runningAppProcesses) {
            if (appProcess.pid == pid) {
                return appProcess.processName
            }
        }
        return null
    }

    internal fun startBotService() {
        val account = getSharedPreferences("account", Context.MODE_PRIVATE)
        this.startService(Intent(this, BotService::class.java).apply {
            putExtra("action", BotService.START_SERVICE)
            putExtra("qq", account.getLong("qq", 0))
            putExtra("pwd", account.getString("pwd", null))
        })
    }

    internal fun stopBotService() {
        startService(Intent(this, BotService::class.java).apply {
            putExtra("action", BotService.STOP_SERVICE)
        })

    }
}