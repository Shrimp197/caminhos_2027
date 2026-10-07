package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.model.PositionConfidence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** Pure lifecycle contract: domain transitions must be explicit, ordered and non-mutating on rejection. */
class WalkingSessionLifecycleContractTest {
    private val start = Instant.parse("2026-09-04T12:00:00Z")
    private val end = Instant.parse("2026-09-04T15:30:00Z")
    private val routeId = "route"
    private val startPosition = RoutePosition(routeId, 0.4, 2.0, "stage-1", PositionConfidence.HIGH)
    private val endPosition = RoutePosition(routeId, 1.6, 2.0, "stage-2", PositionConfidence.HIGH)

    @Test
    fun plannedWalkStartsAsActiveAndRecordsActualStart() {
        val walk = plannedWalk()

        val started = WalkingSessionController.start(walk, startPosition, start)

        assertEquals(WalkStatus.ACTIVE, started.status)
        assertEquals(0.4, started.actualStartKm!!, 0.001)
        assertEquals(start, started.startedAt)
        assertEquals(null, started.actualEndKm)
        assertEquals(null, started.endedAt)
    }

    @Test
    fun activeWalkStopsAsCompletedAndRecordsActualEnd() {
        val active = WalkingSessionController.start(plannedWalk(), startPosition, start)

        val completed = WalkingSessionController.stop(active, endPosition, end)

        assertEquals(WalkStatus.COMPLETED, completed.status)
        assertEquals(0.4, completed.actualStartKm!!, 0.001)
        assertEquals(1.6, completed.actualEndKm!!, 0.001)
        assertEquals(start, completed.startedAt)
        assertEquals(end, completed.endedAt)
    }

    @Test
    fun onlyActiveWalkCanBeResumed() {
        assertFalse(WalkingSessionController.canResume(plannedWalk()))
        assertTrue(WalkingSessionController.canResume(WalkingSessionController.start(plannedWalk(), startPosition, start)))
        assertFalse(
            WalkingSessionController.canResume(
                WalkingSessionController.stop(
                    WalkingSessionController.start(plannedWalk(), startPosition, start),
                    endPosition,
                    end
                )
            )
        )
    }

    @Test
    fun startingAnAlreadyActiveWalkIsRejectedWithoutChangingIt() {
        val active = WalkingSessionController.start(plannedWalk(), startPosition, start)

        val error = assertFails {
            WalkingSessionController.start(active, startPosition.copy(routeKm = 0.8), end)
        }

        assertTrue(error.message.orEmpty().contains("planned walk"))
        assertEquals(WalkStatus.ACTIVE, active.status)
        assertEquals(0.4, active.actualStartKm!!, 0.001)
        assertEquals(start, active.startedAt)
    }

    @Test
    fun stoppingAPlannedWalkIsRejectedWithoutChangingIt() {
        val planned = plannedWalk()

        val error = assertFails {
            WalkingSessionController.stop(planned, endPosition, end)
        }

        assertTrue(error.message.orEmpty().contains("active walk"))
        assertEquals(planned, plannedWalk())
    }

    @Test
    fun stoppingBeforeStartTimeIsRejectedWithoutChangingActiveWalk() {
        val active = WalkingSessionController.start(plannedWalk(), startPosition, start)

        val error = assertFails {
            WalkingSessionController.stop(active, endPosition, start.minusSeconds(1))
        }

        assertTrue(error.message.orEmpty().contains("before walk start time"))
        assertEquals(WalkStatus.ACTIVE, active.status)
        assertEquals(start, active.startedAt)
        assertEquals(null, active.endedAt)
    }

    @Test
    fun routeMismatchIsRejectedBeforeLifecycleTransition() {
        val planned = plannedWalk()
        val foreignPosition = startPosition.copy(routeId = "foreign-route")

        val error = assertFails {
            WalkingSessionController.start(planned, foreignPosition, start)
        }

        assertTrue(error.message.orEmpty().contains("route"))
        assertEquals(WalkStatus.PLANNED, planned.status)
        assertEquals(null, planned.actualStartKm)
        assertEquals(null, planned.startedAt)
    }

    @Test
    fun invalidNumericPositionIsRejectedBeforeLifecycleTransition() {
        val planned = plannedWalk()
        val invalidPosition = startPosition.copy(routeKm = Double.NaN)

        val error = assertFails {
            WalkingSessionController.start(planned, invalidPosition, start)
        }

        assertTrue(error.message.orEmpty().contains("finite"))
        assertEquals(WalkStatus.PLANNED, planned.status)
        assertEquals(null, planned.actualStartKm)
        assertEquals(null, planned.startedAt)
    }

    @Test
    fun blankRoutePositionIsRejectedBeforeLifecycleTransition() {
        val planned = plannedWalk()
        val invalidPosition = startPosition.copy(routeId = "")

        val error = assertFails {
            WalkingSessionController.start(planned, invalidPosition, start)
        }

        assertTrue(error.message.orEmpty().contains("route"))
        assertEquals(WalkStatus.PLANNED, planned.status)
        assertEquals(null, planned.actualStartKm)
    }

    @Test
    fun negativeRouteDistanceAndNegativeRouteKmAreRejected() {
        val planned = plannedWalk()

        val negativeDistanceError = assertFails {
            WalkingSessionController.start(planned, startPosition.copy(distanceToRouteMeters = -0.01), start)
        }
        val negativeKmError = assertFails {
            WalkingSessionController.start(planned, startPosition.copy(routeKm = -0.01), start)
        }

        assertTrue(negativeDistanceError.message.orEmpty().contains("distanceToRouteMeters"))
        assertTrue(negativeKmError.message.orEmpty().contains("routeKm"))
        assertEquals(WalkStatus.PLANNED, planned.status)
    }

    private fun plannedWalk() = Walk(
        id = "walk",
        routeId = routeId,
        plannedStartKm = 0.0,
        plannedDestinationKm = 2.0,
        stageIds = listOf("stage-1", "stage-2")
    )

    private fun assertFails(block: () -> Unit): IllegalArgumentException = try {
        block()
        throw AssertionError("Expected lifecycle boundary rejection")
    } catch (error: IllegalArgumentException) {
        error
    }
}
