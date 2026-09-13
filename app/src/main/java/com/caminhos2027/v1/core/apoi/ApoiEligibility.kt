package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiAvailabilityStatus
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation

/**
 * Single eligibility vocabulary for APOI shown as actionable support during a walk.
 * Publication and availability are orthogonal: both must permit use.
 */
object ApoiEligibility {
    fun isPublished(status: PublicationStatus, includeWarnings: Boolean = true): Boolean =
        status == PublicationStatus.PUBLISHED ||
            (includeWarnings && status == PublicationStatus.PUBLISHED_WITH_WARNING)

    fun isAvailabilityUsable(status: ApoiAvailabilityStatus): Boolean =
        status != ApoiAvailabilityStatus.HISTORICAL &&
            status != ApoiAvailabilityStatus.EXPIRED &&
            status != ApoiAvailabilityStatus.CLOSED

    fun isRouteReachable(relation: RouteRelation): Boolean =
        relation != RouteRelation.DISTANT_POTENTIAL_SUPPORT &&
            relation != RouteRelation.OUTSIDE_ROUTE

    fun isEligibleForWalking(apoi: Apoi, includeWarnings: Boolean = true): Boolean =
        isPublished(apoi.publication.status, includeWarnings) &&
            isAvailabilityUsable(apoi.availability.status) &&
            isRouteReachable(apoi.location.routeRelation)
}
