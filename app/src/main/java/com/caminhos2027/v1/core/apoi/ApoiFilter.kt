package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiAvailabilityStatus
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation

/** User-facing filters over the single APOI catalog. */
data class ApoiFilter(
    val services: Set<ApoiCategory> = emptySet(),
    val includeWarnings: Boolean = true,
    val publicationStatuses: Set<PublicationStatus> = setOf(
        PublicationStatus.PUBLISHED,
        PublicationStatus.PUBLISHED_WITH_WARNING
    )
)

object ApoiFilterEngine {
    fun apply(records: List<Apoi>, routeId: String, currentRouteKm: Double, filter: ApoiFilter = ApoiFilter()): List<Apoi> =
        records.asSequence()
            .filter { it.location.routeId == routeId }
            .filter { it.location.routeKm != null && it.location.routeKm >= currentRouteKm }
            .filter { it.publication.status in filter.publicationStatuses }
            .filter { filter.includeWarnings || it.publication.status != PublicationStatus.PUBLISHED_WITH_WARNING }
            .filter { it.availability.status !in setOf(ApoiAvailabilityStatus.HISTORICAL, ApoiAvailabilityStatus.EXPIRED, ApoiAvailabilityStatus.CLOSED) }
            .filter { it.services.containsAll(filter.services) }
            .filter { it.location.routeRelation != RouteRelation.DISTANT_POTENTIAL_SUPPORT && it.location.routeRelation != RouteRelation.OUTSIDE_ROUTE }
            .sortedBy { it.location.routeKm ?: Double.POSITIVE_INFINITY }
            .toList()
}
