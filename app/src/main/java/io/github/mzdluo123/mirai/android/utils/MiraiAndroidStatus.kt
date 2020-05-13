package io.github.mzdluo123.mirai.android.utils

import android.content.Context
import android.os.Build
import io.github.mzdluo123.mirai.android.BotApplication
import io.github.mzdluo123.mirai.android.BuildConfig
import java.text.SimpleDateFormat

class MiraiAndroidStatus (
    var miraiAndroidVersion:String,
    var coreVersion:String,
    var luaMiraiVersion:String,
    var releaseVersion:String,
    var sdkVersion:Int,
    var memorySize:String,
    var netType:String,
    var startTime:String,
    var logBuffer:Int
) {
    companion object{
        var startTime:Long = 0
        fun recentStatus(context:Context =BotApplication.context ):MiraiAndroidStatus = MiraiAndroidStatus(
            context.packageManager.getPackageInfo(context.packageName, 0).versionName,
            BuildConfig.COREVERSION,
            BuildConfig.LUAMIRAI_VERSION,
            Build.VERSION.RELEASE,
            Build.VERSION.SDK_INT,
            DeviceStatus.getSystemAvaialbeMemorySize(context.applicationContext),
            DeviceStatus.getCurrentNetType(context.applicationContext),
            SimpleDateFormat.getDateTimeInstance().format(startTime),
            BotApplication.getSettingPreference().getString("log_buffer_preference", "300")!!.toInt()
        )
    }

    fun format():String = StringBuilder().apply{
        append("MiraiAndroid v${miraiAndroidVersion}\n")
        append("MiraiCore v${coreVersion}\n")
        append("LuaMirai v${luaMiraiVersion}\n")
        append("系统版本 ${releaseVersion} SDK ${sdkVersion}\n")
        append("内存可用 ${memorySize}\n")
        append("网络 ${netType}\n")
        append("启动时间 ${startTime}\n")
        append("日志缓存行数 $logBuffer")
    }.toString()
}