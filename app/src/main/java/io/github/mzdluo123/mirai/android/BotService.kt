package io.github.mzdluo123.mirai.android

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.command.ConsoleCommandSender


class BotService : Service() {
    lateinit var androidMiraiConsoleUI: AndroidMiraiConsoleUI
        private set
    private val binder = BotBinder()
    private var isStart = false

    companion object {
        const val START_SERVICE = 0
        const val STOP_SERVICE = 1
        const val NOTIFICATION_ID = 1
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
        val accountStore = this.getSharedPreferences("account",Context.MODE_PRIVATE)
        if (action == START_SERVICE && !isStart) {
            MiraiConsole.start(
                androidMiraiConsoleUI,
                path = getExternalFilesDirs(null)[0].absolutePath)
            isStart = true
            createNotification()
            val qq = accountStore.getString("qq","")
            val pwd = accountStore.getString("pwd","")
            if (qq != ""){
                CommandManager.runCommand(ConsoleCommandSender,"login $qq $pwd")
            }
        }
        if (action == STOP_SERVICE) {
            stopSelf()
            MiraiConsole.stop()
            Log.d("Service", "停止")
            stopForeground(true)
            System.exit(0)
        }
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onCreate() {
        super.onCreate()
        androidMiraiConsoleUI = AndroidMiraiConsoleUI(baseContext)
    }

    fun runCommand(command: String) {

        CommandManager.runCommand(ConsoleCommandSender, command)

    }

    inner class BotBinder : Binder() {
        fun getService(): BotService {
            return this@BotService
        }

    }

}
