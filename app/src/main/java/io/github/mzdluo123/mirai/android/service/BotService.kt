@file:Suppress(
    "EXPERIMENTAL_API_USAGE",
    "DEPRECATION_ERROR",
    "OverridingDeprecatedMember",
    "INVISIBLE_REFERENCE",
    "INVISIBLE_MEMBER"
)

package io.github.mzdluo123.mirai.android.service

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.os.Debug
import android.os.IBinder
import android.os.RemoteCallbackList
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import io.github.mzdluo123.mirai.android.AppSettings
import io.github.mzdluo123.mirai.android.IConsole
import io.github.mzdluo123.mirai.android.IbotAidlInterface
import io.github.mzdluo123.mirai.android.NotificationFactory
import io.github.mzdluo123.mirai.android.miraiconsole.AndroidMiraiConsole
import io.github.mzdluo123.mirai.android.miraiconsole.AndroidStatusCommand
import io.github.mzdluo123.mirai.android.miraiconsole.MiraiAndroidLogger
import io.github.mzdluo123.mirai.android.miraiconsole.logException
import io.github.mzdluo123.mirai.android.receiver.PushMsgReceiver
import io.github.mzdluo123.mirai.android.script.ScriptManager
import io.github.mzdluo123.mirai.android.utils.MiraiAndroidStatus
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.executeCommand
import net.mamoe.mirai.console.rootDir
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.message.data.At
import kotlin.system.exitProcess

class BotService : LifecycleService() {
    @ConsoleFrontEndImplementation
    lateinit var consoleFrontEnd: AndroidMiraiConsole
        private set
    private val binder = BotBinder()
    private var isStart = false
    // private lateinit var powerManager: PowerManager

    // private lateinit var wakeLock: PowerManager.WakeLock
    private var bot: Bot? = null
    private val msgReceiver = PushMsgReceiver(this)
    private val allowPushMsg = AppSettings.allowPushMsg

    // 多进程调试辅助
//  init {
//        Debug.waitForDebugger()
//    }

    init {
        if (AppSettings.waitingDebugger) {
            MiraiAndroidLogger.info("等待调试器链接..... PID:${android.os.Process.myPid()}")
            Debug.waitForDebugger()
        }

    }

    companion object {
        const val START_SERVICE = 0
        const val STOP_SERVICE = 1
        const val NOTIFICATION_ID = 1
        const val OFFLINE_NOTIFICATION_ID = 3
        const val TAG = "BOT_SERVICE"

        val consoleUi: RemoteCallbackList<IConsole> = RemoteCallbackList()
    }

    private fun createNotification() {
        startForeground(NOTIFICATION_ID, NotificationFactory.statusNotification())
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    @ConsoleFrontEndImplementation
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            intent?.getIntExtra(
                "action",
                START_SERVICE
            ).let { action ->
                when (action) {
                    START_SERVICE -> startConsole(intent)
                    STOP_SERVICE -> stopConsole()
                }
            }
        } catch (e: Exception) {
            Log.e("onStartCommand", e.message ?: "null")
            MiraiAndroidLogger.error(e)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @ConsoleFrontEndImplementation
    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate() {
        super.onCreate()
        consoleFrontEnd = AndroidMiraiConsole(baseContext, getExternalFilesDir("")!!.toPath())
        //powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
//        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BotWakeLock")
    }

    @ConsoleFrontEndImplementation
    private fun autoLogin(intent: Intent) {
        val qq = intent.getLongExtra("qq", 0)
        val pwd = intent.getStringExtra("pwd")
        if (qq == 0L) return
        //CommandManager.runCommand(ConsoleCommandSender, "login $qq $pwd")
        MiraiAndroidLogger.info("自动登录....")
        val handler = CoroutineExceptionHandler { _, throwable ->
            MiraiAndroidLogger.error("自动登录失败 $throwable")
            logException(throwable)
        }
        // 新的自动登录
        //MiraiConsole.addBot().alsoLogin()
//        GlobalScope.launch(handler) { sendMessage("$qq login successes") }
        val bot = MiraiConsole.addBot(qq, pwd!!.chunkedHexToBytes())
        lifecycleScope.launch(handler) { bot.login() }
    }

    private fun registerDefaultCommand() {
        AndroidStatusCommand.register()
//        register(_description = "显示MiraiAndroid运行状态", _name = "android") { sender, _ ->
//            sender.sendMessage(MiraiAndroidStatus.recentStatus().format())
//            true
//        }
//        register(_description = "查看已加载的脚本", _name = "script", _usage = "script") { sender, _ ->
//            sender.sendMessage(buildString {
//                append("已加载${ScriptManager.instance.hosts.size}个脚本\n")
//                ScriptManager.instance.hosts.joinTo(
//                    this,
//                    "\n"
//                ) { "${it.info.name} ${it.info.version} by ${it.info.author}" }
//                append("\n已加载Bot数量：${ScriptManager.instance.botsSize}")
//            })
//            true
//        }
    }

    @ConsoleFrontEndImplementation
    @SuppressLint("WakelockTimeout")
    private fun startConsole(intent: Intent?) {
        if (isStart) return
        Log.e(TAG, "启动服务")
//        try {
//            wakeLock.acquire()
//        } catch (e: Exception) {
//            Log.e("wakeLockError", e.message ?: "null")
//        }
        MiraiAndroidStatus.startTime = System.currentTimeMillis()
        consoleFrontEnd.start()
        MiraiAndroidLogger.info("工作目录: ${MiraiConsole.rootDir}")
//        MiraiConsole.start(
//            consoleFrontEnd,
//            consoleVersion = BuildConfig.COREVERSION,
//            path = getExternalFilesDir(null).toString()
//        )
        registerReceiver()
        isStart = true
        createNotification()
        registerDefaultCommand()
        intent?.let { autoLogin(it) }
        ScriptManager.onLoad()
        GlobalEventChannel.subscribeAlways<BotOnlineEvent> {
            consoleFrontEnd.afterBotLogin(bot)
            ScriptManager.onEnable(bot)
        }
    }

    private fun stopConsole() {
        if (!isStart) return
        Log.e(TAG, "停止服务")
        if (allowPushMsg) unregisterReceiver(msgReceiver)
//        if (wakeLock.isHeld) {
//            wakeLock.release()
//        }
        MiraiConsole.job.cancel()
        ScriptManager.onDisable()
        lifecycleScope.cancel()
        stopForeground(true)
        stopSelf()
        exitProcess(0)
    }

    private fun registerReceiver() {
        if (allowPushMsg) {
            MiraiAndroidLogger.info("[MA] 正在启动消息推送广播监听器")
            val filter = IntentFilter().apply {
                addAction("io.github.mzdluo123.mirai.android.PushMsg")
                priority = 999
                addDataScheme("ma")
            }
            registerReceiver(msgReceiver, filter)
        }
    }

    internal fun sendFriendMsg(id: Long, msg: String?) {
        bot?.launch {
            MiraiAndroidLogger.info("[MA] 成功处理一个好友消息推送请求: $msg->$id")
            this@BotService.bot!!.getFriend(id)?.sendMessage(msg!!) ?: return@launch
        }
    }

    internal fun sendGroupMsg(id: Long, msg: String?) {
        bot?.launch {
            MiraiAndroidLogger.info("[MA] 成功处理一个群消息推送请求: $msg->$id")
            this@BotService.bot!!.getGroup(id)?.sendMessage(msg!!) ?: return@launch
        }
    }

    internal fun sendGroupMsgWithAT(id: Long, msg: String?, user: Long) {
        bot?.launch {
            MiraiAndroidLogger.info("[MA] 成功处理一个群消息推送请求: $msg->$id")
            val group = this@BotService.bot!!.getGroup(id) ?: return@launch
            group.sendMessage(At(group[user]?.id ?: return@launch) + msg!!)
        }
    }

    @ExperimentalUnsignedTypes
    private fun String.chunkedHexToBytes(): ByteArray =
        this.asSequence().chunked(2).map { (it[0].toString() + it[1]).toUByte(16).toByte() }
            .toList().toByteArray()

    inner class BotBinder : IbotAidlInterface.Stub() {
        override fun runCmd(cmd: String?) {
            cmd?.let {
                //CommandManager.runCommand(ConsoleCommandSender, it)
                lifecycleScope.launch {
                    ConsoleCommandSender.executeCommand(cmd)
                }
            }
        }

        override fun registerConsole(instance: IConsole) {
            consoleUi.register(instance)
        }

        override fun unregisterConsole(instance: IConsole) {
            consoleUi.unregister(instance)
        }

        override fun getLog(): MutableList<String> {
            return MiraiAndroidLogger.logs
        }

        @ConsoleFrontEndImplementation
        override fun submitVerificationResult(result: String?) {
            result?.let {
                consoleFrontEnd.loginSolver.verificationResult.complete(it)
            }
        }

        override fun clearLog() {
            MiraiAndroidLogger.clearLog()
        }

        @ConsoleFrontEndImplementation
        override fun getUrl(): String = consoleFrontEnd.loginSolver.url

        @ConsoleFrontEndImplementation
        override fun getCaptcha(): ByteArray = consoleFrontEnd.loginSolver.captchaData

        override fun getLogonId(): Long {
            return try {
                Bot.instances.first().id
            } catch (e: NoSuchElementException) {
                0
            }
        }

        override fun sendLog(log: String?) {
            MiraiAndroidLogger.info(log)
        }

        override fun getBotInfo(): String = MiraiAndroidStatus.recentStatus().format()

    }
}
