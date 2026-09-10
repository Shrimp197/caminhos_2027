package com.caminhos2027.v1.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.data.AndroidRouteOption
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.walking.WalkingState
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

private val V2Forest = Color(0xFF0E6546)
private val V2ForestSoft = Color(0xFFE6F2EB)
private val V2Sand = Color(0xFFF6F3EC)
private val V2Map = Color(0xFFE7F0E5)
private val V2Grid = Color(0xFFC9D8C7)
private val V2Muted = Color(0xFF68736D)
private val V2Warning = Color(0xFF9A5A00)
private val V2WarningSoft = Color(0xFFFFF1D9)

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
    val progress = state.progress?.progressRatio?.coerceIn(0.0, 1.0) ?: 0.0
    val destinationKm = state.walk.plannedDestinationKm ?: 0.0
    val projectedPoint = state.routePosition?.projectedPoint
    val isTest = routeOptions.firstOrNull { it.id == route.id }?.testOnly == true

    Column(Modifier.fillMaxSize().background(V2Sand)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(route.officialName, color = V2Forest, fontWeight = FontWeight.ExtraBold)
                Text(gpsLabel(state.gpsState), color = gpsColor(state.gpsState), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = onStop) { Text("PARAR") }
        }

        MapCard(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp),
            geometry = route.geometry.points,
            projectedPoint = projectedPoint,
            routeKm = currentKm,
            gpsState = state.gpsState
        )

        Card(
            Modifier.fillMaxWidth().padding(12.dp),
            RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("${fmt(currentKm)} km", "Percorridos", Modifier.weight(1f))
                    MetricCard("${fmt(remainingKm)} km", "Para o fim", Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Progresso", color = V2Muted, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Text("${(progress * 100).toInt()}%", color = V2Forest, fontWeight = FontWeight.ExtraBold)
                }
                LinearProgressIndicator(
                    progress = progress.toFloat(),
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(10.dp)),
                    color = V2Forest,
                    trackColor = V2ForestSoft
                )
                Text("Destino planeado · ${fmt(destinationKm)} km", color = V2Muted, style = MaterialTheme.typography.bodySmall)
                HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = V2Forest)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("A minha posição", color = V2Forest, fontWeight = FontWeight.ExtraBold)
                        Text(positionDetail(state), color = V2Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
                state.nextApoi?.let { apoi ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Place, contentDescription = null, tint = V2Forest)
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
                if (isTest) {
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("QA · controlo do percurso", color = V2Warning, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                        if (state.gpsState != GpsState.ON_ROUTE) Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = V2Warning)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onQaAdvance, Modifier.weight(1f)) { Text("AVANÇAR") }
                        OutlinedButton({ onQaToggleGps(false) }, Modifier.weight(1f)) { Text("PERDER GPS") }
                        OutlinedButton({ onQaToggleGps(true) }, Modifier.weight(1f)) { Text("RECUPERAR") }
                    }
                    OutlinedButton(onQaDeviation, Modifier.fillMaxWidth()) { Text("SIMULAR DESVIO") }
                    Card(Modifier.fillMaxWidth(), RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = V2WarningSoft)) {
                        Text("Ambiente de teste: estes controlos permitem validar GPS, desvio e progressão sem depender do movimento físico.", Modifier.padding(10.dp), color = V2Warning, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun MapCard(
    modifier: Modifier,
    geometry: List<GeoPoint>,
    projectedPoint: GeoPoint?,
    routeKm: Double,
    gpsState: GpsState
) {
    val points = remember(geometry) { geometry.filter { it.latitude.isFinite() && it.longitude.isFinite() } }
    Card(modifier, RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = V2Map), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))) {
                repeat(7) { i ->
                    drawLine(V2Grid, Offset(0f, size.height * (i + 1) / 8f), Offset(size.width, size.height * (i + 1) / 8f), 1f)
                }
                repeat(5) { i ->
                    drawLine(V2Grid, Offset(size.width * (i + 1) / 6f, 0f), Offset(size.width * (i + 1) / 6f, size.height), 1f)
                }
                if (points.size < 2) return@Canvas
                val minLat = points.minOf { it.latitude }
                val maxLat = points.maxOf { it.latitude }
                val minLon = points.minOf { it.longitude }
                val maxLon = points.maxOf { it.longitude }
                val latSpan = max(maxLat - minLat, 1e-9)
                val lonSpan = max(maxLon - minLon, 1e-9)
                val pad = 34f
                val scale = min((size.width - 2 * pad) / lonSpan.toFloat(), (size.height - 2 * pad) / latSpan.toFloat())
                val ox = (size.width - lonSpan.toFloat() * scale) / 2f
                val oy = (size.height - latSpan.toFloat() * scale) / 2f
                fun project(g: GeoPoint) = Offset(ox + (g.longitude - minLon).toFloat() * scale, oy + (maxLat - g.latitude).toFloat() * scale)
                val path = Path().apply {
                    moveTo(project(points.first()).x, project(points.first()).y)
                    points.drop(1).forEach { p -> project(p).also { lineTo(it.x, it.y) } }
                }
                drawPath(path, Color.White, style = Stroke(width = 13f, cap = StrokeCap.Round))
                drawPath(path, V2Forest, style = Stroke(width = 7f, cap = StrokeCap.Round))
                projectedPoint?.let { point ->
                    val marker = project(point)
                    drawCircle(V2ForestSoft, 20f, marker)
                    drawCircle(V2Forest, 11f, marker)
                    if (gpsState == GpsState.NO_SIGNAL) drawCircle(V2WarningSoft, 29f, marker)
                }
            }
            Card(Modifier.align(Alignment.TopStart).padding(12.dp), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .95f))) {
                Column(Modifier.padding(12.dp)) {
                    Text("A MINHA POSIÇÃO", color = V2Forest, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium)
                    Text("${fmt(routeKm)} km", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text(gpsLabel(gpsState), color = gpsColor(gpsState), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                }
            }
            Card(Modifier.align(Alignment.BottomStart).padding(12.dp).border(1.dp, V2ForestSoft, RoundedCornerShape(14.dp)), RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .95f))) {
                Text("Percurso oficial", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = V2Forest, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun MetricCard(value: String, label: String, modifier: Modifier) {
    Card(modifier, RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = V2ForestSoft)) {
        Column(Modifier.padding(12.dp)) {
            Text(value, color = V2Forest, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
            Text(label, color = V2Muted, style = MaterialTheme.typography.bodySmall)
        }
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
    GpsState.NO_SIGNAL, GpsState.POSSIBLE_DEVIATION, GpsState.PROBABLE_DEVIATION -> V2Warning
    GpsState.ACQUIRING -> V2Muted
}

private fun positionDetail(state: WalkingState): String {
    val routeKm = state.routePosition?.routeKm?.let(::fmt) ?: "—"
    val routeDistance = state.routePosition?.distanceToRouteMeters?.let(::fmtMeters) ?: "—"
    val confidence = state.routePosition?.confidence?.name?.lowercase(Locale("pt", "PT")) ?: "desconhecida"
    return "Km no percurso: $routeKm · distância ao traçado: $routeDistance · confiança: $confidence"
}

private fun fmt(value: Double) = String.format(Locale("pt", "PT"), "%.2f", value)
private fun fmtDistance(value: Double) = if (value < 1.0) String.format(Locale("pt", "PT"), "%.0f m", value * 1000.0) else String.format(Locale("pt", "PT"), "%.1f km", value)
private fun fmtMeters(value: Double) = if (value >= 1000.0) String.format(Locale("pt", "PT"), "%.1f km", value / 1000.0) else String.format(Locale("pt", "PT"), "%.0f m", value)
