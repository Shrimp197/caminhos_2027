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
import java.util.Locale

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

    Column(Modifier.fillMaxSize().background(ActiveBg)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.LocationOn, null, tint = ActiveGreen, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(route.officialName, color = ActiveBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(gpsLabel(state.gpsState), color = gpsColor(state.gpsState), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(onClick = onOpenSos, modifier = Modifier.height(40.dp)) {
                Icon(Icons.Filled.ReportProblem, null, modifier = Modifier.size(18.dp))
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

            Row(
                Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActiveMetric("${fmtKm(currentKm)} km", "Percorridos", Modifier.weight(1f))
                ActiveMetric("${fmtKm(remainingKm)} km", "Para o fim", Modifier.weight(1f))
            }

            if (isOffRoute && projectedPoint != null) {
                Card(
                    Modifier.align(Alignment.TopCenter).padding(top = 94.dp, start = 12.dp, end = 12.dp),
                    RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ActiveWarningBg),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(
                            if (state.gpsState == GpsState.PROBABLE_DEVIATION) "Está afastado do Caminho" else "Possível desvio do Caminho",
                            color = ActiveWarning,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "O progresso mantém-se no último ponto válido. Vamos orientá-lo de volta ao Caminho.",
                            color = ActiveWarning,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(
                            onClick = {
                                onNavigateToCoordinate(
                                    projectedPoint.latitude,
                                    projectedPoint.longitude,
                                    "Último ponto conhecido no Caminho"
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("REGRESSAR AO ÚLTIMO PONTO")
                        }
                    }
                }
            }
        }

        Card(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp).padding(top = 6.dp),
            RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                Modifier
                    .height(330.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Caminhada", color = ActiveBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (state.isPaused) "CAMINHADA PAUSADA" else "MAPA · CARTOGRAFIA REAL",
                            color = if (state.isPaused) ActiveWarning else ActiveMuted,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Text("${(progress * 100).toInt()}%", color = ActiveGreen, fontWeight = FontWeight.ExtraBold)
                }

                LinearProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(7.dp),
                    color = ActiveGreen,
                    trackColor = Color(0xFFE2E7E3)
                )

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActiveMetric("${fmtKm(currentKm)} km", "Percorridos", Modifier.weight(1f))
                    ActiveMetric("${fmtKm(remainingKm)} km", "Para o fim", Modifier.weight(1f))
                }

                Text("Progresso · ${(progress * 100).toInt()}%", color = ActiveMuted, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Text("Km no percurso: ${fmtKm(currentKm)} km", color = ActiveMuted, style = MaterialTheme.typography.bodySmall)
                Text("Tempo · caminhada em curso", color = ActiveMuted, style = MaterialTheme.typography.bodySmall)

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, null, tint = ActiveBlue, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Próximos apoios", color = ActiveBlue, fontWeight = FontWeight.ExtraBold)
                        if (state.nextApoi != null) {
                            Text(
                                state.nextApoi.name + (state.nextApoiDistanceKm?.let { " · ${fmtDistance(it)}" } ?: ""),
                                color = ActiveMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text("Não existem apoios publicados neste contexto.", color = ActiveMuted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    OutlinedButton(onClick = onOpenNext10Km, modifier = Modifier.height(36.dp)) {
                        Text("Ver todos", fontWeight = FontWeight.Bold)
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onTogglePause, modifier = Modifier.weight(1f)) {
                        Icon(if (state.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(if (state.isPaused) "RETOMAR CAMINHADA" else "PAUSAR CAMINHADA")
                    }
                    Button(onClick = onOpenApoi, modifier = Modifier.weight(1f)) {
                        Text("VER APOIOS", fontWeight = FontWeight.ExtraBold)
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onOpenDecision, modifier = Modifier.weight(1f)) { Text("OPÇÕES", fontWeight = FontWeight.Bold) }
                    OutlinedButton(onClick = onStop, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Stop, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Terminar")
                    }
                }

                if (BuildConfig.DEBUG && AndroidRouteCatalog.isTestRoute(route.id)) {
                    Card(
                        Modifier.fillMaxWidth(),
                        RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8F6))
                    ) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("QA · percurso de teste", color = ActiveMuted, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                OutlinedButton(onClick = onQaAdvance, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)) { Text("AVANÇAR GPS") }
                                OutlinedButton(onClick = { onQaToggleGps(false) }, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)) { Text("PERDER GPS") }
                                OutlinedButton(onClick = { onQaToggleGps(true) }, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)) { Text("RECUPERAR GPS") }
                            }
                            OutlinedButton(onClick = onQaDeviation, modifier = Modifier.fillMaxWidth()) { Text("SIMULAR DESVIO") }
                        }
                    }
                }
            }
        }

        BottomNavBarV1(WalkingSurface.ACTIVE, onNavigate)

        if (BuildConfig.DEBUG && AndroidRouteCatalog.isTestRoute(route.id)) {
            Column(
                Modifier.size(1.dp).alpha(0f).semantics {
                    contentDescription = "CONTROLOS QA"
                }
            ) {
                Button(onClick = onQaAdvance) { Text("AVANÇAR GPS") }
                Button(onClick = { onQaToggleGps(false) }) { Text("PERDER GPS") }
                Button(onClick = { onQaToggleGps(true) }) { Text("RECUPERAR GPS") }
                Button(onClick = onQaDeviation) { Text("SIMULAR DESVIO") }
            }
        }
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

private fun fmtKm(value: Double): String = String.format(Locale("pt", "PT"), "%.1f", value.coerceAtLeast(0.0))
private fun fmtDistance(value: Double): String = if (value < 1.0) {
    String.format(Locale("pt", "PT"), "%.0f m", value * 1000.0)
} else {
    String.format(Locale("pt", "PT"), "%.1f km", value)
}
