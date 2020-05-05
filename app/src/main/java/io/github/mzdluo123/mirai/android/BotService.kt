package io.github.mzdluo123.mirai.android

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.mzdluo123.mirai.android.utils.DeviceStatus
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.utils.DeviceInfo
import java.text.SimpleDateFormat


class BotService : Service(),CommandOwner {
    lateinit var androidMiraiConsoleUI: AndroidMiraiConsoleUI
        private set
    private val binder = BotBinder()
    private var isStart = false
    private var startTime:Long = 0

// 多进程调试辅助
//  init {
//        Debug.waitForDebugger()
//    }

    companion object {
        const val START_SERVICE = 0
        const val STOP_SERVICE = 1
        const val NOTIFICATION_ID = 1
        const val TAG = "BOT_SERVICE"
    }

    private fun createNotification() {
        //使用兼容版本
        val builder = NotificationCompat.Builder(this, BotApplication.SERVICE_NOTIFICATION)
            //设置状态栏的通知图标
            .setSmallIcon(R.drawable.ic_extension_black_24dp)
            //禁止用户点击删除按钮删除
            .setAutoCancel(false)
            //禁止滑动删除
            .setOngoing(true)
            //右上角的时间显示
            .setShowWhen(true)
            .setContentTitle("MiraiAndroid正在运行")
            .setContentText("请将软件添加到系统后台运行白名单确保能及时处理消息")

        //创建通知
        //设置为前台服务
        startForeground(NOTIFICATION_ID, builder.build())
    }


    override fun onBind(intent: Intent): IBinder {
        return binder
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getIntExtra("action", START_SERVICE)
        if (action == START_SERVICE && !isStart) {
            startTime = System.currentTimeMillis()
            MiraiConsole.start(
                androidMiraiConsoleUI,
                path = getExternalFilesDir(null).toString()
            )
            isStart = true
            createNotification()
            CommandManager.register(this,object :Command{
                override val alias: List<String>
                    get() = listOf()
                override val description: String
                    get() = "MiraiAndroid运行状态"
                override val name: String
                    get() = "android"
                override val usage: String
                    get() = "/android"

                override suspend fun onCommand(sender: CommandSender, args: List<String>): Boolean {
                    sender.sendMessage("""MiraiAndroid v${packageManager.getPackageInfo(packageName,0).versionName}
系统版本 ${Build.VERSION.RELEASE} SDK ${Build.VERSION.SDK_INT}
内存可用 ${DeviceStatus.getSystemAvaialbeMemorySize(applicationContext)}
网络 ${DeviceStatus.getCurrentNetType(applicationContext)}
启动时间 ${SimpleDateFormat.getDateTimeInstance().format(startTime)}
                    """.trimIndent())
                    return true
                }

            })

            val qq = intent.getStringExtra("qq")
            val pwd = intent.getStringExtra("pwd")
            if (qq != null) {
                CommandManager.runCommand(ConsoleCommandSender, "login $qq $pwd")
            }
        }
        if (action == STOP_SERVICE) {
            MiraiConsole.stop()
            stopForeground(true)
            stopSelf()
            System.exit(0)
        }
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onCreate() {
        super.onCreate()
        androidMiraiConsoleUI = AndroidMiraiConsoleUI(baseContext)
    }

    inner class BotBinder : IbotAidlInterface.Stub() {
        override fun runCmd(cmd: String?) {
            if (cmd != null) {
                CommandManager.runCommand(ConsoleCommandSender, cmd)
            }
        }

        override fun getLog(): Array<String> {
            //防止
            // ClassCastException: java.lang.Object[] cannot be cast to java.lang.String[]
            // 不知道有没有更好的写法
            return androidMiraiConsoleUI.logStorage.toArray(
                arrayOfNulls<String>(
                    androidMiraiConsoleUI.logStorage.size
                )
            )
        }

        override fun submitCaptcha(captcha: String?) {
            if (captcha != null) {
                androidMiraiConsoleUI.loginSolver.captcha.complete(captcha)
            }
        }

        override fun clearLog() {
            androidMiraiConsoleUI.logStorage.clear()
        }

        override fun getCaptcha(): ByteArray {
            return androidMiraiConsoleUI.loginSolver.captchaData
        }

        override fun sendLog(log: String?) {
            androidMiraiConsoleUI.logStorage.add(log)
        }
    }

}
