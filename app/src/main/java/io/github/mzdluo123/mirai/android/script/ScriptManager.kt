package io.github.mzdluo123.mirai.android.script

import io.github.mzdluo123.mirai.android.BotApplication
import net.mamoe.mirai.Bot
import java.io.File
import java.io.InputStream

object ScriptManager {
    val scriptFolder = BotApplication.context.getExternalFilesDir("script")!!
//    private val scripts: MutableList<ScriptRuntime> = mutableListOf()

    @ExperimentalUnsignedTypes
    fun onLoad() {
//        val files = scriptFolder.listFiles().filter {
//            it.name.endsWith(".groovy")
//        }
//        files.forEach { file ->
//            kotlin.runCatching {
//                scripts.add(ScriptRuntime(file.toPath()))
//            }.onFailure {
//                MiraiAndroidLogger.error("MiraiAndroid加载脚本${file}时出现问题")
//                MiraiAndroidLogger.error(it)
//            }
//        }
    }

    fun onEnable(bot: Bot) {
//        scripts.forEach { script ->
//            kotlin.runCatching {
//                script.onEnable(bot)
//            }.onFailure {
//                MiraiAndroidLogger.error("MiraiAndroid启用脚本${script.fileName}时出现问题")
//                MiraiAndroidLogger.error(it)
//            }
//        }
    }

    fun onDisable() {
//        scripts.forEach { script ->
//            kotlin.runCatching {
//                script.onDisable()
//            }.onFailure {
//                MiraiAndroidLogger.error("MiraiAndroid停用脚本${script.fileName}时出现问题")
//                MiraiAndroidLogger.error(it)
//            }
//        }
    }

    fun addNewScript(name: String, input: InputStream) {
        val file = File(scriptFolder, name)
        if (file.exists()) {
            file.delete()
            file.createNewFile()
        }
        file.writeBytes(input.readBytes())
        input.close()
    }

    fun deleteScript(name: String) {
        val file = File(scriptFolder, name)
        file.delete()
    }

    fun listScript(): List<String> {
        return scriptFolder.list()!!.toList()
    }
}