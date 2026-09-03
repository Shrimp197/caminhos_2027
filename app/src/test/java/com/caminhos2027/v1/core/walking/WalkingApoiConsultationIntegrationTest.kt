package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.data.ApoiRepository
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RouteRelation
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.Walk
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/** SR integration contract: the walking read model feeds APOI consultation from the same route position. */
class WalkingApoiConsultationIntegrationTest {
    @Test
    fun `walking position is the single source for next APOI consultation`() {
        val route = fixtureRoute()
        val walk = Walk(id = "sr-walk", routeId = route.id, actualStartKm = 0.0)
        val catalog = PublishedApoiCatalog(FakeRepository(listOf(
            apoi("water", 0.7),
            apoi("sleep", 0.9, ApoiCategory.PERNOITA),
            apoi("behind", 0.2)
        )))
        val coordinator = WalkingStateCoordinator(route, walk, catalog.all())

        val walkingState = coordinator.accept(
            RawGpsPosition(40.00225, -8.0, 5.0, Instant.parse("2026-09-01T10:00:00Z"))
        )
        val currentKm = walkingState.routePosition!!.routeKm
        val context = WalkingApoiContext(route.id, catalog)

        val ahead = context.ahead(currentKm)
        val next = context.next(currentKm)

        assertNotNull(next)
        assertEquals(next!!.id, ahead.first().apoi.id)
        assertEquals("water", next.id)
        assertEquals(0.7 - currentKm, ahead.first().distanceKm, 0.01)
        assertEquals("sleep", ahead[1].apoi.id)
    }

    private fun apoi(id: String, km: Double, category: ApoiCategory = ApoiCategory.AGUA) =
        Apoi(
            id, id, null, category, setOf(category),
            ApoiLocation(40.0, -8.0, LocationPrecision.EXACT, "SR", "SR", null, "sr-route", km, 0.0, 0.0, RouteRelation.ON_ROUTE),
            ApoiPublication(PublicationStatus.PUBLISHED, "SR test data")
        )

    private class FakeRepository(private val records: List<Apoi>) : ApoiRepository {
        override fun getAll(): List<Apoi> = records
    }

    private fun fixtureRoute() = Route(
        id = "sr-route", name = "Percurso SR", officialName = "SR — percurso sintético",
        totalDistanceKm = 1.0, source = "SR", updatedAt = "2026-09-01",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.0045, -8.0), GeoPoint(40.009, -8.0))),
        stages = listOf(Stage("stage-1", "sr-route", 1, "SR 1", 0.0, 1.0, 1.0, "Início", "Fim", "SR"))
    )
}
