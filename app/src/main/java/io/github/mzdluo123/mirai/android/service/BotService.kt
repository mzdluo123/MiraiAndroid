@file:Suppress(
    "EXPERIMENTAL_API_USAGE",
    "DEPRECATION_ERROR",
    "OverridingDeprecatedMember",
    "INVISIBLE_REFERENCE",
    "INVISIBLE_MEMBER"
)

package io.github.mzdluo123.mirai.android.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import io.github.mzdluo123.mirai.android.AppSettings
import io.github.mzdluo123.mirai.android.IbotAidlInterface
import io.github.mzdluo123.mirai.android.NotificationFactory
import io.github.mzdluo123.mirai.android.miraiconsole.AndroidMiraiConsole
import io.github.mzdluo123.mirai.android.miraiconsole.MiraiAndroidLogger
import io.github.mzdluo123.mirai.android.receiver.PushMsgReceiver
import io.github.mzdluo123.mirai.android.script.ScriptManager
import io.github.mzdluo123.mirai.android.utils.MiraiAndroidStatus
import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleFrontEnd
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.executeCommand
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.plugin.DeferredPluginLoader
import net.mamoe.mirai.console.plugin.PluginLoader
import net.mamoe.mirai.console.plugin.jvm.JarPluginLoader
import net.mamoe.mirai.console.setting.MultiFileSettingStorage
import net.mamoe.mirai.console.setting.SettingStorage
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.SimpleLogger
import splitties.experimental.ExperimentalSplittiesApi
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess


@ExperimentalSplittiesApi
@ExperimentalUnsignedTypes
class BotService : Service(), MiraiConsoleImplementation {
    lateinit var consoleFrontEnd: AndroidMiraiConsole
        private set
    private val binder = BotBinder()
    private var isStart = false
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private var bot: Bot? = null
    private val msgReceiver = PushMsgReceiver(this)
    private val allowPushMsg = AppSettings.allowPushMsg
    private lateinit var botJob: Job

// 多进程调试辅助
//  init {
//        Debug.waitForDebugger()
//    }

    companion object {
        const val START_SERVICE = 0
        const val STOP_SERVICE = 1
        const val NOTIFICATION_ID = 1
        const val OFFLINE_NOTIFICATION_ID = 3
        const val TAG = "BOT_SERVICE"
    }

    private fun createNotification() {
        NotificationFactory.statusNotification().let {
            startForeground(NOTIFICATION_ID, it) //设置为前台服务
        }
    }

    override fun onBind(intent: Intent): IBinder = binder

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
            MiraiAndroidLogger.info("onStartCommand:发生错误 $e")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate() {
        super.onCreate()
        botJob = Job()
        consoleFrontEnd = AndroidMiraiConsole(baseContext)
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BotWakeLock")
    }

    private fun autoLogin(intent: Intent) {
        val qq = intent.getLongExtra("qq", 0)
        val pwd = intent.getStringExtra("pwd")
        if (qq == 0L) return

        //CommandManager.runCommand(ConsoleCommandSender, "login $qq $pwd")
        MiraiAndroidLogger.info("[INFO] 自动登录....")
        val handler = CoroutineExceptionHandler { _, throwable ->
            MiraiAndroidLogger.info("[ERROR] 自动登录失败 $throwable")
        }

        val bot = Bot(qq, pwd!!.chunkedHexToBytes()) {
            fileBasedDeviceInfo(getExternalFilesDir(null)!!.absolutePath + "/device.json")
            this.loginSolver = MiraiConsole.frontEnd.createLoginSolver()
            this.botLoggerSupplier = {
                SimpleLogger("[BOT $qq]") { _, message, e ->
                    MiraiAndroidLogger.info("[INFO] $message")
                    e?.also {
                        MiraiAndroidLogger.info("[BOT ERROR $qq] $it")
                    }?.printStackTrace()
                }
            }
            this.networkLoggerSupplier = {
                SimpleLogger("BOT $qq") { _, message, e ->
                    MiraiAndroidLogger.info("[NETWORK] $message")
                    e?.also {
                        MiraiAndroidLogger.info("[NETWORK ERROR] $it")
                    }?.printStackTrace()
                }
            }
        }
        this.bot = bot
        launch(handler) { bot.login() }
        bot.subscribeMessages {
            startsWith("/") { message ->
//                if (bot.checkManager(this.sender.id))
//                    CommandManager.runCommand(ContactCommandSender(bot, this.subject), message)
            }
        }

        // 新的自动登录
        //MiraiConsole.addBot().alsoLogin()

//        GlobalScope.launch(handler) { sendMessage("$qq login successes") }
        MiraiConsole.frontEnd.pushBot(bot)
    }

    private fun registerDefaultCommand() {
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

    @SuppressLint("WakelockTimeout")
    private fun startConsole(intent: Intent?) {
        if (isStart) return
        Log.e(TAG, "启动服务")
        try {
            wakeLock.acquire()
        } catch (e: Exception) {
            Log.e("wakeLockError", e.message ?: "null")
        }
        MiraiAndroidStatus.startTime = System.currentTimeMillis()

        MiraiConsoleImplementation.Companion.instance = this
        this.start()
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
    }

    private fun stopConsole() {
        if (!isStart) return
        Log.e(TAG, "停止服务")
        if (allowPushMsg) {
            unregisterReceiver(msgReceiver)
        }
        ScriptManager.instance.disableAll()
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        botJob.cancel()
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
            this@BotService.bot!!.getFriend(id).sendMessage(msg!!)
        }
    }


    internal fun sendGroupMsg(id: Long, msg: String?) {
        bot?.launch {
            MiraiAndroidLogger.info("[MA] 成功处理一个群消息推送请求: $msg->$id")
            this@BotService.bot!!.getGroup(id).sendMessage(msg!!)
        }
    }

    internal fun sendGroupMsgWithAT(id: Long, msg: String?, user: Long) {
        bot?.launch {
            MiraiAndroidLogger.info("[MA] 成功处理一个群消息推送请求: $msg->$id")
            val group = this@BotService.bot!!.getGroup(id)
            group.sendMessage(At(group[user]) + msg!!)
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
                runBlocking {
                    consoleFrontEnd.executeCommand(cmd)
                }
            }
        }

        override fun getLog(): MutableList<String>? {

            return MiraiAndroidLogger.logs
        }

        override fun submitVerificationResult(result: String?) {
            result?.let {
                consoleFrontEnd.loginSolver.verificationResult.complete(it)
            }
        }

        override fun setScriptConfig(config: String?) {

        }

        override fun createScript(name: String, type: Int): Boolean {
            return ScriptManager.instance.createScriptFromFile(File(name), type)
        }

        override fun reloadScript(index: Int): Boolean {
            ScriptManager.instance.reload(index)
            return true
        }

        override fun clearLog() {
            MiraiAndroidLogger.clearLog()
        }

        override fun enableScript(index: Int) {
            ScriptManager.instance.enable(index)
        }

        override fun disableScript(index: Int) {
            ScriptManager.instance.disable(index)
        }

        override fun getUrl(): String = consoleFrontEnd.loginSolver.url

        override fun getScriptSize(): Int = ScriptManager.instance.hosts.size

        override fun getCaptcha(): ByteArray = consoleFrontEnd.loginSolver.captchaData
        override fun getLogonId(): Long {
            return try {
                Bot.botInstances.first().id
            } catch (e: NoSuchElementException) {
                0
            }
        }


        override fun sendLog(log: String?) {
            MiraiAndroidLogger.info(log)
        }

        override fun getBotInfo(): String = MiraiAndroidStatus.recentStatus().format()

        override fun openScript(index: Int) {
            val scriptFile = ScriptManager.instance.hosts[index].scriptFile
            val provideUri: Uri
            provideUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    this@BotService,
                    "io.github.mzdluo123.mirai.android.scriptprovider",
                    scriptFile
                )
            } else {
                Uri.fromFile(scriptFile)
            }
            startActivity(
                Intent("android.intent.action.VIEW").apply {
                    addCategory("android.intent.category.DEFAULT")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(MediaStore.EXTRA_OUTPUT, provideUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    type = "text/plain"
                    setDataAndType(provideUri, type)
                })
        }

        override fun deleteScript(index: Int) {
            ScriptManager.instance.delete(index)
        }

        override fun getHostList(): Array<String> = ScriptManager.instance.getHostInfoStrings()
    }

    override val builtInPluginLoaders: List<PluginLoader<*, *>>
        get() = listOf(DeferredPluginLoader { JarPluginLoader })
    override val consoleCommandSender: ConsoleCommandSender
        get() = consoleFrontEnd
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + botJob

    @ConsoleExperimentalAPI
    override val frontEnd: MiraiConsoleFrontEnd
        get() = consoleFrontEnd
    override val mainLogger: MiraiLogger
        get() = MiraiAndroidLogger
    override val rootDir: File
        get() = getExternalFilesDir(null)!!.absoluteFile
    override val settingStorageForBuiltIns: SettingStorage
        get() = MultiFileSettingStorage(File(getExternalFilesDir(null), "files/settings").also {
            if (!it.exists()) {
                it.mkdir()
            }
        })
    override val settingStorageForJarPluginLoader: SettingStorage
        get() = MultiFileSettingStorage(File(getExternalFilesDir(null), "files/data").also {
            if (!it.exists()) {

                it.mkdir()
            }
        })


}
