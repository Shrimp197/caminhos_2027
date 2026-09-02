package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class WalkingSessionServiceTest {
    private val repository = InMemoryWalkRepository()
    private val service = WalkingSessionService(repository)
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
    fun updateKeepsLifecycleDataAndAcceptsSameRoutePosition() {
        service.prepare(planned)
        service.start("walk-1", position, startTime)

        val updated = service.updatePosition("walk-1", position.copy(routeKm = 14.0))

        assertEquals(WalkStatus.ACTIVE, updated.status)
        assertEquals(12.5, updated.actualStartKm!!, 0.001)
    }

    @Test
    fun stopPersistsCompletedWalkAndNoLongerResumes() {
        service.prepare(planned)
        service.start("walk-1", position, startTime)

        val stopped = service.stop("walk-1", position.copy(routeKm = 18.0), stopTime)

        assertEquals(WalkStatus.COMPLETED, stopped.status)
        assertEquals(18.0, stopped.actualEndKm!!, 0.001)
        assertEquals(stopTime, stopped.endedAt)
        assertNull(service.resume())
    }

    @Test
    fun aNewServiceInstanceCanResumeFromTheSameRepository() {
        service.prepare(planned)
        service.start("walk-1", position, startTime)

        val restartedService = WalkingSessionService(repository)

        assertEquals(WalkStatus.ACTIVE, restartedService.resume()!!.status)
        assertEquals("walk-1", restartedService.resume()!!.id)
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
        service.updatePosition("walk-1", position)
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotUpdateWithWrongRoute() {
        service.prepare(planned)
        service.start("walk-1", position, startTime)
        service.updatePosition("walk-1", position.copy(routeId = "other-route"))
    }

    @Test
    fun repositoryReturnsActiveWalkOnly() {
        service.prepare(planned)
        assertTrue(repository.list().contains(planned))
        assertNull(repository.getActive())
    }
}
