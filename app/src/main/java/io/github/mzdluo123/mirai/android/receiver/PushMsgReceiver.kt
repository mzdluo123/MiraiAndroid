package io.github.mzdluo123.mirai.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.mzdluo123.mirai.android.BotService

class PushMsgReceiver(private val botService: BotService) : BroadcastReceiver() {
    companion object {
        val TAG = PushMsgReceiver.javaClass.name
    }

    override fun onReceive(context: Context, intent: Intent) {
        // 为了兼容垃圾autojs，所以统一string发然后转类型
        val type = intent.getStringExtra("type")?.toInt() ?: return
        val id = intent.getStringExtra("id")?.toLong() ?: return
        val msg = intent.getStringExtra("msg") ?: return
        when (type) {
            1 -> botService.sendFriendMsg(id, msg)
            2 -> botService.sendGroupMsg(id, msg)
        }
    }
}
