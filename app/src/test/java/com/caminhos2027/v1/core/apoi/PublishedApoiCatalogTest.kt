package com.caminhos2027.v1.core.apoi

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
import org.junit.Assert.assertTrue
import org.junit.Test

class PublishedApoiCatalogTest {
    @Test
    fun `application catalog reads only repository publication dataset`() {
        val near = apoi("near", 10.0, setOf(ApoiCategory.AGUA))
        val later = apoi("later", 15.0, setOf(ApoiCategory.PERNOITA))
        val repository = ApoiRepository(object : ApoiDataSource {
            override fun load(): List<Apoi> = listOf(near, later)
        })

        val catalog = PublishedApoiCatalog(repository)

        assertEquals(listOf("near", "later"), catalog.all().map { it.id })
        assertEquals("near", catalog.next("caminho-centenario", 5.0)?.id)
        assertEquals(listOf("later"), catalog.filter(
            "caminho-centenario",
            5.0,
            ApoiFilter(services = setOf(ApoiCategory.PERNOITA))
        ).map { it.id })
    }

    @Test
    fun `catalog preserves warning publication status without treating it as hidden`() {
        val warning = apoi("warning", 12.0, setOf(ApoiCategory.DUCHES), PublicationStatus.PUBLISHED_WITH_WARNING)
        val repository = ApoiRepository(object : ApoiDataSource {
            override fun load(): List<Apoi> = listOf(warning)
        })

        val result = PublishedApoiCatalog(repository).filter("caminho-centenario", 0.0)

        assertEquals(1, result.size)
        assertEquals(PublicationStatus.PUBLISHED_WITH_WARNING, result.single().publication.status)
        assertTrue(result.single().publication.reason == null || result.single().publication.reason!!.isNotBlank())
    }

    private fun apoi(
        id: String,
        routeKm: Double,
        services: Set<ApoiCategory>,
        status: PublicationStatus = PublicationStatus.PUBLISHED
    ) = Apoi(
        id = id,
        name = id,
        description = null,
        mainCategory = services.first(),
        services = services,
        location = ApoiLocation(
            latitude = null,
            longitude = null,
            precision = LocationPrecision.EXACT,
            locality = null,
            municipality = null,
            reference = null,
            routeId = "caminho-centenario",
            routeKm = routeKm,
            distanceToRouteM = 0.0,
            accessDistanceM = 0.0,
            routeRelation = RouteRelation.ON_ROUTE
        ),
        publication = ApoiPublication(status, null)
    )
}
