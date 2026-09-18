package com.caminhos2027.v1.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.data.AndroidRouteOption
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.walking.WalkingState
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlin.math.cos
import kotlin.math.max

private val V2Forest = Color(0xFF0E6546)
private val V2ForestSoft = Color(0xFFE6F2EB)
private val V2Map = Color(0xFFF0F0E9)
private val V2Road = Color(0xFFC9C7BF)
private val V2MajorRoad = Color(0xFFB1AEA4)
private val V2Muted = Color(0xFF68736D)

@Composable
internal fun V1ActiveExperienceScreenV2(
    state: WalkingState,
    route: Route,
    routeOptions: List<AndroidRouteOption>,
    onStop: () -> Unit,
    onOpenApoi: () -> Unit,
    onOpenDecision: () -> Unit,
    onQaAdvance: () -> Unit,
    onQaToggleGps: (Boolean) -> Unit,
    onQaDeviation: () -> Unit
) {
    val currentKm = state.routePosition?.routeKm ?: 0.0
    val remainingKm = state.progress?.remainingKm ?: 0.0
    var nowMillis by remember(state.walk.startedAt) { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.walk.startedAt) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            kotlinx.coroutines.delay(1_000)
        }
    }
    val elapsed = elapsedWalkingTime(state.walk.startedAt, nowMillis)
    val progress = state.progress?.progressRatio?.coerceIn(0.0, 1.0) ?: 0.0
    val destinationKm = state.walk.plannedDestinationKm ?: 0.0
    val projectedPoint = state.routePosition?.projectedPoint

    Column(Modifier.fillMaxSize().background(Color(0xFFF7F5EF))) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(route.officialName, color = V2Forest, fontWeight = FontWeight.ExtraBold)
                Text(gpsLabel(state.gpsState), color = gpsColor(state.gpsState), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = onStop) { Text("PARAR") }
        }

        OfflineCartographicRouteMap(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp),
            geometry = route.geometry.points,
            projectedPoint = projectedPoint,
            gpsState = state.gpsState
        )

        Card(Modifier.fillMaxWidth().padding(12.dp), RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("${fmt(currentKm)} km", "Percorridos", Modifier.weight(1f))
                    MetricCard("${fmt(remainingKm)} km", "Para o fim", Modifier.weight(1f))
                    MetricCard(elapsed, "Tempo", Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Progresso", color = V2Muted, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Text("${(progress * 100).toInt()}%", color = V2Forest, fontWeight = FontWeight.ExtraBold)
                }
                LinearProgressIndicator(progress = { progress.toFloat() }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(10.dp)), color = V2Forest, trackColor = V2ForestSoft)
                Text("Destino planeado · ${fmt(destinationKm)} km", color = V2Muted, style = MaterialTheme.typography.bodySmall)
                HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(Icons.Filled.LocationOn, null, tint = V2Forest)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("A minha posição", color = V2Forest, fontWeight = FontWeight.ExtraBold)
                        Text(positionDetail(state), color = V2Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
                state.nextApoi?.let { apoi ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(Icons.Filled.Place, null, tint = V2Forest)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Próximo APOI", color = V2Muted, style = MaterialTheme.typography.labelLarge)
                            Text(apoi.name, fontWeight = FontWeight.ExtraBold)
                        }
                        Text(state.nextApoiDistanceKm?.let(::fmtDistance) ?: "—", color = V2Forest, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onOpenApoi, Modifier.weight(1f)) { Text("VER APOIOS") }
                    OutlinedButton(onClick = onOpenDecision, Modifier.weight(1f)) { Text("OPÇÕES") }
                }
                if (routeOptions.firstOrNull { it.id == route.id }?.testOnly == true) {
                    HorizontalDivider()
                    Text("QA · percurso de teste", color = V2Muted, fontWeight = FontWeight.ExtraBold)
                    Text(
                        "Controlos apenas para validar GPS simulado. Não aparecem no percurso de produção.",
                        color = V2Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onClick = onQaAdvance, Modifier.weight(1f)) { Text("AVANÇAR GPS") }
                        OutlinedButton(onClick = { onQaToggleGps(false) }, Modifier.weight(1f)) { Text("PERDER GPS") }
                        OutlinedButton(onClick = { onQaToggleGps(true) }, Modifier.weight(1f)) { Text("RECUPERAR GPS") }
                    }
                    OutlinedButton(onClick = onQaDeviation, Modifier.fillMaxWidth()) { Text("SIMULAR DESVIO") }
                }
            }
        }
    }
}

@Composable
private fun OfflineCartographicRouteMap(
    modifier: Modifier,
    geometry: List<GeoPoint>,
    projectedPoint: GeoPoint?,
    gpsState: GpsState
) {
    val points = geometry.filter { it.latitude.isFinite() && it.longitude.isFinite() }
    Card(modifier, RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = V2Map), elevation = CardDefaults.cardElevation(2.dp)) {
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))) {
            Canvas(Modifier.fillMaxSize()) {
                if (points.size >= 2) drawOfflineCartography(points, projectedPoint)
            }
            Card(Modifier.align(Alignment.TopStart).padding(12.dp), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .96f))) {
                Column(Modifier.padding(12.dp)) {
                    Text("A MINHA POSIÇÃO", color = V2Forest, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium)
                    Text(gpsLabel(gpsState), color = gpsColor(gpsState), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                }
            }
            Card(Modifier.align(Alignment.BottomStart).padding(12.dp), RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .96f))) {
                Text("MAPA OFFLINE · PERCURSO OFICIAL", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = V2Forest, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun DrawScope.drawOfflineCartography(route: List<GeoPoint>, projected: GeoPoint?) {
    val minLat = route.minOf { it.latitude }; val maxLat = route.maxOf { it.latitude }
    val minLon = route.minOf { it.longitude }; val maxLon = route.maxOf { it.longitude }
    val latSpan = max(0.01, maxLat - minLat); val lonSpan = max(0.01, maxLon - minLon)
    val midLat = (minLat + maxLat) / 2.0
    val lonScale = cos(midLat * Math.PI / 180.0)
    val projectedLonSpan = lonSpan * lonScale
    val scale = minOf((size.width * .86f) / projectedLonSpan.toFloat(), (size.height * .84f) / latSpan.toFloat())
    val cx = size.width / 2f; val cy = size.height / 2f
    fun p(g: GeoPoint): Offset {
        val x = cx + (((g.longitude - (minLon + maxLon) / 2.0) * lonScale).toFloat() * scale)
        val y = cy - ((g.latitude - midLat).toFloat() * scale)
        return Offset(x, y)
    }

    drawRect(V2Map)
    for (i in 1..8) {
        val x = size.width * i / 9f; drawLine(V2Road.copy(alpha = .20f), Offset(x, 0f), Offset(x, size.height), 1f)
        val y = size.height * i / 9f; drawLine(V2Road.copy(alpha = .20f), Offset(0f, y), Offset(size.width, y), 1f)
    }

    fun corridor(coords: List<GeoPoint>, major: Boolean = false) {
        val path = Path().apply { coords.forEachIndexed { i, g -> val q = p(g); if (i == 0) moveTo(q.x, q.y) else lineTo(q.x, q.y) } }
        drawPath(path, if (major) V2MajorRoad else V2Road, style = Stroke(width = if (major) 5f else 3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
    corridor(listOf(GeoPoint(41.16,-8.63), GeoPoint(40.95,-8.63), GeoPoint(40.64,-8.65), GeoPoint(40.38,-8.73), GeoPoint(39.74,-8.81)), true)
    corridor(listOf(GeoPoint(41.15,-8.61), GeoPoint(41.00,-8.55), GeoPoint(40.75,-8.55), GeoPoint(40.55,-8.45), GeoPoint(40.20,-8.40)))
    corridor(listOf(GeoPoint(40.65,-8.65), GeoPoint(40.45,-8.62), GeoPoint(40.20,-8.61), GeoPoint(39.92,-8.80)))

    val routePath = Path().apply { route.forEachIndexed { i, g -> val q = p(g); if (i == 0) moveTo(q.x, q.y) else lineTo(q.x, q.y) } }
    drawPath(routePath, Color.White, style = Stroke(width = 13f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(routePath, V2Forest, style = Stroke(width = 7f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    fun marker(g: GeoPoint, color: Color, radius: Float = 8f) { val q = p(g); drawCircle(Color.White, radius + 4f, q); drawCircle(color, radius, q) }
    marker(route.first(), V2Forest); marker(route.last(), Color(0xFFC28A16))
    projected?.takeIf { it.latitude.isFinite() && it.longitude.isFinite() }?.let { marker(it, V2Forest, 10f) }

    val cities = listOf(
        GeoPoint(41.1496,-8.6109), GeoPoint(40.6405,-8.6538), GeoPoint(40.2033,-8.4103),
        GeoPoint(39.7436,-8.8071), GeoPoint(39.6297,-8.6736)
    )
    cities.forEach { g -> drawCircle(Color(0xFF4D5A55), 4f, p(g)) }
}

@Composable private fun MetricCard(value: String, label: String, modifier: Modifier) {
    Card(modifier, RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = V2ForestSoft)) {
        Column(Modifier.padding(12.dp)) { Text(value, color = V2Forest, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge); Text(label, color = V2Muted, style = MaterialTheme.typography.bodySmall) }
    }
}

private fun gpsLabel(state: GpsState) = when (state) {
    GpsState.NO_SIGNAL -> "GPS sem sinal"
    GpsState.ACQUIRING -> "A obter sinal GPS"
    GpsState.ON_ROUTE -> "GPS no percurso"
    GpsState.POSSIBLE_DEVIATION -> "Possível desvio"
    GpsState.PROBABLE_DEVIATION -> "Provável desvio"
}

private fun gpsColor(state: GpsState) = when (state) {
    GpsState.ON_ROUTE -> V2Forest
    GpsState.NO_SIGNAL, GpsState.POSSIBLE_DEVIATION, GpsState.PROBABLE_DEVIATION -> Color(0xFF9A5A00)
    GpsState.ACQUIRING -> V2Muted
}

private fun positionDetail(state: WalkingState): String {
    val routeKm = state.routePosition?.routeKm?.let(::fmt) ?: "—"
    val routeDistance = state.routePosition?.distanceToRouteMeters?.let(::fmtMeters) ?: "—"
    val confidence = state.routePosition?.confidence?.name?.lowercase(Locale("pt", "PT")) ?: "desconhecida"
    return "Km no percurso: $routeKm · distância ao traçado: $routeDistance · confiança: $confidence"
}

private fun elapsedWalkingTime(startedAt: Instant?, nowMillis: Long): String {
    if (startedAt == null) return "—"
    val seconds = Duration.between(startedAt, Instant.ofEpochMilli(nowMillis)).seconds.coerceAtLeast(0L)
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) String.format(Locale("pt", "PT"), "%dh %02dm", hours, minutes)
    else String.format(Locale("pt", "PT"), "%02dm", minutes)
}

private fun fmt(value: Double) = String.format(Locale("pt", "PT"), "%.2f", value)
private fun fmtDistance(value: Double) = if (value < 1.0) String.format(Locale("pt", "PT"), "%.0f m", value * 1000.0) else String.format(Locale("pt", "PT"), "%.1f km", value)
private fun fmtMeters(value: Double) = if (value >= 1000.0) String.format(Locale("pt", "PT"), "%.1f km", value / 1000.0) else String.format(Locale("pt", "PT"), "%.0f m", value)
