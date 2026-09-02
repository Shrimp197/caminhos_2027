package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.PublicationStatus

/**
 * Single entry point for the pilgrim-facing APOI catalog.
 * Qualification happens before filtering, so raw/master records cannot leak into the UI.
 */
class ApoiCatalog(
    private val targetYear: Int,
    private val records: List<Apoi>,
    private val evidenceByApoiId: Map<String, ApoiQualificationEvidence>
) {
    fun published(): List<Apoi> = records.mapNotNull { qualified(it) }

    fun filter(routeId: String, currentRouteKm: Double, filter: ApoiFilter = ApoiFilter()): List<Apoi> =
        ApoiFilterEngine.apply(published(), routeId, currentRouteKm, filter)

    fun next(routeId: String, currentRouteKm: Double): Apoi? =
        NextApoiFinder.findNext(published(), routeId, currentRouteKm)

    fun allQualified(): List<ApoiPublicationDecision> = records.map { apoi ->
        ApoiQualification.evaluate(
            apoi,
            evidenceByApoiId[apoi.id] ?: ApoiQualificationEvidence(null),
            targetYear
        )
    }

    private fun qualified(apoi: Apoi): Apoi? {
        val evidence = evidenceByApoiId[apoi.id] ?: ApoiQualificationEvidence(null)
        val decision = ApoiQualification.evaluate(apoi, evidence, targetYear)
        return if (decision.status == PublicationStatus.PUBLISHED ||
            decision.status == PublicationStatus.PUBLISHED_WITH_WARNING
        ) {
            apoi.copy(publication = apoi.publication.copy(status = decision.status, reason = decision.reason))
        } else {
            null
        }
    }
}
