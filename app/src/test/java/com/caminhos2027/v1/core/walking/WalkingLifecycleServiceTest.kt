package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class WalkingLifecycleServiceTest {
    private val repository = InMemoryWalkRepository()
    private val service = WalkingLifecycleService(repository)
    private val routePosition = RoutePosition("test-route", 4.2, 3.0)
    private val laterPosition = RoutePosition("test-route", 9.7, 2.0)

    private fun plannedWalk() = Walk("walk-1", "test-route")

    @Test
    fun prepareAndResumeActiveWalk() {
        service.prepare(plannedWalk())
        assertEquals(WalkStatus.PLANNED, repository.getById("walk-1")?.status)

        val started = service.start("walk-1", routePosition, Instant.parse("2026-09-01T08:00:00Z"))
        assertEquals(WalkStatus.ACTIVE, started.status)
        assertEquals(4.2, started.actualStartKm!!, 0.001)
        assertNotNull(service.resume("walk-1"))
    }

    @Test
    fun stopPersistsCompletionAndRemovesActiveWalk() {
        service.prepare(plannedWalk())
        service.start("walk-1", routePosition, Instant.parse("2026-09-01T08:00:00Z"))
        val completed = service.stop("walk-1", laterPosition, Instant.parse("2026-09-01T12:00:00Z"))

        assertEquals(WalkStatus.COMPLETED, completed.status)
        assertEquals(9.7, completed.actualEndKm!!, 0.001)
        assertEquals(Instant.parse("2026-09-01T12:00:00Z"), completed.endedAt)
        assertNull(service.activeWalk())
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotPrepareSameWalkTwice() {
        service.prepare(plannedWalk())
        service.prepare(plannedWalk())
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotStartWithDifferentRoute() {
        service.prepare(plannedWalk())
        service.start("walk-1", routePosition.copy(routeId = "other-route"), Instant.parse("2026-09-01T08:00:00Z"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotStartUnknownWalk() {
        service.start("missing", routePosition, Instant.parse("2026-09-01T08:00:00Z"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotStopUnknownWalk() {
        service.stop("missing", routePosition, Instant.parse("2026-09-01T12:00:00Z"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun completedWalkCannotBeResumed() {
        service.prepare(plannedWalk())
        service.start("walk-1", routePosition, Instant.parse("2026-09-01T08:00:00Z"))
        service.stop("walk-1", laterPosition, Instant.parse("2026-09-01T12:00:00Z"))
        service.resume("walk-1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotResumeUnknownWalk() {
        service.resume("missing")
    }
}
