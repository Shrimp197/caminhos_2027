package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.AppState
import com.caminhos2027.v1.core.AppStateStore
import com.caminhos2027.v1.core.apoi.ApoiBrowser
import com.caminhos2027.v1.core.apoi.ApoiFilter
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.Route

/** Coordinates APOI consultation and neutral decision context from the shared walking state. */
class WalkingApoiDecisionController(
    private val route: Route,
    private val catalog: com.caminhos2027.v1.core.apoi.PublishedApoiCatalog,
    private val store: AppStateStore
) {
    private val browser = ApoiBrowser(catalog)

    fun browseApoi(
        text: String = "",
        filter: ApoiFilter = ApoiFilter(),
        limit: Int = 8
    ): AppState {
        store.browseApoi(browser, text, filter, limit)
        return store.state
    }

    fun selectApoi(apoiId: String): AppState {
        store.selectApoi(browser, apoiId)
        return store.state
    }

    fun clearApoiSelection(): AppState {
        store.clearApoiSelection(browser)
        return store.state
    }

    fun buildDecision(): AppState {
        store.buildDecision(route, catalog.all())
        return store.state
    }

    fun clearDecision(): AppState {
        store.clearDecision()
        return store.state
    }
}
