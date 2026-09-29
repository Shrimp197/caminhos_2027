package com.caminhos2027.v1.ui

import com.caminhos2027.BuildConfig
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.data.AndroidRouteCatalog
import com.caminhos2027.v1.core.data.AndroidRouteOption
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.walking.WalkingState
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlinx.coroutines.delay

private val ActiveBg = Color(0xFFF7F8F6)
private val ActiveGreen = Color(0xFF159447)
private val ActiveBlue = Color(0xFF164B63)
private val ActiveMuted = Color(0xFF687278)
private val ActiveWarning = Color(0xFF9A5A00)
private val ActiveWarningBg = Color(0xFFFFF1D9)

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
    onNavigateToCoordinate: (Double, Double, String) -> Unit,
    onQaAdvance: () -> Unit,
    onQaToggleGps: (Boolean) -> Unit,
    onQaDeviation: () -> Unit
) {
    val currentKm = state.routePosition?.routeKm ?: state.progress?.currentRouteKm ?: 0.0
    val remainingKm = state.progress?.remainingKm ?: 0.0
    val progress = (state.progress?.progressRatio ?: 0.0).coerceIn(0.0, 1.0)
    val projectedPoint = state.routePosition?.projectedPoint
    val isOffRoute = state.gpsState == GpsState.POSSIBLE_DEVIATION || state.gpsState == GpsState.PROBABLE_DEVIATION
    var clock by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(state.walk.id, state.walk.startedAt) {
        while (true) {
            clock = Instant.now()
            delay(1_000L)
        }
    }
    val elapsedLabel = state.walk.startedAt?.let { elapsedWalkingLabel(it, clock) } ?: "00m"

    Column(Modifier.fillMaxSize().background(Color(0xFFF7F7F4))) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocationOn, null, tint = ActiveGreen, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(route.officialName, color = ActiveBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(gpsLabel(state.gpsState), color = gpsColor(state.gpsState), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(onClick = onOpenSos, modifier = Modifier.height(38.dp), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Filled.ReportProblem, null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(4.dp))
                Text("SOS", fontWeight = FontWeight.ExtraBold)
            }
        }

        Box(Modifier.fillMaxWidth().weight(1f)) {
            RealWalkingMap(
                modifier = Modifier.fillMaxSize(),
                routeId = route.id,
                geometry = route.geometry.points,
                projectedPoint = projectedPoint,
                currentKm = currentKm,
                totalKm = route.totalDistanceKm,
                gpsState = state.gpsState,
                mapOrientation = state.walk.preparation.mapOrientation,
                nextApoi = state.nextApoi
            )

            Row(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActiveMetric("${fmtKm(currentKm)} km", "Percorridos", Modifier.weight(1f))
                ActiveMetric("${fmtKm(remainingKm)} km", "Para o fim", Modifier.weight(1f))
            }

            if (isOffRoute && projectedPoint != null) {
                Card(Modifier.align(Alignment.TopCenter).padding(top = 88.dp, start = 12.dp, end = 12.dp), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ActiveWarningBg), elevation = CardDefaults.cardElevation(3.dp)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(if (state.gpsState == GpsState.PROBABLE_DEVIATION) "Está afastado do Caminho" else "Possível desvio do Caminho", color = ActiveWarning, fontWeight = FontWeight.ExtraBold)
                        Text("O progresso mantém-se no último ponto válido. A orientação leva-o de volta ao último ponto conhecido.", color = ActiveWarning, style = MaterialTheme.typography.bodySmall)
                        Button(onClick = { onNavigateToCoordinate(projectedPoint.latitude, projectedPoint.longitude, "Último ponto conhecido no Caminho") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(11.dp)) {
                            Icon(Icons.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("REGRESSAR AO CAMINHO")
                        }
                    }
                }
            }
        }

        Card(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 18.dp, bottomEnd = 18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(5.dp)) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Box(Modifier.align(Alignment.CenterHorizontally).width(42.dp).height(4.dp).background(Color(0xFFD0D4D1), RoundedCornerShape(4.dp)))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Caminhada", color = ActiveBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                        Text(if (state.isPaused) "CAMINHADA PAUSADA" else "MAPA · CARTOGRAFIA REAL", color = if (state.isPaused) ActiveWarning else ActiveGreen, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelSmall)
                        Text("Progresso · ${(progress * 100).toInt()}%", color = ActiveMuted, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        Text("Tempo · \$elapsedLabel", color = ActiveMuted, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                    Text("${(progress * 100).toInt()}%", color = ActiveGreen, fontWeight = FontWeight.ExtraBold)
                }
                LinearProgressIndicator(progress = { progress.toFloat() }, modifier = Modifier.fillMaxWidth().height(7.dp), color = ActiveGreen, trackColor = Color(0xFFE2E7E3))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActiveMetric("${fmtKm(currentKm)} km", "Percorridos", Modifier.weight(1f))
                    ActiveMetric("${fmtKm(remainingKm)} km", "Para o fim", Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, null, tint = ActiveBlue, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(7.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Próximo apoio", color = ActiveBlue, fontWeight = FontWeight.ExtraBold)
                        Text(state.nextApoi?.name ?: "Sem apoio publicado neste contexto", color = ActiveMuted, style = MaterialTheme.typography.bodySmall)
                    }
                    state.nextApoiDistanceKm?.let { Text("${fmtDistance(it)}", color = ActiveBlue, fontWeight = FontWeight.ExtraBold) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onTogglePause, modifier = Modifier.weight(1f), shape = RoundedCornerShape(11.dp)) {
                        Icon(if (state.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(if (state.isPaused) "RETOMAR CAMINHADA" else "PAUSAR CAMINHADA", fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = onOpenNext10Km, modifier = Modifier.weight(1f), shape = RoundedCornerShape(11.dp)) { Text("PRÓXIMOS 10 KM", fontWeight = FontWeight.ExtraBold) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onOpenApoi, modifier = Modifier.weight(1f), shape = RoundedCornerShape(11.dp)) { Text("VER APOIOS", fontWeight = FontWeight.Bold) }
                    OutlinedButton(onClick = onOpenDecision, modifier = Modifier.weight(1f), shape = RoundedCornerShape(11.dp)) { Text("OPÇÕES", fontWeight = FontWeight.Bold) }
                }
                if (BuildConfig.DEBUG && AndroidRouteCatalog.isTestRoute(route.id)) {
                    Card(Modifier.fillMaxWidth(), RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F6F4))) {
                        Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            OutlinedButton(onClick = onQaAdvance, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)) { Text("GPS+") }
                            OutlinedButton(onClick = { onQaToggleGps(false) }, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)) { Text("SEM GPS") }
                            OutlinedButton(onClick = { onQaToggleGps(true) }, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)) { Text("GPS") }
                            OutlinedButton(onClick = onQaDeviation, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)) { Text("DESVIO") }
                        }
                    }
                }
            }
        }
        BottomNavBarV1(WalkingSurface.ACTIVE, onNavigate)
    }
}

@Composable
private fun ActiveMetric(value: String, label: String, modifier: Modifier) {
    Card(
        modifier,
        RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .97f)),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(horizontal = 13.dp, vertical = 9.dp)) {
            Text(value, color = Color(0xFF202020), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
            Text(label, color = ActiveMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun gpsLabel(state: GpsState): String = when (state) {
    GpsState.ACQUIRING -> "A obter sinal GPS"
    GpsState.ON_ROUTE -> "GPS no percurso"
    GpsState.NO_SIGNAL -> "Sem sinal GPS"
    GpsState.POSSIBLE_DEVIATION -> "Possível desvio"
    GpsState.PROBABLE_DEVIATION -> "Provável desvio"
}

private fun gpsColor(state: GpsState): Color = when (state) {
    GpsState.ON_ROUTE -> ActiveGreen
    GpsState.ACQUIRING -> ActiveMuted
    GpsState.NO_SIGNAL, GpsState.POSSIBLE_DEVIATION, GpsState.PROBABLE_DEVIATION -> ActiveWarning
}

private fun elapsedWalkingLabel(startedAt: Instant, now: Instant): String {
    val seconds = Duration.between(startedAt, now).seconds.coerceAtLeast(0L)
    val hours = seconds / 3600L
    val minutes = (seconds % 3600L) / 60L
    return if (hours > 0L) "${hours}h ${minutes.toString().padStart(2, '0')}m"
    else "${minutes.toString().padStart(2, '0')}m"
}

private fun fmtKm(value: Double): String = String.format(Locale("pt", "PT"), "%.1f", value.coerceAtLeast(0.0))
private fun fmtDistance(value: Double): String = if (value < 1.0) {
    String.format(Locale("pt", "PT"), "%.0f m", value * 1000.0)
} else {
    String.format(Locale("pt", "PT"), "%.1f km", value)
}
