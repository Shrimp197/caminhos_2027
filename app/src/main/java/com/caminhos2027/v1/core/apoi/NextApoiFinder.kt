package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation

/** Finds the next publishable APOI along the route. */
object NextApoiFinder {
    fun findNext(records: List<Apoi>, routeId: String, currentRouteKm: Double): Apoi? =
        records
            .asSequence()
            .filter { it.location.routeId == routeId }
            .filter { it.location.routeKm != null && it.location.routeKm >= currentRouteKm }
            .filter { it.publication.status in setOf(PublicationStatus.PUBLISHED, PublicationStatus.PUBLISHED_WITH_WARNING) }
            .filter { it.location.routeRelation != RouteRelation.DISTANT_POTENTIAL_SUPPORT && it.location.routeRelation != RouteRelation.OUTSIDE_ROUTE }
            .minByOrNull { it.location.routeKm ?: Double.POSITIVE_INFINITY }
}
