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
import java.io.File

@RunWith(AndroidJUnit4::class)
class V1EndToEndTest {
    private lateinit var device: UiDevice
    private lateinit var scenario: ActivityScenario<V1MainActivity>

    @Before
    fun resetToV1() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressHome()
        scenario = ActivityScenario.launch(V1MainActivity::class.java)
        assertTrue("Preparation screen did not appear", device.wait(Until.hasObject(By.text("Prepare a sua caminhada")), 120_000))
        capture("/data/local/tmp/caminhos-preparacao.png")
    }

    @After
    fun closeActivity() { scenario.close() }

    @Test
    fun testVerticalSliceFromPlanToApoiDetailAndDecision() {
        prepareAndStartSr()
        assertTrue("Walking actions did not appear", waitForAnyVisibleText("VER APOIOS", "OPÇÕES", timeoutMs = 30_000))
        assertTrue("Walking map host surface did not appear", waitForAnyVisibleText("A MINHA POSIÇÃO", "VER APOIOS", "OPÇÕES", timeoutMs = 30_000))
        capture("/data/local/tmp/caminhos-navegacao.png")
        assertTrue("QA route controls did not appear", waitForVisibleText("QA · controlo do percurso", 30_000))
        assertTrue("No GPS state was exposed", waitForAnyVisibleText("A obter sinal GPS", "GPS no percurso", timeoutMs = 30_000))

        clickVisibleText("PERDER GPS")
        assertTrue("GPS loss state did not appear", waitForVisibleText("GPS sem sinal", 30_000))
        clickVisibleText("RECUPERAR")
        assertTrue("GPS recovery state did not appear", waitForVisibleText("GPS no percurso", 30_000))

        clickVisibleText("VER APOIOS")
        assertTrue("Apoios screen did not appear", waitForVisibleText("Próximos 10 km", 30_000))
        assertTrue("Apoios search did not appear", waitForVisibleText("Procurar", 30_000))
        assertTrue("Water filter did not appear", waitForVisibleText("Água", 30_000))
        assertTrue("Expected water APOI did not appear", waitForVisibleText("Água SR — TESTE", 30_000))
        clickVisibleText("Água")
        assertTrue("Water filter removed the expected result", waitForVisibleText("Água SR — TESTE", 30_000))
        clickVisibleText("Água SR — TESTE")
        assertTrue("APOI detail did not appear", waitForVisibleText("Localização", 30_000))
        assertTrue("APOI services did not appear", waitForVisibleText("Serviços", 30_000))
        assertTrue("APOI navigation/location action did not appear", waitForAnyVisibleText("Navegar a pé", "Localização exata", timeoutMs = 30_000))

        device.pressBack(); device.waitForIdle()
        assertTrue("APOI browser did not return", waitForVisibleText("Procurar", 30_000))
        device.pressBack(); device.waitForIdle()
        assertTrue("Walking screen did not return", waitForAnyVisibleText("A MINHA POSIÇÃO", "VER APOIOS", "OPÇÕES", timeoutMs = 30_000))

        clickVisibleText("OPÇÕES")
        assertTrue("Decision information did not appear", waitForVisibleText("Informação para decidir", 30_000))
        assertTrue("Stop action did not appear", waitForVisibleText("PARAR AGORA", 30_000))
        assertTrue("Continue action did not appear", waitForVisibleText("CONTINUAR CAMINHADA", 30_000))
        clickVisibleText("CONTINUAR CAMINHADA")
        assertTrue("Continue action did not return to walking", waitForAnyVisibleText("A MINHA POSIÇÃO", "VER APOIOS", "OPÇÕES", timeoutMs = 30_000))
        clickVisibleText("OPÇÕES"); clickVisibleText("PARAR AGORA")
        assertTrue("Stop action did not end walking", waitForVisibleText("Prepare a sua caminhada", 30_000))
    }

    private fun prepareAndStartSr() {
        clickVisibleText("PREPARAR")
        assertTrue("Route selection did not appear", waitForVisibleText("Selecionar percurso", 30_000))
        assertTrue("SR test route did not appear", waitForVisibleText("Trajeto SR", 30_000))
        clickVisibleText("Trajeto SR")
        assertTrue("Preparation home did not return after route selection", waitForAnyVisibleText("Orientação / Pausas", "Pausas", timeoutMs = 30_000))
        if (device.hasObject(By.text("Orientação / Pausas"))) clickVisibleText("Orientação / Pausas") else clickVisibleText("Pausas")
        assertTrue("Breaks screen did not appear", waitForAnyVisibleText("Pausas inteligentes", "Pausas", timeoutMs = 30_000))
        assertTrue("Minute break option did not appear", waitForVisibleText("Parar a cada X minutos", 30_000))
        assertTrue("Distance break option did not appear", waitForVisibleText("Parar a cada X km", 30_000))
        clickVisibleText("APLICAR PAUSAS")
        assertTrue("Preparation screen did not return after breaks", waitForAnyVisibleText("Orientação / Pausas", "Pausas", timeoutMs = 30_000))
        clickVisibleText("INICIAR CAMINHADA")
        assertTrue("Saved plan did not appear", waitForVisibleText("Plano guardado", 30_000))
        assertTrue("Saved-plan explanation did not appear", waitForVisibleText("Guardar o plano não inicia a caminhada.", 30_000))
        clickVisibleText("INICIAR CAMINHADA")
        assertTrue("Walking screen did not appear", waitForAnyVisibleText("A MINHA POSIÇÃO", "VER APOIOS", "OPÇÕES", timeoutMs = 30_000))
    }

    private fun capture(path: String) {
        val file = File(path)
        file.delete()
        assertTrue("Screenshot could not be captured: $path", device.takeScreenshot(file))
        assertTrue("Screenshot was not created: $path", file.isFile && file.length() > 0)
    }

    private fun clickVisibleText(text: String) {
        device.waitForIdle()
        repeat(8) {
            val node = device.findObject(By.text(text))
            if (node != null && !node.visibleBounds.isEmpty) { device.click(node.visibleBounds.centerX(), node.visibleBounds.centerY()); device.waitForIdle(); return }
            device.swipe(device.displayWidth / 2, (device.displayHeight * 0.82).toInt(), device.displayWidth / 2, (device.displayHeight * 0.28).toInt(), 8)
            device.waitForIdle()
        }
        val node = device.wait(Until.findObject(By.text(text)), 5_000)
        assertTrue("Text not found or not visible: $text", node != null && !node.visibleBounds.isEmpty)
        device.click(node.visibleBounds.centerX(), node.visibleBounds.centerY()); device.waitForIdle()
    }

    private fun waitForVisibleText(text: String, timeoutMs: Long): Boolean = device.wait(Until.hasObject(By.text(text)), timeoutMs)

    private fun waitForAnyVisibleText(vararg texts: String, timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (texts.any { device.hasObject(By.text(it)) }) return true
            device.waitForIdle(); Thread.sleep(250)
        }
        return texts.any { device.hasObject(By.text(it)) }
    }
}
