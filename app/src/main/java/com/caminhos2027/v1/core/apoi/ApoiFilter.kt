package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.PublicationStatus

/** Pure filtering rules for the single APOI catalogue. */
data class ApoiFilter(
    val services: Set<ApoiCategory> = emptySet(),
    val includeWarnings: Boolean = true
)

object ApoiFilterEngine {
    fun apply(records: List<Apoi>, filter: ApoiFilter): List<Apoi> = records
        .asSequence()
        .filter { it.publication.status == PublicationStatus.PUBLISHED ||
            (filter.includeWarnings && it.publication.status == PublicationStatus.PUBLISHED_WITH_WARNING) }
        .filter { filter.services.isEmpty() || filter.services.all(it.services::contains) || filter.services.contains(it.mainCategory) }
        .sortedBy { it.location.routeKm ?: Double.POSITIVE_INFINITY }
        .toList()
}
