package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RouteRelation
import com.caminhos2027.v1.core.model.Stage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkingPreparationServiceTest {
    private val route = Route(
        id = "sr-route",
        name = "SR Caminho de Teste",
        officialName = "SR Caminho de Teste",
        totalDistanceKm = 10.0,
        source = "SR",
        updatedAt = "2026-09-01",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.09, -8.0))),
        stages = listOf(
            Stage("s1", "sr-route", 1, "Etapa 1", 0.0, 5.0, 5.0, "A", "B", "SR"),
            Stage("s2", "sr-route", 2, "Etapa 2", 5.0, 10.0, 5.0, "B", "C", "SR")
        )
    )

    private val repository = InMemoryWalkRepository()

    private fun apoi(id: String, km: Double) = Apoi(
        id = id,
        name = id,
        description = null,
        mainCategory = ApoiCategory.AGUA,
        services = setOf(ApoiCategory.AGUA),
        location = ApoiLocation(
            latitude = null, longitude = null, precision = LocationPrecision.UNKNOWN,
            locality = null, municipality = null, reference = null, routeId = "sr-route",
            routeKm = km, distanceToRouteM = null, accessDistanceM = null,
            routeRelation = RouteRelation.ON_ROUTE
        ),
        publication = ApoiPublication(PublicationStatus.PUBLISHED, null)
    )

    @Test
    fun previewDoesNotPersistPlan() {
        val service = WalkingPreparationService(route, repository, listOf(apoi("water", 3.0)))

        val preparation = service.preview("walk-1", 2.0, 8.0)

        assertEquals("walk-1", preparation.walk.id)
        assertEquals(6.0, preparation.walk.plannedDestinationKm!! - preparation.walk.plannedStartKm!!, 0.0001)
        assertEquals(listOf("s1", "s2"), preparation.stages.map { it.id })
        assertEquals(listOf("water"), preparation.relevantApoi.map { it.id })
        assertEquals(null, repository.getById("walk-1"))
    }

    @Test
    fun savePersistsOnlyValidatedPlan() {
        val service = WalkingPreparationService(route, repository)

        val preparation = service.save("walk-2", 5.0, 10.0)

        assertEquals(preparation.walk, repository.getById("walk-2"))
        assertEquals(listOf("s2"), preparation.stages.map { it.id })
        assertTrue(preparation.walk.stageIds.contains("s2"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidDestinationIsRejectedBeforePersistence() {
        val service = WalkingPreparationService(route, repository)
        service.save("walk-invalid", 7.0, 4.0)
    }
}
