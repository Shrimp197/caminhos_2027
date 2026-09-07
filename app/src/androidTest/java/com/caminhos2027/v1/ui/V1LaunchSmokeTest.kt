package com.caminhos2027.v1.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class V1LaunchSmokeTest {
    @Test
    fun v1ActivityLaunches() {
        ActivityScenario.launch(V1MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                check(!activity.isFinishing) { "V1 activity finished during launch" }
            }
        }
    }
}
