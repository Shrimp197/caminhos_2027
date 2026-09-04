package com.caminhos2027.v1.ui

import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.route.WalkingMovementCue

/** User-facing labels for walking tracking state. Domain transitions remain in the GPS evaluator. */
data class WalkingGpsPresentation(
    val label: String,
    val detail: String
)

object WalkingStatusPresentation {
    fun gps(state: GpsState): WalkingGpsPresentation = when (state) {
        GpsState.NO_SIGNAL -> WalkingGpsPresentation(
            label = "Sem sinal GPS",
            detail = "A última posição fiável mantém-se até existir uma nova posição confirmada."
        )
        GpsState.ACQUIRING -> WalkingGpsPresentation(
            label = "A procurar localização",
            detail = "A localização está a ser confirmada; evite tomar a posição atual como definitiva."
        )
        GpsState.ON_ROUTE -> WalkingGpsPresentation(
            label = "No caminho",
            detail = "A localização recente está alinhada com o traçado acompanhado."
        )
        GpsState.POSSIBLE_DEVIATION -> WalkingGpsPresentation(
            label = "Possível desvio",
            detail = "Há sinais afastados do traçado. A posição fiável anterior é mantida por precaução."
        )
        GpsState.PROBABLE_DEVIATION -> WalkingGpsPresentation(
            label = "Provável desvio",
            detail = "Vários sinais afastados do traçado foram observados. Confirme visualmente o caminho antes de continuar."
        )
    }

    fun confidence(confidence: PositionConfidence): String = when (confidence) {
        PositionConfidence.HIGH -> "Confiança da posição: alta"
        PositionConfidence.MEDIUM -> "Confiança da posição: média"
        PositionConfidence.LOW -> "Confiança da posição: baixa"
        PositionConfidence.UNKNOWN -> "Confiança da posição: não conhecida"
    }

    fun movement(cue: WalkingMovementCue): String = when (cue) {
        WalkingMovementCue.FORWARD -> "Movimento: no sentido do percurso"
        WalkingMovementCue.BACKWARD -> "Movimento: em sentido inverso ao percurso"
        WalkingMovementCue.STATIONARY -> "Movimento: sem deslocação relevante"
        WalkingMovementCue.UNKNOWN -> "Movimento: ainda sem referência suficiente"
    }

    fun offlineLabel(isOffline: Boolean): String? =
        if (isOffline) "Dados do percurso disponíveis offline" else null

    fun offlineDetail(isOffline: Boolean): String? =
        if (isOffline) "O percurso e o progresso guardados localmente podem continuar sem rede; o mapa cartográfico offline não está disponível." else null
}
