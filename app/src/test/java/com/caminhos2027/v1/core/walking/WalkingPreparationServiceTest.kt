package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.data.ApoiDataSource
import com.caminhos2027.v1.core.data.ApoiRepository
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.WalkStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WalkingPreparationServiceTest {
    @Test fun previewWithEmptyPublishedCatalogReturnsNoApoiAndDoesNotPersist() {
        val repository = InMemoryWalkRepository()
        val service = WalkingPreparationService(
            route = testRoute(),
            walkRepository = repository,
            apoiCatalog = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { emptyList() }))
        )

        val preparation = service.preview("walk-1", 2.0, 10.0)

        assertEquals(emptyList<Apoi>(), preparation.relevantApoi)
        assertNull(repository.getById("walk-1"))
    }

    @Test fun restorePlannedReturnsSavedPlanWithoutStartingIt() {
        val repository = InMemoryWalkRepository()
        val service = service(repository)
        val saved = service.save("walk-planned", 2.0, 10.0)

        val restored = service.restorePlanned()

        assertEquals("walk-planned", restored!!.walk.id)
        assertEquals(saved.walk, restored.walk)
        assertEquals(WalkStatus.PLANNED, restored.walk.status)
        assertEquals(emptyList<Apoi>(), restored.relevantApoi)
    }

    @Test fun restorePlannedIgnoresActiveAndOtherRoutePlans() {
        val repository = InMemoryWalkRepository()
        val route = testRoute()
        val service = service(repository)
        repository.save(WalkingPlanFactory.create(route.copy(id = "other-route"), "walk-other", 2.0, 10.0))
        repository.save(WalkingPlanFactory.create(route, "walk-planned", 2.0, 10.0))
        repository.save(WalkingPlanFactory.create(route, "walk-active", 2.0, 10.0).copy(status = WalkStatus.ACTIVE, actualStartKm = 2.0))

        val restored = service.restorePlanned()

        assertEquals("walk-planned", restored!!.walk.id)
    }

    @Test fun restorePlannedUsesMostRecentlyStoredPlan() {
        val repository = InMemoryWalkRepository()
        val service = service(repository)
        repository.save(WalkingPlanFactory.create(testRoute(), "walk-old", 1.0, 5.0))
        repository.save(WalkingPlanFactory.create(testRoute(), "walk-new", 3.0, 8.0))

        val restored = service.restorePlanned()

        assertEquals("walk-new", restored!!.walk.id)
        assertEquals(3.0, restored.walk.plannedStartKm!!, 0.001)
    }

    private fun service(repository: InMemoryWalkRepository) = WalkingPreparationService(
        route = testRoute(),
        walkRepository = repository,
        apoiCatalog = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { emptyList() }))
    )

    private fun testRoute() = Route(
        id = "route",
        name = "Teste",
        officialName = "Teste oficial",
        totalDistanceKm = 20.0,
        source = "test",
        updatedAt = "2027-01-01",
        geometry = RouteGeometry(emptyList()),
        stages = emptyList()
    )
}
