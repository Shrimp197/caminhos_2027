package com.caminhos2027.v1.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Close
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
import com.caminhos2027.v1.core.data.AndroidRouteCatalog
import com.caminhos2027.v1.core.data.AndroidRouteOption
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.RawGpsPosition
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.route.RouteLocationEngine
import com.caminhos2027.v1.core.walking.WalkingState
import com.caminhos2027.v1.gps.AndroidLocationSource
import com.caminhos2027.v1.gps.GpxSimulationLocationSource
import com.caminhos2027.v1.gps.LocationSource
import java.time.Instant
import java.util.Locale

private val Forest = Color(0xFF165B43)
private val ForestSoft = Color(0xFFE8F2ED)
private val Sand = Color(0xFFF7F4EE)
private val Ink = Color(0xFF1E2521)
private val Muted = Color(0xFF68736D)

private enum class WalkingSurface { ACTIVE, PREPARATION, APOI_BROWSER, APOI_DETAIL, DECISION }

class V1MainActivity : ComponentActivity() {
    private lateinit var appContainer: AndroidV1AppContainer
    private var locationSource: LocationSource? = null
    private var testLocationSource: GpxSimulationLocationSource? = null
    private var walkingState by mutableStateOf<WalkingState?>(null)
    private var preparedWalk by mutableStateOf<Walk?>(null)
    private var startRequested by mutableStateOf(false)
    private var surface by mutableStateOf(WalkingSurface.ACTIVE)
    private var selectedRouteId by mutableStateOf(AndroidRouteCatalog.CENTENARIO_ID)

    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (hasLocationPermissionAfterResult(permissions)) startWalkingLocationSource()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AndroidV1AppContainer(this)
        selectedRouteId = appContainer.publishedRoute().id
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (surface) {
                    WalkingSurface.ACTIVE -> finish()
                    WalkingSurface.PREPARATION -> surface = WalkingSurface.ACTIVE
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
                    startRequested = startRequested,
                    appState = appContainer.store.state,
                    route = appContainer.publishedRoute(),
                    routeOptions = AndroidRouteCatalog.options,
                    selectedRouteId = selectedRouteId,
                    surface = surface,
                    onPrepare = ::openPreparation,
                    onSelectRoute = ::selectRoute,
                    onConfirmPreparation = ::prepareSelectedWalking,
                    onStart = ::requestStartPreparedWalk,
                    onStop = ::stopWalking,
                    onSimulateStep = ::simulateStep,
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
            if (isTestRoute()) startTestRouteIfNeeded()
            else if (hasLocationPermission()) startWalkingLocationSource() else requestLocationPermission()
        }
    }

    override fun onStop() {
        locationSource?.stop()
        locationSource = null
        testLocationSource = null
        super.onStop()
    }

    private fun hasLocationPermission(): Boolean =
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun hasLocationPermissionAfterResult(permissions: Map<String, Boolean>): Boolean =
        permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    private fun restoreWalkingSession() {
        val restored = appContainer.resumePersistedWalk()
        val state = restored.walking
        if (state != null) {
            walkingState = state
            selectedRouteId = state.walk.routeId
            preparedWalk = null
            startRequested = false
            surface = WalkingSurface.ACTIVE
            return
        }
        preparedWalk = appContainer.restorePreparedWalk()?.walk
        preparedWalk?.let { selectedRouteId = it.routeId }
        walkingState = null
        startRequested = false
        surface = WalkingSurface.ACTIVE
    }

    private fun openPreparation() {
        if (walkingState != null || startRequested) return
        surface = WalkingSurface.PREPARATION
    }

    private fun selectRoute(routeId: String) {
        if (walkingState != null || startRequested) return
        appContainer = AndroidV1AppContainer(this, routeId)
        selectedRouteId = routeId
        preparedWalk = appContainer.restorePreparedWalk()?.walk
        walkingState = null
        startRequested = false
        surface = WalkingSurface.PREPARATION
    }

    private fun prepareSelectedWalking() {
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
        if (isTestRoute()) startTestRouteIfNeeded()
        else if (hasLocationPermission()) startWalkingLocationSource() else requestLocationPermission()
    }

    private fun isTestRoute(): Boolean = AndroidRouteCatalog.options.firstOrNull { it.id == selectedRouteId }?.testOnly == true

    private fun startTestRouteIfNeeded() {
        if (testLocationSource != null) return
        if (walkingState == null && (!startRequested || preparedWalk == null)) return

        val source = GpxSimulationLocationSource(
            points = appContainer.publishedRoute().geometry.points,
            onPosition = { position ->
                runOnUiThread {
                    if (!handleGpsForPreparedWalk(position) && walkingState != null) {
                        walkingState = appContainer.activeController().acceptGps(position).walking
                    }
                }
            }
        )
        testLocationSource = source
        locationSource = source
        source.start()
    }

    private fun simulateStep() {
        if (!isTestRoute() || walkingState == null) return
        testLocationSource?.advance()
    }

    private fun handleGpsForPreparedWalk(position: RawGpsPosition): Boolean {
        if (!startRequested || walkingState != null || preparedWalk == null) return false
        val routePosition = RouteLocationEngine.locate(appContainer.publishedRoute(), position)
        val started = try {
            appContainer.preparationController.startSaved(
                catalog = appContainer.publishedApoiCatalog(),
                position = routePosition,
                now = position.capturedAt
            )
        } catch (_: IllegalArgumentException) {
            return false
        }
        val walking = started.walking ?: return false
        appContainer.attachWalk(walking.walk)
        appContainer.store.setWalking(walking)
        walkingState = walking
        preparedWalk = null
        startRequested = false
        surface = WalkingSurface.ACTIVE
        return true
    }

    private fun startWalkingLocationSource() {
        if (locationSource != null || (walkingState == null && !startRequested)) return
        val source = AndroidLocationSource(
            context = this,
            onPosition = { position ->
                runOnUiThread {
                    if (!handleGpsForPreparedWalk(position) && walkingState != null) {
                        walkingState = appContainer.activeController().acceptGps(position).walking
                    }
                }
            },
            onAvailabilityChanged = { available ->
                if (!available) runOnUiThread {
                    if (walkingState != null) walkingState = appContainer.activeController().markNoSignal(Instant.now()).walking
                }
            }
        )
        locationSource = source
        source.start()
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
        testLocationSource = null
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
    startRequested: Boolean,
    appState: AppState,
    route: Route,
    routeOptions: List<AndroidRouteOption>,
    selectedRouteId: String,
    surface: WalkingSurface,
    onPrepare: () -> Unit,
    onSelectRoute: (String) -> Unit,
    onConfirmPreparation: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onSimulateStep: () -> Unit,
    onOpenApoi: () -> Unit,
    onOpenDecision: () -> Unit,
    onApoiSelected: (Apoi) -> Unit,
    onBackToWalking: () -> Unit,
    onBackToApoiBrowser: () -> Unit,
    onBackToDecision: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Sand) {
        when {
            state != null && surface == WalkingSurface.ACTIVE -> ActiveWalkingScreen(state, route, routeOptions, onStop, onSimulateStep, onOpenApoi, onOpenDecision)
            surface == WalkingSurface.PREPARATION -> PreparationMenuScreen(routeOptions, selectedRouteId, onSelectRoute, onConfirmPreparation, onBackToWalking)
            surface == WalkingSurface.APOI_BROWSER -> {
                val browser = appState.apoiBrowser
                if (browser == null) EmptyFlowState("Consulta de APOI indisponível", "Não foi possível preparar a consulta para a posição atual.", onBackToWalking)
                else {
                    NextApoiScreenV1(browser.results, onApoiSelected = { item -> onApoiSelected(item.apoi) })
                    BrowserBackAction(onBackToWalking)
                }
            }
            surface == WalkingSurface.APOI_DETAIL -> {
                val selected = appState.apoiBrowser?.selected
                if (selected == null) EmptyFlowState("APOI não selecionado", "Selecione um apoio a partir da consulta.", onBackToApoiBrowser)
                else ApoiDetailScreenV1(selected, onBackToApoiBrowser)
            }
            surface == WalkingSurface.DECISION -> {
                val decision = appState.decision
                if (decision == null) EmptyFlowState("Decisão indisponível", "Não foi possível calcular as opções para a posição atual.", onBackToWalking)
                else {
                    WalkingDecisionScreenV1(decision, onApoiSelected)
                    DecisionBackActions(onBackToWalking, onOpenApoi)
                }
            }
            preparedWalk != null -> PreparedWalkScreen(preparedWalk, route, startRequested, onStart)
            else -> NoActiveWalkScreen(onPrepare)
        }
    }
}

@Composable
private fun PreparationMenuScreen(
    options: List<AndroidRouteOption>,
    selectedRouteId: String,
    onSelectRoute: (String) -> Unit,
    onConfirmPreparation: () -> Unit,
    onBack: () -> Unit
) {
    val selected = options.firstOrNull { it.id == selectedRouteId } ?: options.first()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Prepare a sua caminhada", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Escolha o percurso antes de definir a caminhada. Os percursos de teste estão identificados e não entram nos dados de produção.", color = Muted)
        options.forEach { option ->
            val isSelected = option.id == selectedRouteId
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = if (isSelected) ForestSoft else Color.White)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(option.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (option.testOnly) Text("AMBIENTE DE TESTE", color = Forest, fontWeight = FontWeight.Bold)
                    Text(option.description, color = Muted)
                    if (!isSelected) OutlinedButton(onClick = { onSelectRoute(option.id) }) { Text("Selecionar") }
                    else Text("Percurso selecionado", color = Forest, fontWeight = FontWeight.Bold)
                }
            }
        }
        Button(onClick = onConfirmPreparation, modifier = Modifier.fillMaxWidth()) { Text("Preparar ${selected.title}") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Voltar") }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun BrowserBackAction(onBack: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.Start) { OutlinedButton(onClick = onBack) { Text("Voltar à caminhada") } }
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
            Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, tint = Forest, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text("Caminhos de Fátima", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Card(modifier = Modifier.align(Alignment.Center).fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
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
private fun PreparedWalkScreen(walk: Walk, route: Route, startRequested: Boolean, onStart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Card(modifier = Modifier.align(Alignment.Center).fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text("Caminhada preparada", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(route.officialName, style = MaterialTheme.typography.titleMedium, color = Forest)
                Spacer(Modifier.height(4.dp))
                Text("Percurso planeado: ${formatKm(walk.plannedStartKm ?: 0.0)} → ${formatKm(walk.plannedDestinationKm ?: 0.0)} km", style = MaterialTheme.typography.bodyMedium, color = Muted)
                Spacer(Modifier.height(8.dp))
                if (startRequested) {
                    Text(if (AndroidRouteCatalog.options.firstOrNull { it.id == route.id }?.testOnly == true) "A iniciar simulação de GPS…" else "A procurar GPS…", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Forest)
                    Spacer(Modifier.height(4.dp))
                    Text(if (AndroidRouteCatalog.options.firstOrNull { it.id == route.id }?.testOnly == true) "Neste ambiente de teste a posição é simulada a partir do GPX selecionado." else "A caminhada continua preparada. Só passa a ativa quando existir uma posição GPS válida no caminho.", style = MaterialTheme.typography.bodyMedium, color = Muted)
                    Spacer(Modifier.height(18.dp))
                    OutlinedButton(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("Tentar novamente") }
                } else {
                    Text("Ao iniciar, o primeiro sinal GPS define a sua posição real no caminho.", style = MaterialTheme.typography.bodyMedium, color = Muted)
                    Spacer(Modifier.height(18.dp))
                    Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("Iniciar caminhada") }
                }
            }
        }
    }
}

@Composable
private fun ActiveWalkingScreen(
    state: WalkingState,
    route: Route,
    routeOptions: List<AndroidRouteOption>,
    onStop: () -> Unit,
    onSimulateStep: () -> Unit,
    onOpenApoi: () -> Unit,
    onOpenDecision: () -> Unit
) {
    val gpsPresentation = WalkingStatusPresentation.gps(state.gpsState)
    val bottomScrollState = rememberScrollState()
    val testRoute = routeOptions.firstOrNull { it.id == route.id }?.testOnly == true
    Column(modifier = Modifier.fillMaxSize().verticalScroll(bottomScrollState).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Navigation, contentDescription = null, tint = Forest, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(route.officialName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(gpsPresentation.title, style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
            IconButton(onClick = onStop) { Icon(Icons.Filled.Close, contentDescription = "Terminar caminhada") }
        }
        WalkingRouteOverviewSurface(route = route, state = state)
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Posição", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(state.routePosition?.let { "${formatKm(it.routeKm)} km" } ?: "Ainda sem posição fiável", style = MaterialTheme.typography.headlineSmall)
                Text("GPS: ${gpsPresentation.detail}", style = MaterialTheme.typography.bodyMedium, color = Muted)
                state.routePosition?.let { position -> Text("Confiança da posição: ${confidenceLabel(position.confidence)}", style = MaterialTheme.typography.bodyMedium, color = Muted) }
                Text(movementLabel(state.movementCue), style = MaterialTheme.typography.bodyMedium, color = Muted)
                Text(if (state.isOffline) "Modo offline ativo" else "Dados locais ativos", style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
        }
        WalkingRouteProgressSurface(state)
        state.nextApoi?.let { next ->
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = ForestSoft)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Próximo APOI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(next.name, style = MaterialTheme.typography.titleLarge)
                    Text(state.nextApoiDistanceKm?.let { formatKm(it) + " km" } ?: "Distância indisponível", color = Muted)
                }
            }
        }
        if (testRoute) {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = ForestSoft)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Simulação QA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("A posição simulada usa os pontos do GPX do percurso selecionado. Estes dados não são GPS real.", color = Muted)
                    Button(onClick = onSimulateStep, modifier = Modifier.fillMaxWidth()) { Text("Avançar no percurso") }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onOpenApoi, modifier = Modifier.weight(1f)) { Text("Consultar APOI") }
            OutlinedButton(onClick = onOpenDecision, modifier = Modifier.weight(1f)) { Text("Decidir apoio") }
        }
        Spacer(Modifier.height(16.dp))
    }
}

private fun movementLabel(cue: com.caminhos2027.v1.core.route.WalkingMovementCue?): String = when (cue) {
    com.caminhos2027.v1.core.route.WalkingMovementCue.FORWARD -> "Movimento: no sentido do percurso"
    com.caminhos2027.v1.core.route.WalkingMovementCue.BACKWARD -> "Movimento: em sentido inverso ao percurso"
    com.caminhos2027.v1.core.route.WalkingMovementCue.STATIONARY -> "Movimento: sem deslocação relevante"
    com.caminhos2027.v1.core.route.WalkingMovementCue.UNKNOWN, null -> "Movimento: ainda sem referência suficiente"
}

private fun confidenceLabel(confidence: com.caminhos2027.v1.core.model.PositionConfidence): String = when (confidence) {
    com.caminhos2027.v1.core.model.PositionConfidence.HIGH -> "alta"
    com.caminhos2027.v1.core.model.PositionConfidence.MEDIUM -> "média"
    com.caminhos2027.v1.core.model.PositionConfidence.LOW -> "baixa"
    com.caminhos2027.v1.core.model.PositionConfidence.UNKNOWN -> "não conhecida"
}

private fun formatKm(km: Double): String = String.format(Locale.US, "%.2f", km)
