package io.github.mzdluo123.mirai.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.github.mzdluo123.mirai.android.BotService

class PushMsgReceiver(private val botService: BotService) : BroadcastReceiver() {
    companion object {
        val TAG = PushMsgReceiver.javaClass.name
    }

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getIntExtra("type", 0)
        val id = intent.getLongExtra("id", 0)
        val msg = intent.getStringExtra("msg") ?: return
        when (type) {
            1 -> botService.sendFriendMsg(id, msg)
            2 -> botService.sendGroupMsg(id, msg)
        }
    }
}
