package io.github.mzdluo123.mirai.android.miraiconsole

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.mzdluo123.mirai.android.BotApplication
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.activity.CaptchaActivity
import io.github.mzdluo123.mirai.android.activity.UnsafeLoginActivity
import kotlinx.coroutines.CompletableDeferred
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.utils.LoginSolver


class AndroidLoginSolver(private val context: Context) : LoginSolver() {
    lateinit var verificationResult: CompletableDeferred<String>
    lateinit var captchaData: ByteArray
    lateinit var url: String

    companion object {
        const val CAPTCHA_NOTIFICATION_ID = 2
    }

    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? {
        MiraiConsole.frontEnd.pushLog(0L,"本次登录需要输入验证码，请在通知栏点击通知来输入")
        verificationResult = CompletableDeferred()
        captchaData = data
        val notifyIntent = Intent(context, CaptchaActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val notifyPendingIntent = PendingIntent.getActivity(
            context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder =
            NotificationCompat.Builder(
                context,
                BotApplication.CAPTCHA_NOTIFICATION
            )
                .setContentIntent(notifyPendingIntent)
                .setAutoCancel(false)
                //禁止滑动删除
                .setOngoing(true)
                //右上角的时间显示
                .setShowWhen(true)
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.ic_info_black_24dp)
                .setContentTitle("本次登录需要验证码")
                .setContentText("点击这里输入验证码")
        NotificationManagerCompat.from(context).apply {
            notify(CAPTCHA_NOTIFICATION_ID, builder.build())
        }
        return verificationResult.await()
    }

    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? {
        verificationResult = CompletableDeferred()
        this.url = url
        sendVerifyNotification()
        return verificationResult.await()
    }

    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? {
        verificationResult = CompletableDeferred()
        this.url = url
        sendVerifyNotification()
        return verificationResult.await()
    }

    private fun sendVerifyNotification() {
        MiraiConsole.frontEnd.pushLog(0L,"本次登录需要进行验证，请在通知栏点击通知进行验证")

        val notifyIntent = Intent(context, UnsafeLoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val notifyPendingIntent = PendingIntent.getActivity(
            context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder =
            NotificationCompat.Builder(
                context,
                BotApplication.CAPTCHA_NOTIFICATION
            )
                .setContentIntent(notifyPendingIntent)
                .setAutoCancel(false)
                //禁止滑动删除
                .setOngoing(true)
                //右上角的时间显示
                .setShowWhen(true)
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.ic_info_black_24dp)
                .setContentTitle("本次登录需要进行登录验证")
                .setContentText("点击这里开始验证")
        NotificationManagerCompat.from(context).apply {
            notify(CAPTCHA_NOTIFICATION_ID, builder.build())
        }
    }

}

