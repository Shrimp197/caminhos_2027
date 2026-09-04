package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.route.GpsState
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WalkingCheckpointFreshnessBoundaryTest {
    private val route = Route(
        id = "route",
        name = "Route",
        officialName = "Route",
        totalDistanceKm = 1.0,
        source = "test",
        updatedAt = "2026-01-01",
        geometry = RouteGeometry(
            listOf(
                GeoPoint(40.0, -8.0),
                GeoPoint(40.01, -8.0)
            )
        ),
        stages = emptyList()
    )
    private val walk = Walk(id = "walk", routeId = route.id, actualStartKm = 0.0)
    private val position = RoutePosition(route.id, 0.4, 1.0)
    private val now = Instant.parse("2026-09-04T10:00:00Z")

    @Test
    fun checkpointJustUnderFreshnessLimitRemainsFresh() {
        val checkpoint = WalkingCheckpoint(
            routePosition = position,
            gpsState = GpsState.ON_ROUTE,
            isOffline = false,
            lastObservedAt = now.minusNanos(29_999_999_999L)
        )

        val restored = WalkingStateCoordinator(route, walk, emptyList())
            .restoreCheckpoint(checkpoint, now)

        assertEquals(GpsState.ON_ROUTE, restored.gpsState)
        assertEquals(checkpoint.lastObservedAt, restored.routePosition?.let { checkpoint.lastObservedAt })
        assertEquals(checkpoint.lastObservedAt, WalkingStateCoordinator(route, walk, emptyList()).run {
            restoreCheckpoint(checkpoint, now)
            lastReliableObservedAt()
        })
    }

    @Test
    fun checkpointExactlyAtFreshnessLimitIsStale() {
        val checkpoint = WalkingCheckpoint(
            routePosition = position,
            gpsState = GpsState.ON_ROUTE,
            isOffline = false,
            lastObservedAt = now.minusSeconds(30)
        )

        val coordinator = WalkingStateCoordinator(route, walk, emptyList())
        val restored = coordinator.restoreCheckpoint(checkpoint, now)

        assertEquals(GpsState.NO_SIGNAL, restored.gpsState)
        assertNull(coordinator.lastReliableObservedAt())
    }
}
