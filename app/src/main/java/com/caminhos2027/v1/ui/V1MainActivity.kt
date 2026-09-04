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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.caminhos2027.v1.core.AppState
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.route.RouteLocationEngine
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

private enum class WalkingSurface { ACTIVE, APOI_BROWSER, APOI_DETAIL, DECISION }

class V1MainActivity : ComponentActivity() {
    private lateinit var appContainer: AndroidV1AppContainer
    private var locationSource: AndroidLocationSource? = null
    private var walkingState by mutableStateOf<WalkingState?>(null)
    private var preparedWalk by mutableStateOf<Walk?>(null)
    private var startRequested by mutableStateOf(false)
    private var surface by mutableStateOf(WalkingSurface.ACTIVE)

    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (hasLocationPermissionAfterResult(permissions)) startWalkingLocationSource()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AndroidV1AppContainer(this)
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (surface) {
                    WalkingSurface.ACTIVE -> finish()
                    WalkingSurface.APOI_BROWSER -> returnToWalking()
                    WalkingSurface.APOI_DETAIL -> returnToApoiBrowser()
                    WalkingSurface.DECISION -> returnToWalking()
                }
            }
        })
        setContent {
            CaminhosTheme {
                WalkingScreenV1(
                    state = walkingState,
                    preparedWalk = preparedWalk,
                    appState = appContainer.store.state,
                    surface = surface,
                    onPrepare = ::prepareWalking,
                    onStart = ::requestStartPreparedWalk,
                    onStop = ::stopWalking,
                    onOpenApoi = ::openApoiBrowser,
                    onOpenDecision = ::openDecision,
                    onApoiSelected = ::selectApoi,
                    onBackToWalking = ::returnToWalking,
                    onBackToApoiBrowser = ::returnToApoiBrowser,
                    onBackToDecision = ::returnToDecision
                )
            }
        }
        restoreWalkingSession()
    }

    override fun onStart() {
        super.onStart()
        if (walkingState != null || startRequested) {
            if (hasLocationPermission()) startWalkingLocationSource() else requestLocationPermission()
        }
    }

    override fun onStop() {
        locationSource?.stop()
        locationSource = null
        super.onStop()
    }

    private fun hasLocationPermission(): Boolean =
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun hasLocationPermissionAfterResult(permissions: Map<String, Boolean>): Boolean =
        permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    private fun restoreWalkingSession() {
        val resumed = appContainer.runtime.resume() ?: return
        appContainer.attachWalk(resumed.walk)
        walkingState = appContainer.activeController().resume().walking
        preparedWalk = null
        startRequested = false
        surface = WalkingSurface.ACTIVE
    }

    private fun prepareWalking() {
        val prepared = appContainer.preparationController.save(
            walkId = "walk-${System.currentTimeMillis()}",
            startRouteKm = 0.0,
            destinationRouteKm = appContainer.publishedRoute().totalDistanceKm
        )
        preparedWalk = prepared.walking?.walk
        walkingState = null
        startRequested = false
        surface = WalkingSurface.ACTIVE
    }

    private fun requestStartPreparedWalk() {
        require(preparedWalk?.status == WalkStatus.PLANNED) { "A planned walk is required before starting" }
        startRequested = true
        if (hasLocationPermission()) startWalkingLocationSource() else requestLocationPermission()
    }

    private fun handleFirstGpsForPreparedWalk(position: RawGpsPosition): Boolean {
        if (!startRequested || walkingState != null || preparedWalk == null) return false
        val routePosition = RouteLocationEngine.locate(appContainer.publishedRoute(), position)
        val started = appContainer.preparationController.startSaved(
            catalog = appContainer.publishedApoiCatalog(),
            position = routePosition,
            now = position.capturedAt
        )
        val walking = started.walking ?: return false
        appContainer.attachWalk(walking.walk)
        walkingState = appContainer.activeController().resume().walking
        preparedWalk = null
        startRequested = false
        surface = WalkingSurface.ACTIVE
        return true
    }

    private fun startWalkingLocationSource() {
        if (locationSource != null || (walkingState == null && !startRequested)) return
        locationSource = AndroidLocationSource(
            context = this,
            onPosition = { position ->
                runOnUiThread {
                    if (!handleFirstGpsForPreparedWalk(position)) {
                        if (walkingState != null) {
                            walkingState = appContainer.activeController().acceptGps(position).walking
                        }
                    }
                }
            },
            onAvailabilityChanged = { available ->
                if (!available) {
                    runOnUiThread {
                        if (walkingState != null) {
                            walkingState = appContainer.activeController().markNoSignal(Instant.now()).walking
                        }
                    }
                }
            }
        )
        locationSource?.start()
    }

    private fun openApoiBrowser() {
        if (walkingState?.routePosition == null) return
        appContainer.apoiDecisionController.clearDecision()
        appContainer.apoiDecisionController.browseApoi()
        surface = WalkingSurface.APOI_BROWSER
    }

    private fun selectApoi(apoi: Apoi) {
        appContainer.apoiDecisionController.selectApoi(apoi.id)
        surface = WalkingSurface.APOI_DETAIL
    }

    private fun openDecision() {
        if (walkingState?.routePosition == null) return
        appContainer.apoiDecisionController.clearApoiSelection()
        appContainer.apoiDecisionController.buildDecision()
        surface = WalkingSurface.DECISION
    }

    private fun returnToWalking() {
        appContainer.apoiDecisionController.clearApoiSelection()
        appContainer.apoiDecisionController.clearDecision()
        surface = WalkingSurface.ACTIVE
    }

    private fun returnToApoiBrowser() {
        val cameFromDecision = appContainer.store.state.decision != null
        appContainer.apoiDecisionController.clearApoiSelection()
        surface = if (cameFromDecision) WalkingSurface.DECISION else WalkingSurface.APOI_BROWSER
    }

    private fun returnToDecision() {
        appContainer.apoiDecisionController.clearApoiSelection()
        surface = WalkingSurface.DECISION
    }

    private fun stopWalking() {
        val position = walkingState?.routePosition ?: return
        appContainer.runtime.stop(position, Instant.now())
        appContainer.clearSession()
        locationSource?.stop()
        locationSource = null
        walkingState = null
        preparedWalk = null
        startRequested = false
        surface = WalkingSurface.ACTIVE
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

@Composable
private fun WalkingScreenV1(
    state: WalkingState?,
    preparedWalk: Walk?,
    appState: AppState,
    surface: WalkingSurface,
    onPrepare: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onOpenApoi: () -> Unit,
    onOpenDecision: () -> Unit,
    onApoiSelected: (Apoi) -> Unit,
    onBackToWalking: () -> Unit,
    onBackToApoiBrowser: () -> Unit,
    onBackToDecision: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Sand) {
        when {
            state != null && surface == WalkingSurface.ACTIVE -> ActiveWalkingScreen(state, onStop, onOpenApoi, onOpenDecision)
            surface == WalkingSurface.APOI_BROWSER -> {
                val browser = appState.apoiBrowser
                if (browser == null) {
                    EmptyFlowState("Consulta de APOI indisponível", "Não foi possível preparar a consulta para a posição atual.", onBackToWalking)
                } else {
                    NextApoiScreenV1(browser.results, onApoiSelected = { item -> onApoiSelected(item.apoi) })
                    BrowserBackAction(onBackToWalking)
                }
            }
            surface == WalkingSurface.APOI_DETAIL -> {
                val selected = appState.apoiBrowser?.selected
                if (selected == null) {
                    EmptyFlowState("APOI não selecionado", "Selecione um apoio a partir da consulta.", onBackToApoiBrowser)
                } else {
                    ApoiDetailScreenV1(selected, onBackToApoiBrowser)
                }
            }
            surface == WalkingSurface.DECISION -> {
                val decision = appState.decision
                if (decision == null) {
                    EmptyFlowState("Decisão indisponível", "Não foi possível calcular as opções para a posição atual.", onBackToWalking)
                } else {
                    WalkingDecisionScreenV1(decision, onApoiSelected)
                    DecisionBackActions(onBackToWalking, onOpenApoi)
                }
            }
            preparedWalk != null -> PreparedWalkScreen(preparedWalk, onStart)
            else -> NoActiveWalkScreen(onPrepare)
        }
    }
}

@Composable
private fun BrowserBackAction(onBack: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.Start) {
        OutlinedButton(onClick = onBack) { Text("Voltar à caminhada") }
    }
}

@Composable
private fun DecisionBackActions(onBackToWalking: () -> Unit, onOpenApoi: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onBackToWalking) { Text("Voltar à caminhada") }
        OutlinedButton(onClick = onOpenApoi) { Text("Consultar APOI") }
    }
}

@Composable
private fun EmptyFlowState(title: String, message: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, color = Muted)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onBack) { Text("Voltar") }
    }
}

@Composable
private fun NoActiveWalkScreen(onPrepare: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = Forest, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text("Caminhos de Fátima", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                Text("Prepare uma caminhada para começar a acompanhar a sua posição, progresso e APOI.", style = MaterialTheme.typography.bodyLarge, color = Muted)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onPrepare, modifier = Modifier.fillMaxWidth()) { Text("Preparar caminhada") }
            }
        }
    }
}

@Composable
private fun PreparedWalkScreen(walk: Walk, onStart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Card(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text("Caminhada preparada", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Caminho do Centenário", style = MaterialTheme.typography.titleMedium, color = Forest)
                Spacer(Modifier.height(4.dp))
                Text("Percurso planeado: ${formatKm(walk.plannedStartKm ?: 0.0)} → ${formatKm(walk.plannedDestinationKm ?: 0.0)} km", style = MaterialTheme.typography.bodyMedium, color = Muted)
                Spacer(Modifier.height(8.dp))
                Text("Ao iniciar, o primeiro sinal GPS define a sua posição real no caminho.", style = MaterialTheme.typography.bodyMedium, color = Muted)
                Spacer(Modifier.height(18.dp))
                Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("Iniciar caminhada") }
            }
        }
    }
}

@Composable
private fun ActiveWalkingScreen(state: WalkingState, onStop: () -> Unit, onOpenApoi: () -> Unit, onOpenDecision: () -> Unit) {
    val gpsPresentation = WalkingStatusPresentation.gps(state.gpsState)
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
            IconButton(onClick = onStop) { Icon(Icons.Default.Close, contentDescription = "Terminar caminhada", tint = Ink) }
        }
        Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            WalkingRouteProgressSurface(state.progress)
            state.routePosition?.let { position ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Está aqui", style = MaterialTheme.typography.labelLarge, color = Forest)
                        Text("${formatKm(position.routeKm)} km no caminho", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(gpsPresentation.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(gpsPresentation.detail, style = MaterialTheme.typography.bodySmall, color = Muted)
                        Text(WalkingStatusPresentation.confidence(position.confidence), style = MaterialTheme.typography.bodySmall, color = Muted)
                        WalkingStatusPresentation.offlineLabel(state.isOffline)?.let { label ->
                            Text(label, style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                        state.progress?.let { progress ->
                            Text("Faltam ${formatKm(progress.remainingKm)} km até ao destino planeado.", style = MaterialTheme.typography.bodyMedium, color = Muted)
                        }
                        state.nextApoi?.let { apoi ->
                            Text("Próximo APOI: ${apoi.name}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            state.nextApoiDistanceKm?.let { distance -> Text("${formatKm(distance)} km pelo caminho", style = MaterialTheme.typography.bodySmall, color = Muted) }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onOpenApoi, modifier = Modifier.weight(1f)) { Text("Consultar APOI") }
                            Button(onClick = onOpenDecision, modifier = Modifier.weight(1f)) { Text("Decidir") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WalkingMapSurface(gpsState: GpsState, hasPosition: Boolean) {
    Box(modifier = Modifier.fillMaxSize().background(ForestSoft)) {
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Navigation, contentDescription = null, tint = Forest, modifier = Modifier.size(46.dp))
            Spacer(Modifier.height(8.dp))
            Text("Superfície do percurso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                when {
                    !hasPosition -> "A aguardar localização"
                    else -> WalkingStatusPresentation.gps(gpsState).label
                },
                style = MaterialTheme.typography.bodySmall,
                color = Muted
            )
        }
    }
}

private fun stageLabel(progress: WalkingProgress?): String =
    progress?.stageName ?: progress?.stageId ?: "Etapa não definida"

private fun formatKm(value: Double): String = String.format(Locale.US, "%.1f", value)
