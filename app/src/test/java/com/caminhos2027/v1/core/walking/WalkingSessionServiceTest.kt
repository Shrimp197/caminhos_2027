package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.route.GpsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class WalkingSessionServiceTest {
    private val repository = InMemoryWalkRepository()
    private val stateRepository = InMemoryWalkingStateRepository()
    private val service = WalkingSessionService(repository, stateRepository)
    private val position = RoutePosition("test-route", 12.5, 4.0, "stage-2", PositionConfidence.HIGH)
    private val planned = Walk("walk-1", "test-route", plannedStartKm = 10.0, plannedDestinationKm = 20.0)
    private val startTime = Instant.parse("2026-09-01T08:00:00Z")
    private val stopTime = Instant.parse("2026-09-01T15:00:00Z")

    @Test
    fun preparePersistsPlannedWalk() {
        service.prepare(planned)
        assertSame(planned, service.get("walk-1"))
        assertNull(service.resume())
    }

    @Test
    fun startPersistsActiveWalkAndInitialCheckpoint() {
        service.prepare(planned)

        val started = service.start("walk-1", position, startTime)

        assertEquals(WalkStatus.ACTIVE, started.status)
        assertSame(started, service.get("walk-1"))
        assertSame(started, service.resume())
        assertEquals(12.5, service.resumeCheckpoint("walk-1")!!.routePosition!!.routeKm, 0.001)
        assertEquals(GpsState.ACQUIRING, service.resumeCheckpoint("walk-1")!!.gpsState)
    }

    @Test
    fun updateCheckpointsCurrentWalkingStateSeparately() {
        service.prepare(planned)
        service.start("walk-1", position, startTime)
        val state = WalkingState(
            walk = service.get("walk-1")!!,
            routePosition = position.copy(routeKm = 14.0),
            gpsState = GpsState.ON_ROUTE,
            progress = null,
            nextApoi = null,
            nextApoiDistanceKm = null
        )

        val updated = service.updatePosition("walk-1", state)

        assertEquals(14.0, updated.routePosition!!.routeKm, 0.001)
        assertEquals(14.0, service.resumeCheckpoint("walk-1")!!.routePosition!!.routeKm, 0.001)
        assertEquals(GpsState.ON_ROUTE, service.resumeCheckpoint("walk-1")!!.gpsState)
        assertEquals(12.5, updated.walk.actualStartKm!!, 0.001)
    }

    @Test
    fun observedAtIsPreservedWhenOmitted() {
        service.prepare(planned)
        service.start("walk-1", position, startTime)
        val state = WalkingState(service.get("walk-1")!!, position, GpsState.ON_ROUTE, null, null, null)
        val firstObservedAt = startTime.plusSeconds(10)
        service.updatePosition("walk-1", state, firstObservedAt)

        service.updatePosition("walk-1", state.copy(gpsState = GpsState.ACQUIRING))

        assertEquals(firstObservedAt, service.resumeCheckpoint("walk-1")!!.lastObservedAt)
    }

    @Test(expected = IllegalArgumentException::class)
    fun observedAtCannotMoveBackwards() {
        service.prepare(planned)
        service.start("walk-1", position, startTime)
        val state = WalkingState(service.get("walk-1")!!, position, GpsState.ON_ROUTE, null, null, null)
        service.updatePosition("walk-1", state, startTime.plusSeconds(20))

        service.updatePosition("walk-1", state, startTime.plusSeconds(10))
    }

    @Test
    fun rejectedUpdateDoesNotMutatePersistedCheckpoint() {
        service.prepare(planned)
        service.start("walk-1", position, startTime)
        val valid = WalkingState(service.get("walk-1")!!, position.copy(routeKm = 13.0), GpsState.ON_ROUTE, null, null, null)
        service.updatePosition("walk-1", valid, startTime.plusSeconds(20))

        val invalid = valid.copy(routePosition = position.copy(routeKm = Double.NaN))
        try {
            service.updatePosition("walk-1", invalid, startTime.plusSeconds(30))
        } catch (_: IllegalArgumentException) {
            // Expected: validation must happen before persistence.
        }

        val checkpoint = service.resumeCheckpoint("walk-1")!!
        assertEquals(13.0, checkpoint.routePosition!!.routeKm, 0.001)
        assertEquals(startTime.plusSeconds(20), checkpoint.lastObservedAt)
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotPersistNonFiniteRoutePosition() {
        service.prepare(planned)
        service.start("walk-1", position, startTime)
        val badPosition = position.copy(routeKm = Double.NaN)
        val state = WalkingState(service.get("walk-1")!!, badPosition, GpsState.ON_ROUTE, null, null, null)

        service.updatePosition("walk-1", state)
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotPersistNegativeRouteDistance() {
        service.prepare(planned)
        service.start("walk-1", position, startTime)
        val badPosition = position.copy(distanceToRouteMeters = -1.0)
        val state = WalkingState(service.get("walk-1")!!, badPosition, GpsState.ON_ROUTE, null, null, null)

        service.updatePosition("walk-1", state)
    }

    @Test
    fun stopPersistsCompletedWalkAndClearsCurrentState() {
        service.prepare(planned)
        service.start("walk-1", position, startTime)
        val state = WalkingState(
            walk = service.get("walk-1")!!,
            routePosition = position,
            gpsState = GpsState.ON_ROUTE,
            progress = null,
            nextApoi = null,
            nextApoiDistanceKm = null
        )
        service.updatePosition("walk-1", state)

        val stopped = service.stop("walk-1", position.copy(routeKm = 18.0), stopTime)

        assertEquals(WalkStatus.COMPLETED, stopped.status)
        assertEquals(18.0, stopped.actualEndKm!!, 0.001)
        assertEquals(stopTime, stopped.endedAt)
        assertNull(service.resume())
        assertNull(service.resumeCheckpoint("walk-1"))
    }

    @Test
    fun rejectedStopDoesNotCompleteOrClearActiveSession() {
        service.prepare(planned)
        service.start("walk-1", position, startTime)
        val checkpointBefore = service.resumeCheckpoint("walk-1")!!

        try {
            service.stop("walk-1", position.copy(routeId = "other-route"), stopTime)
        } catch (_: IllegalArgumentException) {
            // Expected: controller validation must precede persistence and checkpoint clearing.
        }

        assertEquals(WalkStatus.ACTIVE, service.get("walk-1")!!.status)
        assertSame(service.get("walk-1"), service.resume())
        assertEquals(checkpointBefore.routePosition, service.resumeCheckpoint("walk-1")!!.routePosition)
        assertEquals(GpsState.ACQUIRING, service.resumeCheckpoint("walk-1")!!.gpsState)
    }

    @Test
    fun aNewServiceInstanceCanResumeFromTheSameRepositories() {
        service.prepare(planned)
        service.start("walk-1", position, startTime)
        val state = WalkingState(
            walk = service.get("walk-1")!!,
            routePosition = position,
            gpsState = GpsState.ON_ROUTE,
            progress = null,
            nextApoi = null,
            nextApoiDistanceKm = null
        )
        service.updatePosition("walk-1", state)

        val restartedService = WalkingSessionService(repository, stateRepository)

        assertEquals(WalkStatus.ACTIVE, restartedService.resume()!!.status)
        assertEquals(12.5, restartedService.resumeCheckpoint("walk-1")!!.routePosition!!.routeKm, 0.001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotStartUnknownWalk() {
        service.start("missing", position, startTime)
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotStartWithWrongRoute() {
        service.prepare(planned)
        service.start("walk-1", position.copy(routeId = "other-route"), startTime)
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotStartWithNonFinitePosition() {
        service.prepare(planned)
        service.start("walk-1", position.copy(distanceToRouteMeters = Double.POSITIVE_INFINITY), startTime)
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotUpdatePlannedWalk() {
        service.prepare(planned)
        val state = WalkingState(planned, position, GpsState.ON_ROUTE, null, null, null)
        service.updatePosition("walk-1", state)
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotUpdateWithWrongRoute() {
        service.prepare(planned)
        service.start("walk-1", position, startTime)
        val state = WalkingState(
            service.get("walk-1")!!,
            position.copy(routeId = "other-route"),
            GpsState.ON_ROUTE,
            null,
            null,
            null
        )
        service.updatePosition("walk-1", state)
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotUpdateStateFromAnotherWalk() {
        service.prepare(planned)
        service.start("walk-1", position, startTime)
        val otherWalk = planned.copy(id = "walk-2", status = WalkStatus.ACTIVE)
        val state = WalkingState(otherWalk, position, GpsState.ON_ROUTE, null, null, null)
        service.updatePosition("walk-1", state)
    }

    @Test
    fun repositoryReturnsActiveWalkOnly() {
        service.prepare(planned)
        assertTrue(repository.list().contains(planned))
        assertNull(repository.getActive())
    }
}
