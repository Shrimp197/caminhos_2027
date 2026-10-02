package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.data.ApoiDataSource
import com.caminhos2027.v1.core.data.ApoiRepository
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

class PublishedApoiCatalogTest {
    @Test fun emptyProductionDatasetProducesEmptyCatalog() {
        val catalog = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { emptyList() }))

        assertEquals(emptyList<Apoi>(), catalog.all())
        assertEquals(emptyList<Apoi>(), catalog.filter("route", 0.0))
        assertEquals(null, catalog.next("route", 0.0))
    }

    @Test fun publishedDatasetIsExposedWithoutRequalification() {
        val published = apoi("confirmed", 5.0, PublicationStatus.PUBLISHED)
        val catalog = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { listOf(published) }))

        assertEquals(listOf("confirmed"), catalog.all().map { it.id })
        assertEquals("confirmed", catalog.next("route", 0.0)?.id)
    }

    @Test fun warningRecordRemainsAvailableAndCanBeFiltered() {
        val warning = apoi("warning", 2.0, PublicationStatus.PUBLISHED_WITH_WARNING)
        val catalog = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { listOf(warning) }))

        assertEquals(listOf("warning"), catalog.filter("route", 0.0).map { it.id })
        assertEquals(emptyList<Apoi>(), catalog.filter("route", 0.0, ApoiFilter(includeWarnings = false)).map { it.id })
    }

    @Test fun routeAndDistanceRulesRemainInCatalogBoundary() {
        val valid = apoi("valid", 3.0)
        val behind = apoi("behind", 1.0)
        val otherRoute = valid.copy(id = "other", location = valid.location.copy(routeId = "other"))
        val catalog = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { listOf(valid, behind, otherRoute) }))

        assertEquals(listOf("valid"), catalog.filter("route", 2.0).map { it.id })
        assertEquals("valid", catalog.next("route", 2.0)?.id)
    }

    private fun apoi(
        id: String,
        km: Double,
        publication: PublicationStatus = PublicationStatus.PUBLISHED
    ) = Apoi(
        id = id,
        name = id,
        description = null,
        mainCategory = ApoiCategory.AGUA,
        services = setOf(ApoiCategory.AGUA),
        location = ApoiLocation(
            40.0, -8.0, LocationPrecision.EXACT,
            null, null, null, "route", km, 0.0, null, RouteRelation.ON_ROUTE
        ),
        publication = ApoiPublication(publication, null),
        availability = ApoiAvailability(status = ApoiAvailabilityStatus.CURRENT)
    )
}
