@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package io.github.mzdluo123.mirai.android

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import io.github.mzdluo123.mirai.android.miraiconsole.AndroidMiraiConsole
import io.github.mzdluo123.mirai.android.script.ScriptManager
import io.github.mzdluo123.mirai.android.utils.MiraiAndroidStatus
import io.github.mzdluo123.mirai.android.utils.register
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.CommandOwner
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.ConsoleCommandSender.sendMessage
import net.mamoe.mirai.console.command.ContactCommandSender
import net.mamoe.mirai.console.utils.checkManager
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.utils.SimpleLogger
import java.io.File
import kotlin.system.exitProcess


class BotService : Service(), CommandOwner {
    lateinit var androidMiraiConsole: AndroidMiraiConsole
        private set
    private val binder = BotBinder()
    private var isStart = false
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock

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
        //使用兼容版本
        NotificationCompat.Builder(this, BotApplication.SERVICE_NOTIFICATION)
            .setSmallIcon(R.drawable.ic_extension_black_24dp)//设置状态栏的通知图标
            .setAutoCancel(false) //禁止用户点击删除按钮删除
            .setOngoing(true) //禁止滑动删除
            .setShowWhen(true) //右上角的时间显示
            .setOnlyAlertOnce(true)
            .setStyle(NotificationCompat.BigTextStyle())
            .setContentTitle("MiraiAndroid未登录") //创建通知
            .setContentText("请完成登录并将软件添加到系统后台运行白名单确保能及时处理消息")
            .build()
            .let {
                startForeground(NOTIFICATION_ID, it) //设置为前台服务
            }
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            intent?.getIntExtra("action", START_SERVICE).let { action ->
                when (action) {
                    START_SERVICE -> startConsole(intent)
                    STOP_SERVICE -> stopConsole()
                }
            }
        }catch (e:Exception){
            Log.e("onStartCommand", e.message)
            androidMiraiConsole.pushLog(0L, "onStartCommand:发生错误 $e")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate() {
        super.onCreate()
        androidMiraiConsole =
            AndroidMiraiConsole(
                baseContext
            )
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BotWakeLock")
    }

    private fun autoLogin(intent: Intent) {
        val qq = intent.getLongExtra("qq", 0)
        val pwd = intent.getStringExtra("pwd")
        if (qq == 0L) return

        //CommandManager.runCommand(ConsoleCommandSender, "login $qq $pwd")
        androidMiraiConsole.pushLog(0L, "[INFO] 自动登录....")
        val handler = CoroutineExceptionHandler { _, throwable ->
            androidMiraiConsole.pushLog(0L, "[ERROR] 自动登录失败 $throwable")
        }

        val bot = Bot(qq, pwd!!.chunkedHexToBytes()) {
            fileBasedDeviceInfo(getExternalFilesDir(null)!!.absolutePath + "/device.json")
            this.loginSolver = MiraiConsole.frontEnd.createLoginSolver()
            this.botLoggerSupplier = {
                SimpleLogger("[BOT $qq]") { _, message, e ->
                    androidMiraiConsole.pushLog(0L, "[INFO] $message")
                    e?.also {
                        androidMiraiConsole.pushLog(0L, "[BOT ERROR $qq] $it")
                    }?.printStackTrace()
                }
            }
            this.networkLoggerSupplier = {
                SimpleLogger("BOT $qq") { _, message, e ->
                    androidMiraiConsole.pushLog(0L, "[NETWORK] $message")
                    e?.also {
                        androidMiraiConsole.pushLog(0L, "[NETWORK ERROR] $it")
                    }?.printStackTrace()
                }
            }
        }
        GlobalScope.launch(handler) { bot.login() }


        GlobalScope.launch(handler) { sendMessage("$qq login successes") }
        MiraiConsole.frontEnd.pushBot(bot)
    }

    private fun registerDefaultCommand() {
        register(_description = "显示MiraiAndroid运行状态", _name = "android") { sender, _ ->
            sender.sendMessage(MiraiAndroidStatus.recentStatus().format())
            true
        }
        register(_description = "查看已加载的脚本", _name = "script", _usage = "script") { sender, _ ->
            sender.sendMessage(buildString {
                append("已加载${ScriptManager.instance.hosts.size}个脚本\n")
                ScriptManager.instance.hosts.joinTo(
                    this,
                    "\n"
                ) { "${it.info.name} ${it.info.version} by ${it.info.author}" }
                append("\n已加载Bot数量：${ScriptManager.instance.botsSize}")
            })
            true
        }
    }

    @SuppressLint("WakelockTimeout")
    private fun startConsole(intent: Intent?) {
        if (isStart) return
        isStart = true
        try {
            wakeLock.acquire()
        } catch (e: Exception) {
            Log.e("wakeLockError", e.message)
        }
        MiraiAndroidStatus.startTime = System.currentTimeMillis()
        MiraiConsole.start(
            androidMiraiConsole,
            path = getExternalFilesDir(null).toString()
        )
        createNotification()
        registerDefaultCommand()
        intent?.let { autoLogin(it) }
    }

    private fun stopConsole() {
        ScriptManager.instance.disableAll()
        wakeLock.release()
        MiraiConsole.stop()
        stopForeground(true)
        stopSelf()
        exitProcess(0)
    }

    private fun String.chunkedHexToBytes(): ByteArray =
        this.asSequence().chunked(2).map { (it[0].toString() + it[1]).toUByte(16).toByte() }
            .toList().toByteArray()

    inner class BotBinder : IbotAidlInterface.Stub() {
        override fun runCmd(cmd: String?) {
            cmd?.let {
                CommandManager.runCommand(ConsoleCommandSender, it)
            }
        }

        override fun getLog(): Array<String> {
            //防止
            // ClassCastException: java.lang.Object[] cannot be cast to java.lang.String[]
            // 不知道有没有更好的写法
            return androidMiraiConsole.logStorage.toArray(
                arrayOfNulls(androidMiraiConsole.logStorage.size))
        }

        override fun submitVerificationResult(result: String?) {
            result?.let {
                androidMiraiConsole.loginSolver.verificationResult.complete(it)
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
            androidMiraiConsole.logStorage.clear()
        }

        override fun enableScript(index: Int) {
            ScriptManager.instance.enable(index)
        }

        override fun disableScript(index: Int) {
            ScriptManager.instance.disable(index)
        }

        override fun getUrl(): String = androidMiraiConsole.loginSolver.url

        override fun getScriptSize(): Int = ScriptManager.instance.hosts.size

        override fun getCaptcha(): ByteArray = androidMiraiConsole.loginSolver.captchaData

        override fun sendLog(log: String?) {
            androidMiraiConsole.logStorage.add(log)
        }

        override fun getBotInfo(): String = MiraiAndroidStatus.recentStatus().format()

        override fun openScript(index: Int) {
            val scriptFile = ScriptManager.instance.hosts[index].scriptFile
            val provideUri: Uri
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                provideUri = FileProvider.getUriForFile(
                    this@BotService,
                    "io.github.mzdluo123.mirai.android.scriptprovider",
                    scriptFile
                )
            } else {
                provideUri = Uri.fromFile(scriptFile)
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

}
