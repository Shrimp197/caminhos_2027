package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiAvailabilityStatus
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation

data class ApoiQualificationEvidence(
    val pilgrimSupportConfirmed: Boolean?,
    val criticalConflict: Boolean = false,
    val confirmedForYear: Int? = null
)

data class ApoiPublicationDecision(
    val status: PublicationStatus,
    val reason: String?
)

/** Pure rules that separate known place data from publishable pilgrim support. */
object ApoiQualification {
    fun evaluate(
        apoi: Apoi,
        evidence: ApoiQualificationEvidence,
        targetYear: Int
    ): ApoiPublicationDecision {
        if (targetYear < 1) return decision(PublicationStatus.REVIEW, "Ano-alvo inválido")

        if (apoi.location.routeRelation == RouteRelation.OUTSIDE_ROUTE ||
            apoi.location.routeRelation == RouteRelation.DISTANT_POTENTIAL_SUPPORT
        ) return decision(PublicationStatus.EXCLUDED, "Fora da área normal de apoio do caminho")

        if (apoi.location.routeRelation == RouteRelation.LOCATION_UNCERTAIN ||
            apoi.location.precision == LocationPrecision.UNKNOWN
        ) return decision(PublicationStatus.REVIEW, "Localização insuficientemente confirmada")

        if (apoi.availability.status == ApoiAvailabilityStatus.CLOSED) {
            return decision(PublicationStatus.CLOSED, "APOI confirmado como encerrado")
        }
        if (apoi.availability.status == ApoiAvailabilityStatus.HISTORICAL) {
            return decision(PublicationStatus.HISTORICAL, "Informação histórica")
        }
        if (apoi.availability.status == ApoiAvailabilityStatus.EXPIRED) {
            return decision(PublicationStatus.CLOSED, "Período de validade expirado")
        }
        if (apoi.services.isEmpty()) {
            return decision(PublicationStatus.EXCLUDED, "Sem serviço de apoio identificado")
        }
        if (evidence.pilgrimSupportConfirmed == false) {
            return decision(PublicationStatus.EXCLUDED, "Não foi confirmado apoio ao peregrino")
        }
        if (evidence.criticalConflict) {
            return decision(PublicationStatus.REVIEW, "Existe conflito em informação crítica")
        }
        if (evidence.confirmedForYear != null && evidence.confirmedForYear != targetYear) {
            return decision(PublicationStatus.REVIEW, "Confirmação disponível para outro ano")
        }
        if (evidence.pilgrimSupportConfirmed == null) {
            return decision(PublicationStatus.REVIEW, "Apoio ao peregrino ainda não confirmado")
        }
        if (apoi.availability.status == ApoiAvailabilityStatus.FUTURE_CONFIRMED && evidence.confirmedForYear != targetYear) {
            return decision(PublicationStatus.REVIEW, "Disponibilidade futura sem confirmação para o ano-alvo")
        }
        if (apoi.availability.status == ApoiAvailabilityStatus.AWAITING_CONFIRMATION) {
            return decision(PublicationStatus.PUBLISHED_WITH_WARNING, "Apoio útil, mas confirmação atual pendente")
        }

        return decision(PublicationStatus.PUBLISHED, null)
    }

    private fun decision(status: PublicationStatus, reason: String?) =
        ApoiPublicationDecision(status, reason)
}
