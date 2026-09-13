package com.caminhos2027.v1.core

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
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.walking.WalkingState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AppStateStoreIntegrationTest {
    @Test
    fun browsingUsesCurrentSharedWalkingPosition() {
        val waterNear = apoi("near", "Fonte próxima", 4.0, ApoiCategory.AGUA)
        val waterBehind = apoi("behind", "Fonte atrás", 2.0, ApoiCategory.AGUA)
        val catalog = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { listOf(waterNear, waterBehind) }))
        val browser = com.caminhos2027.v1.core.apoi.ApoiBrowser(catalog)
        val walking = walkingState(3.0)
        val store = AppStateStore(AppState(walking = walking))

        store.browseApoi(browser, filter = ApoiFilter(services = setOf(ApoiCategory.AGUA)))

        assertEquals(listOf("near"), store.state.apoiBrowser?.results?.map { it.apoi.id })
        assertEquals(3.0, store.state.apoiBrowser?.query?.currentRouteKm ?: -1.0, 0.001)
    }

    @Test
    fun selectingApoiDoesNotReplaceWalkingContext() {
        val water = apoi("water", "Fonte", 4.0, ApoiCategory.AGUA)
        val catalog = PublishedApoiCatalog(ApoiRepository(ApoiDataSource { listOf(water) }))
        val browser = com.caminhos2027.v1.core.apoi.ApoiBrowser(catalog)
        val walking = walkingState(3.0)
        val store = AppStateStore(AppState(walking = walking))

        store.browseApoi(browser)
        val walkingBefore = store.state.walking
        store.selectApoi(browser, "water")

        assertSame(walkingBefore, store.state.walking)
        assertEquals("water", store.state.apoiBrowser?.selected?.id)
    }

    private fun walkingState(routeKm: Double) = WalkingState(
        walk = Walk("walk", "route", status = com.caminhos2027.v1.core.model.WalkStatus.ACTIVE),
        routePosition = RoutePosition("route", routeKm, 0.0),
        gpsState = GpsState.ON_ROUTE,
        progress = null,
        nextApoi = null,
        nextApoiDistanceKm = null
    )

    private fun apoi(id: String, name: String, km: Double, category: ApoiCategory) = Apoi(
        id = id,
        name = name,
        description = null,
        mainCategory = category,
        services = setOf(category),
        location = ApoiLocation(
            40.0, -8.0, LocationPrecision.EXACT,
            null, null, null, "route", km, 0.0, 0.0,
            com.caminhos2027.v1.core.model.RouteRelation.ON_ROUTE
        ),
        publication = ApoiPublication(PublicationStatus.PUBLISHED, null)
    )
}
