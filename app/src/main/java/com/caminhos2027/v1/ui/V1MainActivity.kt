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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.AndroidV1AppContainer
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.route.WalkingProgress
import com.caminhos2027.v1.core.walking.WalkingState
import com.caminhos2027.v1.gps.AndroidLocationSource
import java.time.Instant
import java.util.Locale

private val Forest = Color(0xFF165B43)
private val ForestSoft = Color(0xFFE8F2ED)
private val Sand = Color(0xFFF7F4EE)
private val Ink = Color(0xFF1E2521)
private val Muted = Color(0xFF68736D)
private val Warning = Color(0xFF8A6412)

class V1MainActivity : ComponentActivity() {
    private lateinit var appContainer: AndroidV1AppContainer
    private var locationSource: AndroidLocationSource? = null
    private var walkingState by mutableStateOf<WalkingState?>(null)

    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) startWalkingLocationSource()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AndroidV1AppContainer(this)
        setContent { CaminhosTheme { WalkingScreenV1(walkingState) } }
        restoreWalkingSession()
    }

    override fun onStart() {
        super.onStart()
        if (hasLocationPermission()) startWalkingLocationSource() else requestLocationPermission()
    }

    override fun onStop() {
        locationSource?.stop()
        locationSource = null
        super.onStop()
    }

    private fun hasLocationPermission(): Boolean = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    private fun restoreWalkingSession() {
        val resumed = appContainer.runtime.resume() ?: return
        appContainer.attachWalk(resumed.walk)
        walkingState = appContainer.activeController().resume().walking
    }

    private fun startWalkingLocationSource() {
        if (locationSource != null || walkingState == null) return
        locationSource = AndroidLocationSource(
            context = this,
            onPosition = { position -> runOnUiThread { walkingState = appContainer.activeController().acceptGps(position).walking } },
            onAvailabilityChanged = { available -> if (!available) runOnUiThread { walkingState = appContainer.activeController().markNoSignal(Instant.now()).walking } }
        )
        locationSource?.start()
    }
}

@Composable
private fun CaminhosTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(primary = Forest, onPrimary = Color.White, background = Sand, surface = Color.White, onSurface = Ink, onBackground = Ink), content = content)
}

@Composable
private fun WalkingScreenV1(state: WalkingState? = null) {
    Surface(modifier = Modifier.fillMaxSize(), color = Sand) {
        if (state == null) NoActiveWalkScreen() else ActiveWalkingScreen(state)
    }
}

@Composable
private fun NoActiveWalkScreen() {
    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = Forest, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("Caminhos de Fátima", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = {}) { Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Ink) }
        }
        Card(modifier = Modifier.align(Alignment.Center).fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text("Nenhuma caminhada ativa", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Prepare uma caminhada para começar a acompanhar a sua posição, progresso e APOI.", style = MaterialTheme.typography.bodyLarge, color = Muted)
                Spacer(Modifier.height(16.dp))
                Text("Preparar caminhada", color = Forest, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ActiveWalkingScreen(state: WalkingState) {
    Box(modifier = Modifier.fillMaxSize()) {
        WalkingMapSurface(gpsState = state.gpsState, hasPosition = state.routePosition != null)
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                    Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = Forest, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(7.dp))
                    Column {
                        Text("Caminhada atual", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text(stageLabel(state.progress), style = MaterialTheme.typography.labelSmall, color = Muted)
                    }
                }
            }
            IconButton(onClick = {}) { Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Ink) }
        }
        if (state.gpsState != GpsState.ON_ROUTE) GpsStatusChip(state.gpsState, modifier = Modifier.align(Alignment.TopCenter).padding(top = 78.dp))
        PositionContextCard(state, modifier = Modifier.align(Alignment.TopStart).padding(top = 126.dp, start = 12.dp, end = 12.dp))
        Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp)) {
            NextSupportCard(state.nextApoi, state.nextApoiDistanceKm)
            Spacer(Modifier.height(8.dp))
            ProgressCard(state.progress)
        }
    }
}

private fun stageLabel(progress: WalkingProgress?): String = progress?.stageId?.let { "Etapa $it" } ?: "Etapa de referência"

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
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .94f)), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Row(modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
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
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(42.dp).background(ForestSoft, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Forest) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Próximo APOI", style = MaterialTheme.typography.labelMedium, color = Muted)
                if (nextApoi == null) Text("Não há APOI publicado à frente.", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                else {
                    Text(nextApoi.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    distanceKm?.let { Text(formatDistance(it), style = MaterialTheme.typography.bodySmall, color = Muted) }
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Muted)
        }
    }
}

@Composable
private fun ProgressCard(progress: WalkingProgress?) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Progresso", style = MaterialTheme.typography.labelMedium, color = Muted)
            Spacer(Modifier.height(4.dp))
            if (progress == null) Text("Aguardando posição", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            else {
                Text("${formatKm(progress.walkedKm)} km caminhados", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("${formatKm(progress.remainingKm)} km até ao destino planeado", style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
    }
}

@Composable
private fun WalkingMapSurface(gpsState: GpsState, hasPosition: Boolean) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE9EEE9))) {
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Navigation, contentDescription = null, tint = Forest, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(6.dp))
            Text(if (hasPosition) "Mapa da caminhada" else "Mapa indisponível", color = Muted)
            if (gpsState == GpsState.NO_SIGNAL) Text("Última posição mantida", style = MaterialTheme.typography.labelSmall, color = Warning)
        }
    }
}

private fun formatKm(value: Double): String = String.format(Locale.US, "%.2f", value)
private fun formatDistance(value: Double): String = if (value < 1.0) String.format(Locale.US, "%d m", (value * 1000).toInt()) else String.format(Locale.US, "%.1f km", value)
