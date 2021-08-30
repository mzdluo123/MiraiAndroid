package io.github.mzdluo123.mirai.android

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.mzdluo123.mirai.android.ui.about.AboutFragment
import io.github.mzdluo123.mirai.android.ui.console.ConsoleFragment
import io.github.mzdluo123.mirai.android.ui.plugin.PluginFragment
import io.github.mzdluo123.mirai.android.ui.script.ScriptFragment
import io.github.mzdluo123.mirai.android.ui.tools.ToolsFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PageLoadTest {
//
//    @get:Rule
//    val activityRule = ActivityTestRule(MainActivity::class.java)


    @Test
    fun consoleTest() {
        with(launchFragmentInContainer<ConsoleFragment>()) {
            onView(withId(R.id.commandSend_btn)).check(ViewAssertions.matches(isDisplayed()))
        }
    }

    @Test
    fun pluginsTest() {
        with(launchFragmentInContainer<PluginFragment>()) {
            onView(withId(R.id.plugin_recycler)).check(ViewAssertions.matches(isDisplayed()))
        }
    }

    @Test
    fun scriptTest() {
        with(launchFragmentInContainer<ScriptFragment>()) {
            onView(withId(R.id.script_embed_text)).check(ViewAssertions.matches(isDisplayed()))
        }
    }

    @Test
    fun aboutTest() {
        with(launchFragmentInContainer<AboutFragment>()) {
            onView(withText("关于Mirai")).check(ViewAssertions.matches(isDisplayed()))
        }
    }

    @Test
    fun toolsTest() {
        with(launchFragmentInContainer<ToolsFragment>(themeResId = R.style.AppTheme)) {
            onView(withId(R.id.btn_export_device)).check(ViewAssertions.matches(isDisplayed()))
        }
    }
}