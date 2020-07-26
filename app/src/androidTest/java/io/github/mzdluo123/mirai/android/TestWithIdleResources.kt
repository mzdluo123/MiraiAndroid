package io.github.mzdluo123.mirai.android

import androidx.test.espresso.IdlingRegistry
import org.junit.After
import org.junit.Before

open class TestWithIdleResources {

    @Before
    fun before() {
        IdlingRegistry.getInstance().register(IdleResources.logUploadDialogIdleResources)
        IdlingRegistry.getInstance().register(IdleResources.botServiceLoading)
    }

    @After
    fun after() {
        IdlingRegistry.getInstance().unregister(IdleResources.logUploadDialogIdleResources)
        IdlingRegistry.getInstance().register(IdleResources.botServiceLoading)
    }
}