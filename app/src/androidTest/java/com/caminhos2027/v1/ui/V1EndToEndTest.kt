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
    fun testVerticalSliceFromPlanToApoiDetailAndDecision() {
        prepareAndStartSr()

        assertTrue(device.hasObject(By.text("VER APOIOS")))
        assertTrue(device.hasObject(By.text("OPÇÕES")))
        assertTrue(device.hasObject(By.text("QA · controlo do percurso")))
        assertTrue(
            "No GPS state was exposed",
            waitForAnyVisibleText("A obter sinal GPS", "GPS no percurso", timeoutMs = 30_000)
        )

        clickVisibleText("PERDER GPS")
        assertTrue("GPS loss state did not appear", device.wait(Until.hasObject(By.text("GPS sem sinal")), 30_000))
        clickVisibleText("RECUPERAR")
        assertTrue("GPS recovery state did not appear", device.wait(Until.hasObject(By.text("GPS no percurso")), 30_000))

        clickVisibleText("VER APOIOS")
        assertTrue(
            "Apoios screen did not appear",
            device.wait(Until.hasObject(By.text("Próximos 10 km")), 30_000)
        )
        assertTrue(device.hasObject(By.text("Procurar")))
        assertTrue(device.hasObject(By.text("Água")))
        assertTrue(device.hasObject(By.text("Água SR — TESTE")))

        clickVisibleText("Água")
        assertTrue("Water filter removed the expected result", device.wait(Until.hasObject(By.text("Água SR — TESTE")), 30_000))

        clickVisibleText("Água SR — TESTE")
        assertTrue("APOI detail did not appear", device.wait(Until.hasObject(By.text("Localização")), 30_000))
        assertTrue(device.hasObject(By.text("Serviços")))
        assertTrue(device.hasObject(By.text("Navegar a pé")) || device.hasObject(By.text("Localização exata")))

        device.pressBack()
        device.waitForIdle()
        assertTrue("APOI browser did not return", device.wait(Until.hasObject(By.text("Procurar")), 30_000))
        device.pressBack()
        device.waitForIdle()
        assertTrue("Walking screen did not return", device.wait(Until.hasObject(By.text("Caminhada")), 30_000))

        clickVisibleText("OPÇÕES")
        assertTrue(
            "Decision information did not appear",
            device.wait(Until.hasObject(By.text("Informação para decidir")), 30_000)
        )
        assertTrue(device.hasObject(By.text("PARAR AGORA")))
        assertTrue(device.hasObject(By.text("CONTINUAR CAMINHADA")))
        clickVisibleText("CONTINUAR CAMINHADA")
        assertTrue("Continue action did not return to walking", device.wait(Until.hasObject(By.text("Caminhada")), 30_000))

        clickVisibleText("OPÇÕES")
        clickVisibleText("PARAR AGORA")
        assertTrue("Stop action did not end walking", device.wait(Until.hasObject(By.text("Prepare a sua caminhada")), 30_000))
    }

    private fun prepareAndStartSr() {
        clickVisibleText("PREPARAR")
        assertTrue(
            "Route selection did not appear",
            device.wait(Until.hasObject(By.text("Trajeto SR")), 30_000)
        )
        clickVisibleText("Trajeto SR")
        assertTrue(
            "Plan save action did not appear",
            device.wait(Until.hasObject(By.text("GUARDAR PLANO")), 30_000)
        )
        clickVisibleText("GUARDAR PLANO")
        assertTrue(
            "Saved plan did not appear",
            device.wait(Until.hasObject(By.text("Plano guardado")), 30_000)
        )
        assertTrue(device.hasObject(By.text("Guardar o plano não inicia a caminhada.")))
        clickVisibleText("INICIAR CAMINHADA")
        assertTrue(
            "Walking screen did not appear",
            device.wait(Until.hasObject(By.text("Caminhada")), 30_000)
        )
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

    private fun waitForAnyVisibleText(vararg texts: String, timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (texts.any { device.hasObject(By.text(it)) }) return true
            device.waitForIdle()
            Thread.sleep(250)
        }
        return texts.any { device.hasObject(By.text(it)) }
    }
}
