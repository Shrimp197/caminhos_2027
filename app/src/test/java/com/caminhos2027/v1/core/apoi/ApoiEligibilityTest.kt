package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.ApoiAvailabilityStatus
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApoiEligibilityTest {
    @Test
    fun publishedAndWarningStatusesAreExplicit() {
        assertTrue(ApoiEligibility.isPublished(PublicationStatus.PUBLISHED))
        assertTrue(ApoiEligibility.isPublished(PublicationStatus.PUBLISHED_WITH_WARNING))
        assertFalse(ApoiEligibility.isPublished(PublicationStatus.PUBLISHED_WITH_WARNING, includeWarnings = false))
        assertFalse(ApoiEligibility.isPublished(PublicationStatus.REVIEW))
    }

    @Test
    fun historicalExpiredAndClosedAvailabilityIsBlocked() {
        assertTrue(ApoiEligibility.isAvailabilityUsable(ApoiAvailabilityStatus.CURRENT))
        assertTrue(ApoiEligibility.isAvailabilityUsable(ApoiAvailabilityStatus.AWAITING_CONFIRMATION))
        assertFalse(ApoiEligibility.isAvailabilityUsable(ApoiAvailabilityStatus.HISTORICAL))
        assertFalse(ApoiEligibility.isAvailabilityUsable(ApoiAvailabilityStatus.EXPIRED))
        assertFalse(ApoiEligibility.isAvailabilityUsable(ApoiAvailabilityStatus.CLOSED))
    }

    @Test
    fun onlyRouteReachableRelationsRemainActionable() {
        assertTrue(ApoiEligibility.isRouteReachable(RouteRelation.ON_ROUTE))
        assertTrue(ApoiEligibility.isRouteReachable(RouteRelation.NEAR_ROUTE))
        assertTrue(ApoiEligibility.isRouteReachable(RouteRelation.ACCESSIBLE_WITH_DETOUR))
        assertFalse(ApoiEligibility.isRouteReachable(RouteRelation.DISTANT_POTENTIAL_SUPPORT))
        assertFalse(ApoiEligibility.isRouteReachable(RouteRelation.OUTSIDE_ROUTE))
    }
}
