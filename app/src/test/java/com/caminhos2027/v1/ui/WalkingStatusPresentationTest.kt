package com.caminhos2027.v1.ui

import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.route.GpsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WalkingStatusPresentationTest {
    @Test
    fun noSignalExplainsThatLastReliablePositionIsKept() {
        val presentation = WalkingStatusPresentation.gps(GpsState.NO_SIGNAL)

        assertEquals("Sem sinal GPS", presentation.label)
        assertEquals(
            "A última posição fiável mantém-se até existir uma nova posição confirmada.",
            presentation.detail
        )
    }

    @Test
    fun deviationExplainsThatPositionIsHeldForSafety() {
        val presentation = WalkingStatusPresentation.gps(GpsState.POSSIBLE_DEVIATION)

        assertEquals("Possível desvio", presentation.label)
        assertEquals(
            "Há sinais afastados do traçado. A posição fiável anterior é mantida por precaução.",
            presentation.detail
        )
    }

    @Test
    fun probableDeviationAsksForVisualConfirmation() {
        val presentation = WalkingStatusPresentation.gps(GpsState.PROBABLE_DEVIATION)

        assertEquals("Provável desvio", presentation.label)
        assertEquals(
            "Vários sinais afastados do traçado foram observados. Confirme visualmente o caminho antes de continuar.",
            presentation.detail
        )
    }

    @Test
    fun positionConfidenceUsesExplicitPortugueseLabels() {
        assertEquals("Confiança da posição: alta", WalkingStatusPresentation.confidence(PositionConfidence.HIGH))
        assertEquals("Confiança da posição: média", WalkingStatusPresentation.confidence(PositionConfidence.MEDIUM))
        assertEquals("Confiança da posição: baixa", WalkingStatusPresentation.confidence(PositionConfidence.LOW))
        assertEquals("Confiança da posição: não conhecida", WalkingStatusPresentation.confidence(PositionConfidence.UNKNOWN))
    }

    @Test
    fun offlineIndicatorOnlyAppearsWhenStateExplicitlyMarksOfflineData() {
        assertNull(WalkingStatusPresentation.offlineLabel(false))
        assertEquals("Dados locais disponíveis", WalkingStatusPresentation.offlineLabel(true))
    }
}
