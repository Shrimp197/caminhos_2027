package com.caminhos2027.v1.ui

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.data.AndroidRouteOption
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.AudioMode
import com.caminhos2027.v1.core.model.MapOrientation
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.WalkingPreparationConfig

private val RefBlue = Color(0xFF164B63)
private val RefGreen = Color(0xFF159447)
private val RefBg = Color(0xFFF7F7F4)
private val RefBorder = Color(0xFFDCE3DE)
private val RefMuted = Color(0xFF68736D)
private val RefGold = Color(0xFFC28A16)

@Composable
internal fun PreparationExperienceV4(
    route: Route,
    routeOptions: List<AndroidRouteOption>,
    selectedRouteId: String,
    onSelectRoute: (String) -> Unit,
    onConfirm: (Double, Double, WalkingPreparationConfig) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val routeUiPrefs = remember { context.getSharedPreferences("v1_preparation_ui", Context.MODE_PRIVATE) }
    var startKm by rememberSaveable(selectedRouteId) { mutableStateOf(0.0) }
    var destinationKm by rememberSaveable(selectedRouteId, route.totalDistanceKm) { mutableStateOf(route.totalDistanceKm) }
    var config by remember(selectedRouteId) { mutableStateOf(WalkingPreparationConfig()) }
    var notes by rememberSaveable(selectedRouteId) { mutableStateOf("") }
    var dialog by rememberSaveable { mutableStateOf(if (routeUiPrefs.getBoolean("route_selector_seen", false)) null else "route") }
    var error by remember { mutableStateOf<String?>(null) }

    val savePlan: () -> Boolean = {
        val valid = startKm >= 0.0 && destinationKm <= route.totalDistanceKm && startKm < destinationKm
        if (valid) {
            onConfirm(startKm, destinationKm, config.copy(notes = notes.trim().takeIf { it.isNotBlank() }?.let { listOf(it) } ?: emptyList()))
            error = null
        } else error = "O destino tem de ficar depois do início e dentro do percurso."
        valid
    }

    Scaffold(containerColor = RefBg, bottomBar = {
        NavigationBar(modifier = Modifier.navigationBarsPadding(), containerColor = Color.White, tonalElevation = 0.dp) {
            val items = listOf("Resumo" to Icons.Filled.Map, "Mapa" to Icons.Filled.Map, "Apoios" to Icons.Filled.Place, "Diário" to Icons.Filled.Notes, "Mais" to Icons.Filled.Menu)
            items.forEachIndexed { index, item -> NavigationBarItem(selected = index == 0, onClick = { if (index == 4) onBack() }, icon = { Icon(item.second, item.first, modifier = Modifier.size(21.dp)) }, label = { Text(item.first, maxLines = 1) }) }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp).semantics { contentDescription = "PREPARAÇÃO — HOME" }, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth().height(46.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, Modifier.size(40.dp)) { Icon(Icons.Filled.Menu, "Menu", tint = RefBlue) }
                Icon(Icons.Filled.DirectionsWalk, null, tint = RefGold, modifier = Modifier.size(32.dp))
                Column(Modifier.padding(start = 8.dp)) { Text("CAMINHOS", color = RefBlue, fontWeight = FontWeight.ExtraBold); Text("DO PEREGRINO", color = RefBlue, fontWeight = FontWeight.ExtraBold) }
            }
            Text("Prepare a sua caminhada", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = RefBlue, fontWeight = FontWeight.ExtraBold)
            Card(Modifier.fillMaxWidth().height(178.dp), RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = RefBlue), border = BorderStroke(1.dp, RefBorder), elevation = CardDefaults.cardElevation(3.dp)) {
                Box(Modifier.fillMaxSize()) {
                    if (route.id == "caminho-do-centenario") ReferenceHeroArt(Modifier.fillMaxSize()) else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(RefBlue, Color(0xFF2E6C56)))))
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xDD000000)))))
                    Column(Modifier.align(Alignment.BottomStart).padding(14.dp).padding(end = 120.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(route.officialName, color = Color.White, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                        Text("${if (route.id == "caminho-do-centenario") "212" else route.totalDistanceKm.toInt()} km · ${if (route.id == "caminho-do-centenario") "Porto → Fátima" else "Percurso selecionado"}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = { dialog = "route" }, Modifier.align(Alignment.BottomEnd).padding(12.dp).height(44.dp), shape = RoundedCornerShape(11.dp), colors = ButtonDefaults.buttonColors(containerColor = RefGreen), contentPadding = PaddingValues(horizontal = 16.dp)) { Text("PREPARAR", fontWeight = FontWeight.ExtraBold) }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) { PrepTile(Icons.Filled.LocationOn, "Início e fim", Modifier.weight(1f)) { dialog = "range" }; PrepTile(Icons.Filled.Headphones, "Áudio", Modifier.weight(1f)) { dialog = "audio" }; PrepTile(Icons.Filled.Map, "Orientação", Modifier.weight(1f)) { dialog = "orientation" } }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) { PrepTile(Icons.Filled.PauseCircle, "Pausas", Modifier.weight(1f)) { dialog = "breaks" }; PrepTile(Icons.Filled.Place, "Apoios", Modifier.weight(1f)) { dialog = "supports" }; PrepTile(Icons.Filled.Notes, "Notas", Modifier.weight(1f)) { dialog = "notes" } }
            Button(onClick = { if (savePlan()) onStart() }, Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = RefGreen)) { Icon(Icons.Filled.DirectionsWalk, null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(7.dp)); Text("INICIAR CAMINHADA", fontWeight = FontWeight.ExtraBold) }
            TextButton(onClick = { savePlan() }, Modifier.fillMaxWidth()) { Text("GUARDAR PLANO", color = RefBlue, fontWeight = FontWeight.SemiBold) }
            error?.let { Text(it, color = Color(0xFF9A2F2F), fontWeight = FontWeight.SemiBold) }
        }
    }

    when (dialog) {
        "route" -> AlertDialog(onDismissRequest = { dialog = null }, title = { Text("Selecionar percurso") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { routeOptions.forEach { option -> Card(Modifier.fillMaxWidth().clickable { onSelectRoute(option.id); routeUiPrefs.edit().putBoolean("route_selector_seen", true).apply(); dialog = null }, RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = if (option.id == selectedRouteId) Color(0xFFE8F5ED) else Color.White), border = BorderStroke(1.dp, RefBorder)) { Column(Modifier.padding(12.dp)) { Text(option.title, color = RefBlue, fontWeight = FontWeight.ExtraBold); Text(option.description, color = RefMuted); if (option.testOnly) Text("AMBIENTE DE TESTE", color = Color(0xFF9A5A00), fontWeight = FontWeight.Bold) } } } } }, confirmButton = { TextButton(onClick = { dialog = null }) { Text("FECHAR") } })
        "range" -> { var startText by remember(startKm) { mutableStateOf(startKm.toString()) }; var destinationText by remember(destinationKm) { mutableStateOf(destinationKm.toString()) }; AlertDialog(onDismissRequest = { dialog = null }, title = { Text("Início e fim") }, text = { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(startText, { startText = it }, label = { Text("Início km") }, modifier = Modifier.weight(1f), singleLine = true); OutlinedTextField(destinationText, { destinationText = it }, label = { Text("Destino km") }, modifier = Modifier.weight(1f), singleLine = true) } }, confirmButton = { TextButton(onClick = { startText.replace(',', '.').toDoubleOrNull()?.let { startKm = it }; destinationText.replace(',', '.').toDoubleOrNull()?.let { destinationKm = it }; dialog = null }) { Text("APLICAR") } }, dismissButton = { TextButton(onClick = { dialog = null }) { Text("CANCELAR") } }) }
        "audio" -> ChoiceDialog("Áudio", listOf(AudioMode.NORMAL to "ÁUDIO NORMAL", AudioMode.IMMERSIVE to "ÁUDIO IMERSIVO", AudioMode.SILENT to "SEM ÁUDIO"), config.audioMode, { config = config.copy(audioMode = it) }, { dialog = null })
        "orientation" -> ChoiceDialog("Orientação", listOf(MapOrientation.NORTH to "NORTE", MapOrientation.WALK_DIRECTION to "DIREÇÃO DA CAMINHADA"), config.mapOrientation, { config = config.copy(mapOrientation = it) }, { dialog = null })
        "breaks" -> BreaksDialog(config = config, onChange = { config = it }, onClose = { dialog = null })
        "supports" -> AlertDialog(onDismissRequest = { dialog = null }, title = { Text("Apoios") }, text = { val cats = listOf(ApoiCategory.AGUA, ApoiCategory.ALIMENTACAO, ApoiCategory.PERNOITA, ApoiCategory.DUCHES, ApoiCategory.CARREGAMENTO, ApoiCategory.EMERGENCIA); Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { cats.forEach { cat -> FilterChip(selected = cat in config.visibleApoiCategories, onClick = { config = config.copy(visibleApoiCategories = config.visibleApoiCategories.toMutableSet().apply { if (!add(cat)) remove(cat) }) }, label = { Text(if (cat == ApoiCategory.AGUA) "Água" else cat.name.lowercase().replace('_', ' ')) }) } } }, confirmButton = { TextButton(onClick = { dialog = null }) { Text("APLICAR APOIOS") } })
        "notes" -> { var noteText by remember(notes) { mutableStateOf(notes) }; AlertDialog(onDismissRequest = { dialog = null }, title = { Text("Notas") }, text = { OutlinedTextField(noteText, { noteText = it }, label = { Text("Nova nota") }, minLines = 3) }, confirmButton = { TextButton(onClick = { notes = noteText.trim(); dialog = null }) { Text("GUARDAR NOTA") } }, dismissButton = { TextButton(onClick = { dialog = null }) { Text("CANCELAR") } }) }
    }
}

@Composable
private fun BreaksDialog(config: WalkingPreparationConfig, onChange: (WalkingPreparationConfig) -> Unit, onClose: () -> Unit) {
    var minutes by remember(config.customBreakTimeMinutes) { mutableStateOf(config.customBreakTimeMinutes?.toString() ?: "") }
    var distance by remember(config.customBreakDistanceKm) { mutableStateOf(config.customBreakDistanceKm?.toString() ?: "") }
    AlertDialog(onDismissRequest = onClose, title = { Text("Pausas") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Pausas inteligentes", color = RefBlue, fontWeight = FontWeight.Bold)
            Text("Escolha intervalos de tempo e distância para as sugestões de pausa.", color = RefMuted)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(minutes, { minutes = it }, label = { Text("Parar a cada X minutos") }, modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(distance, { distance = it }, label = { Text("Parar a cada X km") }, modifier = Modifier.weight(1f), singleLine = true)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Ativar pausas inteligentes")
                Switch(checked = config.intelligentBreaksEnabled, onCheckedChange = { onChange(config.copy(intelligentBreaksEnabled = it)) })
            }
        }
    }, confirmButton = { TextButton(onClick = { onChange(config.copy(customBreakTimeMinutes = minutes.toIntOrNull(), customBreakDistanceKm = distance.replace(',', '.').toDoubleOrNull())); onClose() }) { Text("APLICAR PAUSAS") } }, dismissButton = { TextButton(onClick = onClose) { Text("CANCELAR") } })
}

@Composable
private fun PrepTile(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick).semantics { contentDescription = label; role = Role.Button }, RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, RefBorder), elevation = CardDefaults.cardElevation(2.dp)) { Column(Modifier.fillMaxWidth().height(92.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(icon, null, tint = RefBlue, modifier = Modifier.size(28.dp)); Spacer(Modifier.height(7.dp)); Text(label, color = RefBlue, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) } }
}

@Composable
private fun <T> ChoiceDialog(title: String, choices: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit, onClose: () -> Unit) {
    AlertDialog(onDismissRequest = onClose, title = { Text(title) }, text = { Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { choices.forEach { (value, label) -> Button(onClick = { onSelect(value) }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if (value == selected) RefGreen else RefBlue)) { Text(label) } } } }, confirmButton = { TextButton(onClick = onClose) { Text("APLICAR") } })
}
