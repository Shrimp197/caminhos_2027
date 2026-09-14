package com.caminhos2027.v1.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.caminhos2027.R
import com.caminhos2027.v1.core.data.AndroidRouteOption
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.AudioMode
import com.caminhos2027.v1.core.model.MapOrientation
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.WalkingPreparationConfig
import java.util.Locale

private val PBlue = Color(0xFF164B63)
private val PGreen = Color(0xFF159447)
private val PSurface = Color(0xFFF7F8F6)
private val PBorder = Color(0xFFE0E4E1)
private val PMuted = Color(0xFF687278)
private val PSoftBlue = Color(0xFFEAF2F7)

private enum class Sub { ROUTES, RANGE, AUDIO, ORIENTATION, BREAKS, APOIS, NOTES }

@Composable
internal fun PreparationExperienceV3(
    route: Route,
    routeOptions: List<AndroidRouteOption>,
    selectedRouteId: String,
    onSelectRoute: (String) -> Unit,
    onConfirm: (Double, Double, WalkingPreparationConfig) -> Unit,
    onBack: () -> Unit
) {
    var sub by rememberSaveable(selectedRouteId) { mutableStateOf<Sub?>(null) }
    var startKm by rememberSaveable(selectedRouteId) { mutableStateOf(0.0) }
    var destinationKm by rememberSaveable(selectedRouteId, route.totalDistanceKm) { mutableStateOf(route.totalDistanceKm) }
    var config by remember(selectedRouteId) { mutableStateOf(WalkingPreparationConfig()) }
    val notes = remember(selectedRouteId) { mutableStateListOf<String>() }

    when (sub) {
        null -> PreparationHomeV3(
            route = route,
            options = routeOptions,
            selectedRouteId = selectedRouteId,
            startKm = startKm,
            destinationKm = destinationKm,
            config = config,
            notesCount = notes.size,
            onRoutes = { sub = Sub.ROUTES },
            onRange = { sub = Sub.RANGE },
            onAudio = { sub = Sub.AUDIO },
            onOrientation = { sub = Sub.ORIENTATION },
            onBreaks = { sub = Sub.BREAKS },
            onApois = { sub = Sub.APOIS },
            onNotes = { sub = Sub.NOTES },
            onSave = {
                if (startKm >= 0.0 && destinationKm <= route.totalDistanceKm && startKm < destinationKm) {
                    onConfirm(startKm, destinationKm, config.copy(notes = notes.toList()))
                }
            },
            onBack = onBack
        )
        Sub.ROUTES -> PrepScaffold("Selecionar percurso", "Escolha o percurso que pretende preparar.", onBack) {
            routeOptions.forEach { option ->
                val chosen = option.id == selectedRouteId
                Card(
                    Modifier.fillMaxWidth().clickable { onSelectRoute(option.id); sub = null },
                    RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = if (chosen) Color(0xFFE9F6EE) else Color.White),
                    border = BorderStroke(1.dp, PBorder)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(option.title, color = PBlue, fontWeight = FontWeight.ExtraBold)
                        Text(option.description, color = PMuted)
                        if (chosen) Text("Selecionado", color = PGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Sub.RANGE -> RangeSub(route.totalDistanceKm, startKm, destinationKm, onBack) { s, d -> startKm = s; destinationKm = d; sub = null }
        Sub.AUDIO -> ChoiceSub("Áudio", "Escolha o modo de áudio.", Icons.Filled.Headphones, listOf("Normal" to AudioMode.NORMAL, "Imersivo" to AudioMode.IMMERSIVE, "Silenciado" to AudioMode.SILENT), config.audioMode, onBack) { config = config.copy(audioMode = it); sub = null }
        Sub.ORIENTATION -> ChoiceSub("Orientação", "Escolha a orientação do mapa.", Icons.Filled.Map, listOf("Norte" to MapOrientation.NORTH, "Direção da caminhada" to MapOrientation.WALK_DIRECTION), config.mapOrientation, onBack) { config = config.copy(mapOrientation = it); sub = null }
        Sub.BREAKS -> BreaksSub(config, onBack) { config = it; sub = null }
        Sub.APOIS -> ApoisSub(config.visibleApoiCategories, onBack) { config = config.copy(visibleApoiCategories = it); sub = null }
        Sub.NOTES -> NotesSub(notes, onBack) { if (it.isNotBlank()) notes += it.trim(); sub = null }
    }
}

@Composable
private fun PreparationHomeV3(
    route: Route,
    options: List<AndroidRouteOption>,
    selectedRouteId: String,
    startKm: Double,
    destinationKm: Double,
    config: WalkingPreparationConfig,
    notesCount: Int,
    onRoutes: () -> Unit,
    onRange: () -> Unit,
    onAudio: () -> Unit,
    onOrientation: () -> Unit,
    onBreaks: () -> Unit,
    onApois: () -> Unit,
    onNotes: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().background(PSurface).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.Menu, "Menu", tint = PBlue) }
            Icon(Icons.Filled.DirectionsWalk, null, Modifier.size(38.dp), tint = Color(0xFFC28A16))
            Column(Modifier.padding(start = 9.dp)) {
                Text("CAMINHOS", color = PBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                Text("DO PEREGRINO", color = PBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
            }
        }
        Text("Prepare a sua caminhada", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = PBlue, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)

        Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), border = BorderStroke(1.dp, PBorder), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
            Box(Modifier.fillMaxWidth().height(250.dp).clickable { onRoutes() }) {
                if (route.id == "caminho-do-centenario") {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(R.drawable.caminho_centenario_hero),
                        contentDescription = "Imagem do Caminho do Centenário",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF6F9DB0), Color(0xFF1E6247)))))
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.DirectionsWalk, null, Modifier.size(64.dp), tint = Color.White.copy(alpha = .9f))
                        Text("PERCURSO DE TESTE", color = Color.White, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                    }
                }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xD9000000)))))
                Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
                    Text(route.officialName, color = Color.White, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
                    Text(if (route.id == "caminho-do-centenario") "212 km · Porto → Fátima" else "${fmt(route.totalDistanceKm)} km · Percurso selecionado", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Button(onClick = onRoutes, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).height(48.dp), shape = RoundedCornerShape(14.dp), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = PGreen)) {
                    Text("PREPARAR", fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tile(Icons.Filled.LocationOn, "Início e fim", Modifier.weight(1f), onRange)
            Tile(Icons.Filled.Headphones, "Áudio", Modifier.weight(1f), onAudio)
            Tile(Icons.Filled.Map, "Orientação", Modifier.weight(1f), onOrientation)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tile(Icons.Filled.PauseCircle, "Pausas", Modifier.weight(1f), onBreaks)
            Tile(Icons.Filled.Place, "Apoios", Modifier.weight(1f), onApois)
            Tile(Icons.Filled.Notes, "Notas", Modifier.weight(1f), onNotes)
        }
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(16.dp), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = PGreen)) {
            Text("INICIAR CAMINHADA", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun Tile(icon: ImageVector, title: String, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier.clickable { onClick() }, RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, PBorder), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.fillMaxWidth().height(126.dp).padding(horizontal = 6.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, Modifier.size(31.dp), tint = PBlue)
            Spacer(Modifier.height(8.dp))
            Text(title, color = PBlue, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable private fun PrepScaffold(title: String, description: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().background(PSurface).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Voltar", tint = PBlue) }; Text(title, color = PBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge) }
        Text(description, color = PMuted)
        content()
        Spacer(Modifier.height(16.dp))
    }
}

@Composable private fun RangeSub(total: Double, start: Double, destination: Double, onBack: () -> Unit, onApply: (Double, Double) -> Unit) {
    var s by rememberSaveable { mutableStateOf(fmt(start)) }
    var d by rememberSaveable { mutableStateOf(fmt(destination)) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    PrepScaffold("Início e fim", "Defina livremente o início e destino no traçado do percurso.", onBack) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(s, { s = it }, Modifier.weight(1f), label = { Text("Início (km)") }, singleLine = true)
            OutlinedTextField(d, { d = it }, Modifier.weight(1f), label = { Text("Destino (km)") }, singleLine = true)
        }
        Text("Percurso: 0,00 → ${fmt(total)} km", color = PMuted)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = {
            val sv = s.replace(',', '.').toDoubleOrNull(); val dv = d.replace(',', '.').toDoubleOrNull()
            error = when { sv == null || dv == null -> "Indique números válidos."; sv < 0 || dv > total -> "Os valores têm de ficar dentro do percurso."; sv >= dv -> "O destino tem de ficar depois do início."; else -> null }
            if (error == null) onApply(requireNotNull(sv), requireNotNull(dv))
        }, Modifier.fillMaxWidth(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = PGreen)) { Text("APLICAR") }
    }
}

@Composable private fun <T> ChoiceSub(title: String, description: String, icon: ImageVector, values: List<Pair<String, T>>, selected: T, onBack: () -> Unit, onApply: (T) -> Unit) {
    PrepScaffold(title, description, onBack) {
        values.forEach { pair ->
            val chosen = pair.second == selected
            Card(Modifier.fillMaxWidth().clickable { onApply(pair.second) }, RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (chosen) PBlue else Color.White), border = BorderStroke(1.dp, PBorder)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(icon, null, tint = if (chosen) Color.White else PBlue)
                    Text(pair.first, color = if (chosen) Color.White else PBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable private fun BreaksSub(config: WalkingPreparationConfig, onBack: () -> Unit, onApply: (WalkingPreparationConfig) -> Unit) {
    var intelligent by remember(config) { mutableStateOf(config.intelligentBreaksEnabled) }
    var time by remember(config) { mutableStateOf(config.customBreakTimeMinutes?.toString() ?: "") }
    var distance by remember(config) { mutableStateOf(config.customBreakDistanceKm?.toString() ?: "") }
    var error by remember(config) { mutableStateOf<String?>(null) }
    PrepScaffold("Pausas", "Escolha pausas inteligentes e/ou intervalos personalizados.", onBack) {
        Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE9F6EE))) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Pausas inteligentes", color = PBlue, fontWeight = FontWeight.ExtraBold)
                Text("Consideram dificuldade, distância e APOIs disponíveis.", color = PMuted, style = MaterialTheme.typography.bodySmall)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (intelligent) "Ativadas" else "Desativadas", Modifier.weight(1f), color = PBlue, fontWeight = FontWeight.Bold)
                    Switch(checked = intelligent, onCheckedChange = { intelligent = it })
                }
            }
        }
        Text("Pausas personalizadas", color = PBlue, fontWeight = FontWeight.ExtraBold)
        OutlinedTextField(time, { time = it }, Modifier.fillMaxWidth(), label = { Text("Parar a cada X minutos") }, singleLine = true)
        OutlinedTextField(distance, { distance = it }, Modifier.fillMaxWidth(), label = { Text("Parar a cada X km") }, singleLine = true)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = {
            val tv = time.toIntOrNull(); val dv = distance.replace(',', '.').toDoubleOrNull()
            error = when { time.isNotBlank() && (tv == null || tv <= 0) -> "Os minutos têm de ser positivos."; distance.isNotBlank() && (dv == null || dv <= 0.0) -> "Os km têm de ser positivos."; else -> null }
            if (error == null) onApply(config.copy(intelligentBreaksEnabled = intelligent, customBreakTimeMinutes = tv?.takeIf { it > 0 }, customBreakDistanceKm = dv?.takeIf { it > 0 }))
        }, Modifier.fillMaxWidth(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = PGreen)) { Text("APLICAR PAUSAS") }
    }
}

@Composable private fun ApoisSub(initial: Set<ApoiCategory>, onBack: () -> Unit, onApply: (Set<ApoiCategory>) -> Unit) {
    var selected by remember(initial) { mutableStateOf(initial) }
    val cats = listOf(ApoiCategory.ALIMENTACAO, ApoiCategory.AGUA, ApoiCategory.DESCANSO, ApoiCategory.PERNOITA, ApoiCategory.DUCHES, ApoiCategory.CARREGAMENTO, ApoiCategory.TRANSPORTE, ApoiCategory.EMERGENCIA)
    PrepScaffold("Apoios", "Escolha os tipos de apoio que pretende ver no mapa.", onBack) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { cats.forEach { cat -> FilterChip(selected = cat in selected, onClick = { selected = if (cat in selected) selected - cat else selected + cat }, label = { Text(categoryLabel(cat)) }) } }
        Button(onClick = { onApply(selected) }, Modifier.fillMaxWidth(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = PGreen)) { Text("APLICAR APOIOS") }
    }
}

@Composable private fun NotesSub(notes: List<String>, onBack: () -> Unit, onAdd: (String) -> Unit) {
    var draft by rememberSaveable { mutableStateOf("") }
    PrepScaffold("Notas", "As notas ficam associadas ao plano guardado.", onBack) {
        OutlinedTextField(draft, { draft = it }, Modifier.fillMaxWidth(), label = { Text("Nova nota") }, minLines = 4)
        Button(onClick = { onAdd(draft); draft = "" }, enabled = draft.isNotBlank(), Modifier.fillMaxWidth(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = PGreen)) { Text("GUARDAR NOTA") }
        notes.asReversed().forEach { note -> Card(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Text(note, Modifier.padding(14.dp), color = PBlue) } }
    }
}

private fun fmt(v: Double) = String.format(Locale("pt", "PT"), "%.2f", v.coerceAtLeast(0.0))
private fun categoryLabel(c: ApoiCategory) = when (c) {
    ApoiCategory.AGUA -> "Água"
    ApoiCategory.ALIMENTACAO -> "Alimentação"
    ApoiCategory.PERNOITA -> "Pernoita"
    ApoiCategory.DESCANSO -> "Descanso"
    ApoiCategory.DUCHES -> "Duches"
    ApoiCategory.CARREGAMENTO -> "Carregamento"
    ApoiCategory.TRANSPORTE -> "Transporte"
    ApoiCategory.EMERGENCIA -> "Emergência"
}
