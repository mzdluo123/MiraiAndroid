package io.github.mzdluo123.mirai.android

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_captcha.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CaptchaActivity : AppCompatActivity() {
    private val conn = object : ServiceConnection {
        lateinit var botService: IbotAidlInterface
        fun sendCaptcha(captcha: String) {
           botService.submitCaptcha(captcha)
        }

        override fun onServiceDisconnected(name: ComponentName?) {}

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            botService = IbotAidlInterface.Stub.asInterface(service)
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
        bindService(intent, conn,  Context.BIND_AUTO_CREATE)
        captchaConfirm_btn.setOnClickListener {
            conn.sendCaptcha(captcha_input.text.toString())
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(conn)
    }

}