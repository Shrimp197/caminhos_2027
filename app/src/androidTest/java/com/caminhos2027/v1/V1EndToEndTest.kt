package com.caminhos2027.v1

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class V1EndToEndTest {
    @Test
    fun launch_prepare_test_route_and_start_simulated_walk() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        val context = instrumentation.targetContext

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: error("Launch intent not found")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)

        assertTrue("Preparation screen did not appear", device.wait(Until.hasObject(By.text("Prepare a sua caminhada")), 30_000))

        assertTrue("Prepare button did not appear", device.wait(Until.hasObject(By.text("PREPARAR")), 10_000))
        device.findObject(By.text("PREPARAR")).click()

        assertTrue("Route option did not appear", device.wait(Until.hasObject(By.text("Trajeto SR")), 10_000))
        device.findObject(By.text("Trajeto SR")).click()

        assertTrue("Save plan button did not appear", device.wait(Until.hasObject(By.text("GUARDAR PLANO")), 10_000))
        device.findObject(By.text("GUARDAR PLANO")).click()

        assertTrue("Saved plan screen did not appear", device.wait(Until.hasObject(By.text("Plano guardado")), 10_000))
        assertTrue("Start button did not appear", device.wait(Until.hasObject(By.text("INICIAR CAMINHADA")), 10_000))
        device.findObject(By.text("INICIAR CAMINHADA")).click()

        assertTrue(
            "Test route did not enter simulated start flow",
            device.wait(Until.hasObject(By.textContains("A iniciar pelo percurso de teste")), 30_000)
                || device.wait(Until.hasObject(By.text("Terminar caminhada")), 30_000)
        )

        assertTrue(
            "Test route did not reach active walking screen",
            device.wait(Until.hasObject(By.text("Terminar caminhada")), 30_000)
        )
    }
}
