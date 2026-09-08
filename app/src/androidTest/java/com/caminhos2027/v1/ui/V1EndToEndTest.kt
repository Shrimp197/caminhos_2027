package com.caminhos2027.v1.ui

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
    fun resetToCleanV1() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.executeShellCommand("pm clear com.caminhos2027")
        device.executeShellCommand("am force-stop com.caminhos2027")
        device.executeShellCommand("am start -n com.caminhos2027/.v1.ui.V1MainActivity")
        assertTrue(device.wait(Until.hasObject(By.text("Prepare a sua caminhada")), 30_000))
    }

    @Test
    fun testRouteCanBePreparedStartedAndWalkingSurfacesOpen() {
        assertTrue(device.wait(Until.hasObject(By.text("PREPARAR")), 10_000))
        device.findObject(By.text("PREPARAR")).click()

        assertTrue(device.wait(Until.hasObject(By.text("Trajeto SR")), 10_000))
        device.findObject(By.text("Trajeto SR")).click()

        assertTrue(device.wait(Until.hasObject(By.textContains("INICIAR CAMINHADA")), 10_000))
        device.findObject(By.textContains("INICIAR CAMINHADA")).click()

        assertTrue(device.wait(Until.hasObject(By.text("Plano guardado")), 10_000))
        device.findObject(By.text("INICIAR CAMINHADA")).click()

        assertTrue(device.wait(Until.hasObject(By.text("Caminhada")), 30_000))
        assertTrue(device.hasObject(By.text("VER APOIOS")))
        assertTrue(device.hasObject(By.text("OPÇÕES")))
        assertTrue(device.hasObject(By.text("QA · controlo do percurso")))

        device.findObject(By.text("VER APOIOS")).click()
        assertTrue(device.wait(Until.hasObject(By.text("Apoios")), 10_000))
        device.pressBack()

        assertTrue(device.wait(Until.hasObject(By.text("Caminhada")), 10_000))
        device.findObject(By.text("OPÇÕES")).click()
        assertTrue(device.wait(Until.hasObject(By.text("Informação para decidir")), 10_000))
        assertTrue(device.hasObject(By.text("Parar agora")))
        assertTrue(device.hasObject(By.text("Continuar")))
    }
}
