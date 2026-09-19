package com.caminhos2027.v1.core.walking

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.RoutePosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** JVM contract for recreating the walking runtime from shared persisted repositories. */
class WalkingSessionRecreationContractTest {
    private val route = Route(
        id = "published",
        name = "published",
        officialName = "published",
        totalDistanceKm = 1.0,
        source = "test",
        updatedAt = "2026-01-01",
        geometry = RouteGeometry(listOf(GeoPoint(40.0, -8.0), GeoPoint(40.009, -8.0))),
        stages = emptyList()
    )

    @Test
    fun recreatedRuntimeRestoresActiveWalkAndCheckpoint() {
        val repositories = Repositories()
        repositories.start("walk-1", 0.25)

        val recreated = repositories.runtime()
        val state = recreated.resume(Instant.parse("2026-01-01T11:00:00Z"))

        assertEquals("walk-1", state?.walk?.id)
        assertEquals(WalkStatus.ACTIVE, state?.walk?.status)
        assertEquals(0.25, state?.routePosition?.routeKm ?: -1.0, 0.0001)
    }

    @Test
    fun recreatedRuntimeDoesNotResurrectCompletedWalk() {
        val repositories = Repositories()
        repositories.start("walk-1", 0.25)
        repositories.service.stop("walk-1", position(0.75), Instant.parse("2026-01-01T10:00:00Z"))

        val recreated = repositories.runtime()

        assertNull(recreated.resume())
        assertEquals(WalkStatus.COMPLETED, repositories.service.get("walk-1")?.status)
        assertNull(repositories.stateRepository.get("walk-1"))
    }

    @Test
    fun existingRuntimeInvalidatesStaleCoordinatorWhenPersistedWalkIsNoLongerActive() {
        val repositories = Repositories()
        repositories.start("walk-1", 0.25)
        val runtime = repositories.runtime()
        runtime.resume() ?: error("expected active session")

        repositories.service.stop("walk-1", position(0.75), Instant.parse("2026-01-01T10:00:00Z"))

        assertNull(runtime.resume())

        val error = try {
            runtime.accept(rawPosition(0.80, "2026-01-01T10:01:00Z"))
            null
        } catch (expected: IllegalArgumentException) {
            expected
        }
        assertTrue(error?.message.orEmpty().contains("has not been started"))
    }

    @Test
    fun rejectedForeignRouteResumeLeavesCurrentCoordinatorUntouched() {
        val repositories = Repositories()
        repositories.start("walk-1", 0.25)
        val runtime = repositories.runtime()
        val current = runtime.resume() ?: error("expected active session")

        repositories.walkRepository.save(walk("foreign", "foreign", WalkStatus.ACTIVE))
        repositories.walkRepository.forceActiveId("foreign")

        val error = try {
            runtime.resume()
            null
        } catch (expected: IllegalArgumentException) {
            expected
        }

        assertTrue(error?.message.orEmpty().contains("published V1 route"))
        val stillRunning = runtime.accept(rawPosition(0.30, "2026-01-01T09:01:00Z"))
        assertEquals(current.walk.id, stillRunning.walk.id)
    }

    private fun rawPosition(routeKm: Double, capturedAt: String) =
        RawGpsPosition(
            latitude = 40.0,
            longitude = -8.0,
            accuracyMeters = 5.0,
            capturedAt = Instant.parse(capturedAt)
        )

    private fun walk(id: String, routeId: String, status: WalkStatus): Walk =
        WalkingPlanFactory.create(route(routeId), id, 0.0, 1.0).copy(status = status)

    private fun route(id: String): Route = route.copy(id = id, name = id, officialName = id)

    private fun position(routeKm: Double) = RoutePosition(
        routeId = route.id,
        routeKm = routeKm,
        distanceToRouteMeters = 0.0
    )

    private inner class Repositories {
        val walkRepository = SelectableActiveWalkRepository()
        val stateRepository = InMemoryWalkingStateRepository()
        val service = WalkingSessionService(walkRepository, stateRepository)

        fun start(id: String, routeKm: Double): Walk {
            service.prepare(walk(id, route.id, WalkStatus.PLANNED))
            return service.start(id, position(routeKm), Instant.parse("2026-01-01T09:00:00Z"))
        }

        fun runtime() = WalkingSessionRuntime(route, service, emptyList())
    }

    private class SelectableActiveWalkRepository : WalkRepository {
        private val delegate = InMemoryWalkRepository()
        private var forcedActiveId: String? = null

        override fun save(walk: Walk) = delegate.save(walk)
        override fun getById(id: String): Walk? = delegate.getById(id)
        override fun getActive(): Walk? = forcedActiveId?.let { delegate.getById(it) }?.takeIf { it.status == WalkStatus.ACTIVE }
            ?: delegate.getActive()
        override fun list(): List<Walk> = delegate.list()

        fun forceActiveId(id: String) {
            forcedActiveId = id
        }
    }
}
