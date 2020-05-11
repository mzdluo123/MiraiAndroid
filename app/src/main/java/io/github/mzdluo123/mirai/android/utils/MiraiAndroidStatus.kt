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

    fun format():String =
        """MiraiAndroid v${miraiAndroidVersion}
MiraiCore v${coreVersion}
LuaMirai v${luaMiraiVersion}
系统版本 ${releaseVersion} SDK ${sdkVersion}
内存可用 ${memorySize}
网络 ${netType}
启动时间 ${startTime}
日志缓存行数 $logBuffer"""
}