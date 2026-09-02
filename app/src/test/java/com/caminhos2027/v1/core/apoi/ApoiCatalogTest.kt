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
import org.junit.Assert.assertEquals
import org.junit.Test

class ApoiCatalogTest {
    private fun apoi(
        id: String,
        km: Double = 10.0,
        availability: ApoiAvailabilityStatus = ApoiAvailabilityStatus.CURRENT,
        publication: PublicationStatus = PublicationStatus.REVIEW
    ) = Apoi(
        id = id,
        name = id,
        description = null,
        mainCategory = ApoiCategory.AGUA,
        services = setOf(ApoiCategory.AGUA),
        location = ApoiLocation(
            40.0, -8.0, LocationPrecision.EXACT,
            null, null, null, "route", km, 0.0, null, RouteRelation.ON_ROUTE
        ),
        publication = ApoiPublication(publication, null),
        availability = ApoiAvailability(status = availability)
    )

    @Test fun onlyQualifiedPublishedRecordsEnterCatalog() {
        val records = listOf(
            apoi("confirmed", 2.0),
            apoi("pending", 3.0, ApoiAvailabilityStatus.AWAITING_CONFIRMATION),
            apoi("closed", 4.0, ApoiAvailabilityStatus.CLOSED)
        )
        val evidence = mapOf(
            "confirmed" to ApoiQualificationEvidence(true, confirmedForYear = 2027),
            "pending" to ApoiQualificationEvidence(true),
            "closed" to ApoiQualificationEvidence(true, confirmedForYear = 2027)
        )

        val result = ApoiCatalog(2027, records, evidence).published()

        assertEquals(listOf("confirmed", "pending"), result.map { it.id })
        assertEquals(PublicationStatus.PUBLISHED, result[0].publication.status)
        assertEquals(PublicationStatus.PUBLISHED_WITH_WARNING, result[1].publication.status)
    }

    @Test fun missingEvidenceDoesNotLeakIntoCatalog() {
        val result = ApoiCatalog(2027, listOf(apoi("unknown")), emptyMap()).published()
        assertEquals(emptyList<Apoi>(), result)
    }

    @Test fun filterUsesQualifiedPublishedRecordsOnly() {
        val records = listOf(apoi("unknown", 2.0), apoi("next", 3.0), apoi("closed", 4.0, ApoiAvailabilityStatus.CLOSED))
        val evidence = mapOf(
            "unknown" to ApoiQualificationEvidence(null),
            "next" to ApoiQualificationEvidence(true, confirmedForYear = 2027),
            "closed" to ApoiQualificationEvidence(true, confirmedForYear = 2027)
        )

        val result = ApoiCatalog(2027, records, evidence).filter("route", 0.0)

        assertEquals(listOf("next"), result.map { it.id })
    }

    @Test fun nextUsesQualifiedPublishedRecordsOnly() {
        val records = listOf(apoi("unknown", 1.0), apoi("next", 3.0), apoi("closed", 2.0, ApoiAvailabilityStatus.CLOSED))
        val evidence = mapOf(
            "unknown" to ApoiQualificationEvidence(null),
            "next" to ApoiQualificationEvidence(true, confirmedForYear = 2027),
            "closed" to ApoiQualificationEvidence(true, confirmedForYear = 2027)
        )

        val result = ApoiCatalog(2027, records, evidence).next("route", 0.0)

        assertEquals("next", result?.id)
    }
}
