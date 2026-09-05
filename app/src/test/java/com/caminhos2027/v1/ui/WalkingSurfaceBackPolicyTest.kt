package com.caminhos2027.v1.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WalkingSurfaceBackPolicyTest {
    @Test
    fun activeWalkingIsTheRootSurface() {
        assertNull(WalkingSurfaceBackPolicy.target(WalkingSurfaceBackPolicy.Surface.ACTIVE))
    }

    @Test
    fun apoIbrowserReturnsToWalking() {
        assertEquals(
            WalkingSurfaceBackPolicy.Surface.ACTIVE,
            WalkingSurfaceBackPolicy.target(WalkingSurfaceBackPolicy.Surface.APOI_BROWSER)
        )
    }

    @Test
    fun apoIdetailReturnsToBrowser() {
        assertEquals(
            WalkingSurfaceBackPolicy.Surface.APOI_BROWSER,
            WalkingSurfaceBackPolicy.target(WalkingSurfaceBackPolicy.Surface.APOI_DETAIL)
        )
    }

    @Test
    fun decisionReturnsToWalking() {
        assertEquals(
            WalkingSurfaceBackPolicy.Surface.ACTIVE,
            WalkingSurfaceBackPolicy.target(WalkingSurfaceBackPolicy.Surface.DECISION)
        )
    }
}
