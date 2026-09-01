package com.caminhos2027.v1.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class WalkingModelsTest {
    @Test
    fun walkDefaultsToPlanned() {
        val walk = Walk(id = "w1", routeId = "route1")
        assertEquals(WalkStatus.PLANNED, walk.status)
        assertEquals(emptyList(), walk.stageIds)
    }

    @Test
    fun routePositionKeepsRawRouteDistanceSeparate() {
        val position = RoutePosition(
            routeId = "route1",
            routeKm = 42.5,
            distanceToRouteMeters = 18.0,
            stageId = "stage2",
            confidence = PositionConfidence.MEDIUM
        )
        assertEquals(42.5, position.routeKm)
        assertEquals(18.0, position.distanceToRouteMeters)
    }

    @Test
    fun walkingNoteCanBeLinkedToWalkAndPosition() {
        val position = RoutePosition("route1", 12.0, 4.0)
        val note = Note(
            id = "n1",
            type = NoteType.WALKING,
            title = "Nota da caminhada — 01/09/2026 14:35",
            content = "Água encontrada.",
            createdAt = java.time.Instant.parse("2026-09-01T14:35:00Z"),
            updatedAt = java.time.Instant.parse("2026-09-01T14:35:00Z"),
            originalDateTime = java.time.Instant.parse("2026-09-01T14:35:00Z"),
            walkId = "w1",
            routePosition = position
        )
        assertEquals(NoteType.WALKING, note.type)
        assertEquals("w1", note.walkId)
        assertEquals(12.0, note.routePosition?.routeKm)
    }
}
