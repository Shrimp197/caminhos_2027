package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
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
    @Test fun contextUsesCurrentPositionAndKeepsCatalogOrdering() {
        val context = WalkingApoiContext("route", PublishedApoiCatalog(FakeRepository(listOf(
            apoi("far", 8.0), apoi("near", 3.0), apoi("past", 1.0)
        ))))

        val result = context.ahead(2.0)

        assertEquals(listOf("near", "far"), result.map { it.apoi.id })
        assertEquals(1.0, result.first().distanceKm, 0.001)
        assertEquals("near", context.next(2.0)?.id)
    }

    @Test fun searchAheadAppliesServiceFilterAndDoesNotSearchBehindPosition() {
        val context = WalkingApoiContext("route", PublishedApoiCatalog(FakeRepository(listOf(
            apoi("water", 4.0, setOf(ApoiCategory.AGUA)),
            apoi("sleep", 5.0, setOf(ApoiCategory.PERNOITA)),
            apoi("past-water", 1.0, setOf(ApoiCategory.AGUA))
        ))))

        val result = context.searchAhead(2.0, "", ApoiFilter(services = setOf(ApoiCategory.AGUA)))

        assertEquals(listOf("water"), result.map { it.apoi.id })
    }

    @Test fun emptyAheadHasNoNext() {
        val context = WalkingApoiContext("route", PublishedApoiCatalog(FakeRepository(emptyList())))
        assertEquals(emptyList<Any>(), context.ahead(10.0))
        assertNull(context.next(10.0))
    }

    private fun apoi(id: String, km: Double, services: Set<ApoiCategory> = setOf(ApoiCategory.AGUA)) =
        Apoi(id, id, null, services.first(), services, ApoiLocation(40.0, -8.0, LocationPrecision.EXACT, null, null, null, "route", km, 0.0, 0.0, RouteRelation.ON_ROUTE), ApoiPublication(PublicationStatus.PUBLISHED, null))

    private class FakeRepository(private val records: List<Apoi>) : ApoiRepository {
        override fun getAll(): List<Apoi> = records
    }
}
