package io.github.mzdluo123.mirai.android.miraiconsole

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.mzdluo123.mirai.android.BotApplication
import io.github.mzdluo123.mirai.android.BotService
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.activity.MainActivity
import io.github.mzdluo123.mirai.android.script.ScriptManager
import io.github.mzdluo123.mirai.android.utils.LoopQueue
import io.github.mzdluo123.mirai.android.utils.MiraiAndroidStatus
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.utils.MiraiConsoleUI
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotReloginEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.SimpleLogger

@ExperimentalUnsignedTypes
class AndroidMiraiConsole(context: Context) : MiraiConsoleUI {
    private val logBuffer = BotApplication.getSettingPreference()
        .getString("log_buffer_preference", "300")!!.toInt()
    private val printToLogcat =
        BotApplication.getSettingPreference().getBoolean("print_to_logcat_preference", false)
    val logStorage = LoopQueue<String>(logBuffer)
    val loginSolver =
        AndroidLoginSolver(context)

    // 使用一个[60s/refreshPerMinute]的数组存放每4秒消息条数
    // 读取时增加最新一分钟，减去最老一分钟
    private val refreshPerMinute = BotApplication.getSettingPreference()
        .getString("status_refresh_count", "15")!!.toInt() // 4s
    private val msgSpeeds = IntArray(refreshPerMinute)
    private var refreshCurrentPos = 0

    companion object {
        const val TAG = "MA"
    }

    override fun createLoginSolver(): LoginSolver = loginSolver

    override fun prePushBot(identity: Long) = Unit

    override fun pushBot(bot: Bot) {
        bot.pushToScriptManager(ScriptManager.instance)
        bot.subscribeBotLifeEvent()
        bot.startRefreshNotificationJob()
    }

    override fun pushBotAdminStatus(identity: Long, admins: List<Long>) = Unit

    override fun pushLog(identity: Long, message: String) {
        logStorage.add(message)
        if (printToLogcat) {
            Log.i(TAG, message)
        }
    }

    override fun pushLog(
        priority: SimpleLogger.LogPriority,
        identityStr: String,
        identity: Long,
        message: String
    ) {
        logStorage.add("[${priority.name}] $message")
        if (printToLogcat){
            Log.i(TAG, "[${priority.name}] $message")
        }

    }

    override fun pushVersion(consoleVersion: String, consoleBuild: String, coreVersion: String) {
        logStorage.add(MiraiAndroidStatus.recentStatus().format())
    }

    override suspend fun requestInput(hint: String): String = ""

    private fun Bot.startRefreshNotificationJob() {
        subscribeMessages { always { msgSpeeds[refreshCurrentPos] += 1 } }
        launch {
            val avatar = downloadAvatar()  // 获取通知展示用的头像
            //点击进入主页
            val notifyIntent = Intent(BotApplication.context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val notifyPendingIntent = PendingIntent.getActivity(
                BotApplication.context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
            var msgSpeed = 0
            val startTime = System.currentTimeMillis()
            while (isActive) {
                /*
                * 总速度+=最新速度 [0] [1] ... [14]
                * 总速度-=最老速度 [1] [2] ... [0]
                */
                msgSpeed += msgSpeeds[refreshCurrentPos]
                if (refreshCurrentPos != refreshPerMinute - 1) {
                    refreshCurrentPos += 1
                } else {
                    refreshCurrentPos = 0
                }
                msgSpeed -= msgSpeeds[refreshCurrentPos]
                msgSpeeds[refreshCurrentPos] = 0
                val notification = NotificationCompat.Builder(
                    BotApplication.context,
                    BotApplication.SERVICE_NOTIFICATION
                )
                    //设置状态栏的通知图标
                    .setSmallIcon(R.drawable.ic_extension_black_24dp)
                    //禁止用户点击删除按钮删除
                    .setAutoCancel(false)
                    //禁止滑动删除
                    .setOngoing(true)
                    //右上角的时间显示
                    .setShowWhen(true).setWhen(startTime)
                    .setOnlyAlertOnce(true)
                    .setLargeIcon(avatar).setContentIntent(notifyPendingIntent)
                    .setContentTitle("MiraiAndroid正在运行")
                    .setContentText("消息速度 ${msgSpeed}/min").build()
                NotificationManagerCompat.from(BotApplication.context).apply {
                    notify(BotService.NOTIFICATION_ID, notification)
                }
                delay(60L / refreshPerMinute * 1000)
            }
        }
    }

    private suspend fun Bot.downloadAvatar(): Bitmap =
        try {
            pushLog(0L, "[INFO] 正在加载头像....")
            HttpClient().get<ByteArray>(selfQQ.avatarUrl).let { avatarData ->
                BitmapFactory.decodeByteArray(avatarData, 0, avatarData.size)
            }
        } catch (e: Exception) {
            delay(1000)
            downloadAvatar()
        }

    private fun Bot.subscribeBotLifeEvent() {
        subscribeAlways<BotOfflineEvent>(priority = Listener.EventPriority.HIGHEST) {
            // 防止一闪而过的掉线
            delay(500)
            if (this.bot.isOnline) return@subscribeAlways
            pushLog(0L, "[INFO] 发送离线通知....")
            val builder =
                NotificationCompat.Builder(
                    BotApplication.context,
                    BotApplication.OFFLINE_NOTIFICATION
                )
                    .setAutoCancel(false)
                    .setOngoing(false)
                    .setShowWhen(true)
                    .setSmallIcon(R.drawable.ic_info_black_24dp)
                    .setContentTitle("Mirai离线")
            when (this) {
                is BotOfflineEvent.Dropped -> builder.setContentText("请检查网络环境")
                is BotOfflineEvent.Force -> {
                    //设置长消息的style
                    builder.setStyle(NotificationCompat.BigTextStyle())
                    builder.setContentText(this.message)
                }
                else -> return@subscribeAlways
            }
            NotificationManagerCompat.from(BotApplication.context).apply {
                notify(BotService.OFFLINE_NOTIFICATION_ID, builder.build())
            }
        }
        subscribeAlways<BotReloginEvent>(priority = Listener.EventPriority.HIGHEST) {
            pushLog(0L, "[INFO] 发送上线通知....")
            NotificationManagerCompat.from(BotApplication.context)
                .cancel(BotService.OFFLINE_NOTIFICATION_ID)
        }
    }

    private fun Bot.pushToScriptManager(manager: ScriptManager) {
        launch { manager.addBot(this@pushToScriptManager) }
    }
}
