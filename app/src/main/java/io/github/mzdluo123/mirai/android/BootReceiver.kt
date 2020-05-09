package io.github.mzdluo123.mirai.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
//    companion object{
//        const val TAG = "BootReceiver"
//    }
    private val ACTION = "android.intent.action.BOOT_COMPLETED"
    override fun onReceive(context: Context, intent: Intent) {
//        Log.e(TAG,"收到广播")
        if (!BotApplication.getSettingPreference().getBoolean("start_on_boot_preference", false)) {
            return
        }

        if (intent.action == ACTION) {
            val intent = Intent(context, BotService::class.java)
            intent.putExtra(
                "action",
                BotService.START_SERVICE
            )
            val account =
                context.getSharedPreferences("account", Context.MODE_PRIVATE)
            val qq = account.getLong("qq", 0)
            val pwd = account.getString("pwd", null)
            intent.putExtra("qq", qq)
            intent.putExtra("pwd", pwd)
            context.startForegroundService(intent)
        }
    }
}