package io.github.mzdluo123.mirai.android


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import io.github.mzdluo123.mirai.android.activity.MainActivity
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class NavTest : TestWithIdleResources() {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)


    @Test
    fun navTest() {
        val appCompatImageButton = onView(
            allOf(
                withContentDescription("打开抽屉式导航栏"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageButton.perform(click())

        val navigationMenuItemView = onView(
            allOf(
                childAtPosition(
                    allOf(
                        withId(R.id.design_navigation_view),
                        childAtPosition(
                            withId(R.id.nav_view),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        navigationMenuItemView.perform(click())

        val appCompatImageButton2 = onView(
            allOf(
                withContentDescription("打开抽屉式导航栏"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageButton2.perform(click())

        val navigationMenuItemView2 = onView(
            allOf(
                childAtPosition(
                    allOf(
                        withId(R.id.design_navigation_view),
                        childAtPosition(
                            withId(R.id.nav_view),
                            0
                        )
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        navigationMenuItemView2.perform(click())

        val appCompatImageButton3 = onView(
            allOf(
                withContentDescription("打开抽屉式导航栏"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageButton3.perform(click())

        val navigationMenuItemView3 = onView(
            allOf(
                childAtPosition(
                    allOf(
                        withId(R.id.design_navigation_view),
                        childAtPosition(
                            withId(R.id.nav_view),
                            0
                        )
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        navigationMenuItemView3.perform(click())

        val appCompatImageButton4 = onView(
            allOf(
                withContentDescription("打开抽屉式导航栏"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageButton4.perform(click())

        val navigationMenuItemView4 = onView(
            allOf(
                childAtPosition(
                    allOf(
                        withId(R.id.design_navigation_view),
                        childAtPosition(
                            withId(R.id.nav_view),
                            0
                        )
                    ),
                    5
                ),
                isDisplayed()
            )
        )
        navigationMenuItemView4.perform(click())

        val appCompatImageButton5 = onView(
            allOf(
                withContentDescription("打开抽屉式导航栏"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageButton5.perform(click())

        val navigationMenuItemView5 = onView(
            allOf(
                childAtPosition(
                    allOf(
                        withId(R.id.design_navigation_view),
                        childAtPosition(
                            withId(R.id.nav_view),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        navigationMenuItemView5.perform(click())

        val appCompatImageButton6 = onView(
            allOf(
                withContentDescription("打开抽屉式导航栏"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
                            0
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatImageButton6.perform(click())

        val appCompatButton = onView(
            allOf(
                withId(R.id.btn_stopService), withText("退出应用"),
                childAtPosition(
                    allOf(
                        withId(R.id.nav_view),
                        childAtPosition(
                            withId(R.id.drawer_layout),
                            1
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        appCompatButton.perform(click())
    }

}
