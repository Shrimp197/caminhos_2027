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
import org.junit.Assert.assertNull
import org.junit.Test

class ApoiBrowserTest {
    @Test fun browseCombinesTextFilterAndRoutePosition() {
        val water = apoi("water", "Fonte de água", 3.0, ApoiCategory.AGUA)
        val food = apoi("food", "Café", 4.0, ApoiCategory.ALIMENTACAO)
        val past = apoi("past", "Fonte antiga", 1.0, ApoiCategory.AGUA)
        val catalog = catalog(water, food, past)

        val state = ApoiBrowser(catalog).browse(
            ApoiBrowserQuery(
                routeId = "route",
                currentRouteKm = 2.0,
                text = "fonte",
                filter = ApoiFilter(services = setOf(ApoiCategory.AGUA))
            )
        )

        assertEquals(listOf("water"), state.results.map { it.apoi.id })
        assertEquals(1.0, state.results.single().distanceKm, 0.001)
    }

    @Test fun selectOnlySelectsAnItemFromCurrentResults() {
        val water = apoi("water", "Fonte de água", 3.0, ApoiCategory.AGUA)
        val browser = ApoiBrowser(catalog(water))
        val state = browser.browse(ApoiBrowserQuery("route", 2.0))

        val selected = browser.select(state, "water")
        assertEquals("water", selected.selected?.id)

        val missing = browser.select(state, "not-present")
        assertNull(missing.selected)
    }

    @Test fun clearSelectionReturnsToListWithoutChangingResults() {
        val water = apoi("water", "Fonte de água", 3.0, ApoiCategory.AGUA)
        val browser = ApoiBrowser(catalog(water))
        val state = browser.browse(ApoiBrowserQuery("route", 2.0))
        val selected = browser.select(state, "water")

        val cleared = browser.clearSelection(selected)
        assertNull(cleared.selected)
        assertEquals(listOf("water"), cleared.results.map { it.apoi.id })
    }

    @Test fun emptyCatalogProducesEmptyBrowserState() {
        val state = ApoiBrowser(PublishedApoiCatalog(ApoiRepository(ApoiDataSource { emptyList() })))
            .browse(ApoiBrowserQuery("route", 0.0))

        assertEquals(emptyList<ApoiAhead>(), state.results)
        assertNull(state.selected)
    }

    private fun catalog(vararg records: Apoi) =
        PublishedApoiCatalog(ApoiRepository(ApoiDataSource { records.toList() }))

    private fun apoi(id: String, name: String, km: Double, category: ApoiCategory) = Apoi(
        id = id,
        name = name,
        description = null,
        mainCategory = category,
        services = setOf(category),
        location = ApoiLocation(
            40.0, -8.0, LocationPrecision.EXACT,
            null, null, null, "route", km, 0.0, 0.0, RouteRelation.ON_ROUTE
        ),
        publication = ApoiPublication(PublicationStatus.PUBLISHED, null)
    )
}
