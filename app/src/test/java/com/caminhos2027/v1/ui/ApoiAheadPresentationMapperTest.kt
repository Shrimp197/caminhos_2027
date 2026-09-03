package com.caminhos2027.v1.ui

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApoiAheadPresentationMapperTest {
    @Test fun formatsShortDistanceInMeters() {
        val result = ApoiAheadPresentationMapper.map(apoi(), 0.8)

        assertEquals("800 m", result.distanceLabel)
        assertEquals("Água", result.categoryLabel)
        assertNull(result.warningLabel)
    }

    @Test fun formatsLongerDistanceInKilometers() {
        val result = ApoiAheadPresentationMapper.map(apoi(), 2.46)

        assertEquals("2.5 km", result.distanceLabel)
    }

    @Test fun exposesWarningWithoutChangingPublicationSemantics() {
        val result = ApoiAheadPresentationMapper.map(
            apoi().copy(publication = ApoiPublication(PublicationStatus.PUBLISHED_WITH_WARNING, "pending")),
            3.0
        )

        assertEquals("Informação com confirmação pendente", result.warningLabel)
    }

    private fun apoi() = Apoi(
        id = "water",
        name = "Fonte de água",
        description = null,
        mainCategory = ApoiCategory.AGUA,
        services = setOf(ApoiCategory.AGUA),
        location = ApoiLocation(
            40.0, -8.0, LocationPrecision.EXACT,
            null, null, null, "route", 3.0, 0.0, 0.0, RouteRelation.ON_ROUTE
        ),
        publication = ApoiPublication(PublicationStatus.PUBLISHED, null)
    )
}
