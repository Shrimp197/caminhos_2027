package com.caminhos2027.v1.core

import com.caminhos2027.v1.core.model.Objective
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppStateTest {
    @Test fun buildKeepsIndependentSlicesTogether() {
        val objective = Objective("objective", "route", "Fátima", targetRouteKm = 211.87)

        val state = AppStateBuilder.build(
            walking = null,
            apoiBrowser = null,
            decision = null,
            objective = objective,
            dataVersion = "2027.1"
        )

        assertNull(state.walking)
        assertNull(state.apoiBrowser)
        assertNull(state.decision)
        assertEquals(objective, state.objective)
        assertEquals("2027.1", state.dataVersion)
    }

    @Test fun defaultStateIsSafeAndEmpty() {
        assertEquals(AppState(), AppStateBuilder.build(walking = null))
    }
}
