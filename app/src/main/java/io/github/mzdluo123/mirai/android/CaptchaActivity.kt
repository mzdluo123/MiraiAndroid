package io.github.mzdluo123.mirai.android

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_captcha.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CaptchaActivity : AppCompatActivity() {
    private val conn = object : ServiceConnection {
        lateinit var botService: BotService
        fun sendCaptcha(captcha: String) {
            botService.androidMiraiConsoleUI.loginSolver.captcha.complete(captcha)
        }

        override fun onServiceDisconnected(name: ComponentName?) {}

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            botService = (service as BotService.BotBinder).getService()
            val data = botService.androidMiraiConsoleUI.loginSolver.captchaData
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
        bindService(intent, conn, 0)
        captchaConfirm_btn.setOnClickListener {
            conn.sendCaptcha(captcha_input.text.toString())
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        unbindService(conn)
    }

}