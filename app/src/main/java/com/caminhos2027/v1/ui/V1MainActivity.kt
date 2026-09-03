package com.caminhos2027.v1.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.map.WalkingMapModel
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.route.WalkingProgress
import com.caminhos2027.v1.core.walking.WalkingState
import com.caminhos2027.v1.gps.AndroidLocationSource
import java.util.Locale

private val Forest = Color(0xFF165B43)
private val ForestSoft = Color(0xFFE8F2ED)
private val Sand = Color(0xFFF7F4EE)
private val Ink = Color(0xFF1E2521)
private val Muted = Color(0xFF68736D)
private val Warning = Color(0xFF8A6412)

class V1MainActivity : ComponentActivity() {
    private var locationSource: AndroidLocationSource? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            startRawLocationSource()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CaminhosTheme { WalkingScreenV1() } }
    }

    override fun onStart() {
        super.onStart()
        if (hasLocationPermission()) startRawLocationSource()
        else requestLocationPermission()
    }

    override fun onStop() {
        locationSource?.stop()
        super.onStop()
    }

    private fun hasLocationPermission(): Boolean =
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun startRawLocationSource() {
        if (locationSource == null) {
            locationSource = AndroidLocationSource(
                context = this,
                onPosition = { /* Raw GPS is connected only after a validated route is available. */ },
                onAvailabilityChanged = { /* Availability is represented by the walking state. */ }
            )
        }
        locationSource?.start()
    }
}

@Composable
private fun CaminhosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Forest,
            onPrimary = Color.White,
            background = Sand,
            surface = Color.White,
            onSurface = Ink,
            onBackground = Ink
        ),
        content = content
    )
}

/** Walking experience shell. Null means there is no active walk; no route data is fabricated. */
@Composable
private fun WalkingScreenV1(state: WalkingState? = null, map: WalkingMapModel? = null) {
    Surface(modifier = Modifier.fillMaxSize(), color = Sand) {
        if (state == null) NoActiveWalkScreen() else ActiveWalkingScreen(state, map)
    }
}

@Composable
private fun NoActiveWalkScreen() {
    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = Forest, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("Caminhos de Fátima", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = {}) { Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Ink) }
        }

        Card(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text("Nenhuma caminhada ativa", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Prepare uma caminhada para começar a acompanhar a sua posição, progresso e APOI.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Muted
                )
                Spacer(Modifier.height(16.dp))
                Text("Preparar caminhada", color = Forest, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ActiveWalkingScreen(state: WalkingState, map: WalkingMapModel?) {
    val gpsState = state.gpsState
    val progress = state.progress
    val nextApoi = state.nextApoi

    Box(modifier = Modifier.fillMaxSize()) {
        WalkingMapSurface(map = map, gpsState = gpsState, hasPosition = state.routePosition != null)

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                    Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = Forest, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(7.dp))
                    Column {
                        Text("Caminhada atual", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text(stageLabel(progress), style = MaterialTheme.typography.labelSmall, color = Muted)
                    }
                }
            }
            IconButton(onClick = {}) { Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Ink) }
        }

        if (gpsState != GpsState.ON_ROUTE) {
            GpsStatusChip(gpsState, modifier = Modifier.align(Alignment.TopCenter).padding(top = 78.dp))
        }

        PositionContextCard(
            state,
            modifier = Modifier.align(Alignment.TopStart).padding(top = 126.dp, start = 12.dp, end = 12.dp)
        )

        Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp)) {
            NextSupportCard(nextApoi, state.nextApoiDistanceKm)
            Spacer(Modifier.height(8.dp))
            ProgressCard(progress)
        }
    }
}

private fun stageLabel(progress: WalkingProgress?): String =
    progress?.stageId?.let { "Etapa $it" } ?: "Etapa de referência"

@Composable
private fun GpsStatusChip(gpsState: GpsState, modifier: Modifier = Modifier) {
    val text = when (gpsState) {
        GpsState.NO_SIGNAL -> "Sem sinal GPS — última posição mantida"
        GpsState.ACQUIRING -> "A obter posição GPS…"
        GpsState.POSSIBLE_DEVIATION -> "Possível desvio do caminho"
        GpsState.PROBABLE_DEVIATION -> "Provável desvio do caminho"
        GpsState.ON_ROUTE -> "No caminho"
    }
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, color = if (gpsState == GpsState.PROBABLE_DEVIATION) Warning else Ink)
    }
}

@Composable
private fun PositionContextCard(state: WalkingState, modifier: Modifier = Modifier) {
    val position = state.routePosition ?: return
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .94f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Forest, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Está aqui", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("${formatKm(position.routeKm)} km no caminho", style = MaterialTheme.typography.bodyMedium)
            }
            Text("±${position.distanceToRouteMeters.toInt()} m", style = MaterialTheme.typography.labelSmall, color = Muted)
        }
    }
}

@Composable
private fun NextSupportCard(nextApoi: Apoi?, distanceKm: Double?) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(ForestSoft), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Forest)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Próximo APOI", style = MaterialTheme.typography.labelMedium, color = Muted)
                    Text(nextApoi?.name ?: "A procurar apoio…", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (nextApoi != null) Text(nextApoi.services.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = "Ver detalhe", tint = Muted)
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Forest, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(5.dp))
                Text(distanceKm?.let { "${formatKm(it)} km pelo caminho" } ?: "Sem APOI seguinte confirmado", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ProgressCard(progress: WalkingProgress?) {
    val walked = progress?.walkedKm ?: 0.0
    val remaining = progress?.remainingKm ?: 0.0
    val ratio = progress?.progressRatio ?: 0.0
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Forest)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${formatKm(walked)} km percorridos", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${formatKm(remaining)} km até ao objetivo", color = Color.White.copy(alpha = .82f), style = MaterialTheme.typography.bodySmall)
                }
                Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(9.dp))
            Box(modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = .25f))) {
                Box(modifier = Modifier.fillMaxWidth(ratio.toFloat().coerceIn(0f, 1f)).height(5.dp).clip(RoundedCornerShape(4.dp)).background(Color.White))
            }
        }
    }
}

private fun formatKm(value: Double): String = String.format(Locale("pt", "PT"), "%.1f", value)

/**
 * Visual map surface. It deliberately renders no invented route geometry.
 * Once a validated route model is supplied, this is the single UI seam for the real map.
 */
@Composable
private fun WalkingMapSurface(
    map: WalkingMapModel?,
    gpsState: GpsState,
    hasPosition: Boolean
) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE9E7E0))) {
        if (map?.hasOfficialGeometry == true) {
            Text(
                "Percurso oficial carregado",
                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted
            )
        } else {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, tint = Muted, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(10.dp))
                Text("Percurso oficial em preparação", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Ink)
                Spacer(Modifier.height(4.dp))
                Text("O mapa só será apresentado quando a geometria validada estiver disponível.", style = MaterialTheme.typography.bodyMedium, color = Muted)
                if (hasPosition && gpsState != GpsState.ON_ROUTE) {
                    Spacer(Modifier.height(8.dp))
                    Text("A posição GPS continua disponível, mas não é projetada num traçado não validado.", style = MaterialTheme.typography.bodySmall, color = Warning)
                }
            }
        }
    }
}
