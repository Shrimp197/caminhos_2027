package com.caminhos2027.v1.ui

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiAvailabilityStatus
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.PublicationStatus

/** Small UI read model for an APOI in the ahead list. No business filtering is done here. */
data class ApoiAheadPresentation(
    val id: String,
    val name: String,
    val categoryLabel: String,
    val distanceLabel: String,
    val warningLabel: String?,
    val availabilityLabel: String
)

object ApoiAheadPresentationMapper {
    fun map(apoi: Apoi, distanceKm: Double): ApoiAheadPresentation =
        ApoiAheadPresentation(
            id = apoi.id,
            name = apoi.name,
            categoryLabel = categoryLabel(apoi.mainCategory),
            distanceLabel = formatDistance(distanceKm),
            warningLabel = warningLabel(apoi.publication.status),
            availabilityLabel = availabilityLabel(apoi.availability.status)
        )

    fun availabilityLabel(status: ApoiAvailabilityStatus): String = when (status) {
        ApoiAvailabilityStatus.CURRENT -> "Disponibilidade atual"
        ApoiAvailabilityStatus.FUTURE_CONFIRMED -> "Disponibilidade futura confirmada"
        ApoiAvailabilityStatus.RECURRING -> "Disponibilidade recorrente"
        ApoiAvailabilityStatus.HISTORICAL -> "Informação histórica"
        ApoiAvailabilityStatus.EXPIRED -> "Informação expirada"
        ApoiAvailabilityStatus.AWAITING_CONFIRMATION -> "Disponibilidade por confirmar"
        ApoiAvailabilityStatus.CLOSED -> "APOI encerrado"
    }

    private fun categoryLabel(category: ApoiCategory): String = when (category) {
        ApoiCategory.ALIMENTACAO -> "Alimentação"
        ApoiCategory.AGUA -> "Água"
        ApoiCategory.DESCANSO -> "Descanso"
        ApoiCategory.PERNOITA -> "Pernoita"
        ApoiCategory.DUCHES -> "Duches"
        ApoiCategory.CARREGAMENTO -> "Carregamento"
        ApoiCategory.TRANSPORTE -> "Transporte"
        ApoiCategory.EMERGENCIA -> "Emergência"
    }

    private fun formatDistance(distanceKm: Double): String = when {
        distanceKm < 1.0 -> "%.0f m".format(distanceKm * 1000.0)
        else -> "%.1f km".format(distanceKm)
    }

    private fun warningLabel(status: PublicationStatus): String? =
        if (status == PublicationStatus.PUBLISHED_WITH_WARNING) {
            "Informação com confirmação pendente"
        } else {
            null
        }
}
