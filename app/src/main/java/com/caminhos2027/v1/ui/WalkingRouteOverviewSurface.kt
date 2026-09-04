package com.caminhos2027.v1.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.model.GeoPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

internal data class WalkingRouteGeometryProfile(
    val points: List<GeoPoint>,
    val cumulativeMeters: List<Double>,
    val totalMeters: Double
)

internal object WalkingRouteOverviewPresenter {
    fun currentRatio(currentRouteKm: Double?, totalDistanceKm: Double): Float {
        if (currentRouteKm == null || !currentRouteKm.isFinite() || !totalDistanceKm.isFinite() || totalDistanceKm <= 0.0) {
            return 0f
        }
        return (currentRouteKm / totalDistanceKm).coerceIn(0.0, 1.0).toFloat()
    }

    fun visiblePathPointIndex(pointCount: Int, ratio: Float): Int {
        if (pointCount <= 1) return 0
        return (ratio.coerceIn(0f, 1f) * (pointCount - 1)).toInt().coerceIn(0, pointCount - 1)
    }

    fun buildGeometryProfile(geometry: List<GeoPoint>): WalkingRouteGeometryProfile {
        val safeGeometry = sanitizeGeometry(geometry)
        if (safeGeometry.isEmpty()) {
            return WalkingRouteGeometryProfile(emptyList(), emptyList(), 0.0)
        }

        val cumulative = ArrayList<Double>(safeGeometry.size)
        cumulative += 0.0
        var total = 0.0
        safeGeometry.zipWithNext().forEach { pair ->
            total += distanceMeters(pair.first, pair.second)
            cumulative += total
        }
        return WalkingRouteGeometryProfile(safeGeometry, cumulative, total)
    }

    fun visiblePathPointIndex(profile: WalkingRouteGeometryProfile, ratio: Float): Int {
        val points = profile.points
        if (points.size <= 1) return 0
        if (!profile.totalMeters.isFinite() || profile.totalMeters <= 0.0 || profile.cumulativeMeters.size != points.size) {
            return visiblePathPointIndex(points.size, ratio)
        }

        val targetMeters = profile.totalMeters * ratio.coerceIn(0f, 1f)
        var low = 0
        var high = profile.cumulativeMeters.lastIndex
        while (low < high) {
            val middle = (low + high) ushr 1
            if (profile.cumulativeMeters[middle] < targetMeters) {
                low = middle + 1
            } else {
                high = middle
            }
        }
        return low.coerceIn(0, points.lastIndex)
    }

    fun visiblePathPointIndex(geometry: List<GeoPoint>, ratio: Float): Int =
        visiblePathPointIndex(buildGeometryProfile(geometry), ratio)

    /** Returns the bearing of the route segment at the current visual position. */
    fun routeBearingDegrees(profile: WalkingRouteGeometryProfile, ratio: Float): Double? {
        val points = profile.points
        if (points.size < 2) return null
        val index = visiblePathPointIndex(profile, ratio)
        val startIndex = (index - 1).coerceAtLeast(0).coerceAtMost(points.lastIndex - 1)
        val start = points[startIndex]
        val end = points[startIndex + 1]
        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)
        val deltaLon = Math.toRadians(end.longitude - start.longitude)
        val y = sin(deltaLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)
        if (!x.isFinite() || !y.isFinite() || (x == 0.0 && y == 0.0)) return null
        return Math.toDegrees(atan2(y, x)).let { (it + 360.0) % 360.0 }
    }

    fun routeBearingDegrees(geometry: List<GeoPoint>, ratio: Float): Double? =
        routeBearingDegrees(buildGeometryProfile(geometry), ratio)

    fun routeDirectionLabel(bearingDegrees: Double?): String? {
        if (bearingDegrees == null || !bearingDegrees.isFinite()) return null
        val normalized = ((bearingDegrees % 360.0) + 360.0) % 360.0
        val directions = arrayOf("Norte", "Nordeste", "Este", "Sudeste", "Sul", "Sudoeste", "Oeste", "Noroeste")
        val index = (((normalized + 22.5) / 45.0).toInt()) % directions.size
        return directions[index]
    }

    fun sanitizeGeometry(geometry: List<GeoPoint>): List<GeoPoint> =
        geometry.filter { it.latitude.isFinite() && it.longitude.isFinite() }

    private fun distanceMeters(a: GeoPoint, b: GeoPoint): Double {
        val earthRadiusMeters = 6_371_000.0
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val deltaLat = lat2 - lat1
        val deltaLon = Math.toRadians(b.longitude - a.longitude)
        val sinLat = sin(deltaLat / 2.0)
        val sinLon = sin(deltaLon / 2.0)
        val h = sinLat * sinLat + cos(lat1) * cos(lat2) * sinLon * sinLon
        return 2.0 * earthRadiusMeters * atan2(Math.sqrt(h.coerceIn(0.0, 1.0)), Math.sqrt((1.0 - h).coerceIn(0.0, 1.0)))
    }
}

@Composable
internal fun WalkingRouteOverviewSurface(
    geometry: List<GeoPoint>,
    currentRouteKm: Double?,
    totalDistanceKm: Double,
    modifier: Modifier = Modifier
) {
    val ratio = WalkingRouteOverviewPresenter.currentRatio(currentRouteKm, totalDistanceKm)
    val profile = remember(geometry) { WalkingRouteOverviewPresenter.buildGeometryProfile(geometry) }
    val directionLabel = WalkingRouteOverviewPresenter.routeDirectionLabel(
        WalkingRouteOverviewPresenter.routeBearingDegrees(profile, ratio)
    )
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Traçado local", style = MaterialTheme.typography.labelLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Text("Visualização esquemática do percurso oficial, disponível sem rede.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF68736D))
        Canvas(modifier = Modifier.fillMaxWidth().height(190.dp)) {
            if (profile.points.size < 2) return@Canvas
            val safeGeometry = profile.points
            val minLat = safeGeometry.minOf { it.latitude }
            val maxLat = safeGeometry.maxOf { it.latitude }
            val minLon = safeGeometry.minOf { it.longitude }
            val maxLon = safeGeometry.maxOf { it.longitude }
            val latSpan = max(maxLat - minLat, 1e-9)
            val lonSpan = max(maxLon - minLon, 1e-9)
            val padding = 12f
            val scaleX = (size.width - padding * 2f) / lonSpan.toFloat()
            val scaleY = (size.height - padding * 2f) / latSpan.toFloat()
            val scale = minOf(scaleX, scaleY)
            val renderedWidth = lonSpan.toFloat() * scale
            val renderedHeight = latSpan.toFloat() * scale
            val offsetX = (size.width - renderedWidth) / 2f
            val offsetY = (size.height - renderedHeight) / 2f

            fun project(point: GeoPoint): Offset = Offset(
                x = offsetX + (point.longitude - minLon).toFloat() * scale,
                y = offsetY + (maxLat - point.latitude).toFloat() * scale
            )

            val path = Path().apply {
                moveTo(project(safeGeometry.first()).x, project(safeGeometry.first()).y)
                safeGeometry.drop(1).forEach { point ->
                    val projected = project(point)
                    lineTo(projected.x, projected.y)
                }
            }
            drawPath(path = path, color = Color(0xFF165B43), style = Stroke(width = 6f, cap = StrokeCap.Round))

            val currentIndex = WalkingRouteOverviewPresenter.visiblePathPointIndex(profile, ratio)
            val current = project(safeGeometry[currentIndex])
            drawCircle(color = Color.White, radius = 10f, center = current)
            drawCircle(color = Color(0xFF165B43), radius = 6f, center = current)
            drawCircle(color = Color(0xFF165B43), radius = 5f, center = project(safeGeometry.first()))
            drawCircle(color = Color(0xFF165B43), radius = 5f, center = project(safeGeometry.last()))
        }
        Text(
            "Início · posição atual · destino",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF68736D)
        )
        directionLabel?.let {
            Text(
                "Sentido do traçado: $it",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF68736D)
            )
        }
    }
}
