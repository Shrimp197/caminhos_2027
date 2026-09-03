package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi

/**
 * Read model for APOI consultation during a walk.
 * It keeps search/filter state together so UI does not reimplement catalog rules.
 */
data class ApoiBrowserQuery(
    val routeId: String,
    val currentRouteKm: Double,
    val text: String = "",
    val filter: ApoiFilter = ApoiFilter(),
    val limit: Int = 8
)

data class ApoiBrowserState(
    val query: ApoiBrowserQuery,
    val results: List<ApoiAhead>,
    val selected: Apoi? = null
)

class ApoiBrowser(
    private val catalog: PublishedApoiCatalog
) {
    fun browse(query: ApoiBrowserQuery): ApoiBrowserState {
        val results = catalog.ahead(
            routeId = query.routeId,
            currentRouteKm = query.currentRouteKm,
            query = query.text,
            filter = query.filter,
            limit = query.limit
        )
        return ApoiBrowserState(query = query, results = results)
    }

    fun select(state: ApoiBrowserState, apoiId: String): ApoiBrowserState =
        state.copy(selected = state.results.firstOrNull { it.apoi.id == apoiId }?.apoi)

    fun clearSelection(state: ApoiBrowserState): ApoiBrowserState =
        state.copy(selected = null)
}
