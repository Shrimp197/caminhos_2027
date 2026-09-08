package com.caminhos2027.v1.ui

import androidx.test.core.app.ActivityScenario
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
    private lateinit var scenario: ActivityScenario<V1MainActivity>

    @Before
    fun resetToV1() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressHome()
        scenario = ActivityScenario.launch(V1MainActivity::class.java)
        assertTrue(
            "Preparation screen did not appear",
            device.wait(Until.hasObject(By.text("Prepare a sua caminhada")), 120_000)
        )
    }

    @After
    fun closeActivity() {
        scenario.close()
    }

    @Test
    fun testRouteCanBePreparedStartedAndWalkingSurfacesOpen() {
        clickVisibleText("PREPARAR")

        assertTrue(
            "Route selection did not appear",
            device.wait(Until.hasObject(By.text("Trajeto SR")), 30_000)
        )
        clickVisibleText("Trajeto SR")

        assertTrue(
            "Start action did not appear",
            device.wait(Until.hasObject(By.textContains("INICIAR CAMINHADA")), 30_000)
        )
        clickVisibleTextContaining("INICIAR CAMINHADA")

        assertTrue(
            "Saved plan did not appear",
            device.wait(Until.hasObject(By.text("Plano guardado")), 30_000)
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
            "Apoios screen did not appear",
            device.wait(Until.hasObject(By.text("Próximos 10 km")), 30_000)
        )
        assertTrue(device.hasObject(By.textContains("Apoios")))
        device.pressBack()
        device.waitForIdle()

        assertTrue(
            "Walking screen did not return",
            device.wait(Until.hasObject(By.text("Caminhada")), 30_000)
        )
        clickVisibleText("OPÇÕES")
        assertTrue(
            "Decision information did not appear",
            device.wait(Until.hasObject(By.text("Informação para decidir")), 30_000)
        )
        assertTrue(device.hasObject(By.text("Parar agora")))
        assertTrue(device.hasObject(By.text("Continuar")))
    }

    private fun clickVisibleText(text: String) {
        device.waitForIdle()
        val node = device.wait(Until.findObject(By.text(text)), 30_000)
        assertTrue("Text not found: $text", node != null)
        val bounds = node.visibleBounds
        assertTrue("Text is not visible: $text", !bounds.isEmpty)
        device.click(bounds.centerX(), bounds.centerY())
        device.waitForIdle()
    }

    private fun clickVisibleTextContaining(text: String) {
        device.waitForIdle()
        val node = device.wait(Until.findObject(By.textContains(text)), 30_000)
        assertTrue("Text not found: $text", node != null)
        val bounds = node.visibleBounds
        assertTrue("Text is not visible: $text", !bounds.isEmpty)
        device.click(bounds.centerX(), bounds.centerY())
        device.waitForIdle()
    }
}
