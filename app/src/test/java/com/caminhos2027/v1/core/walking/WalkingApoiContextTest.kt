package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.apoi.ApoiAhead
import com.caminhos2027.v1.core.apoi.ApoiFilter
import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.data.ApoiDataSource
import com.caminhos2027.v1.core.data.ApoiRepository
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

class WalkingApoiContextTest {
    @Test fun aheadUsesCurrentWalkRouteAndPosition() {
        val context = context(listOf(apoi("water", 8.0, PublicationStatus.PUBLISHED)))
        assertEquals(listOf("water"), context.ahead(5.0).map { it.apoi.id })
    }

    @Test fun searchAheadCombinesQueryAndServiceFilter() {
        val context = context(listOf(
            apoi("water", 6.0, PublicationStatus.PUBLISHED),
            apoi("sleep", 8.0, PublicationStatus.PUBLISHED)
        ))
        val result = context.searchAhead(
            currentRouteKm = 5.0,
            query = "water",
            filter = ApoiFilter(services = setOf(ApoiCategory.AGUA))
        )
        assertEquals(listOf("water"), result.map { it.apoi.id })
    }

    @Test fun nextReturnsNullWhenNothingIsPublishedAhead() {
        val context = context(listOf(apoi("old", 3.0, PublicationStatus.PUBLISHED)))
        assertNull(context.next(5.0))
    }

    private fun context(records: List<Apoi>) = WalkingApoiContext(
        routeId = "route",
        catalog = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { records }))
    )

    private fun apoi(id: String, km: Double, status: PublicationStatus) = Apoi(
        id = id,
        name = id,
        description = null,
        mainCategory = ApoiCategory.AGUA,
        services = setOf(ApoiCategory.AGUA),
        location = ApoiLocation(40.0, -8.0, LocationPrecision.EXACT, null, null, null, "route", km, 0.0, 0.0, RouteRelation.ON_ROUTE),
        publication = ApoiPublication(status, null)
    )
}
