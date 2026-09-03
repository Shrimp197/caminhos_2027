package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation
import org.junit.Assert.assertEquals
import org.junit.Test

class ApoiSearchTest {
    @Test fun searchMatchesNameWithoutAccentSensitivity() {
        val result = ApoiSearch.apply(listOf(apoi("1", "Albergue São João"), apoi("2", "Fonte Norte")), "sao joao")
        assertEquals(listOf("1"), result.map { it.id })
    }

    @Test fun searchMatchesDescriptionLocalityReferenceAndServices() {
        val record = apoi(
            "1",
            "Ponto de apoio",
            description = "Água potável e descanso",
            locality = "Ribeiro da Vide",
            reference = "junto à igreja",
            services = setOf(ApoiCategory.AGUA, ApoiCategory.DESCANSO)
        )
        val records = listOf(record, apoi("2", "Outro ponto"))

        assertEquals(listOf("1"), ApoiSearch.apply(records, "potavel").map { it.id })
        assertEquals(listOf("1"), ApoiSearch.apply(records, "ribeiro").map { it.id })
        assertEquals(listOf("1"), ApoiSearch.apply(records, "igreja").map { it.id })
        assertEquals(listOf("1"), ApoiSearch.apply(records, "agua").map { it.id })
    }

    @Test fun blankQueryReturnsRecordsUnchanged() {
        val records = listOf(apoi("1", "Um"), apoi("2", "Dois"))
        assertEquals(records, ApoiSearch.apply(records, "   "))
    }

    private fun apoi(
        id: String,
        name: String,
        description: String? = null,
        locality: String? = null,
        reference: String? = null,
        services: Set<ApoiCategory> = setOf(ApoiCategory.AGUA)
    ) = Apoi(
        id = id,
        name = name,
        description = description,
        mainCategory = services.first(),
        services = services,
        location = ApoiLocation(
            latitude = 40.0,
            longitude = -8.0,
            precision = LocationPrecision.EXACT,
            locality = locality,
            municipality = null,
            reference = reference,
            routeId = "route",
            routeKm = 1.0,
            distanceToRouteM = 0.0,
            accessDistanceM = 0.0,
            routeRelation = RouteRelation.ON_ROUTE
        ),
        publication = ApoiPublication(PublicationStatus.PUBLISHED, null)
    )
}
