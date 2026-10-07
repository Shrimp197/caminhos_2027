package com.caminhos2027.v1.core

import com.caminhos2027.v1.core.apoi.ApoiBrowserState
import com.caminhos2027.v1.core.model.Objective
import com.caminhos2027.v1.core.walking.WalkingDecisionContext
import com.caminhos2027.v1.core.walking.WalkingState

/** Shared application read model for the V1 experience.
 * UI reads this aggregate; domain components remain responsible for producing each slice.
 */
data class AppState(
    val walking: WalkingState? = null,
    val apoiBrowser: ApoiBrowserState? = null,
    val decision: WalkingDecisionContext? = null,
    val objective: Objective? = null,
    val dataVersion: String? = null
)

object AppStateBuilder {
    fun build(
        walking: WalkingState?,
        apoiBrowser: ApoiBrowserState? = null,
        decision: WalkingDecisionContext? = null,
        objective: Objective? = null,
        dataVersion: String? = null
    ): AppState = AppState(
        walking = walking,
        apoiBrowser = apoiBrowser,
        decision = decision,
        objective = objective,
        dataVersion = dataVersion
    )
}
