package com.caminhos2027.v1.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.caminhos2027.R
import com.caminhos2027.v1.core.data.AndroidRouteOption
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.AudioMode
import com.caminhos2027.v1.core.model.MapOrientation
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.WalkingPreparationConfig

private val BrandBlue = Color(0xFF164B63)
private val BrandGreen = Color(0xFF1B9B50)
private val BrandGold = Color(0xFFC68A12)
private val Surface = Color(0xFFF5F6F4)
private val CardBorder = Color(0xFFE0E3DF)
private val Muted = Color(0xFF6C7478)
private val SoftGreen = Color(0xFFE9F6EE)
private val SoftBlue = Color(0xFFEAF2F7)
private val TestRed = Color(0xFFC63B3B)

@Composable
internal fun PreparationExperienceV1(
    route: Route,
    routeOptions: List<AndroidRouteOption>,
    selectedRouteId: String,
    onSelectRoute: (String) -> Unit,
    onConfirm: (Double, Double, WalkingPreparationConfig) -> Unit,
    onBack: () -> Unit
) {
    var sub by rememberSaveable(selectedRouteId) { mutableStateOf<PreparationSubscreen?>(null) }
    var config by remember(selectedRouteId) { mutableStateOf(WalkingPreparationConfig()) }
    val stages = remember(route.id, route.totalDistanceKm, route.stages) {
        route.stages.ifEmpty {
            listOf(
                Stage(
                    id = "${route.id}-test-stage",
                    routeId = route.id,
                    number = 1,
                    name = route.officialName,
                    startRouteKm = 0.0,
                    endRouteKm = route.totalDistanceKm,
                    distanceKm = route.totalDistanceKm,
                    startName = "Início",
                    endName = "Fim",
                    source = "QA synthetic test stage"
                )
            )
        }
    }
    var selectedStageIds by remember(selectedRouteId, stages) { mutableStateOf(stages.take(1).map(Stage::id)) }
    var startKm by remember(selectedRouteId, stages) { mutableStateOf(stages.first().startRouteKm) }
    var destinationKm by remember(selectedRouteId, stages) { mutableStateOf(stages.first().endRouteKm) }
    val notes = remember(selectedRouteId) { mutableStateListOf<String>() }

    when (sub) {
        null -> PreparationHome(
            route = route,
            routeOptions = routeOptions,
            selectedRouteId = selectedRouteId,
            stages = stages,
            startKm = startKm,
            destinationKm = destinationKm,
            selectedStageIds = selectedStageIds,
            config = config,
            onBack = onBack,
            onOpen = { sub = it },
            onStart = { onConfirm(startKm, destinationKm, config) }
        )
        PreparationSubscreen.ROUTE -> RouteSubscreen(routeOptions, selectedRouteId, { sub = null }) { routeId ->
            onSelectRoute(routeId)
            sub = null
        }
        PreparationSubscreen.STAGE -> StageSubscreen(stages, selectedStageIds, { sub = null }) { ids, start, end ->
            selectedStageIds = ids
            startKm = start
            destinationKm = end
            sub = null
        }
        PreparationSubscreen.AUDIO -> ChoiceSubscreen(
            title = "Áudio",
            icon = Icons.Filled.Headphones,
            values = listOf("Normal" to AudioMode.NORMAL, "Imersivo" to AudioMode.IMMERSIVE, "Silenciado" to AudioMode.SILENT),
            selected = config.audioMode,
            onBack = { sub = null }
        ) { config = config.copy(audioMode = it); sub = null }
        PreparationSubscreen.ORIENTATION -> ChoiceSubscreen(
            title = "Orientação do mapa",
            icon = Icons.Filled.Map,
            values = listOf("Norte" to MapOrientation.NORTH, "Direção da caminhada" to MapOrientation.WALK_DIRECTION),
            selected = config.mapOrientation,
            onBack = { sub = null }
        ) { config = config.copy(mapOrientation = it); sub = null }
        PreparationSubscreen.BREAKS -> BreaksSubscreen(config, { sub = null }) { config = it; sub = null }
        PreparationSubscreen.APOIS -> ApoiSubscreen(config, { sub = null }) { config = config.copy(visibleApoiCategories = it); sub = null }
        PreparationSubscreen.NOTES -> NotesSubscreen(notes, { sub = null }) { note -> if (note.isNotBlank()) notes += note.trim() }
    }
}

private enum class PreparationSubscreen { ROUTE, STAGE, AUDIO, ORIENTATION, BREAKS, APOIS, NOTES }

@Composable
private fun PreparationHome(
    route: Route,
    routeOptions: List<AndroidRouteOption>,
    selectedRouteId: String,
    stages: List<Stage>,
    startKm: Double,
    destinationKm: Double,
    selectedStageIds: List<String>,
    config: WalkingPreparationConfig,
    onBack: () -> Unit,
    onOpen: (PreparationSubscreen) -> Unit,
    onStart: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Surface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = BrandBlue) }
            Image(
                painter = painterResource(R.drawable.ic_launcher_source),
                contentDescription = "Caminhos do Peregrino",
                modifier = Modifier.size(34.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(start = 9.dp)) {
                Text("CAMINHOS", color = BrandBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                Text("DO PEREGRINO", color = BrandBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("Prepare a sua caminhada", color = BrandBlue, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        RouteSelectionCard(
            route = route,
            isTest = routeOptions.firstOrNull { it.id == selectedRouteId }?.testOnly == true,
            onClick = { onOpen(PreparationSubscreen.ROUTE) }
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PrepTile(icon = Icons.Filled.LocationOn, label = "Início e fim", value = selectedStageLabel(stages, selectedStageIds), modifier = Modifier.fillMaxWidth()) {
                onOpen(PreparationSubscreen.STAGE)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PrepTile(icon = Icons.Filled.Headphones, label = "Áudio", value = audioLabel(config.audioMode), modifier = Modifier.weight(1f)) { onOpen(PreparationSubscreen.AUDIO) }
                PrepTile(icon = Icons.Filled.Straighten, label = "Orientação", value = orientationLabel(config.mapOrientation), modifier = Modifier.weight(1f)) { onOpen(PreparationSubscreen.ORIENTATION) }
                PrepTile(icon = Icons.Filled.PauseCircle, label = "Pausas", value = breakLabel(config), modifier = Modifier.weight(1f)) { onOpen(PreparationSubscreen.BREAKS) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PrepTile(icon = Icons.Filled.Place, label = "Apoios", value = apoiLabel(config.visibleApoiCategories), modifier = Modifier.weight(1f)) { onOpen(PreparationSubscreen.APOIS) }
                PrepTile(icon = Icons.Filled.Notes, label = "Notas", value = "Criar ou consultar", modifier = Modifier.weight(1f)) { onOpen(PreparationSubscreen.NOTES) }
            }
        }

        if (selectedStageIds.isNotEmpty()) {
            Text(
                "${fmt(destinationKm - startKm)} km seleccionados",
                color = Muted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BrandGreen)
        ) {
            Text("▶  INICIAR CAMINHADA", fontWeight = FontWeight.ExtraBold)
        }

        if (routeOptions.firstOrNull { it.id == selectedRouteId }?.testOnly == true) {
            Text("TESTE · percurso controlado", color = TestRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun RouteSelectionCard(route: Route, isTest: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, CardBorder),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(210.dp)) {
            if (!isTest) {
                Image(
                    painter = painterResource(R.drawable.caminho_centenario_hero),
                    contentDescription = "Imagem do Caminho do Centenário",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF7FA6B8), Color(0xFF35634B)))))
            }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xB9000000)))))
            Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text(route.officialName, color = Color.White, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
                Text(routeDisplay(route), color = Color.White, fontWeight = FontWeight.Bold)
                Text(if (isTest) "Percurso de teste" else "Percurso em 8 etapas", color = Color.White.copy(alpha = .92f))
            }
            Button(
                onClick = onClick,
                modifier = Modifier.align(Alignment.BottomEnd).padding(14.dp),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BrandGreen)
            ) { Text("PREPARAR", fontWeight = FontWeight.ExtraBold) }
        }
    }
}

@Composable
private fun PrepTile(icon: ImageVector, label: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardBorder),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(27.dp), tint = BrandBlue)
            Spacer(Modifier.height(6.dp))
            Text(label, color = BrandBlue, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
            Text(value, color = Muted, style = MaterialTheme.typography.labelSmall, maxLines = 2, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun RouteSubscreen(options: List<AndroidRouteOption>, selected: String, onBack: () -> Unit, onApply: (String) -> Unit) = SecondaryScaffold("Percurso", onBack) {
    options.forEach { option ->
        Card(
            Modifier.fillMaxWidth().clickable { onApply(option.id) },
            RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CardBorder),
            colors = CardDefaults.cardColors(containerColor = if (option.id == selected) SoftGreen else Color.White)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(option.title, color = BrandBlue, fontWeight = FontWeight.ExtraBold)
                Text(option.description, color = Muted)
                if (option.testOnly) Text("TESTE", color = TestRed, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun StageSubscreen(stages: List<Stage>, selectedIds: List<String>, onBack: () -> Unit, onApply: (List<String>, Double, Double) -> Unit) {
    var selected by remember(selectedIds) { mutableStateOf(selectedIds.toSet()) }
    SecondaryScaffold("Início e fim", onBack) {
        Text("Escolha as etapas oficiais que pretende realizar. O início e o fim correspondem ao intervalo das etapas seleccionadas.", color = Muted)
        stages.forEach { stage ->
            val chosen = stage.id in selected
            Card(
                Modifier.fillMaxWidth().clickable { selected = if (chosen) selected - stage.id else selected + stage.id },
                RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (chosen) SoftBlue else Color.White),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(38.dp).clip(CircleShape).background(if (chosen) BrandBlue else CardBorder), contentAlignment = Alignment.Center) {
                        Text(stage.number.toString(), color = if (chosen) Color.White else BrandBlue, fontWeight = FontWeight.ExtraBold)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(stage.name, color = BrandBlue, fontWeight = FontWeight.Bold)
                        Text("${fmt(stage.distanceKm)} km · aprox.", color = Muted)
                    }
                }
            }
        }
        val chosen = stages.filter { it.id in selected }.sortedBy { it.number }
        Button(onClick = { if (chosen.isNotEmpty()) onApply(chosen.map(Stage::id), chosen.first().startRouteKm, chosen.last().endRouteKm) }, enabled = chosen.isNotEmpty(), modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BrandGreen)) {
            Text("APLICAR")
        }
    }
}

@Composable
private fun <T> ChoiceSubscreen(title: String, icon: ImageVector, values: List<Pair<String, T>>, selected: T, onBack: () -> Unit, onApply: (T) -> Unit) = SecondaryScaffold(title, onBack) {
    values.forEach { (label, value) ->
        val chosen = value == selected
        Card(Modifier.fillMaxWidth().clickable { onApply(value) }, RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (chosen) BrandBlue else Color.White), border = BorderStroke(1.dp, CardBorder)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = if (chosen) Color.White else BrandBlue)
                Text(label, color = if (chosen) Color.White else BrandBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BreaksSubscreen(config: WalkingPreparationConfig, onBack: () -> Unit, onApply: (WalkingPreparationConfig) -> Unit) {
    var intelligent by remember(config) { mutableStateOf(config.intelligentBreaksEnabled) }
    var time by remember(config) { mutableStateOf(config.customBreakTimeMinutes?.toString() ?: "") }
    var distance by remember(config) { mutableStateOf(config.customBreakDistanceKm?.toString() ?: "") }
    SecondaryScaffold("Pausas", onBack) {
        Text("As pausas inteligentes consideram dificuldade, distância e APOIs disponíveis. As personalizadas usam o intervalo escolhido por si.", color = Muted)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Pausas inteligentes", color = BrandBlue, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Switch(checked = intelligent, onCheckedChange = { intelligent = it })
        }
        HorizontalDivider()
        OutlinedTextField(time, { time = it }, label = { Text("Parar a cada X minutos") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(distance, { distance = it }, label = { Text("Parar a cada X km") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Button(onClick = { onApply(config.copy(intelligentBreaksEnabled = intelligent, customBreakTimeMinutes = time.toIntOrNull()?.takeIf { it > 0 }, customBreakDistanceKm = distance.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 })) }, modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BrandGreen)) {
            Text("APLICAR PAUSAS")
        }
    }
}

@Composable
private fun ApoiSubscreen(config: WalkingPreparationConfig, onBack: () -> Unit, onApply: (Set<ApoiCategory>) -> Unit) {
    var selected by remember(config) { mutableStateOf(config.visibleApoiCategories) }
    val categories = listOf(ApoiCategory.ALIMENTACAO, ApoiCategory.AGUA, ApoiCategory.DESCANSO, ApoiCategory.PERNOITA, ApoiCategory.DUCHES, ApoiCategory.CARREGAMENTO, ApoiCategory.TRANSPORTE, ApoiCategory.EMERGENCIA)
    SecondaryScaffold("Apoios no mapa", onBack) {
        Text("Escolha os tipos de apoio que pretende ver durante a caminhada.", color = Muted)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { category ->
                FilterChip(selected = category in selected, onClick = { selected = if (category in selected) selected - category else selected + category }, label = { Text(categoryLabel(category)) })
            }
        }
        Button(onClick = { onApply(selected) }, modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BrandGreen)) { Text("APLICAR APOIOS") }
    }
}

@Composable
private fun NotesSubscreen(notes: List<String>, onBack: () -> Unit, onCreate: (String) -> Unit) {
    var draft by rememberSaveable { mutableStateOf("") }
    SecondaryScaffold("Notas", onBack) {
        Text("Crie ou consulte notas da caminhada.", color = Muted)
        OutlinedTextField(draft, { draft = it }, label = { Text("Nova nota") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
        Button(onClick = { onCreate(draft); draft = "" }, enabled = draft.isNotBlank(), modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BrandGreen)) { Text("GUARDAR NOTA") }
        notes.asReversed().forEach { note ->
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Text(note, Modifier.padding(14.dp), color = BrandBlue) }
        }
    }
}

@Composable
private fun SecondaryScaffold(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().background(Surface).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Voltar", tint = BrandBlue) }
            Text(title, color = BrandBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
        }
        content()
        Spacer(Modifier.height(18.dp))
    }
}

private fun routeDisplay(route: Route) = if (route.id == "caminho-do-centenario") "212 km · Porto → Fátima" else route.officialName
private fun selectedStageLabel(stages: List<Stage>, ids: List<String>) = stages.filter { it.id in ids }.sortedBy { it.number }.let { chosen -> when { chosen.isEmpty() -> "Escolha uma etapa"; chosen.size == 1 -> "Etapa ${chosen.first().number} · ${chosen.first().startName} → ${chosen.first().endName}"; else -> "${chosen.size} etapas seleccionadas" } }
private fun fmt(v: Double) = String.format(java.util.Locale.US, "%.0f", v.coerceAtLeast(0.0))
private fun audioLabel(v: AudioMode) = when (v) { AudioMode.NORMAL -> "Normal"; AudioMode.IMMERSIVE -> "Imersivo"; AudioMode.SILENT -> "Silenciado" }
private fun orientationLabel(v: MapOrientation) = when (v) { MapOrientation.NORTH -> "Norte"; MapOrientation.WALK_DIRECTION -> "Direção da caminhada" }
private fun breakLabel(c: WalkingPreparationConfig) = when { c.intelligentBreaksEnabled && (c.customBreakTimeMinutes != null || c.customBreakDistanceKm != null) -> "Inteligentes + Personalizadas"; c.intelligentBreaksEnabled -> "Inteligentes"; c.customBreakTimeMinutes != null || c.customBreakDistanceKm != null -> "Personalizadas"; else -> "Sem pausas" }
private fun apoiLabel(c: Set<ApoiCategory>) = if (c.isEmpty()) "Escolher tipos" else c.sortedBy(ApoiCategory::name).take(2).joinToString(" · ") { categoryLabel(it) } + if (c.size > 2) " +${c.size - 2}" else ""
private fun categoryLabel(c: ApoiCategory) = when (c) { ApoiCategory.AGUA -> "Água"; ApoiCategory.ALIMENTACAO -> "Alimentação"; ApoiCategory.PERNOITA -> "Pernoita"; ApoiCategory.DESCANSO -> "Descanso"; ApoiCategory.DUCHES -> "Duches"; ApoiCategory.CARREGAMENTO -> "Carregamento"; ApoiCategory.TRANSPORTE -> "Transporte"; ApoiCategory.EMERGENCIA -> "Emergência" }
