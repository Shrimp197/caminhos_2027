package com.caminhos2027.v1.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronDown
import androidx.compose.material.icons.filled.ChevronUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.AppState
import com.caminhos2027.v1.core.data.AndroidRouteOption
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.walking.WalkingState
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

internal enum class WalkingSurface { ACTIVE, PREPARATION, APOI_BROWSER, APOI_DETAIL, DECISION }

private val AppForest = Color(0xFF0E6546)
private val AppForestDark = Color(0xFF0A5138)
private val AppForestSoft = Color(0xFFE6F2EB)
private val AppSand = Color(0xFFF6F3EC)
private val AppMap = Color(0xFFE9F0E7)
private val AppMapGrid = Color(0xFFD5DED3)
private val AppInk = Color(0xFF1C2520)
private val AppMuted = Color(0xFF6A746E)
private val AppWarning = Color(0xFF9A5A00)
private val AppWarningSoft = Color(0xFFFFF1D9)

@Composable
internal fun V1PrimaryExperienceScreen(
    state: WalkingState?,
    preparedWalk: Walk?,
    startRequested: Boolean,
    pendingStartDistanceMeters: Double?,
    appState: AppState,
    route: Route,
    routeOptions: List<AndroidRouteOption>,
    selectedRouteId: String,
    surface: WalkingSurface,
    onPrepare: () -> Unit,
    onSelectRoute: (String) -> Unit,
    onConfirmPreparation: (Double, Double) -> Unit,
    onStart: () -> Unit,
    onCancelPendingStart: () -> Unit,
    onStop: () -> Unit,
    onOpenApoi: () -> Unit,
    onOpenDecision: () -> Unit,
    onApoiSelected: (Apoi) -> Unit,
    onApoiScopeChanged: (Double?) -> Unit,
    onApoiSearchChanged: (String) -> Unit,
    onApoiFilterToggled: (ApoiCategory) -> Unit,
    onBackToWalking: () -> Unit,
    onBackToApoiBrowser: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = AppSand) {
        when {
            surface == WalkingSurface.PREPARATION -> PreparationExperienceScreen(
                route = route,
                options = routeOptions,
                selectedRouteId = selectedRouteId,
                onSelectRoute = onSelectRoute,
                onConfirmPreparation = onConfirmPreparation,
                onBack = onBackToWalking
            )
            surface == WalkingSurface.APOI_BROWSER -> {
                val browser = appState.apoiBrowser
                if (browser == null) {
                    EmptyExperienceState("Apoios indisponíveis", "Ainda não existe uma consulta preparada para a posição atual.", onBackToWalking)
                } else {
                    ApoiHubScreenV1(
                        query = browser.query,
                        items = browser.results,
                        onSearchChanged = onApoiSearchChanged,
                        onFilterToggled = onApoiFilterToggled,
                        onScopeChanged = onApoiScopeChanged,
                        onApoiSelected = onApoiSelected,
                        onBack = onBackToWalking
                    )
                }
            }
            surface == WalkingSurface.APOI_DETAIL -> {
                val selected = appState.apoiBrowser?.selected
                if (selected == null) EmptyExperienceState("APOI não selecionado", "Escolha um apoio da lista para ver o detalhe.", onBackToApoiBrowser)
                else ApoiDetailScreenV1(selected, onBackToApoiBrowser)
            }
            surface == WalkingSurface.DECISION -> {
                val decision = appState.decision
                if (decision == null) EmptyExperienceState("Decisão indisponível", "Não foi possível preparar as opções para a posição atual.", onBackToWalking)
                else DecisionExperienceScreen(decision = decision, onBack = onBackToWalking, onOpenApoi = onOpenApoi)
            }
            state != null -> ActiveWalkingExperienceScreen(
                state = state,
                route = route,
                routeOptions = routeOptions,
                onStop = onStop,
                onOpenApoi = onOpenApoi,
                onOpenDecision = onOpenDecision
            )
            preparedWalk != null -> PreparedWalkExperienceScreen(
                walk = preparedWalk,
                route = route,
                startRequested = startRequested,
                pendingStartDistanceMeters = pendingStartDistanceMeters,
                onStart = onStart,
                onCancelPendingStart = onCancelPendingStart
            )
            else -> PreparationLandingScreen(
                route = route,
                routeOptions = routeOptions,
                onPrepare = onPrepare
            )
        }
    }
}

@Composable
private fun PreparationLandingScreen(
    route: Route,
    routeOptions: List<AndroidRouteOption>,
    onPrepare: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Menu, contentDescription = null, tint = AppInk)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("CAMINHOS", fontWeight = FontWeight.Bold, color = AppForest)
                Text("DO PEREGRINO", fontWeight = FontWeight.Bold, color = AppForest)
            }
            Icon(Icons.Filled.DirectionsWalk, contentDescription = null, tint = AppForest, modifier = Modifier.size(28.dp))
        }

        Text("Prepare a sua caminhada", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Escolha o percurso e defina livremente onde começa e onde termina. As etapas oficiais são referência, não uma obrigação.", color = AppMuted)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppForest)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(route.officialName, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${formatKm(route.totalDistanceKm)} km · percurso disponível localmente", color = Color.White.copy(alpha = 0.88f))
                Spacer(Modifier.height(6.dp))
                Text("O acompanhamento usa o traçado versionado e a sua posição GPS. O plano guardado não inicia a caminhada.", color = Color.White.copy(alpha = 0.88f))
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = onPrepare,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("PREPARAR") }
            }
        }

        PreparationPrinciplesCard()

        if (routeOptions.any { it.testOnly }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AppWarningSoft)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Ambientes de teste", fontWeight = FontWeight.Bold, color = AppWarning)
                    Text("SR e HF aparecem apenas em builds debug e servem para testar a experiência com dados fictícios. Não são percursos de produção.", color = AppInk)
                }
            }
        }
    }
}

@Composable
private fun PreparationPrinciplesCard() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Como funciona", fontWeight = FontWeight.Bold)
            PrincipleRow("1", "Preparar", "Escolher início e destino no percurso.")
            PrincipleRow("2", "Guardar", "O plano fica preparado; ainda não começa.")
            PrincipleRow("3", "Iniciar", "A caminhada começa quando existir uma posição válida no percurso.")
        }
    }
}

@Composable
private fun PrincipleRow(number: String, title: String, detail: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(AppForestSoft), contentAlignment = Alignment.Center) {
            Text(number, fontWeight = FontWeight.Bold, color = AppForest)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(detail, color = AppMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PreparationExperienceScreen(
    route: Route,
    options: List<AndroidRouteOption>,
    selectedRouteId: String,
    onSelectRoute: (String) -> Unit,
    onConfirmPreparation: (Double, Double) -> Unit,
    onBack: () -> Unit
) {
    val selected = options.firstOrNull { it.id == selectedRouteId } ?: options.first()
    var startText by remember(selectedRouteId) { mutableStateOf("0.00") }
    var destinationText by remember(selectedRouteId) { mutableStateOf(formatKm(route.totalDistanceKm)) }
    var validationMessage by remember(selectedRouteId) { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TopBar(title = "Preparação", onBack = onBack)
        Text("1 · INÍCIO DA CAMINHADA", style = MaterialTheme.typography.labelLarge, color = AppForest, fontWeight = FontWeight.Bold)
        Text("Escolha o percurso", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        options.forEach { option ->
            val chosen = option.id == selectedRouteId
            Card(
                modifier = Modifier.fillMaxWidth().clickable(enabled = !chosen) { onSelectRoute(option.id) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (chosen) AppForestSoft else Color.White)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(if (chosen) AppForest else AppSand), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Navigation, contentDescription = null, tint = if (chosen) Color.White else AppForest, modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(option.title, fontWeight = FontWeight.Bold)
                        Text(option.description, color = AppMuted, style = MaterialTheme.typography.bodySmall)
                        if (option.testOnly) Text("AMBIENTE DE TESTE", color = AppWarning, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                    if (chosen) Text("SELECIONADO", color = AppForest, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Início e fim", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("O ponto inicial e o destino são valores no percurso. As etapas oficiais não são recalculadas a partir da caminhada.", color = AppMuted)
                Text("Percurso · ${selected.title} · ${formatKm(route.totalDistanceKm)} km", color = AppForest, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = startText, onValueChange = { startText = it; validationMessage = null }, label = { Text("Início (km)") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = destinationText, onValueChange = { destinationText = it; validationMessage = null }, label = { Text("Destino (km)") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Text("Valores válidos: 0.00 → ${formatKm(route.totalDistanceKm)} km", style = MaterialTheme.typography.bodySmall, color = AppMuted)
                validationMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                Button(
                    onClick = {
                        val start = startText.replace(',', '.').toDoubleOrNull()
                        val destination = destinationText.replace(',', '.').toDoubleOrNull()
                        when {
                            start == null || destination == null -> validationMessage = "Indique valores numéricos para o início e o destino."
                            !start.isFinite() || !destination.isFinite() -> validationMessage = "Os valores têm de ser números válidos."
                            start < 0.0 || destination < 0.0 -> validationMessage = "O início e o destino não podem ser negativos."
                            start > route.totalDistanceKm || destination > route.totalDistanceKm -> validationMessage = "Os valores ultrapassam a extensão do percurso."
                            start >= destination -> validationMessage = "O destino tem de ficar depois do início."
                            else -> onConfirmPreparation(start, destination)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("GUARDAR PLANO") }
            }
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun PreparedWalkExperienceScreen(
    walk: Walk,
    route: Route,
    startRequested: Boolean,
    pendingStartDistanceMeters: Double?,
    onStart: () -> Unit,
    onCancelPendingStart: () -> Unit
) {
    val waitingForRoute = pendingStartDistanceMeters != null && pendingStartDistanceMeters > 0.0
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Plano guardado", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(route.officialName, color = AppForest, fontWeight = FontWeight.Bold)
                Text("${formatKm(walk.plannedStartKm ?: 0.0)} → ${formatKm(walk.plannedDestinationKm ?: 0.0)} km", style = MaterialTheme.typography.titleLarge)
                Text("Guardar o plano não inicia a caminhada. O início real depende da posição GPS válida no percurso.", color = AppMuted)
                if (startRequested) {
                    HorizontalDivider()
                    Text(
                        when {
                            route.id == "sr-test" || route.id == "hf-test" -> "A procurar posição de teste…"
                            waitingForRoute -> "Está fora do percurso"
                            else -> "A procurar GPS…"
                        },
                        fontWeight = FontWeight.Bold,
                        color = if (waitingForRoute) AppWarning else AppForest
                    )
                    when {
                        route.id == "sr-test" || route.id == "hf-test" -> Text("A posição é simulada pelo GPX do ambiente de teste e percorre a mesma cadeia de estado do GPS real.", color = AppMuted)
                        waitingForRoute -> Text("Está a ${formatMeters(pendingStartDistanceMeters ?: 0.0)} do percurso. Aproxime-se do traçado; a aplicação iniciará a caminhada quando a posição entrar no caminho.", color = AppMuted)
                        else -> Text("A caminhada continua preparada até surgir uma posição válida no caminho.", color = AppMuted)
                    }
                    Button(onClick = onCancelPendingStart, modifier = Modifier.fillMaxWidth()) { Text("CANCELAR INÍCIO") }
                    if (waitingForRoute) OutlinedButton(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("CONTINUAR A PROCURAR") }
                } else {
                    Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("INICIAR CAMINHADA") }
                }
            }
        }
    }
}

@Composable
private fun ActiveWalkingExperienceScreen(
    state: WalkingState,
    route: Route,
    routeOptions: List<AndroidRouteOption>,
    onStop: () -> Unit,
    onOpenApoi: () -> Unit,
    onOpenDecision: () -> Unit
) {
    var sheetExpanded by remember { mutableStateOf(true) }
    val progress = state.progress
    val currentKm = state.routePosition?.routeKm
    val remainingKm = progress?.remainingKm ?: state.walk.plannedDestinationKm?.let { destination -> (destination - (currentKm ?: 0.0)).coerceAtLeast(0.0) }
    val routeProgress = progress?.progressRatio ?: 0.0
    val testRoute = routeOptions.firstOrNull { it.id == route.id }?.testOnly == true

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Menu, contentDescription = null, tint = AppInk)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(route.officialName, fontWeight = FontWeight.Bold)
                    Text(if (state.isOffline) "Offline · dados locais" else "Caminhada ativa · ${gpsStateTitle(state)}", color = AppMuted, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onStop) { Icon(Icons.Filled.Close, contentDescription = "Terminar caminhada") }
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                RouteMapCanvas(
                    modifier = Modifier.fillMaxSize(),
                    geometry = route.geometry.points,
                    currentRouteKm = currentKm,
                    totalDistanceKm = route.totalDistanceKm,
                    startRouteKm = state.walk.plannedStartKm,
                    destinationRouteKm = state.walk.plannedDestinationKm
                )
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MapStatCard(value = formatKm(currentKm ?: 0.0), label = "Percorridos")
                    MapStatCard(value = formatKm(remainingKm ?: 0.0), label = "Para o fim")
                }
                Card(
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 96.dp, end = 14.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = AppForest, modifier = Modifier.padding(13.dp).size(26.dp))
                }
                Card(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 14.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(AppForestSoft), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Place, contentDescription = null, tint = AppForest, modifier = Modifier.size(19.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Próximo APOI", style = MaterialTheme.typography.labelLarge, color = AppMuted)
                            Text(state.nextApoi?.name ?: "Nenhum apoio publicado nesta seleção", fontWeight = FontWeight.Bold)
                        }
                        Text(state.nextApoiDistanceKm?.let(::formatDistanceLabel) ?: "—", color = AppForest, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.fillMaxWidth().clickable { sheetExpanded = !sheetExpanded }, contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(width = 44.dp, height = 5.dp).clip(RoundedCornerShape(8.dp)).background(AppMapGrid))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Caminhada", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${formatKm(currentKm ?: 0.0)} km percorridos · ${formatKm(remainingKm ?: 0.0)} km para o destino", color = AppMuted)
                    }
                    Icon(if (sheetExpanded) Icons.Filled.ChevronDown else Icons.Filled.ChevronUp, contentDescription = null, tint = AppForest)
                }

                LinearProgressIndicator(progress = { routeProgress.toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(10.dp)), color = AppForest, trackColor = AppForestSoft)

                if (sheetExpanded) {
                    state.progress?.stageName?.takeIf { it.isNotBlank() }?.let { stage -> Text("Etapa atual · $stage", color = AppForest, fontWeight = FontWeight.Bold) }
                    ProgressSummary(state, route)
                    Text("Próximos apoios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    state.nextApoi?.let { next ->
                        ApoiInlineSummary(next, state.nextApoiDistanceKm)
                    } ?: Text("Não existem APOI publicados à frente nesta seleção.", color = AppMuted)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onOpenApoi, modifier = Modifier.weight(1f)) { Text("VER APOIOS") }
                        OutlinedButton(onClick = onOpenDecision, modifier = Modifier.weight(1f)) { Text("OPÇÕES") }
                    }
                    Text(
                        "A orientação mostra o que existe e as consequências; a decisão continua a ser do peregrino.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted
                    )
                    if (testRoute) {
                        Text("QA: o percurso é simulado; GPS real e dados de produção não são alterados.", style = MaterialTheme.typography.labelSmall, color = AppWarning)
                    }
                }
            }
        }
    }
}

@Composable
private fun MapStatCard(value: String, label: String) {
    Card(modifier = Modifier.width(140.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = AppMuted)
        }
    }
}

@Composable
private fun ProgressSummary(state: WalkingState, route: Route) {
    val progress = state.progress ?: return
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Progresso", fontWeight = FontWeight.Bold)
            Text("${(progress.progressRatio * 100.0).coerceIn(0.0, 100.0).let { String.format(Locale("pt", "PT"), "%.1f", it) }}%", color = AppForest, fontWeight = FontWeight.Bold)
        }
        Text("${formatKm(progress.currentRouteKm)} km / ${formatKm(progress.targetRouteKm)} km", style = MaterialTheme.typography.titleMedium)
        Text("${formatKm(progress.walkedKm)} km feitos · ${formatKm(progress.remainingKm)} km restantes", color = AppMuted)
        Text("Traçado: ${if (route.totalDistanceKm > 0.0) "disponível localmente" else "indisponível"}", style = MaterialTheme.typography.bodySmall, color = AppMuted)
    }
}

@Composable
private fun ApoiInlineSummary(apoi: Apoi, distanceKm: Double?) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(AppForestSoft), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Place, contentDescription = null, tint = AppForest, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(apoi.name, fontWeight = FontWeight.Bold)
            Text(apoi.services.sortedBy { it.name }.joinToString(" · ") { categoryLabel(it) }, color = AppMuted, style = MaterialTheme.typography.bodySmall)
        }
        Text(distanceKm?.let(::formatDistanceLabel) ?: "—", fontWeight = FontWeight.Bold, color = AppForest)
    }
}

@Composable
private fun ApoiHubScreenV1(
    query: com.caminhos2027.v1.core.apoi.ApoiBrowserQuery,
    items: List<com.caminhos2027.v1.core.apoi.ApoiAhead>,
    onSearchChanged: (String) -> Unit,
    onFilterToggled: (ApoiCategory) -> Unit,
    onScopeChanged: (Double?) -> Unit,
    onApoiSelected: (Apoi) -> Unit,
    onBack: () -> Unit
) {
    val nextTenKm = query.maxDistanceKm == 10.0
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TopBar(title = if (nextTenKm) "Próximos 10 km" else "Apoios", onBack = onBack)
        Text(
            if (nextTenKm) "Veja os apoios disponíveis à frente, ordenados pela distância no Caminho." else "Consulte e filtre os APOI do contexto atual, sem perder a referência à distância no percurso.",
            color = AppMuted
        )
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = nextTenKm, onClick = { onScopeChanged(10.0) }, label = { Text("Próximos 10 km") })
            FilterChip(selected = !nextTenKm, onClick = { onScopeChanged(null) }, label = { Text("Todos os apoios") })
        }
        OutlinedTextField(
            value = query.text,
            onValueChange = onSearchChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Procurar por nome") }
        )
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(ApoiCategory.AGUA, ApoiCategory.ALIMENTACAO, ApoiCategory.PERNOITA, ApoiCategory.DESCANSO, ApoiCategory.DUCHES, ApoiCategory.CARREGAMENTO, ApoiCategory.TRANSPORTE, ApoiCategory.EMERGENCIA).forEach { category ->
                FilterChip(selected = category in query.filter.services, onClick = { onFilterToggled(category) }, label = { Text(categoryLabel(category)) })
            }
        }
        if (items.isEmpty()) {
            EmptyApoiContext(nextTenKm)
        } else {
            if (nextTenKm) {
                items.forEachIndexed { index, item ->
                    ApoiTimelineCard(item = item, first = index == 0, onOpen = { onApoiSelected(item.apoi) })
                }
            } else {
                items.forEach { item ->
                    ApoiRichCard(item = item, onOpen = { onApoiSelected(item.apoi) })
                }
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun EmptyApoiContext(nextTenKm: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(if (nextTenKm) "Sem APOI publicado nos próximos 10 km" else "Sem APOI para estes critérios", fontWeight = FontWeight.Bold)
            Text("A ausência de resultado não significa que não exista apoio no terreno; significa apenas que não há registos elegíveis nesta seleção de dados.", color = AppMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ApoiTimelineCard(item: com.caminhos2027.v1.core.apoi.ApoiAhead, first: Boolean, onOpen: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.width(58.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(formatDistanceLabel(item.distanceKm), fontWeight = FontWeight.Bold, color = AppForest)
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(AppForest))
            if (!first) Spacer(Modifier.height(2.dp))
        }
        ApoiRichCard(item = item, onOpen = onOpen, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ApoiRichCard(item: com.caminhos2027.v1.core.apoi.ApoiAhead, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val apoi = item.apoi
    Card(modifier = modifier.fillMaxWidth().clickable { onOpen() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(AppForestSoft), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Place, contentDescription = null, tint = AppForest, modifier = Modifier.size(21.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(apoi.services.firstOrNull()?.let(::categoryLabel) ?: "APOIO", style = MaterialTheme.typography.labelSmall, color = AppForest, fontWeight = FontWeight.Bold)
                    Text(apoi.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Text(formatDistanceLabel(item.distanceKm), color = AppForest, fontWeight = FontWeight.Bold)
            }
            Text(apoi.services.sortedBy { it.name }.joinToString(" · ") { categoryLabel(it) }, color = AppMuted, style = MaterialTheme.typography.bodySmall)
            val presentation = ApoiAheadPresentationMapper.map(apoi, item.distanceKm)
            Text(presentation.availabilityLabel, style = MaterialTheme.typography.bodySmall)
            if (apoi.publication.status == PublicationStatus.PUBLISHED_WITH_WARNING) {
                Text("Informação com ressalva", color = AppWarning, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
            Text("Ver detalhe", color = AppForest, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DecisionExperienceScreen(decision: com.caminhos2027.v1.core.walking.WalkingDecisionContext, onBack: () -> Unit, onOpenApoi: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TopBar(title = "Opções", onBack = onBack)
        Text("Informação para decidir", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("A aplicação não escolhe por si. Mostra as consequências das opções a partir da posição atual.", color = AppMuted)
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Parar agora", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Não há distância adicional a percorrer.", color = AppMuted)
                HorizontalDivider()
                Text("Continuar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Ficam ${formatKm(decision.remainingToPlannedDestinationKm)} km até ao destino planeado.", color = AppMuted)
                Button(onClick = onOpenApoi, modifier = Modifier.fillMaxWidth()) { Text("VER APOIOS ANTES DE DECIDIR") }
            }
        }
    }
}

@Composable
private fun EmptyExperienceState(title: String, message: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(message, color = AppMuted)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onBack) { Text("Voltar") }
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RouteMapCanvas(
    modifier: Modifier,
    geometry: List<GeoPoint>,
    currentRouteKm: Double?,
    totalDistanceKm: Double,
    startRouteKm: Double?,
    destinationRouteKm: Double?
) {
    val points = remember(geometry) { geometry.filter { it.latitude.isFinite() && it.longitude.isFinite() } }
    Canvas(modifier = modifier.background(AppMap)) {
        val horizontalLines = 7
        val verticalLines = 5
        for (i in 1 until horizontalLines) {
            val y = size.height * i / horizontalLines
            drawLine(AppMapGrid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }
        for (i in 1 until verticalLines) {
            val x = size.width * i / verticalLines
            drawLine(AppMapGrid, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
        }
        if (points.size < 2) return@Canvas

        val minLat = points.minOf { it.latitude }
        val maxLat = points.maxOf { it.latitude }
        val minLon = points.minOf { it.longitude }
        val maxLon = points.maxOf { it.longitude }
        val latSpan = max(maxLat - minLat, 1e-9)
        val lonSpan = max(maxLon - minLon, 1e-9)
        val pad = 34f
        val scale = min((size.width - pad * 2) / lonSpan.toFloat(), (size.height - pad * 2) / latSpan.toFloat())
        val renderedWidth = lonSpan.toFloat() * scale
        val renderedHeight = latSpan.toFloat() * scale
        val offsetX = (size.width - renderedWidth) / 2f
        val offsetY = (size.height - renderedHeight) / 2f

        fun project(point: GeoPoint): Offset = Offset(
            offsetX + (point.longitude - minLon).toFloat() * scale,
            offsetY + (maxLat - point.latitude).toFloat() * scale
        )

        val path = Path().apply {
            moveTo(project(points.first()).x, project(points.first()).y)
            points.drop(1).forEach { point ->
                val p = project(point)
                lineTo(p.x, p.y)
            }
        }
        drawPath(path = path, color = Color.White, style = Stroke(width = 12f, cap = StrokeCap.Round))
        drawPath(path = path, color = AppForest, style = Stroke(width = 6f, cap = StrokeCap.Round))

        val currentRatio = ((currentRouteKm ?: 0.0) / totalDistanceKm.coerceAtLeast(0.001)).coerceIn(0.0, 1.0)
        val startRatio = ((startRouteKm ?: 0.0) / totalDistanceKm.coerceAtLeast(0.001)).coerceIn(0.0, 1.0)
        val destinationRatio = ((destinationRouteKm ?: totalDistanceKm) / totalDistanceKm.coerceAtLeast(0.001)).coerceIn(0.0, 1.0)
        val profileIndex = { ratio: Double -> (ratio * (points.lastIndex)).toInt().coerceIn(0, points.lastIndex) }

        val startPoint = project(points[profileIndex(startRatio)])
        val destinationPoint = project(points[profileIndex(destinationRatio)])
        val currentPoint = project(points[profileIndex(currentRatio)])
        drawCircle(AppForestSoft, radius = 13f, center = currentPoint)
        drawCircle(AppForest, radius = 8f, center = currentPoint)
        drawCircle(Color.White, radius = 6f, center = startPoint)
        drawCircle(AppForest, radius = 4f, center = startPoint)
        drawCircle(Color.White, radius = 6f, center = destinationPoint)
        drawCircle(AppForestDark, radius = 4f, center = destinationPoint)
    }
}

private fun gpsStateTitle(state: WalkingState): String = when (state.gpsState) {
    com.caminhos2027.v1.core.model.GpsState.NO_SIGNAL -> "GPS sem sinal"
    com.caminhos2027.v1.core.model.GpsState.ACQUIRING -> "A obter sinal GPS"
    com.caminhos2027.v1.core.model.GpsState.ON_ROUTE -> "GPS no percurso"
    com.caminhos2027.v1.core.model.GpsState.POSSIBLE_DEVIATION -> "Possível desvio"
    com.caminhos2027.v1.core.model.GpsState.PROBABLE_DEVIATION -> "Provável desvio"
}

private fun categoryLabel(category: ApoiCategory): String = when (category) {
    ApoiCategory.ALIMENTACAO -> "Alimentação"
    ApoiCategory.AGUA -> "Água"
    ApoiCategory.DESCANSO -> "Descanso"
    ApoiCategory.PERNOITA -> "Pernoita"
    ApoiCategory.DUCHES -> "Duches"
    ApoiCategory.CARREGAMENTO -> "Carregamento"
    ApoiCategory.TRANSPORTE -> "Transporte"
    ApoiCategory.EMERGENCIA -> "Emergência"
}

private fun formatKm(value: Double): String = String.format(Locale("pt", "PT"), "%.2f", value)

private fun formatDistanceLabel(value: Double): String =
    if (value < 1.0) String.format(Locale("pt", "PT"), "%.0f m", value * 1000.0)
    else String.format(Locale("pt", "PT"), "%.1f km", value)

private fun formatMeters(value: Double): String =
    if (value >= 1000.0) String.format(Locale("pt", "PT"), "%.1f km", value / 1000.0)
    else String.format(Locale("pt", "PT"), "%.0f m", value)
