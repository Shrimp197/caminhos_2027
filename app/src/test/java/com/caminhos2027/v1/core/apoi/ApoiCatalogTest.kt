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
            null, null, null, "route", 10.0, 0.0, null, RouteRelation.ON_ROUTE
        ),
        publication = ApoiPublication(publication, null),
        availability = ApoiAvailability(status = availability)
    )

    @Test fun onlyQualifiedPublishedRecordsEnterCatalog() {
        val records = listOf(
            apoi("confirmed"),
            apoi("pending", ApoiAvailabilityStatus.AWAITING_CONFIRMATION),
            apoi("closed", ApoiAvailabilityStatus.CLOSED)
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
}
