package io.github.mzdluo123.mirai.android

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import com.fanjun.keeplive.KeepLive
import com.fanjun.keeplive.config.ForegroundNotification
import com.fanjun.keeplive.config.KeepLiveService
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute
import io.github.mzdluo123.mirai.android.NotificationFactory.initNotification
import io.github.mzdluo123.mirai.android.activity.CrashReportActivity
import io.github.mzdluo123.mirai.android.appcenter.UpdateListener
import io.github.mzdluo123.mirai.android.crash.MiraiAndroidReportSenderFactory
import io.github.mzdluo123.mirai.android.service.BotService
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.acra.ACRA
import org.acra.config.CoreConfigurationBuilder
import org.acra.config.DialogConfigurationBuilder
import org.acra.data.StringFormat
import splitties.init.injectAsAppCtx


class BotApplication : Application() {
    companion object {

        lateinit var context: BotApplication
            private set

        val httpClient = lazy { OkHttpClient() }
        val json = lazy { Json.Default }
    }


    override fun onCreate() {
        super.onCreate()
        injectAsAppCtx()
        context = this
        initAppCenter()
        val processName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            getProcessName()
        else
            myGetProcessName()

        // 防止服务进程多次初始化
        if (processName?.isEmpty() == false && processName == packageName) {
            initNotification()
        }
    }

    private fun initAppCenter() {

        AppCenter.start(
            this, "70a7bed9-65ce-4526-a448-0be273dbb652",
            Analytics::class.java, Crashes::class.java, Distribute::class.java
        )
        Distribute.setListener(UpdateListener())
    }


    //崩溃事件注册
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        ACRA.init(this, CoreConfigurationBuilder(this).apply {
            setBuildConfigClass(BuildConfig::class.java)
                .setReportFormat(StringFormat.JSON)
            setReportSenderFactoryClasses(MiraiAndroidReportSenderFactory::class.java)
                .setBuildConfigClass(BuildConfig::class.java)
            getPluginConfigurationBuilder(DialogConfigurationBuilder::class.java)
                .setReportDialogClass(CrashReportActivity::class.java)
                .setEnabled(true)


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

    internal fun keepLive() {
        val notification = ForegroundNotification("MiraiAndroid", "保活服务已启动", R.mipmap.ic_launcher)
        KeepLive.startWork(this, KeepLive.RunMode.ROGUE, notification, object : KeepLiveService {
            override fun onWorking() {
                startBotService()
            }

            override fun onStop() {
                stopBotService()
            }

        })
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