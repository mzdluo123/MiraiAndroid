package io.github.mzdluo123.mirai.android.console

import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import io.github.mzdluo123.mirai.android.IdleResources
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.childAtPosition
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@LargeTest
@RunWith(AndroidJUnit4::class)
class ConsoleIntentTest {

    @Rule
    @JvmField
    var mActivityTestRule = IntentsTestRule(MainActivity::class.java)


    private val device = UiDevice.getInstance(getInstrumentation())

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


    @Test
    fun uploadLogTest() {

        val overflowMenuButton = onView(
            allOf(
                withContentDescription("更多选项"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.toolbar),
                        2
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        overflowMenuButton.perform(click())

        val appCompatTextView = onView(
            allOf(
                withId(R.id.title), withText("分享日志"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.content),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatTextView.perform(click())

        intending(hasAction(Intent.ACTION_CHOOSER)).respondWith(
            Instrumentation.ActivityResult(
                0,
                null
            )
        )

        device.pressBack()
    }

}