package com.caminhos2027.v1.ui

import android.content.Context
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.data.AndroidRouteOption
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.AudioMode
import com.caminhos2027.v1.core.model.MapOrientation
import com.caminhos2027.v1.core.model.Walk
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.WalkingPreparationConfig

private val RefBlue = Color(0xFF164B63)
private val RefGreen = Color(0xFF159447)
private val RefBg = Color(0xFFF7F7F4)
private val RefBorder = Color(0xFFDCE3DE)
private val RefMuted = Color(0xFF68736D)
private val RefGold = Color(0xFFC28A16)

@Composable
internal fun PreparationExperienceV4(route: Route, routeOptions: List<AndroidRouteOption>, selectedRouteId: String, plannedWalk: Walk?, startRequested: Boolean, pendingStartDistanceMeters: Double?, onSelectRoute: (String) -> Unit, onConfirm: (Double, Double, WalkingPreparationConfig) -> Unit, onStart: () -> Unit, onCancelStart: () -> Unit, onNavigateToCoordinate: (Double, Double, String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("v1_preparation_ui", Context.MODE_PRIVATE) }
    var startKm by rememberSaveable(selectedRouteId, plannedWalk?.id) { mutableStateOf(plannedWalk?.plannedStartKm ?: 0.0) }
    var destinationKm by rememberSaveable(selectedRouteId, plannedWalk?.id, route.totalDistanceKm) { mutableStateOf(plannedWalk?.plannedDestinationKm ?: route.totalDistanceKm) }
    var config by remember(selectedRouteId, plannedWalk?.id) { mutableStateOf(plannedWalk?.preparation ?: WalkingPreparationConfig()) }
    var notes by rememberSaveable(selectedRouteId, plannedWalk?.id) { mutableStateOf(plannedWalk?.preparation?.notes?.firstOrNull().orEmpty()) }
    var dialog by rememberSaveable { mutableStateOf(if (prefs.getBoolean("route_selector_seen", false)) null else "route") }
    var error by remember { mutableStateOf<String?>(null) }

    fun savePlan(): Boolean {
        val valid = startKm >= 0.0 && destinationKm <= route.totalDistanceKm && startKm < destinationKm
        if (!valid) { error = "O destino tem de ficar depois do início e dentro do percurso."; return false }
        onConfirm(startKm, destinationKm, config.copy(notes = notes.trim().takeIf { it.isNotBlank() }?.let(::listOf) ?: emptyList()))
        error = null
        return true
    }

    Scaffold(containerColor = RefBg, bottomBar = {
        NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
            listOf("Resumo" to Icons.Filled.Home, "Mapa" to Icons.Filled.Map, "Apoios" to Icons.Filled.Place, "Diário" to Icons.Filled.MenuBook, "Mais" to Icons.Filled.MoreHoriz).forEachIndexed { index, item ->
                NavigationBarItem(selected = false, enabled = true, onClick = { if (index == 4) onBack() }, icon = { Icon(item.second, item.first, modifier = Modifier.size(20.dp)) }, label = { Text(item.first, maxLines = 1, fontSize = 11.sp) })
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).navigationBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp).semantics { contentDescription = "PREPARAÇÃO — HOME" }, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth().height(46.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, Modifier.size(38.dp)) { Icon(Icons.Filled.Menu, "Menu", tint = RefBlue) }
                Icon(Icons.Filled.DirectionsWalk, null, tint = RefGold, modifier = Modifier.size(25.dp))
                Column(Modifier.padding(start = 6.dp)) { Text("CAMINHOS", color = RefBlue, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp); Text("DO PEREGRINO", color = RefBlue, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp) }
            }
            Text("Prepare a sua caminhada", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = RefBlue, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            Card(Modifier.fillMaxWidth().height(132.dp), RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = RefBlue), border = BorderStroke(1.dp, RefBorder), elevation = CardDefaults.cardElevation(2.dp)) {
                Box(Modifier.fillMaxSize()) {
                    if (route.id == "caminho-do-centenario") ReferenceHeroArt(Modifier.fillMaxSize())
                    else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(RefBlue, Color(0xFF2E6C56)))))
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xA8000000)))))
                    Column(Modifier.align(Alignment.BottomStart).padding(13.dp).padding(end = 118.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(route.officialName, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1)
                        Text("${if (route.id == "caminho-do-centenario") "211,9" else String.format(java.util.Locale("pt", "PT"), "%.1f", route.totalDistanceKm)} km · ${if (route.id == "caminho-do-centenario") "Porto → Fátima" else "Percurso selecionado"}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Button(onClick = { dialog = "route" }, Modifier.align(Alignment.BottomEnd).padding(8.dp).height(38.dp), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = RefGreen), contentPadding = PaddingValues(horizontal = 12.dp)) { Text("PREPARAR", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp) }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) { PrepTile(Icons.Filled.LocationOn, "Início e fim", Modifier.weight(1f)) { dialog = "range" }; PrepTile(Icons.Filled.Headphones, "Áudio", Modifier.weight(1f)) { dialog = "audio" }; PrepTile(Icons.Filled.Map, "Orientação", Modifier.weight(1f)) { dialog = "orientation" } }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { PrepTile(Icons.Filled.PauseCircle, "Pausas", Modifier.weight(1f)) { dialog = "breaks" }; PrepTile(Icons.Filled.Place, "Apoios", Modifier.weight(1f)) { dialog = "supports" }; PrepTile(Icons.Filled.Notes, "Notas", Modifier.weight(1f)) { dialog = "notes" } }
            if (plannedWalk == null) {
                Button(onClick = { if (savePlan()) onStart() }, Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(13.dp), colors = ButtonDefaults.buttonColors(containerColor = RefGreen)) { Icon(Icons.Filled.DirectionsWalk, null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(6.dp)); Text("INICIAR CAMINHADA", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp) }
                TextButton(onClick = { savePlan() }, Modifier.fillMaxWidth().height(30.dp)) { Text("GUARDAR PLANO", color = RefBlue, fontWeight = FontWeight.SemiBold) }
            } else {
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, RefBorder)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("Plano guardado", color = RefGreen, fontWeight = FontWeight.ExtraBold)
                        Text("${plannedWalk.plannedStartKm?.let { fmtKm(it) } ?: fmtKm(startKm)} km → ${plannedWalk.plannedDestinationKm?.let { fmtKm(it) } ?: fmtKm(destinationKm)} km", color = RefBlue, fontWeight = FontWeight.ExtraBold)
                        Text("Guardar o plano não inicia a caminhada.", color = RefMuted, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        Text("Estado · PLANNED · a caminhada ainda não começou.", color = RefMuted, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        Text("Áudio · ${audioLabelV4(plannedWalk.preparation.audioMode)}", color = RefBlue, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        Text("Orientação · ${orientationLabelV4(plannedWalk.preparation.mapOrientation)}", color = RefBlue, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        Text("Apoios · ${plannedWalk.preparation.visibleApoiCategories.size} tipo(s) selecionado(s)", color = RefBlue, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        Text("Notas · ${plannedWalk.preparation.notes.size} guardada(s)", color = RefBlue, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        Text(if (plannedWalk.preparation.intelligentBreaksEnabled) "Pausas · inteligentes ativas" else "Pausas · inteligentes desativadas", color = RefBlue, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick = { savePlan() }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) { Text("GUARDAR ALTERAÇÕES", fontWeight = FontWeight.Bold) }
                    }
                }
                if (startRequested) {
                    Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if ((pendingStartDistanceMeters ?: 0.0) > 100.0) "A orientar para o início" else "A procurar uma posição GPS precisa…", color = if ((pendingStartDistanceMeters ?: 0.0) > 100.0) Color(0xFF9A5A00) else RefBlue, fontWeight = FontWeight.ExtraBold)
                            pendingStartDistanceMeters?.let { Text("Distância ao início planeado: ${fmtMetersV4(it)}", color = RefMuted) }
                            Text("A caminhada ainda não começou. Vamos levá-lo até ao início planeado; só começa quando o GPS confirmar que chegou ao percurso.", color = RefMuted, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                            Button(onClick = {
                                val target = pointAtRouteKmForNavigation(route, plannedWalk.plannedStartKm ?: startKm)
                                onNavigateToCoordinate(target.latitude, target.longitude, "Início da caminhada · ${route.officialName}")
                            }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = RefGreen), shape = RoundedCornerShape(11.dp)) { Text("NAVEGAR ATÉ AO INÍCIO", fontWeight = FontWeight.ExtraBold) }
                            TextButton(onClick = onCancelStart, Modifier.fillMaxWidth()) { Text("CANCELAR INÍCIO", color = RefBlue, fontWeight = FontWeight.SemiBold) }
                        }
                    }
                } else {
                    Button(onClick = onStart, Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(13.dp), colors = ButtonDefaults.buttonColors(containerColor = RefGreen)) { Icon(Icons.Filled.DirectionsWalk, null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(6.dp)); Text("INICIAR CAMINHADA", fontWeight = FontWeight.ExtraBold) }
                }
            }
            error?.let { Text(it, color = Color(0xFF9A2F2F), fontWeight = FontWeight.SemiBold) }
        }
    }

    when (dialog) {
        "route" -> AlertDialog(onDismissRequest = { dialog = null }, title = { Text("Selecionar percurso") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { routeOptions.forEach { option -> Card(Modifier.fillMaxWidth().clickable { onSelectRoute(option.id); prefs.edit().putBoolean("route_selector_seen", true).apply(); dialog = null }, RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = if (option.id == selectedRouteId) Color(0xFFE8F5ED) else Color.White), border = BorderStroke(1.dp, RefBorder)) { Column(Modifier.padding(12.dp)) { Text(option.title, color = RefBlue, fontWeight = FontWeight.ExtraBold); Text(option.description, color = RefMuted); if (option.testOnly) Text("AMBIENTE DE TESTE", color = Color(0xFF9A5A00), fontWeight = FontWeight.Bold) } } } } }, confirmButton = { TextButton(onClick = { dialog = null }) { Text("FECHAR") } })
        "range" -> { var startText by remember(startKm) { mutableStateOf(startKm.toString()) }; var destinationText by remember(destinationKm) { mutableStateOf(destinationKm.toString()) }; AlertDialog(onDismissRequest = { dialog = null }, title = { Text("Início e fim") }, text = { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(startText, { startText = it }, label = { Text("Início km") }, modifier = Modifier.weight(1f), singleLine = true); OutlinedTextField(destinationText, { destinationText = it }, label = { Text("Destino km") }, modifier = Modifier.weight(1f), singleLine = true) } }, confirmButton = { TextButton(onClick = { startText.replace(',', '.').toDoubleOrNull()?.let { startKm = it }; destinationText.replace(',', '.').toDoubleOrNull()?.let { destinationKm = it }; dialog = null }) { Text("APLICAR") } }, dismissButton = { TextButton(onClick = { dialog = null }) { Text("CANCELAR") } }) }
        "audio" -> ChoiceDialog("Áudio", listOf(AudioMode.NORMAL to "ÁUDIO NORMAL", AudioMode.IMMERSIVE to "ÁUDIO IMERSIVO", AudioMode.SILENT to "SEM ÁUDIO"), config.audioMode, { config = config.copy(audioMode = it) }, { dialog = null })
        "orientation" -> ChoiceDialog("Orientação", listOf(MapOrientation.NORTH to "NORTE", MapOrientation.WALK_DIRECTION to "DIREÇÃO DA CAMINHADA"), config.mapOrientation, { config = config.copy(mapOrientation = it) }, { dialog = null })
        "breaks" -> BreaksDialog(config, { config = it }, { dialog = null })
        "supports" -> AlertDialog(onDismissRequest = { dialog = null }, title = { Text("Apoios") }, text = { val cats = listOf(ApoiCategory.AGUA, ApoiCategory.ALIMENTACAO, ApoiCategory.PERNOITA, ApoiCategory.DUCHES, ApoiCategory.CARREGAMENTO, ApoiCategory.EMERGENCIA); Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { cats.forEach { cat -> FilterChip(selected = cat in config.visibleApoiCategories, onClick = { config = config.copy(visibleApoiCategories = config.visibleApoiCategories.toMutableSet().apply { if (!add(cat)) remove(cat) }) }, label = { Text(if (cat == ApoiCategory.AGUA) "Água" else cat.name.lowercase().replace('_', ' ')) }) } } }, confirmButton = { TextButton(onClick = { dialog = null }) { Text("APLICAR APOIOS") } })
        "notes" -> { var noteText by remember(notes) { mutableStateOf(notes) }; AlertDialog(onDismissRequest = { dialog = null }, title = { Text("Notas") }, text = { OutlinedTextField(noteText, { noteText = it }, label = { Text("Nova nota") }, minLines = 3) }, confirmButton = { TextButton(onClick = { notes = noteText.trim(); dialog = null }) { Text("GUARDAR NOTA") } }, dismissButton = { TextButton(onClick = { dialog = null }) { Text("CANCELAR") } }) }
    }
}

@Composable
private fun BreaksDialog(config: WalkingPreparationConfig, onChange: (WalkingPreparationConfig) -> Unit, onClose: () -> Unit) {
    var minutes by remember(config.customBreakTimeMinutes) { mutableStateOf(config.customBreakTimeMinutes?.toString() ?: "") }
    var distance by remember(config.customBreakDistanceKm) { mutableStateOf(config.customBreakDistanceKm?.toString() ?: "") }
    AlertDialog(onDismissRequest = onClose, title = { Text("Pausas") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("Pausas inteligentes", color = RefBlue, fontWeight = FontWeight.Bold); Text("Escolha intervalos de tempo e distância para as sugestões de pausa.", color = RefMuted); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(minutes, { minutes = it }, label = { Text("Parar a cada X minutos") }, modifier = Modifier.weight(1f), singleLine = true); OutlinedTextField(distance, { distance = it }, label = { Text("Parar a cada X km") }, modifier = Modifier.weight(1f), singleLine = true) }; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Ativar pausas inteligentes"); Switch(checked = config.intelligentBreaksEnabled, onCheckedChange = { onChange(config.copy(intelligentBreaksEnabled = it)) }) } } }, confirmButton = { TextButton(onClick = { onChange(config.copy(customBreakTimeMinutes = minutes.toIntOrNull(), customBreakDistanceKm = distance.replace(',', '.').toDoubleOrNull())); onClose() }) { Text("APLICAR PAUSAS") } }, dismissButton = { TextButton(onClick = onClose) { Text("CANCELAR") } })
}

@Composable
private fun PrepTile(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier.semantics(mergeDescendants = true) { contentDescription = label; role = Role.Button }.clickable(onClick = onClick), RoundedCornerShape(13.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, RefBorder), elevation = CardDefaults.cardElevation(1.dp)) { Column(Modifier.fillMaxWidth().height(62.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(icon, null, tint = RefBlue, modifier = Modifier.size(21.dp)); Spacer(Modifier.height(4.dp)); Text(label, color = RefBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center) } }
}

@Composable
private fun <T> ChoiceDialog(title: String, choices: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit, onClose: () -> Unit) {
    AlertDialog(onDismissRequest = onClose, title = { Text(title) }, text = { Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { choices.forEach { (value, label) -> Button(onClick = { onSelect(value) }, Modifier.fillMaxWidth().semantics { contentDescription = label }, colors = ButtonDefaults.buttonColors(containerColor = if (value == selected) RefGreen else RefBlue)) { Text(label) } } } }, confirmButton = { TextButton(onClick = onClose) { Text("APLICAR") } })
}


private fun audioLabelV4(mode: AudioMode): String = when (mode) {
    AudioMode.NORMAL -> "normal"
    AudioMode.IMMERSIVE -> "imersivo"
    AudioMode.SILENT -> "sem áudio"
}

private fun orientationLabelV4(orientation: MapOrientation): String = when (orientation) {
    MapOrientation.NORTH -> "norte"
    MapOrientation.WALK_DIRECTION -> "direção da caminhada"
}

private fun fmtKm(value: Double): String = String.format(java.util.Locale("pt", "PT"), "%.2f km", value.coerceAtLeast(0.0))
private fun fmtMetersV4(value: Double): String = if (value < 1000.0) String.format(java.util.Locale("pt", "PT"), "%.0f m", value) else String.format(java.util.Locale("pt", "PT"), "%.1f km", value / 1000.0)
