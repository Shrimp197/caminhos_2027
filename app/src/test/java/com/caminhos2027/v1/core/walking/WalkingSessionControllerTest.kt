package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class WalkingSessionControllerTest {
    private val routePosition = RoutePosition("test-route", 12.5, 4.0, "stage-2", PositionConfidence.HIGH)
    private val planned = Walk("walk-1", "test-route", plannedStartKm = 10.0, plannedDestinationKm = 20.0)
    private val startTime = Instant.parse("2026-09-01T08:00:00Z")
    private val stopTime = Instant.parse("2026-09-01T15:00:00Z")

    @Test
    fun startMarksWalkActiveAndRecordsActualStart() {
        val started = WalkingSessionController.start(planned, routePosition, startTime)
        assertEquals(WalkStatus.ACTIVE, started.status)
        assertEquals(12.5, started.actualStartKm!!, 0.001)
        assertEquals(startTime, started.startedAt)
        assertTrue(WalkingSessionController.canResume(started))
    }

    @Test
    fun stopMarksActiveWalkCompletedAndRecordsEnd() {
        val started = WalkingSessionController.start(planned, routePosition, startTime)
        val stopped = WalkingSessionController.stop(started, routePosition.copy(routeKm = 15.0), stopTime)
        assertEquals(WalkStatus.COMPLETED, stopped.status)
        assertEquals(15.0, stopped.actualEndKm!!, 0.001)
        assertEquals(stopTime, stopped.endedAt)
        assertFalse(WalkingSessionController.canResume(stopped))
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotStartCompletedWalk() {
        WalkingSessionController.start(planned.copy(status = WalkStatus.COMPLETED), routePosition, startTime)
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotCrossRoutesWhenStarting() {
        WalkingSessionController.start(planned, routePosition.copy(routeId = "other-route"), startTime)
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotStopPlannedWalk() {
        WalkingSessionController.stop(planned, routePosition, stopTime)
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotCrossRoutesWhenStopping() {
        val started = WalkingSessionController.start(planned, routePosition, startTime)
        WalkingSessionController.stop(started, routePosition.copy(routeId = "other-route"), stopTime)
    }
}
