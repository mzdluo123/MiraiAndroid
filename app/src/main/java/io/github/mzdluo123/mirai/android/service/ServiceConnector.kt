package io.github.mzdluo123.mirai.android.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.DeadObjectException
import android.os.IBinder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import io.github.mzdluo123.mirai.android.IConsole
import io.github.mzdluo123.mirai.android.IbotAidlInterface
import io.github.mzdluo123.mirai.android.IdleResources


class ServiceConnector(var context: Context) : ServiceConnection, LifecycleObserver {

    lateinit var botService: IbotAidlInterface
        private set

    var connectStatus = MutableLiveData(false)
        private set

    private var callback: IConsole? = null

    override fun onServiceDisconnected(name: ComponentName?) {
        connectStatus.value = false
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        botService = IbotAidlInterface.Stub.asInterface(service)
        connectStatus.value = true
        if (callback != null) {
            botService.registerConsole(callback)
        }

        if (!IdleResources.botServiceLoading.isIdleNow) {
            IdleResources.botServiceLoading.decrement()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun connect() {
        if (!connectStatus.value!!) {
            context.bindService(
                Intent(context, BotService::class.java),
                this,
                Context.BIND_ABOVE_CLIENT
            )
            if (IdleResources.botServiceLoading.isIdleNow) {
                IdleResources.botServiceLoading.increment()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        if (connectStatus.value!!) {
            if (callback != null) {
                botService.registerConsole(callback)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        if (connectStatus.value!!) {
            if (callback != null) {
                try {
                    botService.unregisterConsole(callback)
                } catch (ignore: DeadObjectException) {

                }

                callback = null
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun disconnect() {
        if (connectStatus.value!!) {
            if (callback != null) {
                try {
                    botService.unregisterConsole(callback)
                } catch (ignore: DeadObjectException) {

                }

            }
            context.unbindService(this)
            connectStatus.value = false
        }
    }

    fun registerConsole(callback: IConsole) {
        this.callback = callback

    }

}