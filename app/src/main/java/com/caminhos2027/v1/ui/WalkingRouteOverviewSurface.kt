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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.model.GeoPoint
import kotlin.math.max

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

    fun sanitizeGeometry(geometry: List<GeoPoint>): List<GeoPoint> =
        geometry.filter { it.latitude.isFinite() && it.longitude.isFinite() }
}

@Composable
internal fun WalkingRouteOverviewSurface(
    geometry: List<GeoPoint>,
    currentRouteKm: Double?,
    totalDistanceKm: Double,
    modifier: Modifier = Modifier
) {
    val ratio = WalkingRouteOverviewPresenter.currentRatio(currentRouteKm, totalDistanceKm)
    val safeGeometry = WalkingRouteOverviewPresenter.sanitizeGeometry(geometry)
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Traçado local", style = MaterialTheme.typography.labelLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Text("Visualização esquemática do percurso oficial, disponível sem rede.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF68736D))
        Canvas(modifier = Modifier.fillMaxWidth().height(190.dp)) {
            if (safeGeometry.size < 2) return@Canvas
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

            val currentIndex = WalkingRouteOverviewPresenter.visiblePathPointIndex(safeGeometry.size, ratio)
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
    }
}
