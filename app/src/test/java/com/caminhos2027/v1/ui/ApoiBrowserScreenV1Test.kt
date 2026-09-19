package com.caminhos2027.v1.ui

import com.caminhos2027.v1.core.apoi.ApoiAhead
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation
import org.junit.Assert.assertEquals
import org.junit.Test

class ApoiBrowserScreenV1Test {
    @Test fun selectedServiceStateIsRepresentedByInputContract() {
        val selected = setOf(ApoiCategory.AGUA, ApoiCategory.PERNOITA)
        assertEquals(setOf(ApoiCategory.AGUA, ApoiCategory.PERNOITA), selected)
    }

    @Test fun browserItemsKeepDomainOrderAndDistance() {
        val items = listOf(ApoiAhead(apoi("water"), 2.5), ApoiAhead(apoi("sleep"), 5.0))
        assertEquals(listOf("water", "sleep"), items.map { it.apoi.id })
        assertEquals(2.5, items.first().distanceKm, 0.001)
    }

    private fun apoi(id: String) = Apoi(
        id = id,
        name = id,
        description = null,
        mainCategory = ApoiCategory.AGUA,
        services = setOf(ApoiCategory.AGUA),
        location = ApoiLocation(40.0, -8.0, LocationPrecision.EXACT, null, null, null, "route", 5.0, 0.0, 0.0, RouteRelation.ON_ROUTE),
        publication = ApoiPublication(PublicationStatus.PUBLISHED, null)
    )
}
