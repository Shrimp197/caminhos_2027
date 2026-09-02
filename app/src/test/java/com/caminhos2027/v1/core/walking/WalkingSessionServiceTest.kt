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
    fun startPersistsActiveWalk() {
        service.prepare(planned)

        val started = service.start("walk-1", position, startTime)

        assertEquals(WalkStatus.ACTIVE, started.status)
        assertSame(started, service.get("walk-1"))
        assertSame(started, service.resume())
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
            nextApoi = null
        )

        val updated = service.updatePosition("walk-1", state)

        assertEquals(14.0, updated.routePosition!!.routeKm, 0.001)
        assertSame(updated, service.resumeState("walk-1"))
        assertEquals(12.5, updated.walk.actualStartKm!!, 0.001)
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
            nextApoi = null
        )
        service.updatePosition("walk-1", state)

        val stopped = service.stop("walk-1", position.copy(routeKm = 18.0), stopTime)

        assertEquals(WalkStatus.COMPLETED, stopped.status)
        assertEquals(18.0, stopped.actualEndKm!!, 0.001)
        assertEquals(stopTime, stopped.endedAt)
        assertNull(service.resume())
        assertNull(service.resumeState("walk-1"))
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
            nextApoi = null
        )
        service.updatePosition("walk-1", state)

        val restartedService = WalkingSessionService(repository, stateRepository)

        assertEquals(WalkStatus.ACTIVE, restartedService.resume()!!.status)
        assertEquals(12.5, restartedService.resumeState("walk-1")!!.routePosition!!.routeKm, 0.001)
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
    fun cannotUpdatePlannedWalk() {
        service.prepare(planned)
        val state = WalkingState(planned, position, GpsState.ON_ROUTE, null, null)
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
            null
        )
        service.updatePosition("walk-1", state)
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotUpdateStateFromAnotherWalk() {
        service.prepare(planned)
        service.start("walk-1", position, startTime)
        val otherWalk = planned.copy(id = "walk-2", status = WalkStatus.ACTIVE)
        val state = WalkingState(otherWalk, position, GpsState.ON_ROUTE, null, null)
        service.updatePosition("walk-1", state)
    }

    @Test
    fun repositoryReturnsActiveWalkOnly() {
        service.prepare(planned)
        assertTrue(repository.list().contains(planned))
        assertNull(repository.getActive())
    }
}
