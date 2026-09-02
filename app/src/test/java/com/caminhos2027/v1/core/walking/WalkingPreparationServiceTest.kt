package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.apoi.PublishedApoiCatalog
import com.caminhos2027.v1.core.data.ApoiDataSource
import com.caminhos2027.v1.core.data.ApoiRepository
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
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
