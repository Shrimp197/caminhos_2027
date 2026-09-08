package com.caminhos2027.v1.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class V1EndToEndTest {
    private lateinit var device: UiDevice

    @Before
    fun resetToV1() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressHome()
        device.executeShellCommand("am start -n com.caminhos2027/.v1.ui.V1MainActivity")
        assertTrue(
            "Preparation screen did not appear",
            device.wait(Until.hasObject(By.text("Prepare a sua caminhada")), 60_000)
        )
    }

    @After
    fun closeActivity() {
        device.pressHome()
    }

    @Test
    fun testRouteCanBePreparedStartedAndWalkingSurfacesOpen() {
        clickVisibleText("PREPARAR")

        assertTrue(
            "Route selection did not appear",
            device.wait(Until.hasObject(By.text("Trajeto SR")), 10_000)
        )
        clickVisibleText("Trajeto SR")

        assertTrue(
            "Start action did not appear",
            device.wait(Until.hasObject(By.textContains("INICIAR CAMINHADA")), 10_000)
        )
        clickVisibleTextContaining("INICIAR CAMINHADA")

        assertTrue(
            "Saved plan did not appear",
            device.wait(Until.hasObject(By.text("Plano guardado")), 10_000)
        )
        clickVisibleText("INICIAR CAMINHADA")

        assertTrue(
            "Walking screen did not appear",
            device.wait(Until.hasObject(By.text("Caminhada")), 30_000)
        )
        assertTrue(device.hasObject(By.text("VER APOIOS")))
        assertTrue(device.hasObject(By.text("OPÇÕES")))
        assertTrue(device.hasObject(By.text("QA · controlo do percurso")))

        clickVisibleText("VER APOIOS")
        assertTrue(
            "Apoi​os screen did not appear",
            device.wait(Until.hasObject(By.text("Próximos 10 km")), 10_000)
        )
        assertTrue(device.hasObject(By.textContains("Apoios")))
        device.pressBack()

        assertTrue(
            "Walking screen did not return",
            device.wait(Until.hasObject(By.text("Caminhada")), 10_000)
        )
        clickVisibleText("OPÇÕES")
        assertTrue(
            "Decision information did not appear",
            device.wait(Until.hasObject(By.text("Informação para decidir")), 10_000)
        )
        assertTrue(device.hasObject(By.text("Parar agora")))
        assertTrue(device.hasObject(By.text("Continuar")))
    }

    private fun clickVisibleText(text: String) {
        val object = device.wait(Until.findObject(By.text(text)), 10_000)
        assertTrue("Text not found: $text", object != null)
        val bounds = object.visibleBounds
        assertTrue("Text is not visible: $text", !bounds.isEmpty)
        device.click(bounds.centerX(), bounds.centerY())
    }

    private fun clickVisibleTextContaining(text: String) {
        val object = device.wait(Until.findObject(By.textContains(text)), 10_000)
        assertTrue("Text not found: $text", object != null)
        val bounds = object.visibleBounds
        assertTrue("Text is not visible: $text", !bounds.isEmpty)
        device.click(bounds.centerX(), bounds.centerY())
    }
}
