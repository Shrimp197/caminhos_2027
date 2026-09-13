package com.caminhos2027.v1.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import androidx.test.platform.app.InstrumentationRegistry
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
        device.waitForIdle()
        device.executeShellCommand("pm clear com.caminhos2027")
        device.executeShellCommand("monkey -p com.caminhos2027 1")
        assertTrue(
            "V1 home did not appear",
            waitForVisibleText("Prepare a sua caminhada", 120_000)
        )
    }

    @Test
    fun testVerticalSliceFromPlanToApoiDetailAndDecision() {
        prepareAndStartSr()

        assertTrue("Walking screen did not appear", waitForVisibleText("A MINHA POSIÇÃO", 30_000))
        assertTrue("Support action did not appear", waitForVisibleText("VER APOIOS", 30_000))
        assertTrue("Options action did not appear", waitForVisibleText("OPÇÕES", 30_000))
        assertTrue("QA route control did not appear", waitForVisibleText("QA · controlo do percurso", 30_000))

        clickVisibleText("VER APOIOS")
        assertTrue("Support detail did not appear", waitForAnyVisibleText(listOf("Água", "Alimentação", "Descanso", "Pernoita"), 30_000))

        clickVisibleText("Voltar")
        assertTrue("Walking screen did not return", waitForVisibleText("A MINHA POSIÇÃO", 30_000))

        clickVisibleText("OPÇÕES")
        assertTrue("Options screen did not appear", waitForVisibleText("OPÇÕES", 30_000))
        clickVisibleText("PAUSAR")
        assertTrue("Pause state did not appear", waitForAnyVisibleText(listOf("CAMINHADA PAUSADA", "RETOMAR"), 30_000))
        clickVisibleText("RETOMAR")
        assertTrue("Walking screen did not resume", waitForVisibleText("A MINHA POSIÇÃO", 30_000))

        clickVisibleText("OPÇÕES")
        clickVisibleText("PARAR AGORA")
        assertTrue("Stop confirmation did not appear", waitForAnyVisibleText(listOf("Informação para decidir", "PARAR AGORA"), 30_000))
        assertTrue("Decision information did not appear", waitForVisibleText("Informação para decidir", 30_000))
        assertTrue("Stop action did not appear", waitForVisibleText("PARAR AGORA", 30_000))
        assertTrue("Continue action did not appear", waitForVisibleText("CONTINUAR CAMINHADA", 30_000))
        clickVisibleText("CONTINUAR CAMINHADA")
        assertTrue("Continue action did not return to walking", waitForVisibleText("A MINHA POSIÇÃO", 30_000))

        clickVisibleText("OPÇÕES")
        clickVisibleText("PARAR AGORA")
        assertTrue("Stop action did not end walking", waitForVisibleText("Prepare a sua caminhada", 30_000))
    }

    private fun prepareAndStartSr() {
        clickVisibleText("PREPARAR")
        assertTrue(
            "Route selection did not appear",
            waitForVisibleText("Selecionar percurso", 30_000)
        )
        assertTrue("SR test route did not appear", waitForVisibleText("Trajeto SR", 30_000))
        clickVisibleText("Trajeto SR")
        assertTrue(
            "Preparation home did not return after route selection",
            waitForVisibleText("Pausas", 30_000)
        )

        clickVisibleText("Pausas")
        assertTrue("Smart breaks title did not appear", waitForVisibleText("Pausas inteligentes", 30_000))
        assertTrue("Minute break option did not appear", waitForVisibleText("Parar a cada X minutos", 30_000))
        assertTrue("Distance break option did not appear", waitForVisibleText("Parar a cada X km", 30_000))
        clickVisibleText("APLICAR PAUSAS")
        assertTrue("Preparation home did not return after applying breaks", waitForVisibleText("Pausas", 30_000))

        clickVisibleText("GUARDAR PLANO")
        assertTrue(
            "Saved plan did not appear",
            waitForVisibleText("Plano guardado", 30_000)
        )
        assertTrue(
            "Saved-plan explanation did not appear",
            waitForVisibleText("Guardar o plano não inicia a caminhada.", 30_000)
        )
        clickVisibleText("INICIAR CAMINHADA")
        assertTrue(
            "Walking screen did not appear after starting plan",
            waitForVisibleText("A MINHA POSIÇÃO", 30_000)
        )
    }

    private fun clickVisibleText(text: String, timeoutMs: Long = 30_000) {
        assertTrue("Text did not become clickable: $text", waitForVisibleText(text, timeoutMs))
        device.findObject(By.text(text)).click()
        device.waitForIdle()
    }

    private fun waitForVisibleText(text: String, timeoutMs: Long): Boolean =
        device.wait(Until.hasObject(By.text(text)), timeoutMs)

    private fun waitForAnyVisibleText(texts: List<String>, timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (texts.any { device.hasObject(By.text(it)) }) return true
            Thread.sleep(250)
        }
        return false
    }
}
