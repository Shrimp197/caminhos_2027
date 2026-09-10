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
import java.util.Locale

private val PrepBlue = Color(0xFF164B63)
private val PrepGreen = Color(0xFF1B9B50)
private val PrepSurface = Color(0xFFF5F6F4)
private val PrepBorder = Color(0xFFE0E3DF)
private val PrepMuted = Color(0xFF6C7478)
private val PrepSoftGreen = Color(0xFFE9F6EE)
private val PrepSoftBlue = Color(0xFFEAF2F7)
private val PrepRed = Color(0xFFC63B3B)

private enum class PrepSubscreen { ROUTES, START_END, AUDIO, ORIENTATION, BREAKS, APOIS, NOTES }

@Composable
internal fun PreparationExperienceV2(
    route: Route,
    routeOptions: List<AndroidRouteOption>,
    selectedRouteId: String,
    onSelectRoute: (String) -> Unit,
    onConfirm: (Double, Double, WalkingPreparationConfig) -> Unit,
    onBack: () -> Unit
) {
    var sub by rememberSaveable(selectedRouteId) { mutableStateOf<PrepSubscreen?>(null) }
    var startKm by rememberSaveable(selectedRouteId) { mutableStateOf(0.0) }
    var destinationKm by rememberSaveable(selectedRouteId, route.totalDistanceKm) { mutableStateOf(route.totalDistanceKm) }
    var rangeError by rememberSaveable(selectedRouteId, route.totalDistanceKm) { mutableStateOf<String?>(null) }
    var config by remember(selectedRouteId) { mutableStateOf(WalkingPreparationConfig()) }
    val notes = remember(selectedRouteId) { mutableStateListOf<String>() }
    val stages = remember(route.id, route.stages) { route.stages }

    when (sub) {
        null -> PreparationLandingV2(
            route = route,
            routeOptions = routeOptions,
            selectedRouteId = selectedRouteId,
            startKm = startKm,
            destinationKm = destinationKm,
            config = config,
            notesCount = notes.size,
            rangeError = rangeError,
            onSelectRoute = { sub = PrepSubscreen.ROUTES },
            onOpenStartEnd = { sub = PrepSubscreen.START_END },
            onOpenAudio = { sub = PrepSubscreen.AUDIO },
            onOpenOrientation = { sub = PrepSubscreen.ORIENTATION },
            onOpenBreaks = { sub = PrepSubscreen.BREAKS },
            onOpenApois = { sub = PrepSubscreen.APOIS },
            onOpenNotes = { sub = PrepSubscreen.NOTES },
            onSave = {
                val valid = startKm in 0.0..route.totalDistanceKm && destinationKm in 0.0..route.totalDistanceKm && startKm < destinationKm
                rangeError = if (valid) null else "Escolha um início e destino válidos dentro do percurso."
                if (valid) onConfirm(startKm, destinationKm, config.copy(notes = notes.toList()))
            },
            onBack = onBack
        )
        PrepSubscreen.ROUTES -> RouteChooserV2(routeOptions, selectedRouteId, onBack) { id ->
            onSelectRoute(id)
            sub = null
        }
        PrepSubscreen.START_END -> StartEndV2(route.totalDistanceKm, startKm, destinationKm, onBack) { s, d ->
            startKm = s
            destinationKm = d
            rangeError = null
            sub = null
        }
        PrepSubscreen.AUDIO -> ChoiceV2(
            title = "Áudio",
            description = "Defina como pretende receber o áudio durante a caminhada.",
            icon = Icons.Filled.Headphones,
            values = listOf("Normal" to AudioMode.NORMAL, "Imersivo" to AudioMode.IMMERSIVE, "Silenciado" to AudioMode.SILENT),
            selected = config.audioMode,
            onBack = { sub = null }
        ) { config = config.copy(audioMode = it); sub = null }
        PrepSubscreen.ORIENTATION -> ChoiceV2(
            title = "Orientação",
            description = "Escolha a orientação do mapa durante a caminhada.",
            icon = Icons.Filled.Map,
            values = listOf("Norte" to MapOrientation.NORTH, "Direção da caminhada" to MapOrientation.WALK_DIRECTION),
            selected = config.mapOrientation,
            onBack = { sub = null }
        ) { config = config.copy(mapOrientation = it); sub = null }
        PrepSubscreen.BREAKS -> BreaksV2(config, { sub = null }) { config = it; sub = null }
        PrepSubscreen.APOIS -> ApoisV2(config.visibleApoiCategories, { sub = null }) { config = config.copy(visibleApoiCategories = it); sub = null }
        PrepSubscreen.NOTES -> NotesV2(notes, { sub = null }) { note -> if (note.isNotBlank()) notes += note.trim() }
    }
}

@Composable
private fun PreparationLandingV2(
    route: Route,
    routeOptions: List<AndroidRouteOption>,
    selectedRouteId: String,
    startKm: Double,
    destinationKm: Double,
    config: WalkingPreparationConfig,
    notesCount: Int,
    rangeError: String?,
    onSelectRoute: () -> Unit,
    onOpenStartEnd: () -> Unit,
    onOpenAudio: () -> Unit,
    onOpenOrientation: () -> Unit,
    onOpenBreaks: () -> Unit,
    onOpenApois: () -> Unit,
    onOpenNotes: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(PrepSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = PrepBlue) }
            Image(painterResource(R.drawable.ic_launcher_source), "Caminhos do Peregrino", Modifier.size(34.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            Column(Modifier.padding(start = 9.dp)) {
                Text("CAMINHOS", color = PrepBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                Text("DO PEREGRINO", color = PrepBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
            }
        }
        Text("Prepare a sua caminhada", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = PrepBlue, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)

        RouteCardV2(route, selectedRouteId, onSelectRoute)

        Card(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, PrepBorder)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitleV2(Icons.Filled.LocationOn, "Início e fim", "Defina livremente onde começa e termina a sua caminhada.", onOpenStartEnd)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryValueV2("Início", "${fmt(startKm)} km", Modifier.weight(1f), onOpenStartEnd)
                    SummaryValueV2("Destino", "${fmt(destinationKm)} km", Modifier.weight(1f), onOpenStartEnd)
                }
                Text("${fmt((destinationKm - startKm).coerceAtLeast(0.0))} km planeados · as etapas oficiais são apenas referência.", color = PrepMuted, style = MaterialTheme.typography.bodySmall)
                rangeError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold) }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PrepTileV2(Icons.Filled.Headphones, "Áudio", audioLabelV2(config.audioMode), Modifier.weight(1f), onOpenAudio)
            PrepTileV2(Icons.Filled.Straighten, "Orientação", orientationLabelV2(config.mapOrientation), Modifier.weight(1f), onOpenOrientation)
            PrepTileV2(Icons.Filled.PauseCircle, "Pausas", breakLabelV2(config), Modifier.weight(1f), onOpenBreaks)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PrepTileV2(Icons.Filled.Place, "Apoios", apoiLabelV2(config.visibleApoiCategories), Modifier.weight(1f), onOpenApois)
            PrepTileV2(Icons.Filled.Notes, "Notas", if (notesCount == 0) "Adicionar" else "${notesCount} nota(s)", Modifier.weight(1f), onOpenNotes)
            PrepTileV2(Icons.Filled.LocationOn, "Percurso", "Selecionar", Modifier.weight(1f), onSelectRoute)
        }

        if (routeOptions.firstOrNull { it.id == selectedRouteId }?.testOnly == true) {
            Text("TESTE · percurso controlado", color = PrepRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        }

        Button(onClick = onSave, Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)) {
            Text("GUARDAR PLANO", fontWeight = FontWeight.ExtraBold)
        }
        Text("Guardar o plano não inicia a caminhada. Depois de guardar, verá o botão INICIAR CAMINHADA.", Modifier.fillMaxWidth(), color = PrepMuted, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
    }
}

@Composable
private fun RouteCardV2(route: Route, selectedRouteId: String, onSelectRoute: () -> Unit) {
    val isSelected = route.id == selectedRouteId
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), border = BorderStroke(1.dp, PrepBorder), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Box(Modifier.fillMaxWidth().height(210.dp).clickable { onSelectRoute() }) {
            if (route.id == "caminho-do-centenario") {
                Image(painterResource(R.drawable.caminho_centenario_hero), "Imagem do Caminho do Centenário", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF7FA6B8), Color(0xFF35634B)))))
            }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xB9000000)))))
            Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text(route.officialName, color = Color.White, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
                Text(routeDisplayV2(route), color = Color.White, fontWeight = FontWeight.Bold)
                Text(if (isSelected) "Percurso selecionado" else "Toque para selecionar", color = Color.White.copy(alpha = .92f))
            }
            Button(onClick = onSelectRoute, Modifier.align(Alignment.BottomEnd).padding(14.dp), shape = RoundedCornerShape(12.dp)) {
                Text("PREPARAR", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun RouteChooserV2(options: List<AndroidRouteOption>, selected: String, onBack: () -> Unit, onApply: (String) -> Unit) {
    PrepScaffoldV2("Selecionar percurso", "Escolha o percurso que pretende preparar.", onBack) {
        options.forEach { option ->
            val chosen = option.id == selected
            Card(Modifier.fillMaxWidth().clickable { onApply(option.id) }, RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (chosen) PrepSoftGreen else Color.White), border = BorderStroke(1.dp, PrepBorder)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(option.title, color = PrepBlue, fontWeight = FontWeight.ExtraBold)
                    Text(option.description, color = PrepMuted)
                    if (option.testOnly) Text("AMBIENTE DE TESTE", color = PrepRed, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelSmall)
                    if (chosen) Text("Selecionado", color = PrepGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StartEndV2(totalKm: Double, start: Double, destination: Double, onBack: () -> Unit, onApply: (Double, Double) -> Unit) {
    var s by rememberSaveable { mutableStateOf(fmt(start)) }
    var d by rememberSaveable { mutableStateOf(fmt(destination)) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    PrepScaffoldV2("Início e fim", "O início e destino são km no traçado do percurso.", onBack) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(s, { s = it }, label = { Text("Início (km)") }, Modifier.weight(1f), singleLine = true)
            OutlinedTextField(d, { d = it }, label = { Text("Destino (km)") }, Modifier.weight(1f), singleLine = true)
        }
        Text("Percurso: 0,00 → ${fmt(totalKm)} km", color = PrepMuted)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold) }
        Button(onClick = {
            val startKm = s.replace(',', '.').toDoubleOrNull()
            val destinationKm = d.replace(',', '.').toDoubleOrNull()
            error = when {
                startKm == null || destinationKm == null -> "Indique números válidos."
                startKm < 0 || destinationKm > totalKm -> "Os valores têm de ficar dentro do percurso."
                startKm >= destinationKm -> "O destino tem de ficar depois do início."
                else -> null
            }
            if (error == null) onApply(requireNotNull(startKm), requireNotNull(destinationKm))
        }, Modifier.fillMaxWidth()) { Text("APLICAR") }
    }
}

@Composable
private fun <T> ChoiceV2(title: String, description: String, icon: ImageVector, values: List<Pair<String, T>>, selected: T, onBack: () -> Unit, onApply: (T) -> Unit) {
    PrepScaffoldV2(title, description, onBack) {
        values.forEach { (label, value) ->
            val chosen = value == selected
            Card(Modifier.fillMaxWidth().clickable { onApply(value) }, RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (chosen) PrepBlue else Color.White), border = BorderStroke(1.dp, PrepBorder)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(icon, null, Modifier.size(28.dp), tint = if (chosen) Color.White else PrepBlue)
                    Text(label, color = if (chosen) Color.White else PrepBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BreaksV2(config: WalkingPreparationConfig, onBack: () -> Unit, onApply: (WalkingPreparationConfig) -> Unit) {
    var intelligent by remember(config) { mutableStateOf(config.intelligentBreaksEnabled) }
    var timeText by remember(config) { mutableStateOf(config.customBreakTimeMinutes?.toString() ?: "") }
    var distanceText by remember(config) { mutableStateOf(config.customBreakDistanceKm?.toString() ?: "") }
    var error by remember(config) { mutableStateOf<String?>(null) }
    PrepScaffoldV2("Pausas", "Escolha pausas inteligentes e, opcionalmente, intervalos personalizados.", onBack) {
        Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = PrepSoftGreen), border = BorderStroke(1.dp, PrepBorder)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Pausas inteligentes", color = PrepBlue, fontWeight = FontWeight.ExtraBold)
                Text("Consideram dificuldade, distância e APOIs disponíveis.", color = PrepMuted, style = MaterialTheme.typography.bodySmall)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (intelligent) "Ativadas" else "Desativadas", color = PrepBlue, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Switch(intelligent, { intelligent = it })
                }
            }
        }
        HorizontalDivider()
        Text("Pausas personalizadas", color = PrepBlue, fontWeight = FontWeight.ExtraBold)
        Text("Pode definir um ou ambos os intervalos.", color = PrepMuted, style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(timeText, { timeText = it }, label = { Text("Parar a cada X minutos") }, Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(distanceText, { distanceText = it }, label = { Text("Parar a cada X km") }, Modifier.fillMaxWidth(), singleLine = true)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold) }
        Button(onClick = {
            val time = timeText.toIntOrNull()
            val distance = distanceText.replace(',', '.').toDoubleOrNull()
            error = when {
                timeText.isNotBlank() && (time == null || time <= 0) -> "Os minutos têm de ser um número positivo."
                distanceText.isNotBlank() && (distance == null || distance <= 0.0) -> "Os km têm de ser um número positivo."
                else -> null
            }
            if (error == null) onApply(config.copy(intelligentBreaksEnabled = intelligent, customBreakTimeMinutes = time?.takeIf { it > 0 }, customBreakDistanceKm = distance?.takeIf { it > 0.0 }))
        }, Modifier.fillMaxWidth()) { Text("APLICAR PAUSAS") }
    }
}

@Composable
private fun ApoisV2(selectedInitial: Set<ApoiCategory>, onBack: () -> Unit, onApply: (Set<ApoiCategory>) -> Unit) {
    var selected by remember(selectedInitial) { mutableStateOf(selectedInitial) }
    val categories = listOf(ApoiCategory.ALIMENTACAO, ApoiCategory.AGUA, ApoiCategory.DESCANSO, ApoiCategory.PERNOITA, ApoiCategory.DUCHES, ApoiCategory.CARREGAMENTO, ApoiCategory.TRANSPORTE, ApoiCategory.EMERGENCIA)
    PrepScaffoldV2("Apoios", "Escolha os tipos de apoio a apresentar no mapa.", onBack) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { category ->
                FilterChip(selected = category in selected, onClick = { selected = if (category in selected) selected - category else selected + category }, label = { Text(categoryLabelV2(category)) })
            }
        }
        Button(onClick = { onApply(selected) }, Modifier.fillMaxWidth()) { Text("APLICAR APOIOS") }
    }
}

@Composable
private fun NotesV2(notes: List<String>, onBack: () -> Unit, onAdd: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    PrepScaffoldV2("Notas", "As notas ficam associadas ao plano guardado.", onBack) {
        OutlinedTextField(text, { text = it }, label = { Text("Nova nota") }, Modifier.fillMaxWidth(), minLines = 4)
        Button(onClick = { onAdd(text); text = "" }, enabled = text.isNotBlank(), Modifier.fillMaxWidth()) { Text("GUARDAR NOTA") }
        notes.asReversed().forEach { note -> Card(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Text(note, Modifier.padding(14.dp), color = PrepBlue) } }
    }
}

@Composable
private fun PrepScaffoldV2(title: String, description: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().background(PrepSurface).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Voltar", tint = PrepBlue) }
            Text(title, color = PrepBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
        }
        Text(description, color = PrepMuted)
        content()
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun SectionTitleV2(icon: ImageVector, title: String, description: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = PrepBlue)
        Column(Modifier.weight(1f).padding(start = 9.dp)) {
            Text(title, color = PrepBlue, fontWeight = FontWeight.Bold)
            Text(description, color = PrepMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SummaryValueV2(title: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier.clickable { onClick() }, RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = PrepSoftBlue), border = BorderStroke(1.dp, PrepBorder)) {
        Column(Modifier.padding(12.dp)) {
            Text(title, color = PrepMuted, style = MaterialTheme.typography.labelSmall)
            Text(value, color = PrepBlue, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun PrepTileV2(icon: ImageVector, label: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier.clickable { onClick() }, RoundedCornerShape(14.dp), border = BorderStroke(1.dp, PrepBorder), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(27.dp), tint = PrepBlue)
            Spacer(Modifier.height(6.dp))
            Text(label, color = PrepBlue, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
            Text(value, color = PrepMuted, style = MaterialTheme.typography.labelSmall, maxLines = 2, textAlign = TextAlign.Center)
        }
    }
}

private fun routeDisplayV2(route: Route) = if (route.id == "caminho-do-centenario") "212 km · Porto → Fátima" else "${fmt(route.totalDistanceKm)} km"
private fun fmt(value: Double) = String.format(Locale("pt", "PT"), "%.2f", value.coerceAtLeast(0.0))
private fun audioLabelV2(value: AudioMode) = when (value) { AudioMode.NORMAL -> "Normal"; AudioMode.IMMERSIVE -> "Imersivo"; AudioMode.SILENT -> "Silenciado" }
private fun orientationLabelV2(value: MapOrientation) = when (value) { MapOrientation.NORTH -> "Norte"; MapOrientation.WALK_DIRECTION -> "Direção da caminhada" }
private fun breakLabelV2(config: WalkingPreparationConfig) = when {
    config.intelligentBreaksEnabled && (config.customBreakTimeMinutes != null || config.customBreakDistanceKm != null) -> "Inteligentes + personalizadas"
    config.intelligentBreaksEnabled -> "Inteligentes"
    config.customBreakTimeMinutes != null || config.customBreakDistanceKm != null -> "Personalizadas"
    else -> "Sem pausas"
}
private fun apoiLabelV2(categories: Set<ApoiCategory>) = if (categories.isEmpty()) "Escolher tipos" else categories.sortedBy(ApoiCategory::name).take(2).joinToString(" · ") { categoryLabelV2(it) } + if (categories.size > 2) " +${categories.size - 2}" else ""
private fun categoryLabelV2(value: ApoiCategory) = when (value) {
    ApoiCategory.AGUA -> "Água"
    ApoiCategory.ALIMENTACAO -> "Alimentação"
    ApoiCategory.PERNOITA -> "Pernoita"
    ApoiCategory.DESCANSO -> "Descanso"
    ApoiCategory.DUCHES -> "Duches"
    ApoiCategory.CARREGAMENTO -> "Carregamento"
    ApoiCategory.TRANSPORTE -> "Transporte"
    ApoiCategory.EMERGENCIA -> "Emergência"
}