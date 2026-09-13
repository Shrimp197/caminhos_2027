package com.caminhos2027.v1.ui

import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.route.WalkingMovementCue
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
    fun presentationTitleRemainsAnExactAliasOfTheDisplayedLabel() {
        val presentation = WalkingStatusPresentation.gps(GpsState.ON_ROUTE)

        assertEquals(presentation.label, presentation.title)
        assertEquals("No caminho", presentation.title)
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
    fun movementUsesExplicitPortugueseLabels() {
        assertEquals("Movimento: no sentido do percurso", WalkingStatusPresentation.movement(WalkingMovementCue.FORWARD))
        assertEquals("Movimento: em sentido inverso ao percurso", WalkingStatusPresentation.movement(WalkingMovementCue.BACKWARD))
        assertEquals("Movimento: sem deslocação relevante", WalkingStatusPresentation.movement(WalkingMovementCue.STATIONARY))
        assertEquals("Movimento: ainda sem referência suficiente", WalkingStatusPresentation.movement(WalkingMovementCue.UNKNOWN))
    }

    @Test
    fun offlinePresentationIsOnlyVisibleWhenStateExplicitlyMarksOfflineData() {
        assertNull(WalkingStatusPresentation.offlineLabel(false))
        assertNull(WalkingStatusPresentation.offlineDetail(false))
        assertEquals("Dados do percurso disponíveis offline", WalkingStatusPresentation.offlineLabel(true))
        assertEquals(
            "O percurso e o progresso guardados localmente podem continuar sem rede; o mapa cartográfico offline não está disponível.",
            WalkingStatusPresentation.offlineDetail(true)
        )
    }
}
