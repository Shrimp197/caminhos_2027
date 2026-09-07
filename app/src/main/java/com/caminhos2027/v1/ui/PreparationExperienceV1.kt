package com.caminhos2027.v1.ui

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.OutlinedButton
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
    var selectedStageIds by remember(selectedRouteId) { mutableStateOf(route.stages.take(1).map { it.id }) }
    var startKm by remember(selectedRouteId) { mutableStateOf(route.stages.firstOrNull()?.startRouteKm ?: 0.0) }
    var destinationKm by remember(selectedRouteId) { mutableStateOf(route.stages.firstOrNull()?.endRouteKm ?: route.totalDistanceKm) }
    val notes = remember { mutableStateListOf<String>() }

    when (sub) {
        null -> PreparationHome(
            route = route,
            routeOptions = routeOptions,
            selectedRouteId = selectedRouteId,
            config = config,
            startKm = startKm,
            destinationKm = destinationKm,
            selectedStageIds = selectedStageIds,
            onBack = onBack,
            onOpen = { sub = it },
            onStart = { onConfirm(startKm, destinationKm, config) }
        )
        PreparationSubscreen.ROUTE -> RouteSubscreen(routeOptions, selectedRouteId, onBack = { sub = null }) {
            onSelectRoute(it)
            sub = null
        }
        PreparationSubscreen.STAGE -> StageSubscreen(
            stages = route.stages,
            selectedIds = selectedStageIds,
            onBack = { sub = null },
            onApply = { stageIds, start, end ->
                selectedStageIds = stageIds
                startKm = start
                destinationKm = end
                sub = null
            }
        )
        PreparationSubscreen.AUDIO -> ChoiceSubscreen(
            title = "Áudio",
            icon = Icons.Filled.Headphones,
            values = listOf("Normal" to AudioMode.NORMAL, "Imersivo" to AudioMode.IMMERSIVE, "Silenciado" to AudioMode.SILENT),
            selected = config.audioMode,
            onBack = { sub = null },
            onApply = { config = config.copy(audioMode = it); sub = null }
        )
        PreparationSubscreen.ORIENTATION -> ChoiceSubscreen(
            title = "Orientação do mapa",
            icon = Icons.Filled.Map,
            values = listOf("Norte" to MapOrientation.NORTH, "Direção da caminhada" to MapOrientation.WALK_DIRECTION),
            selected = config.mapOrientation,
            onBack = { sub = null },
            onApply = { config = config.copy(mapOrientation = it); sub = null }
        )
        PreparationSubscreen.BREAKS -> BreaksSubscreen(config, onBack = { sub = null }) { updated ->
            config = updated
            sub = null
        }
        PreparationSubscreen.APOIS -> ApoiSubscreen(config, onBack = { sub = null }) { categories ->
            config = config.copy(visibleApoiCategories = categories)
            sub = null
        }
        PreparationSubscreen.NOTES -> NotesSubscreen(notes, onBack = { sub = null }) { text ->
            if (text.isNotBlank()) notes += text.trim()
        }
    }
}

@Composable
private fun PreparationHome(
    route: Route,
    routeOptions: List<AndroidRouteOption>,
    selectedRouteId: String,
    config: WalkingPreparationConfig,
    startKm: Double,
    destinationKm: Double,
    selectedStageIds: List<String>,
    onBack: () -> Unit,
    onOpen: (PreparationSubscreen) -> Unit,
    onStart: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Page).verticalScroll(rememberScrollState())) {
        RouteHero(route, isTest = route.id == "sr-test" || route.id == "hf-test", onBack = onBack)
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PrepCard(Icons.Filled.Route, "Percurso", routeDisplay(route), onClick = { onOpen(PreparationSubscreen.ROUTE) })
            PrepCard(
                Icons.Filled.LocationOn,
                "Etapa e início/fim",
                selectedStageLabel(route, selectedStageIds),
                onClick = { onOpen(PreparationSubscreen.STAGE) },
                secondary = "${fmt(startKm)} km → ${fmt(destinationKm)} km"
            )
            ChoiceCard("Áudio", Icons.Filled.Headphones, audioLabel(config.audioMode), onClick = { onOpen(PreparationSubscreen.AUDIO) })
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ChoiceCard("Orientação", Icons.Filled.Map, orientationLabel(config.mapOrientation), Modifier.weight(1f)) { onOpen(PreparationSubscreen.ORIENTATION) }
                ChoiceCard("Pausas", Icons.Filled.PauseCircle, breakLabel(config), Modifier.weight(1f)) { onOpen(PreparationSubscreen.BREAKS) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ChoiceCard("APOIs no mapa", Icons.Filled.Place, apoiLabel(config.visibleApoiCategories), Modifier.weight(1f)) { onOpen(PreparationSubscreen.APOIS) }
                ChoiceCard("Notas", Icons.Filled.Notes, "Criar ou consultar", Modifier.weight(1f)) { onOpen(PreparationSubscreen.NOTES) }
            }
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp)) {
                Text("▶  INICIAR CAMINHADA", fontWeight = FontWeight.Bold)
            }
            if (routeOptions.any { it.testOnly && it.id == selectedRouteId }) {
                Text("Ambiente de teste · SR/HF", color = TextMuted, modifier = Modifier.padding(horizontal = 4.dp))
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun RouteHero(route: Route, isTest: Boolean, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(270.dp)) {
        if (!isTest) {
            Image(
                painter = painterResource(R.drawable.caminho_centenario_hero),
                contentDescription = "",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF86A7BE), Color(0xFF285A48)))))
            Box(Modifier.fillMaxSize().padding(top = 120.dp).background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC102B3F)))))
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x33000000), Color(0xB3000000)))))
        IconButton(onBack, modifier = Modifier.padding(10.dp).clip(CircleShape).background(Color.White.copy(alpha = .92f))) {
            Icon(Icons.Filled.ArrowBack, "Voltar", tint = Blue)
        }
        Column(Modifier.align(Alignment.BottomStart).padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text(route.officialName, color = Color.White, fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            Text(routeDisplay(route), color = Color.White.copy(alpha = .95f), fontWeight = FontWeight.SemiBold)
            Text(if (isTest) "Percurso de teste" else "Percurso completo em 8 etapas", color = Color.White.copy(alpha = .9f))
        }
    }
}

@Composable
private fun PrepCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, onClick: () -> Unit, secondary: String? = null) {
    Card(Modifier.fillMaxWidth().clickable { onClick() }, RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Border), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            Icon(icon, null, tint = Blue, modifier = Modifier.size(28.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Blue, fontWeight = FontWeight.Bold)
                Text(value, color = Color(0xFF284A6B))
                secondary?.let { Text(it, color = TextMuted) }
            }
            Text("›", color = Blue, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun ChoiceCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    PrepCard(icon, title, value, onClick, modifier = modifier)
}

private fun PrepCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth().clickable { onClick() }, RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Border), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Icon(icon, null, tint = Blue, modifier = Modifier.size(24.dp))
                Text(title, color = Blue, fontWeight = FontWeight.Bold)
            }
            Text(value, color = Color(0xFF58718A))
        }
    }
}

@Composable
private fun RouteSubscreen(options: List<AndroidRouteOption>, selected: String, onBack: () -> Unit, onApply: (String) -> Unit) {
    SecondaryScaffold("Percurso", onBack) {
        options.forEach { option ->
            val chosen = option.id == selected
            Card(Modifier.fillMaxWidth().clickable { onApply(option.id) }, RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = if (chosen) BlueSoft else Color.White)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(option.title, color = Blue, fontWeight = FontWeight.Bold)
                    Text(option.description, color = TextMuted)
                    if (option.testOnly) Text("TESTE", color = Danger, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StageSubscreen(stages: List<Stage>, selectedIds: List<String>, onBack: () -> Unit, onApply: (List<String>, Double, Double) -> Unit) {
    var selected by remember(selectedIds) { mutableStateOf(selectedIds.toSet()) }
    SecondaryScaffold("Etapa e início/fim", onBack) {
        Text("Seleccione uma ou mais etapas do percurso. As distâncias indicadas são referências aproximadas onde a fonte assim as define.", color = TextMuted)
        stages.forEach { stage ->
            val chosen = stage.id in selected
            Card(Modifier.fillMaxWidth().clickable {
                selected = if (chosen) selected - stage.id else selected + stage.id
            }, RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (chosen) BlueSoft else Color.White)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(38.dp).clip(CircleShape).background(if (chosen) Blue else Border), contentAlignment = Alignment.Center) {
                        Text(stage.number.toString(), color = if (chosen) Color.White else Blue, fontWeight = FontWeight.Bold)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(stage.name, fontWeight = FontWeight.Bold, color = Blue)
                        Text("${fmt(stage.distanceKm)} km (aprox.)", color = TextMuted)
                    }
                }
            }
        }
        val chosenStages = stages.filter { it.id in selected }.sortedBy { it.number }
        Button(onClick = {
            val first = chosenStages.firstOrNull() ?: return@Button
            val last = chosenStages.lastOrNull() ?: first
            onApply(chosenStages.map { it.id }, first.startRouteKm, last.endRouteKm)
        }, modifier = Modifier.fillMaxWidth(), enabled = chosenStages.isNotEmpty()) { Text("APLICAR ETAPA") }
    }
}

@Composable
private fun <T> ChoiceSubscreen(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, values: List<Pair<String, T>>, selected: T, onBack: () -> Unit, onApply: (T) -> Unit) {
    SecondaryScaffold(title, onBack) {
        values.forEach { (label, value) ->
            val chosen = value == selected
            Card(Modifier.fillMaxWidth().clickable { onApply(value) }, RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = if (chosen) Blue else Color.White)) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                    Icon(icon, null, tint = if (chosen) Color.White else Blue, modifier = Modifier.size(28.dp))
                    Text(label, color = if (chosen) Color.White else Blue, fontWeight = FontWeight.Bold)
                }
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
        Text("Pausas inteligentes consideram dificuldade, distância e APOIs disponíveis. As personalizadas permitem definir um intervalo por tempo e/ou distância.", color = TextMuted)
        ToggleRow("Inteligentes", intelligent) { intelligent = it }
        HorizontalDivider()
        OutlinedTextField(time, { time = it }, label = { Text("Parar a cada X minutos") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(distance, { distance = it }, label = { Text("Parar a cada X km") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Button(onClick = {
            onApply(config.copy(intelligentBreaksEnabled = intelligent, customBreakTimeMinutes = time.toIntOrNull()?.takeIf { it > 0 }, customBreakDistanceKm = distance.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 }))
        }, modifier = Modifier.fillMaxWidth()) { Text("APLICAR PAUSAS") }
    }
}

@Composable
private fun ApoiSubscreen(config: WalkingPreparationConfig, onBack: () -> Unit, onApply: (Set<ApoiCategory>) -> Unit) {
    var selected by remember(config) { mutableStateOf(config.visibleApoiCategories) }
    val cats = listOf(ApoiCategory.ALIMENTACAO, ApoiCategory.AGUA, ApoiCategory.DESCANSO, ApoiCategory.PERNOITA, ApoiCategory.DUCHES, ApoiCategory.CARREGAMENTO, ApoiCategory.TRANSPORTE, ApoiCategory.EMERGENCIA)
    SecondaryScaffold("APOIs no mapa", onBack) {
        Text("Escolha os tipos de apoio que pretende ver no mapa durante esta caminhada.", color = TextMuted)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cats.forEach { category ->
                FilterChip(selected.contains(category), { selected = if (category in selected) selected - category else selected + category }, label = { Text(categoryLabel(category)) })
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onApply(selected) }, modifier = Modifier.fillMaxWidth()) { Text("APLICAR APOIs") }
    }
}

@Composable
private fun NotesSubscreen(notes: List<String>, onBack: () -> Unit, onCreate: (String) -> Unit) {
    var draft by rememberSaveable { mutableStateOf("") }
    SecondaryScaffold("Notas", onBack) {
        Text("Crie ou consulte notas associadas à caminhada.", color = TextMuted)
        OutlinedTextField(draft, { draft = it }, label = { Text("Nova nota") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
        Button(onClick = { onCreate(draft); draft = "" }, modifier = Modifier.fillMaxWidth(), enabled = draft.isNotBlank()) { Text("GUARDAR NOTA") }
        if (notes.isNotEmpty()) {
            HorizontalDivider()
            notes.asReversed().forEach { Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Text(it, Modifier.padding(14.dp), color = Blue) } }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(label, color = Blue, fontWeight = FontWeight.Bold) }
        Switch(checked, onCheckedChange)
    }
}

@Composable
private fun SecondaryScaffold(title: String, onBack: () -> Unit, content: @Composable Column.() -> Unit) {
    Column(Modifier.fillMaxSize().background(Page).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack) { Icon(Icons.Filled.ArrowBack, "Voltar", tint = Blue) }
            Text(title, color = Blue, fontWeight = FontWeight.Bold, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
        }
        content()
        Spacer(Modifier.height(18.dp))
    }
}

private fun routeDisplay(route: Route): String = when (route.id) {
    "caminho-do-centenario" -> "Porto – Fátima · 212 km"
    else -> route.officialName
}

private fun selectedStageLabel(route: Route, ids: List<String>): String {
    val chosen = route.stages.filter { it.id in ids }.sortedBy { it.number }
    return if (chosen.isEmpty()) "Escolha as etapas" else if (chosen.size == 1) "Etapa ${chosen.first().number} · ${chosen.first().startName} → ${chosen.first().endName}" else "${chosen.size} etapas seleccionadas"
}

private fun fmt(value: Double): String = String.format(java.util.Locale.US, "%.0f", value)
private fun audioLabel(mode: AudioMode) = when (mode) { AudioMode.NORMAL -> "Normal"; AudioMode.IMMERSIVE -> "Imersivo"; AudioMode.SILENT -> "Silenciado" }
private fun orientationLabel(value: MapOrientation) = when (value) { MapOrientation.NORTH -> "Norte"; MapOrientation.WALK_DIRECTION -> "Direção da caminhada" }
private fun breakLabel(config: WalkingPreparationConfig) = when {
    config.intelligentBreaksEnabled && (config.customBreakTimeMinutes != null || config.customBreakDistanceKm != null) -> "Inteligentes + Personalizadas"
    config.intelligentBreaksEnabled -> "Inteligentes"
    config.customBreakTimeMinutes != null || config.customBreakDistanceKm != null -> "Personalizadas"
    else -> "Sem pausas definidas"
}
private fun apoiLabel(categories: Set<ApoiCategory>) = if (categories.isEmpty()) "Nenhum tipo seleccionado" else categories.sortedBy(ApoiCategory::name).take(2).joinToString(" · ") { categoryLabel(it) } + if (categories.size > 2) " +${categories.size - 2}" else ""
private fun categoryLabel(category: ApoiCategory) = when (category) {
    ApoiCategory.AGUA -> "Água"
    ApoiCategory.ALIMENTACAO -> "Alimentação"
    ApoiCategory.PERNOITA -> "Pernoita"
    ApoiCategory.DESCANSO -> "Descanso"
    ApoiCategory.DUCHES -> "Duches"
    ApoiCategory.CARREGAMENTO -> "Carregamento"
    ApoiCategory.TRANSPORTE -> "Transporte"
    ApoiCategory.EMERGENCIA -> "Emergência"
}
