package com.caminhos2027.v1.core

import com.caminhos2027.v1.core.apoi.ApoiBrowser
import com.caminhos2027.v1.core.apoi.ApoiBrowserQuery
import com.caminhos2027.v1.core.apoi.ApoiFilter
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.Objective
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.walking.WalkingDecisionSupport
import com.caminhos2027.v1.core.walking.WalkingState

/** Small in-memory state holder for the V1 UI. It is intentionally not a framework. */
class AppStateStore(initial: AppState = AppState()) {
    var state: AppState = initial
        private set

    fun setWalking(walking: WalkingState?) {
        state = state.copy(walking = walking)
    }

    fun browseApoi(
        browser: ApoiBrowser,
        text: String = "",
        filter: ApoiFilter = ApoiFilter(),
        limit: Int = 8
    ) {
        val walking = requireWalkingPosition()
        state = state.copy(
            apoiBrowser = browser.browse(
                ApoiBrowserQuery(
                    routeId = walking.walk.routeId,
                    currentRouteKm = walking.routePosition.routeKm,
                    text = text,
                    filter = filter,
                    limit = limit
                )
            )
        )
    }

    fun selectApoi(browser: ApoiBrowser, apoiId: String) {
        val browserState = requireNotNull(state.apoiBrowser) { "APOI browser has not been initialized" }
        state = state.copy(apoiBrowser = browser.select(browserState, apoiId))
    }

    fun clearApoiSelection(browser: ApoiBrowser) {
        val browserState = requireNotNull(state.apoiBrowser) { "APOI browser has not been initialized" }
        state = state.copy(apoiBrowser = browser.clearSelection(browserState))
    }

    fun buildDecision(route: Route, publishedApoi: List<Apoi>) {
        val walking = requireWalkingPosition()
        state = state.copy(
            decision = WalkingDecisionSupport.build(
                route = route,
                walk = walking.walk,
                position = walking.routePosition,
                publishedApoi = publishedApoi
            )
        )
    }

    fun setObjective(objective: Objective?) {
        state = state.copy(objective = objective)
    }

    fun clearDecision() {
        state = state.copy(decision = null)
    }

    fun setDataVersion(version: String?) {
        state = state.copy(dataVersion = version)
    }

    private fun requireWalkingPosition(): WalkingStateWithPosition {
        val walking = requireNotNull(state.walking) { "No active walking state" }
        val position = requireNotNull(walking.routePosition) { "Current route position is unavailable" }
        return WalkingStateWithPosition(walking, position)
    }

    private data class WalkingStateWithPosition(
        val walking: WalkingState,
        val routePosition: com.caminhos2027.v1.core.model.RoutePosition
    ) {
        val walk get() = walking.walk
    }
}
