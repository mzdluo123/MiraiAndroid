package io.github.mzdluo123.mirai.android.appcenter

import com.microsoft.appcenter.Flags
import com.microsoft.appcenter.analytics.Analytics

internal fun trace(event: String) {
    Analytics.trackEvent(event)
}


internal fun traceCritical(event: String) {
    Analytics.trackEvent(event, mapOf(), Flags.CRITICAL)
}

internal fun trace(event: String, vararg prop: Pair<String, String>) {
    val map = hashMapOf<String, String>()
    prop.forEach { map[it.first] = it.second }
    Analytics.trackEvent(event, map)
}