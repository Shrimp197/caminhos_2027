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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.caminhos2027.v1.core.apoi.ApoiAhead
import com.caminhos2027.v1.core.apoi.ApoiBrowserState
import com.caminhos2027.v1.core.data.AndroidRouteOption
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.walking.WalkingDecisionContext
import com.caminhos2027.v1.core.walking.WalkingState
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

internal enum class WalkingSurface { ACTIVE, PREPARATION, APOI_BROWSER, APOI_DETAIL, DECISION }

private val Forest = Color(0xFF0E6546)
private val ForestSoft = Color(0xFFE6F2EB)
private val Sand = Color(0xFFF6F3EC)
private val MapBg = Color(0xFFE9F0E7)
private val Grid = Color(0xFFD5DED3)
private val Muted = Color(0xFF68736D)
private val Warning = Color(0xFF9A5A00)
private val WarningSoft = Color(0xFFFFF1D9)

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
    onQaAdvance: () -> Unit,
    onQaToggleGps: (Boolean) -> Unit,
    onQaDeviation: () -> Unit,
    onBackToWalking: () -> Unit,
    onBackToApoiBrowser: () -> Unit
) {
    Surface(Modifier.fillMaxSize(), color = Sand) {
        when (surface) {
            WalkingSurface.PREPARATION -> PreparationScreen(route, routeOptions, selectedRouteId, onSelectRoute, onConfirmPreparation, onBackToWalking)
            WalkingSurface.APOI_BROWSER -> ApoiBrowserSurface(appState.apoiBrowser, onApoiSearchChanged, onApoiFilterToggled, onApoiScopeChanged, onApoiSelected, onBackToWalking)
            WalkingSurface.APOI_DETAIL -> {
                val selected = appState.apoiBrowser?.selected
                if (selected == null) EmptyState("APOI não selecionado", "Escolha um apoio da lista.", onBackToApoiBrowser)
                else ApoiDetailScreenV1(selected, onBackToApoiBrowser)
            }
            WalkingSurface.DECISION -> {
                val decision = appState.decision
                if (decision == null) EmptyState("Opções indisponíveis", "Não foi possível calcular as opções para a posição atual.", onBackToWalking)
                else DecisionScreen(decision, onBackToWalking, onOpenApoi)
            }
            WalkingSurface.ACTIVE -> when {
                state != null -> ActiveWalkingScreen(state, route, routeOptions, onStop, onOpenApoi, onOpenDecision, onQaAdvance, onQaToggleGps, onQaDeviation)
                preparedWalk != null -> PreparedWalkScreen(preparedWalk, route, startRequested, pendingStartDistanceMeters, onStart, onCancelPendingStart)
                else -> LandingScreen(route, routeOptions, onPrepare)
            }
        }
    }
}

@Composable
private fun LandingScreen(route: Route, options: List<AndroidRouteOption>, onPrepare: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Menu, null)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("CAMINHOS", fontWeight = FontWeight.Bold, color = Forest)
                Text("DO PEREGRINO", fontWeight = FontWeight.Bold, color = Forest)
            }
            Icon(Icons.Filled.DirectionsWalk, null, tint = Forest, modifier = Modifier.size(28.dp))
        }
        Text("Prepare a sua caminhada", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Defina livremente o início e o destino. As etapas oficiais são referência; a caminhada não fica presa a elas.", color = Muted)
        Card(Modifier.fillMaxWidth(), RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Forest)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(route.officialName, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${fmtKm(route.totalDistanceKm)} km · traçado local", color = Color.White.copy(alpha = .88f))
                Text("O plano guardado não inicia a caminhada. O início real depende de uma posição GPS válida no percurso.", color = Color.White.copy(alpha = .88f))
                Button(onClick = onPrepare, Modifier.fillMaxWidth()) { Text("PREPARAR") }
            }
        }
        Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Fluxo da caminhada", fontWeight = FontWeight.Bold)
                Text("Preparar → guardar → iniciar → acompanhar posição e progresso → consultar apoios → decidir.", color = Muted)
                Text("Orientar sem retirar autonomia: a aplicação apresenta informação e opções, não escolhe pelo peregrino.", color = Forest, fontWeight = FontWeight.SemiBold)
            }
        }
        if (options.any { it.testOnly }) {
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = WarningSoft)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("SR e HF · ambiente de teste", fontWeight = FontWeight.Bold, color = Warning)
                    Text("Percursos de teste com dados fictícios isolados, destinados a exercitar a experiência completa sem depender de GPS físico.")
                }
            }
        }
    }
}

@Composable
private fun PreparationScreen(route: Route, options: List<AndroidRouteOption>, selectedRouteId: String, onSelectRoute: (String) -> Unit, onConfirm: (Double, Double) -> Unit, onBack: () -> Unit) {
    var start by remember(selectedRouteId) { mutableStateOf("0.00") }
    var destination by remember(selectedRouteId) { mutableStateOf(fmtKm(route.totalDistanceKm)) }
    var error by remember(selectedRouteId) { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TopBar("Preparação", onBack)
        Text("1 · PREPARAÇÃO", color = Forest, fontWeight = FontWeight.Bold)
        Text("Escolha o percurso", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        options.forEach { option ->
            val selected = option.id == selectedRouteId
            Card(Modifier.fillMaxWidth().clickable(enabled = !selected) { onSelectRoute(option.id) }, RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (selected) ForestSoft else Color.White)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(42.dp).clip(CircleShape).background(if (selected) Forest else Sand), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Navigation, null, tint = if (selected) Color.White else Forest) }
                    Column(Modifier.weight(1f)) {
                        Text(option.title, fontWeight = FontWeight.Bold)
                        Text(option.description, color = Muted, style = MaterialTheme.typography.bodySmall)
                        if (option.testOnly) Text("AMBIENTE DE TESTE", color = Warning, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        Card(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Início e fim", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Os valores são km no percurso. As etapas oficiais não são recalculadas a partir da execução.", color = Muted)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(start, { start = it; error = null }, label = { Text("Início (km)") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(destination, { destination = it; error = null }, label = { Text("Destino (km)") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(onClick = {
                    val s = start.replace(',', '.').toDoubleOrNull()
                    val d = destination.replace(',', '.').toDoubleOrNull()
                    error = when {
                        s == null || d == null -> "Indique números válidos."
                        s < 0.0 || d < 0.0 -> "Os valores não podem ser negativos."
                        s > route.totalDistanceKm || d > route.totalDistanceKm -> "O valor ultrapassa o percurso."
                        s >= d -> "O destino tem de ficar depois do início."
                        else -> null
                    }
                    if (error == null) onConfirm(requireNotNull(s), requireNotNull(d))
                }, Modifier.fillMaxWidth()) { Text("GUARDAR PLANO") }
            }
        }
        OutlinedButton(onBack, Modifier.fillMaxWidth()) { Text("Cancelar") }
    }
}

@Composable
private fun PreparedWalkScreen(walk: Walk, route: Route, startRequested: Boolean, pendingDistance: Double?, onStart: () -> Unit, onCancel: () -> Unit) {
    val test = route.id == "sr-test" || route.id == "hf-test"
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center) {
        Card(Modifier.fillMaxWidth(), RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Plano guardado", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(route.officialName, color = Forest, fontWeight = FontWeight.Bold)
                Text("${fmtKm(walk.plannedStartKm ?: 0.0)} → ${fmtKm(walk.plannedDestinationKm ?: 0.0)} km", style = MaterialTheme.typography.titleLarge)
                Text("Guardar o plano não inicia a caminhada.", color = Muted)
                if (startRequested) {
                    HorizontalDivider()
                    Text(if (test) "A iniciar pelo percurso de teste…" else if ((pendingDistance ?: 0.0) > 0.0) "Está fora do percurso" else "A procurar GPS…", fontWeight = FontWeight.Bold, color = if ((pendingDistance ?: 0.0) > 0.0) Warning else Forest)
                    pendingDistance?.let { Text("Distância ao percurso: ${fmtMeters(it)}", color = Muted) }
                    Text(if (test) "A simulação percorre o mesmo fluxo de posição, projeção e estado usado pelo GPS real." else "A caminhada só fica ativa quando existir uma posição válida no percurso.", color = Muted)
                    Button(onCancel, Modifier.fillMaxWidth()) { Text("CANCELAR INÍCIO") }
                } else {
                    Button(onStart, Modifier.fillMaxWidth()) { Text("INICIAR CAMINHADA") }
                }
            }
        }
    }
}

@Composable
private fun ActiveWalkingScreen(state: WalkingState, route: Route, options: List<AndroidRouteOption>, onStop: () -> Unit, onOpenApoi: () -> Unit, onOpenDecision: () -> Unit, onQaAdvance: () -> Unit, onQaToggleGps: (Boolean) -> Unit, onQaDeviation: () -> Unit) {
    var expanded by remember { mutableStateOf(true) }
    val current = state.routePosition?.routeKm ?: state.progress?.currentRouteKm ?: 0.0
    val remaining = state.progress?.remainingKm ?: ((state.walk.plannedDestinationKm ?: route.totalDistanceKm) - current).coerceAtLeast(0.0)
    val test = options.firstOrNull { it.id == route.id }?.testOnly == true
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Menu, null)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(route.officialName, fontWeight = FontWeight.Bold)
                    Text(if (state.isOffline) "Offline · dados locais" else gpsTitle(state.gpsState), color = Muted, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onStop) { Icon(Icons.Filled.Close, "Terminar caminhada") }
            }
            Box(Modifier.fillMaxWidth().weight(1f)) {
                RouteMapCanvas(Modifier.fillMaxSize(), route.geometry.points, current, route.totalDistanceKm)
                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(fmtKm(state.progress?.walkedKm ?: current), "Percorridos")
                    StatCard(fmtKm(remaining), "Para o fim")
                }
                Card(Modifier.align(Alignment.TopEnd).padding(top = 92.dp, end = 14.dp), CircleShape, colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Icon(Icons.Filled.LocationOn, null, tint = Forest, modifier = Modifier.padding(12.dp).size(24.dp))
                }
                Card(Modifier.align(Alignment.BottomCenter).padding(14.dp), RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Place, null, tint = Forest, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Próximo APOI", color = Muted, style = MaterialTheme.typography.labelLarge)
                            Text(state.nextApoi?.name ?: "Nenhum APOI publicado", fontWeight = FontWeight.Bold)
                        }
                        Text(state.nextApoiDistanceKm?.let(::fmtDistance) ?: "—", color = Forest, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Card(Modifier.align(Alignment.BottomCenter).fillMaxWidth(), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Box(Modifier.fillMaxWidth().clickable { expanded = !expanded }, contentAlignment = Alignment.Center) { Box(Modifier.size(44.dp, 5.dp).clip(RoundedCornerShape(8.dp)).background(Grid)) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Caminhada", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${fmtKm(current)} km percorridos · ${fmtKm(remaining)} km para o destino", color = Muted)
                    }
                    Text(if (expanded) "⌄" else "⌃", style = MaterialTheme.typography.titleLarge, color = Forest)
                }
                LinearProgressIndicator(progress = (state.progress?.progressRatio ?: 0.0).toFloat().coerceIn(0f, 1f), Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(10.dp)), color = Forest, trackColor = ForestSoft)
                if (expanded) {
                    state.progress?.stageName?.takeIf(String::isNotBlank)?.let { Text("Etapa atual · $it", color = Forest, fontWeight = FontWeight.Bold) }
                    Text("${fmtKm(state.progress?.walkedKm ?: 0.0)} km feitos · ${fmtKm(remaining)} km restantes", color = Muted)
                    state.nextApoi?.let { Text("Próximo · ${it.name} · ${state.nextApoiDistanceKm?.let(::fmtDistance) ?: "—"}", fontWeight = FontWeight.SemiBold) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onOpenApoi, Modifier.weight(1f)) { Text("VER APOIOS") }
                        OutlinedButton(onOpenDecision, Modifier.weight(1f)) { Text("OPÇÕES") }
                    }
                    Text("A informação ajuda a decidir; a aplicação não decide pelo peregrino.", style = MaterialTheme.typography.bodySmall, color = Muted)
                    if (test) {
                        HorizontalDivider()
                        Text("QA · controlo do percurso", fontWeight = FontWeight.Bold, color = Warning)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(onQaAdvance, Modifier.weight(1f)) { Text("AVANÇAR") }
                            OutlinedButton({ onQaToggleGps(false) }, Modifier.weight(1f)) { Text("PERDER GPS") }
                            OutlinedButton({ onQaToggleGps(true) }, Modifier.weight(1f)) { Text("RECUPERAR") }
                        }
                        OutlinedButton(onQaDeviation, Modifier.fillMaxWidth()) { Text("SIMULAR DESVIO") }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String) {
    Card(Modifier.width(130.dp), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(12.dp)) { Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(label, color = Muted, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun ApoiBrowserSurface(state: ApoiBrowserState?, onSearch: (String) -> Unit, onFilter: (ApoiCategory) -> Unit, onScope: (Double?) -> Unit, onSelect: (Apoi) -> Unit, onBack: () -> Unit) {
    if (state == null) {
        EmptyState("Apoios indisponíveis", "A consulta precisa de uma posição de percurso válida.", onBack)
        return
    }
    val tenKm = state.query.maxDistanceKm == 10.0
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        TopBar(if (tenKm) "Próximos 10 km" else "Apoios", onBack)
        Text(if (tenKm) "Apoios à frente, ordenados pela distância no Caminho." else "Consulte e filtre os apoios disponíveis no contexto atual.", color = Muted)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            FilterChip(tenKm, { onScope(10.0) }, label = { Text("Próximos 10 km") })
            FilterChip(!tenKm, { onScope(null) }, label = { Text("Todos") })
        }
        OutlinedTextField(state.query.text, onSearch, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Procurar") })
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf(ApoiCategory.AGUA, ApoiCategory.ALIMENTACAO, ApoiCategory.PERNOITA, ApoiCategory.DESCANSO, ApoiCategory.DUCHES, ApoiCategory.CARREGAMENTO, ApoiCategory.TRANSPORTE, ApoiCategory.EMERGENCIA).forEach { c ->
                FilterChip(c in state.query.filter.services, { onFilter(c) }, label = { Text(catLabel(c)) })
            }
        }
        if (state.results.isEmpty()) {
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (tenKm) "Sem APOI publicado nos próximos 10 km" else "Sem APOI para estes critérios", fontWeight = FontWeight.Bold)
                    Text("A ausência de resultado não afirma que não exista apoio no terreno; apenas não há registos elegíveis nesta seleção.", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            state.results.forEach { item -> ApoiCard(item, onSelect) }
        }
    }
}

@Composable
private fun ApoiCard(item: ApoiAhead, onSelect: (Apoi) -> Unit) {
    val apoi = item.apoi
    val presentation = ApoiAheadPresentationMapper.map(apoi, item.distanceKm)
    Card(
        Modifier.fillMaxWidth().clickable { onSelect(apoi) },
        RoundedCornerShape(19.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Place, null, tint = Forest, modifier = Modifier.size(23.dp))
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text(presentation.categoryLabel, color = Forest, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    Text(presentation.name, fontWeight = FontWeight.Bold)
                }
                Text(presentation.distanceLabel, color = Forest, fontWeight = FontWeight.Bold)
            }
            Text(apoi.services.sortedBy { it.name }.joinToString(" · ") { catLabel(it) }, color = Muted, style = MaterialTheme.typography.bodySmall)
            Text(presentation.availabilityLabel, style = MaterialTheme.typography.bodySmall)
            presentation.warningLabel?.let { Text(it, color = Warning, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall) }
            Text("Ver detalhe", color = Forest, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DecisionScreen(context: WalkingDecisionContext, onBack: () -> Unit, onOpenApoi: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TopBar("Opções", onBack)
        Text("Informação para decidir", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("A aplicação mostra consequências; não escolhe por si.", color = Muted)
        Card(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Parar agora", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Sem distância adicional no caminho.", color = Muted)
                HorizontalDivider()
                Text("Continuar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${fmtKm(context.remainingToPlannedDestinationKm)} km até ao destino planeado.", color = Muted)
                if (context.continueWalking.relevantApoi.isNotEmpty()) Text("Existem ${context.continueWalking.relevantApoi.size} APOI elegíveis no restante do percurso planeado.", color = Forest, fontWeight = FontWeight.SemiBold)
                Button(onOpenApoi, Modifier.fillMaxWidth()) { Text("VER APOIOS") }
            }
        }
    }
}

@Composable private fun EmptyState(title: String, message: String, onBack: () -> Unit) { Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center) { Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text(message, color = Muted); Spacer(Modifier.height(14.dp)); OutlinedButton(onBack) { Text("Voltar") } } }
@Composable private fun TopBar(title: String, onBack: () -> Unit) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { IconButton(onBack) { Icon(Icons.Filled.ArrowBack, "Voltar") }; Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) } }

@Composable
private fun RouteMapCanvas(modifier: Modifier, geometry: List<GeoPoint>, currentKm: Double, totalKm: Double) {
    val points = remember(geometry) { geometry.filter { it.latitude.isFinite() && it.longitude.isFinite() } }
    Canvas(modifier.background(MapBg)) {
        repeat(6) { i -> drawLine(Grid, Offset(0f, size.height * (i + 1) / 7f), Offset(size.width, size.height * (i + 1) / 7f), 1f) }
        repeat(4) { i -> drawLine(Grid, Offset(size.width * (i + 1) / 5f, 0f), Offset(size.width * (i + 1) / 5f, size.height), 1f) }
        if (points.size < 2) return@Canvas
        val minLat = points.minOf { it.latitude }
        val maxLat = points.maxOf { it.latitude }
        val minLon = points.minOf { it.longitude }
        val maxLon = points.maxOf { it.longitude }
        val latSpan = max(maxLat - minLat, 1e-9)
        val lonSpan = max(maxLon - minLon, 1e-9)
        val pad = 30f
        val scale = min((size.width - 2 * pad) / lonSpan.toFloat(), (size.height - 2 * pad) / latSpan.toFloat())
        val ox = (size.width - lonSpan.toFloat() * scale) / 2f
        val oy = (size.height - latSpan.toFloat() * scale) / 2f
        fun project(g: GeoPoint) = Offset(ox + (g.longitude - minLon).toFloat() * scale, oy + (maxLat - g.latitude).toFloat() * scale)
        val path = Path().apply {
            moveTo(project(points.first()).x, project(points.first()).y)
            points.drop(1).forEach { point -> val p = project(point); lineTo(p.x, p.y) }
        }
        drawPath(path, Color.White, style = Stroke(12f, cap = StrokeCap.Round))
        drawPath(path, Forest, style = Stroke(6f, cap = StrokeCap.Round))
        val ratio = (currentKm / totalKm.coerceAtLeast(0.001)).coerceIn(0.0, 1.0)
        val marker = project(points[(ratio * points.lastIndex).toInt().coerceIn(0, points.lastIndex)])
        drawCircle(ForestSoft, 14f, marker)
        drawCircle(Forest, 8f, marker)
    }
}

private fun gpsTitle(state: GpsState) = when (state) {
    GpsState.NO_SIGNAL -> "GPS sem sinal"
    GpsState.ACQUIRING -> "A obter sinal GPS"
    GpsState.ON_ROUTE -> "GPS no percurso"
    GpsState.POSSIBLE_DEVIATION -> "Possível desvio"
    GpsState.PROBABLE_DEVIATION -> "Provável desvio"
}

private fun catLabel(category: ApoiCategory) = when (category) {
    ApoiCategory.AGUA -> "Água"
    ApoiCategory.ALIMENTACAO -> "Alimentação"
    ApoiCategory.PERNOITA -> "Pernoita"
    ApoiCategory.DESCANSO -> "Descanso"
    ApoiCategory.DUCHES -> "Duches"
    ApoiCategory.CARREGAMENTO -> "Carregamento"
    ApoiCategory.TRANSPORTE -> "Transporte"
    ApoiCategory.EMERGENCIA -> "Emergência"
}

private fun fmtKm(value: Double) = String.format(Locale("pt", "PT"), "%.2f", value)
private fun fmtDistance(value: Double) = if (value < 1.0) String.format(Locale("pt", "PT"), "%.0f m", value * 1000.0) else String.format(Locale("pt", "PT"), "%.1f km", value)
private fun fmtMeters(value: Double) = if (value >= 1000.0) String.format(Locale("pt", "PT"), "%.1f km", value / 1000.0) else String.format(Locale("pt", "PT"), "%.0f m", value)
