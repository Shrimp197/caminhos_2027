package com.caminhos2027.v1.ui

import com.caminhos2027.BuildConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
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

private val WalkBlue = Color(0xFF164B63)
private val WalkGreen = Color(0xFF159447)
private val WalkBg = Color(0xFFF7F7F4)
private val WalkMuted = Color(0xFF68736D)
private val WalkWarning = Color(0xFF9A5A00)
private val WalkWarningBg = Color(0xFFFFF1D9)

@Composable
internal fun V1ActiveExperienceScreenV3(
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
    val isTestRoute = BuildConfig.DEBUG && AndroidRouteCatalog.isTestRoute(state.walk.routeId)
    val elapsedLabel = elapsedLabelV3(state.walk.startedAt)
    var expanded by rememberSaveable(state.walk.id) { mutableStateOf(true) }
    val sheetHeight = if (expanded) 380.dp else 220.dp

    Scaffold(containerColor = WalkBg, bottomBar = {
        BottomNavBarV1(WalkingSurface.ACTIVE, onNavigate)
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
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

            Card(
                Modifier.align(Alignment.TopStart).padding(10.dp),
                RoundedCornerShape(13.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text("MAPA · CARTOGRAFIA REAL", color = WalkBlue, fontWeight = FontWeight.ExtraBold)
                    Text("Tempo · $elapsedLabel", color = WalkMuted, fontWeight = FontWeight.SemiBold)
                }
            }

            Row(
                Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(top = 78.dp, start = 10.dp, end = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(formatKm(currentKm), "Percorridos", Modifier.weight(1f))
                MetricCard(formatKm(remainingKm), "Para o fim", Modifier.weight(1f))
            }

            Row(
                Modifier.align(Alignment.TopEnd).padding(top = 88.dp, end = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButtonCard(Icons.Filled.ReportProblem, "SOS", onOpenSos)
                IconButtonCard(Icons.Filled.Stop, "Parar", onStop)
            }

            if (isOffRoute && projectedPoint != null) {
                Card(
                    Modifier.align(Alignment.TopCenter).padding(top = 128.dp, start = 12.dp, end = 12.dp),
                    RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WalkWarningBg),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            if (state.gpsState == GpsState.PROBABLE_DEVIATION) "Está afastado do Caminho" else "Possível desvio do Caminho",
                            color = WalkWarning,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text("O progresso mantém-se no último ponto válido.", color = WalkWarning)
                        Button(
                            onClick = {
                                onNavigateToCoordinate(
                                    projectedPoint.latitude,
                                    projectedPoint.longitude,
                                    "Último ponto conhecido no Caminho"
                                )
                            },
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WalkGreen)
                        ) {
                            Icon(Icons.Filled.ArrowForward, null, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("REGRESSAR AO CAMINHO", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            Card(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp).height(sheetHeight),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Box(
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(52.dp)
                            .height(22.dp)
                            .clickable { expanded = !expanded }
                            .pointerInput(Unit) {
                                var totalDrag = 0f
                                detectVerticalDragGestures(
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        totalDrag += dragAmount
                                    },
                                    onDragEnd = {
                                        when {
                                            totalDrag < -60f -> expanded = true
                                            totalDrag > 60f -> expanded = false
                                        }
                                        totalDrag = 0f
                                    },
                                    onDragCancel = { totalDrag = 0f }
                                )
                            }
                            .semantics {
                                contentDescription = if (expanded) "Recolher painel de caminhada" else "Expandir painel de caminhada"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(Modifier.width(42.dp).height(4.dp).background(Color(0xFFD0D4D1), RoundedCornerShape(4.dp)))
                    }

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Caminhada", color = WalkBlue, fontWeight = FontWeight.ExtraBold)
                            Text(
                                if (state.isPaused) "CAMINHADA PAUSADA" else gpsLabelV3(state.gpsState),
                                color = if (state.isPaused) WalkWarning else WalkGreen,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${(progress * 100).toInt()}%", color = WalkGreen, fontWeight = FontWeight.ExtraBold)
                            Text("Progresso", color = WalkMuted)
                        }
                    }

                    LinearProgressIndicator(
                        progress = { progress.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(5.dp),
                        color = WalkGreen,
                        trackColor = Color(0xFFE2E7E3)
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricCard(formatKm(currentKm), "Percorridos", Modifier.weight(1f))
                        MetricCard(formatKm(remainingKm), "Para o fim", Modifier.weight(1f))
                    }
                    Text(
                        "Km no percurso: " + formatKm(currentKm),
                        color = WalkMuted,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        modifier = Modifier.semantics {
                            contentDescription = "Km no percurso: " + formatKm(currentKm)
                        }
                    )

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, null, tint = WalkBlue, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Próximo apoio", color = WalkBlue, fontWeight = FontWeight.ExtraBold)
                            Text(state.nextApoi?.name ?: "Sem apoio publicado neste contexto", color = WalkMuted, maxLines = 1)
                        }
                        state.nextApoiDistanceKm?.let { Text(formatKm(it), color = WalkBlue, fontWeight = FontWeight.ExtraBold) }
                    }

                        if (isTestRoute) {
                            Card(Modifier.fillMaxWidth(), RoundedCornerShape(11.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F6F4))) {
                                Column(Modifier.padding(7.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text("QA · percurso de teste", color = WalkWarning, fontWeight = FontWeight.Bold)
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        OutlinedButton(onClick = onQaAdvance, Modifier.weight(1f)) { Text("AVANÇAR GPS") }
                                        OutlinedButton(onClick = { onQaToggleGps(false) }, Modifier.weight(1f)) { Text("PERDER GPS") }
                                    }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        OutlinedButton(onClick = { onQaToggleGps(true) }, Modifier.weight(1f)) { Text("RECUPERAR GPS") }
                                        OutlinedButton(onClick = onQaDeviation, Modifier.weight(1f)) { Text("SIMULAR DESVIO") }
                                    }
                                }
                            }
                        }


                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onTogglePause, Modifier.weight(1f), shape = RoundedCornerShape(11.dp)) {
                            Icon(if (state.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (state.isPaused) "RETOMAR CAMINHADA" else "PAUSAR CAMINHADA", fontWeight = FontWeight.Bold)
                        }
                        Button(onClick = onOpenNext10Km, Modifier.weight(1f), shape = RoundedCornerShape(11.dp), colors = ButtonDefaults.buttonColors(containerColor = WalkGreen)) {
                            Text("PRÓXIMOS 10 KM", fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    if (expanded) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onOpenApoi, Modifier.weight(1f), shape = RoundedCornerShape(11.dp)) {
                                Text("VER APOIOS", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(onClick = onOpenDecision, Modifier.weight(1f), shape = RoundedCornerShape(11.dp)) {
                                Text("OPÇÕES", fontWeight = FontWeight.Bold)
                            }
                        }

                        Text("Tempo · $elapsedLabel", color = WalkMuted, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun NavItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) { NavigationBarItem(selected = selected, onClick = onClick, icon = { Icon(icon, label, modifier = Modifier.size(21.dp)) }, label = { Text(label, maxLines = 1) }) }

@Composable
private fun MetricCard(value: String, label: String, modifier: Modifier) { Card(modifier, RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) { Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) { Text(value, color = WalkBlue, fontWeight = FontWeight.ExtraBold); Text(label, color = WalkMuted) } } }

@Composable
private fun IconButtonCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) { Button(onClick = onClick, shape = RoundedCornerShape(13.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = WalkBlue), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp)) { Icon(icon, label, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text(label, fontWeight = FontWeight.Bold) } }

private fun formatKm(value: Double): String = String.format(Locale.US, "%.1f km", value.coerceAtLeast(0.0))

private fun elapsedLabelV3(startedAt: Instant?): String { if (startedAt == null) return "--:--"; val elapsed = Duration.between(startedAt, Instant.now()).coerceAtLeast(Duration.ZERO); val hours = elapsed.toHours(); val minutes = elapsed.toMinutesPart(); val seconds = elapsed.toSecondsPart(); return if (hours > 0) String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds) else String.format(Locale.US, "%02d:%02d", minutes, seconds) }

private fun gpsLabelV3(state: GpsState): String = when (state) { GpsState.ACQUIRING -> "A procurar uma posição GPS precisa"; GpsState.ON_ROUTE -> "GPS no percurso"; GpsState.NO_SIGNAL -> "GPS sem sinal · última posição mantida"; GpsState.POSSIBLE_DEVIATION -> "Possível desvio"; GpsState.PROBABLE_DEVIATION -> "Desvio provável" }
