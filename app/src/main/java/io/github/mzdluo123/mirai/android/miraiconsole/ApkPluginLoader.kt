package io.github.mzdluo123.mirai.android.miraiconsole

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import io.github.mzdluo123.mirai.android.BotApplication
import java.io.File

object ApkPluginLoader {
    fun listPlugins(): Sequence<PackageInfo> {
        return BotApplication.context.packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            .asSequence()
            .filter { it.applicationInfo.metaData?.containsKey("miraiandroid_plugin") ?: false }
    }

    fun apkPluginFile(): Sequence<File> {
        return listPlugins().map { File(it.applicationInfo.publicSourceDir) }
    }
}