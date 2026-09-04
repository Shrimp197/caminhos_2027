package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.route.GpsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class WalkingCheckpointJsonCodecTest {
    private val checkpoint = WalkingCheckpoint(
        routePosition = RoutePosition(
            routeId = "route-1",
            routeKm = 12.345,
            distanceToRouteMeters = 4.5,
            stageId = "stage-2",
            confidence = PositionConfidence.HIGH
        ),
        gpsState = GpsState.ON_ROUTE,
        isOffline = true,
        lastObservedAt = Instant.parse("2026-09-04T10:20:30.123Z")
    )

    @Test
    fun encodeDecodePreservesAllCheckpointFields() {
        val decoded = WalkingCheckpointJsonCodec.decode(WalkingCheckpointJsonCodec.encode(checkpoint))
        assertEquals(checkpoint, decoded)
    }

    @Test
    fun emptyOptionalPositionIsPreserved() {
        val source = checkpoint.copy(routePosition = null, lastObservedAt = null)
        assertEquals(source, WalkingCheckpointJsonCodec.decode(WalkingCheckpointJsonCodec.encode(source)))
    }

    @Test
    fun malformedJsonReturnsNull() {
        assertNull(WalkingCheckpointJsonCodec.decode("not-json"))
    }

    @Test
    fun missingRequiredGpsStateReturnsNull() {
        assertNull(WalkingCheckpointJsonCodec.decode("{\"version\":1}"))
    }

    @Test
    fun unknownVersionReturnsNull() {
        assertNull(WalkingCheckpointJsonCodec.decode("{\"version\":99,\"gpsState\":\"ON_ROUTE\"}"))
    }

    @Test
    fun nonIntegerVersionReturnsNull() {
        assertNull(WalkingCheckpointJsonCodec.decode("{\"version\":\"1\",\"gpsState\":\"ON_ROUTE\"}"))
    }

    @Test
    fun legacyUnversionedCheckpointRemainsReadable() {
        val decoded = WalkingCheckpointJsonCodec.decode(
            """
            {
              "routePosition": {
                "routeId": "route-1",
                "routeKm": 12.0,
                "distanceToRouteMeters": 5.0,
                "stageId": null,
                "confidence": "MEDIUM"
              },
              "gpsState": "ACQUIRING",
              "isOffline": false,
              "lastObservedAt": "2026-09-04T10:20:30Z"
            }
            """.trimIndent()
        )

        assertNotNull(decoded)
        assertEquals(12.0, decoded!!.routePosition!!.routeKm, 0.001)
    }

    @Test
    fun blankRouteIdIsRejected() {
        assertNull(
            WalkingCheckpointJsonCodec.decode(
                """
                {
                  "version": 1,
                  "routePosition": {
                    "routeId": "   ",
                    "routeKm": 12,
                    "distanceToRouteMeters": 5,
                    "stageId": null,
                    "confidence": "LOW"
                  },
                  "gpsState": "ON_ROUTE"
                }
                """.trimIndent()
            )
        )
    }

    @Test
    fun blankStageIdIsNormalizedToNull() {
        val decoded = WalkingCheckpointJsonCodec.decode(
            """
            {
              "version": 1,
              "routePosition": {
                "routeId": "route-1",
                "routeKm": 12,
                "distanceToRouteMeters": 5,
                "stageId": "   ",
                "confidence": "LOW"
              },
              "gpsState": "ON_ROUTE"
            }
            """.trimIndent()
        )
        assertEquals(null, decoded!!.routePosition!!.stageId)
    }

    @Test
    fun invalidNumericPositionIsDiscardedWithoutDiscardingCheckpointMetadata() {
        val decoded = WalkingCheckpointJsonCodec.decode(
            """
            {
              "version": 1,
              "routePosition": {
                "routeId": "route-1",
                "routeKm": -1,
                "distanceToRouteMeters": 5,
                "stageId": null,
                "confidence": "LOW"
              },
              "gpsState": "NO_SIGNAL",
              "isOffline": false,
              "lastObservedAt": null
            }
            """.trimIndent()
        )
        assertNotNull(decoded)
        assertEquals(null, decoded!!.routePosition)
        assertEquals(GpsState.NO_SIGNAL, decoded.gpsState)
        assertEquals(false, decoded.isOffline)
    }

    @Test
    fun nonFiniteNumericPositionIsDiscardedWithoutDiscardingCheckpointMetadata() {
        val decoded = WalkingCheckpointJsonCodec.decode(
            """
            {
              "version": 1,
              "routePosition": {
                "routeId": "route-1",
                "routeKm": 1e309,
                "distanceToRouteMeters": 5,
                "stageId": null,
                "confidence": "LOW"
              },
              "gpsState": "ON_ROUTE"
            }
            """.trimIndent()
        )
        assertNotNull(decoded)
        assertNull(decoded!!.routePosition)
        assertEquals(GpsState.ON_ROUTE, decoded.gpsState)
    }

    @Test
    fun nonFiniteDistanceIsDiscardedWithoutDiscardingCheckpointMetadata() {
        val decoded = WalkingCheckpointJsonCodec.decode(
            """
            {
              "version": 1,
              "routePosition": {
                "routeId": "route-1",
                "routeKm": 12,
                "distanceToRouteMeters": 1e309,
                "stageId": null,
                "confidence": "LOW"
              },
              "gpsState": "NO_SIGNAL",
              "isOffline": true
            }
            """.trimIndent()
        )
        assertNotNull(decoded)
        assertNull(decoded!!.routePosition)
        assertEquals(GpsState.NO_SIGNAL, decoded.gpsState)
        assertEquals(true, decoded.isOffline)
    }

    @Test
    fun invalidTimestampIsRejected() {
        assertNull(
            WalkingCheckpointJsonCodec.decode(
                "{\"version\":1,\"gpsState\":\"ON_ROUTE\",\"isOffline\":false,\"lastObservedAt\":\"invalid\"}"
            )
        )
    }

    @Test
    fun unknownEnumValueIsRejected() {
        assertNull(
            WalkingCheckpointJsonCodec.decode(
                "{\"version\":1,\"gpsState\":\"UNKNOWN_STATE\",\"isOffline\":false,\"lastObservedAt\":null}"
            )
        )
    }

    @Test
    fun invalidPositionIsDiscardedWithoutDiscardingCheckpointMetadata() {
        val decoded = WalkingCheckpointJsonCodec.decode(
            """
            {
              "version": 1,
              "routePosition": {
                "routeId": "route-1",
                "routeKm": -1,
                "distanceToRouteMeters": 5,
                "stageId": null,
                "confidence": "LOW"
              },
              "gpsState": "NO_SIGNAL",
              "isOffline": true,
              "lastObservedAt": "2026-09-04T10:20:30Z"
            }
            """.trimIndent()
        )
        assertNotNull(decoded)
        assertEquals(null, decoded!!.routePosition)
        assertEquals(GpsState.NO_SIGNAL, decoded.gpsState)
        assertEquals(true, decoded.isOffline)
    }
}
