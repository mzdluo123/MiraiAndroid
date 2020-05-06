package io.github.mzdluo123.mirai.android.script

import com.ooooonly.luaMirai.lua.MiraiGlobals
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import java.io.File


class LuaScriptHost(file: File, dataFolder: File) : BaseScriptHost(file, dataFolder) {
    private val scriptOut = object : MiraiGlobals.Printable {
        override fun print(msg: String?) {
            MiraiConsole.frontEnd.pushLog(0L, "[Lua] $msg")
        }

    }
    private val globals = MiraiGlobals(scriptOut)

    override fun load(bot: Bot) {
        MiraiConsole.frontEnd.pushLog(0L, "[Lua] 正在加载lua脚本 ${file.name}")
        bot.launch {
            globals.loadfile(file.absolutePath).call()
            globals.onLoad(bot)
        }
    }

    override fun disable() {
        globals.onFinish()
    }

}