package io.github.mzdluo123.mirai.android.script

import com.ooooonly.luaMirai.lua.MiraiGlobals
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import java.io.File


class LuaScriptHost(file: File, dataFolder: File) : BaseScriptHost(file, dataFolder) {

    override fun load(bot: Bot) {
        MiraiConsole.frontEnd.pushLog(0L, "[Lua] 正在加载lua脚本 ${file.name}")
        bot.launch {
            val globals = MiraiGlobals()
            val chunk = globals.loadfile(file.absolutePath)
            chunk.call()
        }

    }

    override fun disable() {
        return
    }


}