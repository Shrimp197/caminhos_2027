package com.caminhos2027.v1

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class V1EndToEndTest {
    private lateinit var device: UiDevice

    @Before
    fun resetToV1Activity() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.executeShellCommand("pm clear com.caminhos2027")
        device.executeShellCommand("am force-stop com.caminhos2027")
        device.executeShellCommand("am start -n com.caminhos2027/.v1.ui.V1MainActivity")
        assertTrue("Preparation screen did not appear", device.wait(Until.hasObject(By.text("Prepare a sua caminhada")), 30_000))
    }

    @Test
    fun launchPrepareTestRouteAndStartSimulatedWalk() {
        assertTrue("Prepare button did not appear", device.wait(Until.hasObject(By.text("PREPARAR")), 10_000))
        device.findObject(By.text("PREPARAR")).click()

        assertTrue("Route option did not appear", device.wait(Until.hasObject(By.text("Trajeto SR")), 10_000))
        device.findObject(By.text("Trajeto SR")).click()

        assertTrue("Save plan button did not appear", device.wait(Until.hasObject(By.text("GUARDAR PLANO")), 10_000))
        device.findObject(By.text("GUARDAR PLANO")).click()

        assertTrue("Saved plan screen did not appear", device.wait(Until.hasObject(By.text("Plano guardado")), 10_000))
        assertTrue("Start button did not appear", device.wait(Until.hasObject(By.text("INICIAR CAMINHADA")), 10_000))
        device.findObject(By.text("INICIAR CAMINHADA")).click()

        assertTrue("Test route did not reach active walking screen", device.wait(Until.hasObject(By.text("Caminhada")), 30_000))
        assertTrue(device.hasObject(By.text("QA · controlo do percurso")))
    }
}
