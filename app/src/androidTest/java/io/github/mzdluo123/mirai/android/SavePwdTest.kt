package io.github.mzdluo123.mirai.android


import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import io.github.mzdluo123.mirai.android.activity.MainActivity
import io.github.mzdluo123.mirai.android.service.BotService
import io.github.mzdluo123.mirai.android.service.ServiceConnector
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class SavePwdTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun savePwdTest() {

        val overflowMenuButton = onView(
            allOf(
                withContentDescription("更多选项"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.toolbar),
                        2
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        overflowMenuButton.perform(click())

        val materialTextView = onView(
            allOf(
                withId(R.id.title), withText("设置自动登录"),
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
        materialTextView.perform(click())

        val appCompatEditText = onView(
            allOf(
                withId(R.id.qq_input),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.custom),
                        0
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        appCompatEditText.perform(replaceText("123"), closeSoftKeyboard())

        val appCompatEditText2 = onView(
            allOf(
                withId(R.id.password_input),
                childAtPosition(
                    childAtPosition(
                        withId(android.R.id.custom),
                        0
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        appCompatEditText2.perform(replaceText("qwe"), closeSoftKeyboard())

        val materialButton = onView(
            allOf(
                withId(android.R.id.button1), withText("设置自动登录"),
            )
        )
        materialButton.perform(scrollTo(), click())

        runBlocking {
            BotApplication.context.stopBotService()
            delay(2000)
            BotApplication.context.startBotService()
            val feature = CompletableDeferred<Boolean>()
            val conn = ServiceConnector(BotApplication.context)
            BotApplication.context.bindService(
                Intent(BotApplication.context, BotService::class.java),
                conn,
                Context.BIND_AUTO_CREATE
            )
            val console = object : IConsole.Stub() {
                override fun newLog(log: String?) {
                    if (log != null && "自动登录" in log) {
                        feature.complete(true)
                    }
                }
            }
            conn.registerConsole(console)
            BotApplication.context.getSharedPreferences("account", Context.MODE_PRIVATE).apply {
                edit().remove("qq").remove("pwd").commit()
            }
            withTimeout(5000) {
                Assert.assertTrue(feature.await())
            }

        }
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
