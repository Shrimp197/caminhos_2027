package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiAvailability
import com.caminhos2027.v1.core.model.ApoiAvailabilityStatus
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation
import kotlin.test.Test
import kotlin.test.assertEquals

class ApoiFilterTest {
    private fun apoi(
        id: String,
        km: Double,
        services: Set<ApoiCategory>,
        status: PublicationStatus = PublicationStatus.PUBLISHED,
        availability: ApoiAvailabilityStatus = ApoiAvailabilityStatus.AWAITING_CONFIRMATION
    ) = Apoi(
        id,
        id,
        null,
        services.first(),
        services,
        ApoiLocation(null, null, LocationPrecision.UNKNOWN, null, null, null, "route", km, null, null, RouteRelation.ON_ROUTE),
        ApoiPublication(status, null),
        availability = ApoiAvailability(status = availability)
    )

    @Test fun filtersByAllSelectedServicesAndOrdersByRouteKm() {
        val records = listOf(
            apoi("later", 8.0, setOf(ApoiCategory.AGUA, ApoiCategory.DESCANSO)),
            apoi("near", 3.0, setOf(ApoiCategory.AGUA, ApoiCategory.DESCANSO, ApoiCategory.CARREGAMENTO)),
            apoi("single", 2.0, setOf(ApoiCategory.AGUA))
        )
        val result = ApoiFilterEngine.apply(records, "route", 0.0, ApoiFilter(services = setOf(ApoiCategory.AGUA, ApoiCategory.DESCANSO)))
        assertEquals(listOf("near", "later"), result.map { it.id })
    }

    @Test fun excludesWarningsWhenRequested() {
        val records = listOf(apoi("warning", 2.0, setOf(ApoiCategory.AGUA), PublicationStatus.PUBLISHED_WITH_WARNING))
        assertEquals(emptyList(), ApoiFilterEngine.apply(records, "route", 0.0, ApoiFilter(includeWarnings = false)))
    }

    @Test fun excludesHistoricalAndCandidateByDefault() {
        val records = listOf(
            apoi("historical", 2.0, setOf(ApoiCategory.AGUA), PublicationStatus.HISTORICAL),
            apoi("candidate", 3.0, setOf(ApoiCategory.AGUA), PublicationStatus.CANDIDATE),
            apoi("published", 4.0, setOf(ApoiCategory.AGUA))
        )
        assertEquals(listOf("published"), ApoiFilterEngine.apply(records, "route", 0.0).map { it.id })
    }

    @Test fun excludesHistoricalExpiredAndClosedAvailability() {
        val records = listOf(
            apoi("historical", 2.0, setOf(ApoiCategory.AGUA), availability = ApoiAvailabilityStatus.HISTORICAL),
            apoi("expired", 3.0, setOf(ApoiCategory.AGUA), availability = ApoiAvailabilityStatus.EXPIRED),
            apoi("closed", 4.0, setOf(ApoiCategory.AGUA), availability = ApoiAvailabilityStatus.CLOSED),
            apoi("awaiting", 5.0, setOf(ApoiCategory.AGUA))
        )
        assertEquals(listOf("awaiting"), ApoiFilterEngine.apply(records, "route", 0.0).map { it.id })
    }
}
