package com.caminhos2027.v1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.caminhos2027.v1.core.data.AndroidRouteCatalog
import com.caminhos2027.v1.core.data.AndroidRouteOption
import com.caminhos2027.v1.core.model.AudioMode
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.MapOrientation
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.walking.WalkingState
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min

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
    onTogglePause: () -> Unit,
    onOpenApoi: () -> Unit,
    onOpenDecision: () -> Unit,
    onOpenNext10Km: () -> Unit,
    onOpenSummary: () -> Unit,
    onOpenDiary: () -> Unit,
    onOpenMore: () -> Unit,
    onOpenSos: () -> Unit,
    onOpenPilgrimMode: () -> Unit,
    onNavigate: (WalkingSurface) -> Unit,
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
    val elapsed = elapsedWalkingTime(
        startedAt = state.walk.startedAt,
        nowMillis = nowMillis,
        pausedAt = state.pausedAt,
        pausedDurationSeconds = state.pausedDurationSeconds
    )
    val progress = state.progress?.progressRatio?.coerceIn(0.0, 1.0) ?: 0.0
    val destinationKm = state.walk.plannedDestinationKm ?: 0.0
    val projectedPoint = state.routePosition?.projectedPoint

    ActiveWalkingAudioFeedback(state)

    Column(Modifier.fillMaxSize().background(Color(0xFFF7F5EF))) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(route.officialName, color = V2Forest, fontWeight = FontWeight.ExtraBold)
                Text(
                    if (state.isPaused) "GPS em pausa" else gpsLabel(state.gpsState),
                    color = if (state.isPaused) Color(0xFF7A4A00) else gpsColor(state.gpsState),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "MAPA · CARTOGRAFIA REAL",
                    color = V2Muted,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    "Tempo · " + elapsed,
                    color = V2Muted,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = onOpenSos) { Text("SOS") }
                OutlinedButton(onClick = onStop) { Text("PARAR") }
            }
        }

        Box(Modifier.fillMaxWidth().weight(1f)) {
            RealWalkingMap(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                geometry = route.geometry.points,
                projectedPoint = projectedPoint,
                currentKm = currentKm,
                totalKm = route.totalDistanceKm,
                gpsState = state.gpsState,
                mapOrientation = state.walk.preparation.mapOrientation,
                nextApoi = state.nextApoi
            )

            DraggableWalkingSheet(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
        Card(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .padding(12.dp),
            RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                pauseRecommendationText(state)?.let { recommendation ->
                    Card(
                        Modifier.fillMaxWidth(),
                        RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = V2ForestSoft)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("PAUSA INTELIGENTE", color = V2Forest, fontWeight = FontWeight.ExtraBold)
                            Text(recommendation, color = V2Forest, fontWeight = FontWeight.SemiBold)
                            Text("Pode parar agora e retomar quando quiser.", color = V2Muted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
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
                if (state.isPaused) {
                    Card(
                        Modifier.fillMaxWidth(),
                        RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1D9))
                    ) {
                        Text(
                            "CAMINHADA PAUSADA · a posição ficou guardada e o GPS não está a ser aceite.",
                            Modifier.padding(12.dp),
                            color = Color(0xFF7A4A00),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onTogglePause, Modifier.weight(1f)) {
                        Text(if (state.isPaused) "RETOMAR CAMINHADA" else "PAUSAR CAMINHADA")
                    }
                    OutlinedButton(onClick = onOpenApoi, Modifier.weight(1f)) { Text("VER APOIOS") }
                    OutlinedButton(onClick = onOpenNext10Km, Modifier.weight(1f)) { Text("PRÓXIMOS 10 KM") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onOpenDecision, Modifier.weight(1f)) { Text("OPÇÕES") }
                    OutlinedButton(onClick = onOpenPilgrimMode, Modifier.weight(1f)) { Text("MODO PEREGRINO") }
                }
                if (AndroidRouteCatalog.isTestRoute(state.walk.routeId)) {
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
        BottomNavBarV1(WalkingSurface.ACTIVE, onNavigate)
    }
}

@Composable
private fun ActiveWalkingAudioFeedback(state: WalkingState) {
    val mode = state.walk.preparation.audioMode
    if (mode == AudioMode.SILENT) return

    val context = LocalContext.current
    val feedback = remember(context) { AndroidWalkingAudioFeedback(context) }
    var previousGps by remember(state.walk.id) { mutableStateOf<GpsState?>(null) }
    var previousPaused by remember(state.walk.id) { mutableStateOf(state.isPaused) }
    var previousApoiId by remember(state.walk.id) { mutableStateOf(state.nextApoi?.id) }
    var previousPauseRecommended by remember(state.walk.id) { mutableStateOf(false) }

    DisposableEffect(feedback) {
        onDispose { feedback.release() }
    }

    val pauseRecommended = pauseRecommendationText(state) != null
    LaunchedEffect(state.walk.id, state.gpsState, state.isPaused, state.nextApoi?.id, pauseRecommended) {
        val message = when {
            !previousPaused && state.isPaused -> "Caminhada pausada."
            previousPaused && !state.isPaused -> "Caminhada retomada."
            previousGps == GpsState.ON_ROUTE && state.gpsState == GpsState.NO_SIGNAL ->
                "Sinal GPS perdido. A última posição foi mantida."
            previousGps == GpsState.NO_SIGNAL && state.gpsState == GpsState.ON_ROUTE ->
                "Sinal GPS recuperado. Está no percurso."
            state.gpsState == GpsState.POSSIBLE_DEVIATION && previousGps != GpsState.POSSIBLE_DEVIATION ->
                "Possível desvio do percurso."
            state.gpsState == GpsState.PROBABLE_DEVIATION && previousGps != GpsState.PROBABLE_DEVIATION ->
                "Provável desvio do percurso."
            !previousPauseRecommended && pauseRecommended ->
                "Pausa recomendada. Pode parar agora e retomar quando quiser."
            mode == AudioMode.IMMERSIVE && previousApoiId != null && previousApoiId != state.nextApoi?.id && state.nextApoi != null ->
                "Próximo APOI: " + state.nextApoi.name + ". " +
                    (state.nextApoiDistanceKm?.let(::fmtDistance) ?: "distância não disponível") + "."
            else -> null
        }
        message?.let { feedback.speak(it, mode) }
        previousGps = state.gpsState
        previousPaused = state.isPaused
        previousApoiId = state.nextApoi?.id
        previousPauseRecommended = pauseRecommended
    }
}


@Composable
private fun DraggableWalkingSheet(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val minHeight = with(density) { 190.dp.toPx() }
    val maxHeight = with(density) { 620.dp.toPx() }
    val initialHeight = with(density) { 260.dp.toPx() }
    var heightPx by androidx.compose.runtime.remember { mutableFloatStateOf(initialHeight) }

    Box(
        modifier
            .height(with(density) { heightPx.toDp() })
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
            .shadow(6.dp, RoundedCornerShape(26.dp))
            .draggable(
                state = rememberDraggableState { delta ->
                    heightPx = (heightPx - delta).coerceIn(minHeight, maxHeight)
                },
                orientation = Orientation.Vertical,
                onDragStopped = {
                    heightPx = if (heightPx < (minHeight + maxHeight) / 2f) minHeight else maxHeight
                }
            )
    ) {
        Column(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp, bottom = 3.dp)
                    .size(width = 44.dp, height = 5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFB9C1BC))
            )
            content()
        }
    }
}

private fun pointAtRouteKm(points: List<GeoPoint>, routeKm: Double, totalKm: Double): GeoPoint? {
    if (points.isEmpty()) return null
    if (points.size == 1) return points.first()
    val target = routeKm.coerceIn(0.0, totalKm.coerceAtLeast(0.0))
    if (target <= 0.0) return points.first()
    var accumulated = 0.0
    for (index in 1 until points.size) {
        val a = points[index - 1]
        val b = points[index]
        val segment = geoDistanceKmV2(a, b)
        if (accumulated + segment >= target) {
            val fraction = if (segment <= 0.0) 0.0 else ((target - accumulated) / segment).coerceIn(0.0, 1.0)
            return GeoPoint(
                latitude = a.latitude + (b.latitude - a.latitude) * fraction,
                longitude = a.longitude + (b.longitude - a.longitude) * fraction
            )
        }
        accumulated += segment
    }
    return points.last()
}

private fun geoDistanceKmV2(a: GeoPoint, b: GeoPoint): Double {
    val earthRadiusKm = 6371.0088
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)
    val dLat = lat2 - lat1
    val dLon = Math.toRadians(b.longitude - a.longitude)
    val sinLat = kotlin.math.sin(dLat / 2.0)
    val sinLon = kotlin.math.sin(dLon / 2.0)
    val h = sinLat * sinLat + kotlin.math.cos(lat1) * kotlin.math.cos(lat2) * sinLon * sinLon
    return 2.0 * earthRadiusKm * kotlin.math.asin(kotlin.math.sqrt(h.coerceIn(0.0, 1.0)))
}

private fun routeBearingDegrees(route: List<GeoPoint>, projected: GeoPoint?): Float {
    if (route.size < 2 || projected == null) return 0f
    val index = route.indices.minByOrNull { index ->
        val point = route[index]
        val dLat = point.latitude - projected.latitude
        val dLon = point.longitude - projected.longitude
        dLat * dLat + dLon * dLon
    } ?: return 0f
    val from = if (index < route.lastIndex) route[index] else route[index - 1]
    val to = if (index < route.lastIndex) route[index + 1] else route[index]
    val meanLat = (from.latitude + to.latitude) / 2.0
    val east = (to.longitude - from.longitude) * cos(meanLat * Math.PI / 180.0)
    val north = to.latitude - from.latitude
    if (east == 0.0 && north == 0.0) return 0f
    return Math.toDegrees(kotlin.math.atan2(east, north)).toFloat()
}

@Composable private fun MetricCard(value: String, label: String, modifier: Modifier) {
    Card(
        modifier.semantics { contentDescription = label },
        RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = V2ForestSoft)
    ) {
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

private fun pauseRecommendationText(state: WalkingState): String? {
    if (state.isPaused) return null
    state.pauseRecommendation?.let { return it }
    val startedAt = state.walk.startedAt ?: return null
    val now = Instant.now()
    val currentPauseSeconds = state.pausedAt?.let { Duration.between(it, now).seconds.coerceAtLeast(0L) } ?: 0L
    val activeSeconds = (
        Duration.between(startedAt, now).seconds -
            state.pausedDurationSeconds.coerceAtLeast(0L) -
            currentPauseSeconds
        ).coerceAtLeast(0L)
    val minutes = activeSeconds / 60L
    val threshold = state.walk.preparation.customBreakTimeMinutes?.takeIf { it > 0 } ?: 60
    return if (state.walk.preparation.intelligentBreaksEnabled && minutes >= threshold) {
        "Já passaram $minutes min de caminhada."
    } else null
}
private fun elapsedWalkingTime(
    startedAt: Instant?,
    nowMillis: Long,
    pausedAt: Instant?,
    pausedDurationSeconds: Long
): String {
    if (startedAt == null) return "—"
    val now = Instant.ofEpochMilli(nowMillis)
    val currentPauseSeconds = pausedAt?.let { Duration.between(it, now).seconds.coerceAtLeast(0L) } ?: 0L
    val seconds = (
        Duration.between(startedAt, now).seconds -
            pausedDurationSeconds.coerceAtLeast(0L) -
            currentPauseSeconds
        ).coerceAtLeast(0L)
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) String.format(Locale("pt", "PT"), "%dh %02dm", hours, minutes)
    else String.format(Locale("pt", "PT"), "%02dm", minutes)
}

private fun fmt(value: Double) = String.format(Locale("pt", "PT"), "%.2f", value)
private fun fmtDistance(value: Double) = if (value < 1.0) String.format(Locale("pt", "PT"), "%.0f m", value * 1000.0) else String.format(Locale("pt", "PT"), "%.1f km", value)
private fun fmtMeters(value: Double) = if (value >= 1000.0) String.format(Locale("pt", "PT"), "%.1f km", value / 1000.0) else String.format(Locale("pt", "PT"), "%.0f m", value)
