package com.caminhos2027.v1.core.model

import java.time.Instant

/** A pilgrim's actual or planned walking session. */
data class Walk(
    val id: String,
    val routeId: String,
    val plannedStartKm: Double? = null,
    val plannedDestinationKm: Double? = null,
    val actualStartKm: Double? = null,
    val actualEndKm: Double? = null,
    val startedAt: Instant? = null,
    val endedAt: Instant? = null,
    val status: WalkStatus = WalkStatus.PLANNED,
    val stageIds: List<String> = emptyList()
)

enum class WalkStatus {
    PLANNED,
    ACTIVE,
    COMPLETED,
    CANCELLED
}

/** Overall pilgrimage objective; it provides context, never a rigid command. */
data class Objective(
    val id: String,
    val routeId: String,
    val destination: String,
    val targetDate: String? = null,
    val targetRouteKm: Double? = null,
    val active: Boolean = true
)

/** Raw physical position supplied by the device. */
data class RawGpsPosition(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Double? = null,
    val capturedAt: Instant
)

/** Position projected onto the official route geometry. */
data class RoutePosition(
    val routeId: String,
    val routeKm: Double,
    val distanceToRouteMeters: Double,
    val stageId: String? = null,
    val confidence: PositionConfidence = PositionConfidence.UNKNOWN
)

enum class PositionConfidence {
    HIGH,
    MEDIUM,
    LOW,
    UNKNOWN
}
