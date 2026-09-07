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
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Route
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
import androidx.compose.ui.unit.dp
import com.caminhos2027.R
import com.caminhos2027.v1.core.data.AndroidRouteOption
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.AudioMode
import com.caminhos2027.v1.core.model.MapOrientation
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.Stage
import com.caminhos2027.v1.core.model.WalkingPreparationConfig

private val Blue = Color(0xFF194A73)
private val BlueSoft = Color(0xFFEAF3FA)
private val Page = Color(0xFFF6F9FB)
private val TextMuted = Color(0xFF718096)
private val Border = Color(0xFFD9E6F0)
private val Danger = Color(0xFFD72F3F)

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
            listOf(Stage("${route.id}-test-stage", route.id, 1, route.officialName, 0.0, route.totalDistanceKm, route.totalDistanceKm, "Início", "Fim", "QA synthetic test stage"))
        }
    }
    var selectedStageIds by remember(selectedRouteId, stages) { mutableStateOf(stages.take(1).map(Stage::id)) }
    var startKm by remember(selectedRouteId, stages) { mutableStateOf(stages.first().startRouteKm) }
    var destinationKm by remember(selectedRouteId, stages) { mutableStateOf(stages.first().endRouteKm) }
    val notes = remember(selectedRouteId) { mutableStateListOf<String>() }

    when (sub) {
        null -> PreparationHome(route, routeOptions, selectedRouteId, config, stages, startKm, destinationKm, selectedStageIds, onBack, { sub = it }) { onConfirm(startKm, destinationKm, config) }
        PreparationSubscreen.ROUTE -> RouteSubscreen(routeOptions, selectedRouteId, { sub = null }) { routeId -> onSelectRoute(routeId); sub = null }
        PreparationSubscreen.STAGE -> StageSubscreen(stages, selectedStageIds, { sub = null }) { ids, start, end -> selectedStageIds = ids; startKm = start; destinationKm = end; sub = null }
        PreparationSubscreen.AUDIO -> ChoiceSubscreen("Áudio", Icons.Filled.Headphones, listOf("Normal" to AudioMode.NORMAL, "Imersivo" to AudioMode.IMMERSIVE, "Silenciado" to AudioMode.SILENT), config.audioMode, { sub = null }) { config = config.copy(audioMode = it); sub = null }
        PreparationSubscreen.ORIENTATION -> ChoiceSubscreen("Orientação do mapa", Icons.Filled.Map, listOf("Norte" to MapOrientation.NORTH, "Direção da caminhada" to MapOrientation.WALK_DIRECTION), config.mapOrientation, { sub = null }) { config = config.copy(mapOrientation = it); sub = null }
        PreparationSubscreen.BREAKS -> BreaksSubscreen(config, { sub = null }) { config = it; sub = null }
        PreparationSubscreen.APOIS -> ApoiSubscreen(config, { sub = null }) { config = config.copy(visibleApoiCategories = it); sub = null }
        PreparationSubscreen.NOTES -> NotesSubscreen(notes, { sub = null }) { note -> if (note.isNotBlank()) notes += note.trim() }
    }
}

private enum class PreparationSubscreen { ROUTE, STAGE, AUDIO, ORIENTATION, BREAKS, APOIS, NOTES }

@Composable
private fun PreparationHome(route: Route, routeOptions: List<AndroidRouteOption>, selectedRouteId: String, config: WalkingPreparationConfig, stages: List<Stage>, startKm: Double, destinationKm: Double, selectedStageIds: List<String>, onBack: () -> Unit, onOpen: (PreparationSubscreen) -> Unit, onStart: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Page).verticalScroll(rememberScrollState())) {
        RouteHero(route, route.id == "sr-test" || route.id == "hf-test", onBack)
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PrepCard(Icons.Filled.Route, "Percurso", routeDisplay(route), onClick = { onOpen(PreparationSubscreen.ROUTE) })
            PrepCard(Icons.Filled.LocationOn, "Etapa e início/fim", selectedStageLabel(stages, selectedStageIds), "${fmt(startKm)} km → ${fmt(destinationKm)} km", onClick = { onOpen(PreparationSubscreen.STAGE) })
            ChoiceCard("Áudio", Icons.Filled.Headphones, audioLabel(config.audioMode)) { onOpen(PreparationSubscreen.AUDIO) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChoiceCard("Orientação", Icons.Filled.Map, orientationLabel(config.mapOrientation), Modifier.weight(1f)) { onOpen(PreparationSubscreen.ORIENTATION) }
                ChoiceCard("Pausas", Icons.Filled.PauseCircle, breakLabel(config), Modifier.weight(1f)) { onOpen(PreparationSubscreen.BREAKS) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChoiceCard("APOIs no mapa", Icons.Filled.Place, apoiLabel(config.visibleApoiCategories), Modifier.weight(1f)) { onOpen(PreparationSubscreen.APOIS) }
                ChoiceCard("Notas", Icons.Filled.Notes, "Criar ou consultar", Modifier.weight(1f)) { onOpen(PreparationSubscreen.NOTES) }
            }
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp)) { Text("▶  INICIAR CAMINHADA", fontWeight = FontWeight.Bold) }
            if (routeOptions.any { it.id == selectedRouteId && it.testOnly }) Text("Ambiente de teste · SR/HF", color = TextMuted)
        }
    }
}

@Composable
private fun RouteHero(route: Route, isTest: Boolean, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(270.dp)) {
        if (!isTest) Image(painter = painterResource(R.drawable.caminho_centenario_hero), contentDescription = "", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF87AFC2), Color(0xFF2C5F4C)))))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x22000000), Color(0xB3000000)))))
        IconButton(onClick = onBack, modifier = Modifier.padding(10.dp).clip(CircleShape).background(Color.White.copy(alpha = .92f))) { Icon(Icons.Filled.ArrowBack, "Voltar", tint = Blue) }
        Column(Modifier.align(Alignment.BottomStart).padding(20.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(route.officialName, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Text(routeDisplay(route), color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(if (isTest) "Percurso de teste" else "Percurso completo em 8 etapas", color = Color.White.copy(alpha = .92f))
        }
    }
}

@Composable
private fun PrepCard(icon: ImageVector, title: String, value: String, secondary: String? = null, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier.fillMaxWidth().clickable { onClick() }, RoundedCornerShape(22.dp), border = BorderStroke(1.dp, Border), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(27.dp), tint = Blue)
            Column(Modifier.weight(1f)) { Text(title, color = Blue, fontWeight = FontWeight.Bold); Text(value, color = Color(0xFF284A6B)); secondary?.let { Text(it, color = TextMuted) } }
            Text("›", color = Blue, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun ChoiceCard(title: String, icon: ImageVector, value: String, modifier: Modifier = Modifier, onOpen: () -> Unit) {
    PrepCard(icon, title, value, modifier = modifier, onClick = onOpen)
}

@Composable
private fun RouteSubscreen(options: List<AndroidRouteOption>, selected: String, onBack: () -> Unit, onApply: (String) -> Unit) = SecondaryScaffold("Percurso", onBack) {
    options.forEach { option -> Card(Modifier.fillMaxWidth().clickable { onApply(option.id) }, RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = if (option.id == selected) BlueSoft else Color.White)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(option.title, color = Blue, fontWeight = FontWeight.Bold); Text(option.description, color = TextMuted); if (option.testOnly) Text("TESTE", color = Danger, fontWeight = FontWeight.Bold) } } }
}

@Composable
private fun StageSubscreen(stages: List<Stage>, selectedIds: List<String>, onBack: () -> Unit, onApply: (List<String>, Double, Double) -> Unit) {
    var selected by remember(selectedIds) { mutableStateOf(selectedIds.toSet()) }
    SecondaryScaffold("Etapa e início/fim", onBack) {
        Text("Seleccione uma ou mais etapas. As distâncias aproximadas são referências e não substituem a geometria oficial.", color = TextMuted)
        stages.forEach { stage ->
            val chosen = stage.id in selected
            Card(Modifier.fillMaxWidth().clickable { selected = if (chosen) selected - stage.id else selected + stage.id }, RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (chosen) BlueSoft else Color.White)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { Box(Modifier.size(38.dp).clip(CircleShape).background(if (chosen) Blue else Border), contentAlignment = Alignment.Center) { Text(stage.number.toString(), color = if (chosen) Color.White else Blue, fontWeight = FontWeight.Bold) }; Column(Modifier.weight(1f)) { Text(stage.name, fontWeight = FontWeight.Bold, color = Blue); Text("${fmt(stage.distanceKm)} km (aprox.)", color = TextMuted) } }
            }
        }
        val chosen = stages.filter { it.id in selected }.sortedBy { it.number }
        Button(onClick = { if (chosen.isNotEmpty()) onApply(chosen.map(Stage::id), chosen.first().startRouteKm, chosen.last().endRouteKm) }, enabled = chosen.isNotEmpty(), modifier = Modifier.fillMaxWidth()) { Text("APLICAR ETAPA") }
    }
}

@Composable
private fun <T> ChoiceSubscreen(title: String, icon: ImageVector, values: List<Pair<String, T>>, selected: T, onBack: () -> Unit, onApply: (T) -> Unit) = SecondaryScaffold(title, onBack) {
    values.forEach { (label, value) ->
        val chosen = value == selected
        Card(Modifier.fillMaxWidth().clickable { onApply(value) }, RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = if (chosen) Blue else Color.White)) { Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) { Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = if (chosen) Color.White else Blue); Text(label, color = if (chosen) Color.White else Blue, fontWeight = FontWeight.Bold) } }
    }
}

@Composable
private fun BreaksSubscreen(config: WalkingPreparationConfig, onBack: () -> Unit, onApply: (WalkingPreparationConfig) -> Unit) {
    var intelligent by remember(config) { mutableStateOf(config.intelligentBreaksEnabled) }
    var time by remember(config) { mutableStateOf(config.customBreakTimeMinutes?.toString() ?: "") }
    var distance by remember(config) { mutableStateOf(config.customBreakDistanceKm?.toString() ?: "") }
    SecondaryScaffold("Pausas", onBack) {
        Text("As pausas inteligentes consideram dificuldade, distância e APOIs disponíveis. As personalizadas usam tempo e/ou distância definidos por si.", color = TextMuted)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Inteligentes", color = Blue, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Switch(checked = intelligent, onCheckedChange = { intelligent = it }) }
        HorizontalDivider()
        OutlinedTextField(time, { time = it }, label = { Text("Parar a cada X minutos") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(distance, { distance = it }, label = { Text("Parar a cada X km") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Button(onClick = { onApply(config.copy(intelligentBreaksEnabled = intelligent, customBreakTimeMinutes = time.toIntOrNull()?.takeIf { it > 0 }, customBreakDistanceKm = distance.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 })) }, modifier = Modifier.fillMaxWidth()) { Text("APLICAR PAUSAS") }
    }
}

@Composable
private fun ApoiSubscreen(config: WalkingPreparationConfig, onBack: () -> Unit, onApply: (Set<ApoiCategory>) -> Unit) {
    var selected by remember(config) { mutableStateOf(config.visibleApoiCategories) }
    val categories = listOf(ApoiCategory.ALIMENTACAO, ApoiCategory.AGUA, ApoiCategory.DESCANSO, ApoiCategory.PERNOITA, ApoiCategory.DUCHES, ApoiCategory.CARREGAMENTO, ApoiCategory.TRANSPORTE, ApoiCategory.EMERGENCIA)
    SecondaryScaffold("APOIs no mapa", onBack) {
        Text("Escolha os tipos de apoio que pretende ver no mapa durante esta caminhada.", color = TextMuted)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { categories.forEach { c -> FilterChip(c in selected, { selected = if (c in selected) selected - c else selected + c }, label = { Text(categoryLabel(c)) }) } }
        Button(onClick = { onApply(selected) }, modifier = Modifier.fillMaxWidth()) { Text("APLICAR APOIs") }
    }
}

@Composable
private fun NotesSubscreen(notes: List<String>, onBack: () -> Unit, onCreate: (String) -> Unit) {
    var draft by rememberSaveable { mutableStateOf("") }
    SecondaryScaffold("Notas", onBack) {
        Text("Crie ou consulte notas da caminhada.", color = TextMuted)
        OutlinedTextField(draft, { draft = it }, label = { Text("Nova nota") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
        Button(onClick = { onCreate(draft); draft = "" }, enabled = draft.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("GUARDAR NOTA") }
        notes.asReversed().forEach { note -> Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Text(note, Modifier.padding(14.dp), color = Blue) } }
    }
}

@Composable
private fun SecondaryScaffold(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().background(Page).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Voltar", tint = Blue) }; Text(title, color = Blue, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) }
        content()
        Spacer(Modifier.height(18.dp))
    }
}

private fun routeDisplay(route: Route) = if (route.id == "caminho-do-centenario") "Porto – Fátima · 212 km" else route.officialName
private fun selectedStageLabel(stages: List<Stage>, ids: List<String>) = stages.filter { it.id in ids }.sortedBy { it.number }.let { c -> when { c.isEmpty() -> "Escolha as etapas"; c.size == 1 -> "Etapa ${c.first().number} · ${c.first().startName} → ${c.first().endName}"; else -> "${c.size} etapas seleccionadas" } }
private fun fmt(v: Double) = String.format(java.util.Locale.US, "%.0f", v)
private fun audioLabel(v: AudioMode) = when (v) { AudioMode.NORMAL -> "Normal"; AudioMode.IMMERSIVE -> "Imersivo"; AudioMode.SILENT -> "Silenciado" }
private fun orientationLabel(v: MapOrientation) = when (v) { MapOrientation.NORTH -> "Norte"; MapOrientation.WALK_DIRECTION -> "Direção da caminhada" }
private fun breakLabel(c: WalkingPreparationConfig) = when { c.intelligentBreaksEnabled && (c.customBreakTimeMinutes != null || c.customBreakDistanceKm != null) -> "Inteligentes + Personalizadas"; c.intelligentBreaksEnabled -> "Inteligentes"; c.customBreakTimeMinutes != null || c.customBreakDistanceKm != null -> "Personalizadas"; else -> "Sem pausas definidas" }
private fun apoiLabel(c: Set<ApoiCategory>) = if (c.isEmpty()) "Escolher tipos" else c.sortedBy(ApoiCategory::name).take(2).joinToString(" · ") { categoryLabel(it) } + if (c.size > 2) " +${c.size - 2}" else ""
private fun categoryLabel(c: ApoiCategory) = when (c) { ApoiCategory.AGUA -> "Água"; ApoiCategory.ALIMENTACAO -> "Alimentação"; ApoiCategory.PERNOITA -> "Pernoita"; ApoiCategory.DESCANSO -> "Descanso"; ApoiCategory.DUCHES -> "Duches"; ApoiCategory.CARREGAMENTO -> "Carregamento"; ApoiCategory.TRANSPORTE -> "Transporte"; ApoiCategory.EMERGENCIA -> "Emergência" }
