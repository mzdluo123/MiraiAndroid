package io.github.mzdluo123.mirai.android.script

import io.github.mzdluo123.mirai.android.BotApplication
import net.mamoe.mirai.Bot
import java.io.File
import java.io.InputStream

object ScriptManager {
    val scriptFolder = BotApplication.context.getExternalFilesDir("script")!!

    init {


    }

    fun onEnable(bot: Bot) {

    }

    fun onDisable() {

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
        return scriptFolder.list().toList()
    }
}