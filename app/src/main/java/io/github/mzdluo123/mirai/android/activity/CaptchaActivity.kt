package io.github.mzdluo123.mirai.android.activity

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.mzdluo123.mirai.android.BotService
import io.github.mzdluo123.mirai.android.IbotAidlInterface
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.miraiconsole.AndroidLoginSolver
import kotlinx.android.synthetic.main.activity_captcha.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@ExperimentalUnsignedTypes
class CaptchaActivity : AppCompatActivity() {
    private val conn = object : ServiceConnection {
        lateinit var botService: IbotAidlInterface
        fun sendCaptcha(captcha: String) {
            botService.submitVerificationResult(captcha)
        }

        override fun onServiceDisconnected(name: ComponentName?) {}

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            botService =
                IbotAidlInterface.Stub.asInterface(
                    service
                )
            val data = botService.captcha
            lifecycleScope.launch(Dispatchers.Main) {
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                captcha_view.setImageBitmap(bitmap)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_captcha)
    }


    override fun onStart() {
        super.onStart()
        val intent = Intent(baseContext, BotService::class.java)
        bindService(intent, conn, Context.BIND_AUTO_CREATE)
        captchaConfirm_btn.setOnClickListener {
            conn.sendCaptcha(captcha_input.text.toString())
            // 删除通知
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(AndroidLoginSolver.CAPTCHA_NOTIFICATION_ID)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(conn)
    }

}