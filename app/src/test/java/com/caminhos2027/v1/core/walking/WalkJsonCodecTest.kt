package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class WalkJsonCodecTest {
    private val walk = Walk(
        id = "walk-1",
        routeId = "route-1",
        plannedStartKm = 2.0,
        plannedDestinationKm = 20.0,
        actualStartKm = 2.5,
        actualEndKm = null,
        startedAt = Instant.parse("2026-09-04T10:00:00Z"),
        endedAt = null,
        status = WalkStatus.ACTIVE,
        stageIds = listOf("stage-1", "stage-2")
    )

    @Test
    fun encodeDecodePreservesWalk() {
        assertEquals(walk, WalkJsonCodec.decode(WalkJsonCodec.encode(walk)))
    }

    @Test
    fun emptyOptionalsRoundTrip() {
        val source = Walk("walk-2", "route-1")
        assertEquals(source, WalkJsonCodec.decode(WalkJsonCodec.encode(source)))
    }

    @Test
    fun malformedJsonReturnsNull() {
        assertNull(WalkJsonCodec.decode("not-json"))
    }

    @Test
    fun unknownVersionReturnsNull() {
        assertNull(WalkJsonCodec.decode("{\"version\":99,\"id\":\"w\",\"routeId\":\"r\"}"))
    }

    @Test
    fun legacyUnversionedWalkRemainsReadable() {
        val decoded = WalkJsonCodec.decode(
            """
            {
              "id": "walk-legacy",
              "routeId": "route-1",
              "plannedStartKm": 1.0,
              "plannedDestinationKm": 2.0,
              "actualStartKm": null,
              "actualEndKm": null,
              "startedAt": null,
              "endedAt": null,
              "status": "PLANNED",
              "stageIds": "stage-1\u001fstage-2"
            }
            """.trimIndent()
        )
        assertNotNull(decoded)
        assertEquals(listOf("stage-1", "stage-2"), decoded!!.stageIds)
    }

    @Test
    fun invalidNumericFieldReturnsNull() {
        assertNull(
            WalkJsonCodec.decode(
                "{\"version\":1,\"id\":\"w\",\"routeId\":\"r\",\"plannedStartKm\":-1,\"status\":\"PLANNED\"}"
            )
        )
    }

    @Test
    fun invalidTimestampReturnsNull() {
        assertNull(
            WalkJsonCodec.decode(
                "{\"version\":1,\"id\":\"w\",\"routeId\":\"r\",\"status\":\"PLANNED\",\"startedAt\":\"bad\"}"
            )
        )
    }

    @Test
    fun endBeforeStartReturnsNull() {
        assertNull(
            WalkJsonCodec.decode(
                "{\"version\":1,\"id\":\"w\",\"routeId\":\"r\",\"status\":\"COMPLETED\",\"startedAt\":\"2026-09-04T11:00:00Z\",\"endedAt\":\"2026-09-04T10:00:00Z\"}"
            )
        )
    }
}
