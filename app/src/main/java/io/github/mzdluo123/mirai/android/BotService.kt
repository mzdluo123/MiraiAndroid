package io.github.mzdluo123.mirai.android

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.ConsoleCommandSender


class BotService : Service() {
    lateinit var androidMiraiConsoleUI: AndroidMiraiConsoleUI
        private set
    private val binder = BotBinder()
    private var isStart = false

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
            MiraiConsole.start(
                androidMiraiConsoleUI,
                path = getExternalFilesDir(null).toString()
            )
            isStart = true
            createNotification()
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
