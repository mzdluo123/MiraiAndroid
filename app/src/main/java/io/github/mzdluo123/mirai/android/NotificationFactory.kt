package io.github.mzdluo123.mirai.android

import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.TaskStackBuilder
import io.github.mzdluo123.mirai.android.activity.MainActivity
import io.github.mzdluo123.mirai.android.miraiconsole.AndroidLoginSolver
import io.github.mzdluo123.mirai.android.service.BotService

object NotificationFactory {

    const val SERVICE_NOTIFICATION = "service"
    const val CAPTCHA_NOTIFICATION = "captcha"
    const val OFFLINE_NOTIFICATION = "offline"

    val context by lazy {
        BotApplication.context
    }


    internal fun initNotification() {
        val notificationManager = NotificationManagerCompat.from(context)

        val statusChannel =
            NotificationChannelCompat.Builder(SERVICE_NOTIFICATION, NotificationManagerCompat.IMPORTANCE_MIN)
                .setName("状态通知")
                .setDescription("Mirai正在运行的通知")

        val captchaChannel =
            NotificationChannelCompat.Builder(CAPTCHA_NOTIFICATION, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName("验证码通知")
                .setDescription("登录需要输入验证码时的通知")

        val offlineChannel =
            NotificationChannelCompat.Builder(OFFLINE_NOTIFICATION, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName("离线通知")
                .setDescription("Mirai因各种原因离线的通知")

        if (BuildConfig.DEBUG) {
            offlineChannel.setImportance(NotificationManagerCompat.IMPORTANCE_MIN)
            captchaChannel.setImportance(NotificationManagerCompat.IMPORTANCE_MIN)
        }

        notificationManager.createNotificationChannel(statusChannel.build())
        notificationManager.createNotificationChannel(captchaChannel.build())
        notificationManager.createNotificationChannel(offlineChannel.build())

    }

    internal fun dismissAllNotification() {
        NotificationManagerCompat.from(context).apply {
            cancel(BotService.OFFLINE_NOTIFICATION_ID)
            cancel(AndroidLoginSolver.CAPTCHA_NOTIFICATION_ID)

        }
    }


    internal fun statusNotification(
        content: String = "请完成登录并将软件添加到系统后台运行白名单确保能及时处理消息",
        avatar: Bitmap? = null
    ): Notification {

        return NotificationCompat.Builder(
            context,
            SERVICE_NOTIFICATION
        )
            .setSmallIcon(R.drawable.ic_extension_black_24dp)//设置状态栏的通知图标
            .setAutoCancel(false) //禁止用户点击删除按钮删除
            .setOngoing(true) //禁止滑动删除
            .setShowWhen(true) //右上角的时间显示
            .setOnlyAlertOnce(true)
            .setStyle(NotificationCompat.BigTextStyle())
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    0
                )
            )
            .setContentTitle("MiraiAndroid") //创建通知
            .setContentText(content)
            .setLargeIcon(avatar)
            .build()

    }

    internal fun offlineNotification(content: String, bigTheme: Boolean = false): Notification {

        val builder = NotificationCompat.Builder(
            context,
            OFFLINE_NOTIFICATION
        )
            .setAutoCancel(false)
            .setOngoing(false)
            .setShowWhen(true)
            .setSmallIcon(R.drawable.ic_info_black_24dp)
            .setContentTitle("Mirai离线")
            .setContentText(content)

        if (bigTheme) {
            builder.setStyle(NotificationCompat.BigTextStyle())
        }
        return builder.build()

    }

    internal fun captchaNotification(activity: Class<*>): Notification {

        val notifyIntent = Intent(context, activity)
//        val notifyPendingIntent = PendingIntent.getActivity(
//            context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
//        )
        val notifyPendingIntent = TaskStackBuilder.create(BotApplication.context)
            .addParentStack(MainActivity::class.java)
            .addNextIntent(notifyIntent)
            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)


        return NotificationCompat.Builder(
            context,
            CAPTCHA_NOTIFICATION
        )
            .setContentIntent(notifyPendingIntent)
            .setAutoCancel(false)
            //禁止滑动删除
            .setOngoing(false)
            //右上角的时间显示
            .setShowWhen(true)
            .setSmallIcon(R.drawable.ic_info_black_24dp)
            .setContentTitle("本次登录需要进行登录验证")
            .setContentText("点击这里开始验证")
            .build()
    }

}