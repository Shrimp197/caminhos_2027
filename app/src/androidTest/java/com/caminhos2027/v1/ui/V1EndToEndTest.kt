package com.caminhos2027.v1.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.StaleObjectException
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
        assertTrue(
            "Route-selection preparation screen did not appear",
            waitForVisibleText("Selecionar percurso", 120_000)
        )
        capture("caminhos-preparacao.png")
    }

    @After
    fun closeActivity() { scenario.close() }

    @Test
    fun testVerticalSliceFromPlanToApoiDetailAndDecision() {
        assertTrue("Centenário route option missing", waitForVisibleText("Caminho do Centenário", 30_000))
        clickVisibleText("Caminho do Centenário")
        assertTrue("Preparation home did not appear after route selection", waitForVisibleText("Prepare a sua caminhada", 30_000))

        prepareAndStartSr()
        assertTrue("Walking actions did not appear", waitForAnyVisibleText("VER APOIOS", "OPÇÕES", timeoutMs = 30_000))
        assertTrue("Real map label did not appear", waitForVisibleTextOrDescription("MAPA REAL · OPENSTREETMAP", 30_000))
        assertTrue("Position label did not appear", waitForVisibleText("A minha posição", 30_000))
        assertTrue("Route progress did not appear", waitForVisibleText("Progresso", 30_000))
        assertTrue("Elapsed walking time did not appear", waitForVisibleText("Tempo", 30_000))
        assertTrue("QA controls did not appear for the test route", waitForVisibleText("QA · percurso de teste", 30_000))
        clickVisibleText("AVANÇAR GPS")
        assertTrue("Simulated GPS advance did not update the route position", waitForVisibleText("Km no percurso:", 30_000))
        clickVisibleText("PERDER GPS")
        assertTrue("GPS loss state did not appear", waitForVisibleText("GPS sem sinal", 30_000))
        clickVisibleText("RECUPERAR GPS")
        assertTrue("GPS recovery state did not appear", waitForVisibleText("GPS no percurso", 30_000))
        clickVisibleText("SIMULAR DESVIO")
        clickVisibleText("SIMULAR DESVIO")
        assertTrue(
            "Deviation state did not appear",
            waitForAnyVisibleText("Possível desvio", "Provável desvio", timeoutMs = 30_000)
        )

        val positionBeforePause = visibleTextValue("Km no percurso:")
        clickVisibleText("PAUSAR CAMINHADA")
        assertTrue("Pause state did not appear", waitForVisibleText("CAMINHADA PAUSADA", 30_000))
        assertTrue("Resume action did not appear", waitForVisibleText("RETOMAR CAMINHADA", 30_000))
        val positionWhilePaused = visibleTextValue("Km no percurso:")
        assertTrue(
            "Paused walk changed route position before recreation",
            positionBeforePause != null && positionBeforePause == positionWhilePaused
        )

        scenario.close()
        scenario = ActivityScenario.launch(V1MainActivity::class.java)
        assertTrue("Paused walking session did not persist", waitForVisibleText("CAMINHADA PAUSADA", 30_000))
        assertTrue("Persisted pause did not expose resume action", waitForVisibleText("RETOMAR CAMINHADA", 30_000))
        val persistedPausedPosition = visibleTextValue("Km no percurso:")
        assertTrue(
            "Persisted paused position was lost",
            positionWhilePaused != null && positionWhilePaused == persistedPausedPosition
        )

        clickVisibleText("RETOMAR CAMINHADA")
        assertTrue("Walking session did not resume", waitForVisibleText("PAUSAR CAMINHADA", 30_000))
        clickVisibleText("AVANÇAR GPS")
        val positionAfterResume = visibleTextValue("Km no percurso:")
        assertTrue(
            "GPS did not advance after pause/resume",
            persistedPausedPosition != null && persistedPausedPosition != positionAfterResume
        )
        capture("caminhos-navegacao.png")

        clickVisibleText("PAUSAR CAMINHADA")
        assertTrue("Pause state did not appear", waitForVisibleText("CAMINHADA PAUSADA", 30_000))
        scenario.close()
        scenario = ActivityScenario.launch(V1MainActivity::class.java)
        assertTrue("Paused walking state did not persist after activity recreation", waitForVisibleText("CAMINHADA PAUSADA", 30_000))
        clickVisibleText("RETOMAR CAMINHADA")
        assertTrue("Walking session did not resume", waitForVisibleText("A minha posição", 30_000))

        clickVisibleText("VER APOIOS")
        assertTrue("Apoios screen did not appear", waitForVisibleText("Próximos 10 km", 30_000))
        assertTrue("Apoios search did not appear", waitForVisibleText("Procurar", 30_000))
        assertTrue("Water filter did not appear", waitForVisibleText("Água", 30_000))
        assertTrue("Expected water APOI did not appear", waitForVisibleText("Água SR — TESTE", 30_000))
        clickVisibleText("Água SR — TESTE")
        assertTrue("APOI detail did not appear", waitForVisibleText("Localização", 30_000))
        assertTrue("APOI services did not appear", waitForVisibleText("Serviços", 30_000))
        device.pressBack(); device.waitForIdle()
        assertTrue("APOI browser did not return", waitForVisibleText("Procurar", 30_000))
        device.pressBack(); device.waitForIdle()
        assertTrue("Walking screen did not return", waitForVisibleText("A minha posição", 30_000))

        clickVisibleText("OPÇÕES")
        assertTrue("Decision information did not appear", waitForVisibleText("Informação para decidir", 30_000))
        assertTrue("Stop action did not appear", waitForVisibleText("PARAR AGORA", 30_000))
        assertTrue("Continue action did not appear", waitForVisibleText("CONTINUAR CAMINHADA", 30_000))
        clickVisibleText("CONTINUAR CAMINHADA")
        assertTrue("Continue action did not return to walking", waitForVisibleText("A minha posição", 30_000))
        clickVisibleText("OPÇÕES"); clickVisibleText("PARAR AGORA")
        assertTrue("Stop action did not end walking", waitForVisibleText("Prepare a sua caminhada", 30_000))

        prepareAndStartSr()
        assertTrue("Second walking cycle did not start", waitForVisibleText("A minha posição", 30_000))
        clickVisibleText("OPÇÕES"); clickVisibleText("PARAR AGORA")
        assertTrue("Second walking cycle did not stop cleanly", waitForVisibleText("Prepare a sua caminhada", 30_000))
    }

    private fun prepareAndStartSr() {
        clickVisibleText("PREPARAR")
        assertTrue("Route selection did not appear", waitForVisibleText("Selecionar percurso", 30_000))
        assertTrue("SR test route did not appear", waitForVisibleText("Trajeto SR", 30_000))
        clickVisibleText("Trajeto SR")
        assertTrue("Preparation home did not return after route selection", waitForVisibleText("Pausas", 30_000))
        clickVisibleText("Áudio")
        assertTrue("Audio configuration did not appear", waitForVisibleText("Áudio", 30_000))
        assertTrue("Immersive audio option did not appear", waitForVisibleText("ÁUDIO IMERSIVO", 30_000))
        clickVisibleText("ÁUDIO IMERSIVO")
        clickVisibleText("APLICAR")
        assertTrue("Audio configuration did not close", waitForVisibleText("Prepare a sua caminhada", 30_000))

        clickVisibleText("Orientação")
        assertTrue("Orientation configuration did not appear", waitForVisibleText("Orientação", 30_000))
        assertTrue("Walking-direction orientation option did not appear", waitForVisibleText("DIREÇÃO DA CAMINHADA", 30_000))
        clickVisibleText("DIREÇÃO DA CAMINHADA")
        clickVisibleText("APLICAR")
        assertTrue("Orientation configuration did not close", waitForVisibleText("Prepare a sua caminhada", 30_000))

        clickVisibleText("Apoios")
        assertTrue("Apoios configuration did not appear", waitForVisibleText("Apoios", 30_000))
        assertTrue("Water APOI category did not appear", waitForVisibleText("Água", 30_000))
        clickVisibleText("Água")
        clickVisibleText("APLICAR APOIOS")
        assertTrue("APOIs configuration did not close", waitForVisibleText("Prepare a sua caminhada", 30_000))

        clickVisibleText("Notas")
        assertTrue("Notes screen did not appear", waitForVisibleText("Notas", 30_000))
        assertTrue("Note input did not appear", waitForVisibleText("Nova nota", 30_000))
        setVisibleTextField("Nota E2E persistida")
        clickVisibleText("GUARDAR NOTA")
        assertTrue("Saved note did not return to preparation", waitForVisibleText("Prepare a sua caminhada", 30_000))
        clickVisibleText("Pausas")
        assertTrue("Breaks screen did not appear", waitForAnyVisibleText("Pausas inteligentes", "Pausas", timeoutMs = 30_000))
        assertTrue("Minute break option did not appear", waitForVisibleText("Parar a cada X minutos", 30_000))
        assertTrue("Distance break option did not appear", waitForVisibleText("Parar a cada X km", 30_000))
        clickVisibleText("APLICAR PAUSAS")
        assertTrue("Preparation screen did not return after breaks", waitForAnyVisibleText("Orientação / Pausas", "Pausas", timeoutMs = 30_000))
        clickVisibleText("GUARDAR PLANO")
        assertTrue("Saved plan did not appear", waitForVisibleText("Plano guardado", 30_000))
        assertTrue("Saved-plan explanation did not appear", waitForVisibleText("Guardar o plano não inicia a caminhada.", 30_000))
        assertTrue("Saved plan did not show persisted audio choice", waitForVisibleText("Áudio · imersivo", 30_000))
        assertTrue("Saved plan did not show persisted orientation choice", waitForVisibleText("Orientação · direção da caminhada", 30_000))
        assertTrue("Saved plan did not show persisted APOI choice", waitForVisibleText("Apoios · 1 tipo(s) selecionado(s)", 30_000))
        assertTrue("Saved plan did not show persisted note count", waitForVisibleText("Notas · 1 guardada(s)", 30_000))
        assertTrue("Saved plan did not show persisted pause configuration", waitForVisibleText("Pausas · inteligentes ativas", 30_000))
        assertTrue("Saved plan did not expose an explicit start action", waitForVisibleText("INICIAR CAMINHADA", 30_000))
        scenario.close()
        scenario = ActivityScenario.launch(V1MainActivity::class.java)
        assertTrue("Saved plan did not persist after activity recreation", waitForVisibleText("Plano guardado", 30_000))
        assertTrue("Persisted plan lost the audio choice", waitForVisibleText("Áudio · imersivo", 30_000))
        assertTrue("Persisted plan lost the orientation choice", waitForVisibleText("Orientação · direção da caminhada", 30_000))
        assertTrue("Persisted plan lost the APOI choice", waitForVisibleText("Apoios · 1 tipo(s) selecionado(s)", 30_000))
        assertTrue("Persisted plan lost the note count", waitForVisibleText("Notas · 1 guardada(s)", 30_000))
        assertTrue("Persisted plan did not keep explicit start action", waitForVisibleText("INICIAR CAMINHADA", 30_000))
        clickVisibleText("INICIAR CAMINHADA")
        assertTrue("Walking screen did not appear", waitForVisibleText("A minha posição", 30_000))
    }

    private fun capture(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.filesDir, name)
        file.delete()
        assertTrue("Screenshot could not be captured: $name", device.takeScreenshot(file))
        assertTrue("Screenshot was not created: $name", file.isFile && file.length() > 0)
    }

    private fun setVisibleTextField(value: String) {
        val field = device.wait(Until.findObject(By.clazz("android.widget.EditText")), 5_000)
        assertTrue("Editable note field was not found", field != null)
        field!!.click()
        field.setText(value)
        device.waitForIdle()
    }

    private fun visibleTextValue(prefix: String): String? {
        val node = findVisibleTextOrDescription(prefix) ?: return null
        return try {
            node.text
        } catch (_: StaleObjectException) {
            null
        }
    }

    private fun clickVisibleText(text: String) {
        device.waitForIdle()
        repeat(12) {
            val node = findVisibleTextOrDescription(text)
            if (node != null) {
                try {
                    val bounds = node.visibleBounds
                    if (bounds.isEmpty) {
                        throw IllegalStateException("Visible node has no usable bounds: $text")
                    }
                    if (node.isClickable) {
                        node.click()
                    } else {
                        device.click(bounds.centerX(), bounds.centerY())
                    }
                    device.waitForIdle()
                    return
                } catch (_: StaleObjectException) {
                    // Re-query on the next iteration; Compose may replace the node between lookup and click.
                }
            }
            device.swipe(
                device.displayWidth / 2,
                (device.displayHeight * 0.82).toInt(),
                device.displayWidth / 2,
                (device.displayHeight * 0.28).toInt(),
                8
            )
            device.waitForIdle()
        }
        val node = device.wait(Until.findObject(By.textContains(text)), 5_000)
            ?: device.wait(Until.findObject(By.descContains(text)), 5_000)
        val visible = try {
            node != null && !node.visibleBounds.isEmpty
        } catch (_: StaleObjectException) {
            false
        }
        assertTrue("Text or content description not found or not visible: $text", visible)
        try {
            if (node!!.isClickable) {
                node.click()
            } else {
                val bounds = node.visibleBounds
                assertTrue("Visible node has no usable bounds: $text", !bounds.isEmpty)
                device.click(bounds.centerX(), bounds.centerY())
            }
        } catch (_: StaleObjectException) {
            val retry = findVisibleTextOrDescription(text)
            assertTrue("Text or content description became stale before click: $text", retry != null)
            val bounds = retry!!.visibleBounds
            assertTrue("Retry node has no usable bounds: $text", !bounds.isEmpty)
            device.click(bounds.centerX(), bounds.centerY())
        }
        device.waitForIdle()
    }

    private fun findVisibleTextOrDescription(text: String): androidx.test.uiautomator.UiObject2? {
        val descriptionNode = device.findObject(By.descContains(text))
        try {
            if (descriptionNode != null && !descriptionNode.visibleBounds.isEmpty) return descriptionNode
        } catch (_: StaleObjectException) {
            // Fall through to text lookup.
        }
        val textNode = device.findObject(By.textContains(text))
        return try {
            textNode?.takeIf { !it.visibleBounds.isEmpty }
        } catch (_: StaleObjectException) {
            null
        }
    }

    private fun waitForVisibleTextOrDescription(text: String, timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val node = findVisibleTextOrDescription(text)
            if (node != null) return true
            device.waitForIdle()
            Thread.sleep(150)
        }
        return findVisibleTextOrDescription(text) != null
    }

    private fun waitForVisibleText(text: String, timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val node = device.findObject(By.textContains(text))
            try {
                if (node != null && !node.visibleBounds.isEmpty) return true
            } catch (_: StaleObjectException) {
                continue
            }
            device.swipe(
                device.displayWidth / 2,
                (device.displayHeight * 0.82).toInt(),
                device.displayWidth / 2,
                (device.displayHeight * 0.28).toInt(),
                8
            )
            device.waitForIdle()
            Thread.sleep(150)
        }
        return false
    }

    private fun waitForAnyVisibleText(vararg texts: String, timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (texts.any { device.hasObject(By.text(it)) }) return true
            device.waitForIdle(); Thread.sleep(250)
        }
        return texts.any { device.hasObject(By.text(it)) }
    }
}
