package com.caminhos2027.v1.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.WalkingPreparationConfig
import java.util.Locale

private val PBlue = Color(0xFF164B63)
private val PGreen = Color(0xFF159447)
private val PSurface = Color(0xFFF7F8F6)
private val PBorder = Color(0xFFE0E4E1)
private val PMuted = Color(0xFF687278)

@Composable
internal fun PreparationExperienceV3(
    route: Route,
    routeOptions: List<AndroidRouteOption>,
    selectedRouteId: String,
    onSelectRoute: (String) -> Unit,
    onConfirm: (Double, Double, WalkingPreparationConfig) -> Unit,
    onBack: () -> Unit
) {
    var screen by rememberSaveable(selectedRouteId) { mutableStateOf("home") }
    var startKm by rememberSaveable(selectedRouteId) { mutableStateOf(0.0) }
    var destinationKm by rememberSaveable(selectedRouteId, route.totalDistanceKm) { mutableStateOf(route.totalDistanceKm) }
    var config by remember(selectedRouteId) { mutableStateOf(WalkingPreparationConfig()) }
    val notes = remember(selectedRouteId) { mutableStateListOf<String>() }

    when (screen) {
        "routes" -> PrepScaffold("Selecionar percurso", "Escolha o percurso que pretende preparar.", { screen = "home" }) {
            routeOptions.forEach { option ->
                Card(Modifier.fillMaxWidth().clickable { onSelectRoute(option.id); screen = "home" }, RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (option.id == selectedRouteId) Color(0xFFE9F6EE) else Color.White), border = BorderStroke(1.dp, PBorder)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(option.title, color = PBlue, fontWeight = FontWeight.ExtraBold)
                        Text(option.description, color = PMuted)
                    }
                }
            }
        }
        "range" -> RangeSub(route.totalDistanceKm, startKm, destinationKm, { screen = "home" }) { s, d -> startKm = s; destinationKm = d; screen = "home" }
        "breaks" -> BreaksSub(config, { screen = "home" }) { config = it; screen = "home" }
        "audio" -> AudioSub(config.audioEnabled, { screen = "home" }) { config = config.copy(audioEnabled = it); screen = "home" }
        "supports" -> SupportsSub({ screen = "home" })
        "notes" -> NotesSub(notes.toList(), { screen = "home" }) { text -> if (text.isNotBlank()) notes.add(text.trim()); screen = "home" }
        else -> PreparationHomeV3(route, config, notes.size,
            onRoutes = { screen = "routes" }, onRange = { screen = "range" }, onBreaks = { screen = "breaks" },
            onAudio = { screen = "audio" }, onSupports = { screen = "supports" }, onNotes = { screen = "notes" },
            onConfirm = {
                if (startKm >= 0.0 && destinationKm <= route.totalDistanceKm && startKm < destinationKm) onConfirm(startKm, destinationKm, config.copy(notes = notes.toList()))
            }, onBack = onBack)
    }
}

@Composable
private fun PreparationHomeV3(route: Route, config: WalkingPreparationConfig, notesCount: Int, onRoutes: () -> Unit, onRange: () -> Unit, onBreaks: () -> Unit, onAudio: () -> Unit, onSupports: () -> Unit, onNotes: () -> Unit, onConfirm: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(PSurface).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.Menu, "Menu", tint = PBlue) }
            Icon(Icons.Filled.DirectionsWalk, null, Modifier.size(38.dp), tint = Color(0xFFC28A16))
            Column(Modifier.padding(start = 9.dp)) { Text("CAMINHOS", color = PBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium); Text("DO PEREGRINO", color = PBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium) }
        }
        Text("Prepare a sua caminhada", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = PBlue, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
        Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), border = BorderStroke(1.dp, PBorder), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
            Box(Modifier.fillMaxWidth().height(250.dp)) {
                if (route.id == "caminho-do-centenario" || route.officialName.contains("Centenário", ignoreCase = true)) Image(painterResource(R.drawable.caminho_centenario_hero), "Imagem do Caminho do Centenário", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF6F9DB0), Color(0xFF1E6247)))))
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xD9000000)))))
                Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
                    Text(route.officialName, color = Color.White, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
                    Text(if (route.id == "caminho-do-centenario" || route.officialName.contains("Centenário", ignoreCase = true)) "212 km · Porto → Fátima" else "${fmt(route.totalDistanceKm)} km · Percurso selecionado", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Button(onClick = onRoutes, Modifier.align(Alignment.BottomEnd).padding(16.dp).height(48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = PGreen)) { Text("PREPARAR", fontWeight = FontWeight.ExtraBold) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tile(Icons.Filled.LocationOn, "Início e fim", Modifier.weight(1f), onRange)
            Tile(Icons.Filled.Headphones, "Áudio", Modifier.weight(1f), onAudio)
            Tile(Icons.Filled.Map, "Orientação / Pausas", Modifier.weight(1f), onBreaks)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tile(Icons.Filled.Place, "Apoios", Modifier.weight(1f), onSupports)
            Tile(Icons.Filled.Notes, "Notas", Modifier.weight(1f), onNotes)
            Spacer(Modifier.weight(1f).height(126.dp))
        }
        Button(onClick = onConfirm, Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = PGreen)) { Text("INICIAR CAMINHADA", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium) }
        Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), border = BorderStroke(1.dp, PBorder), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("PROGRESSO DA CAMINHADA", color = PBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                Text("A sua caminhada começa aqui.", color = PMuted)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProgressMetric("0 km", "Percorrido", Modifier.weight(1f))
                    ProgressMetric(fmt(route.totalDistanceKm) + " km", "Percurso", Modifier.weight(1f))
                }
                if (config.audioEnabled) Text("Áudio de orientação ativo", color = PGreen, fontWeight = FontWeight.Bold)
            }
        }
        if (notesCount > 0) Text("$notesCount nota(s) guardada(s)", color = PMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ProgressMetric(value: String, label: String, modifier: Modifier) {
    Card(modifier, RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F6F3))) {
        Column(Modifier.padding(12.dp)) { Text(value, color = PBlue, fontWeight = FontWeight.ExtraBold); Text(label, color = PMuted, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun Tile(icon: ImageVector, title: String, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, PBorder), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.fillMaxWidth().height(126.dp).padding(horizontal = 6.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(icon, null, Modifier.size(31.dp), tint = PBlue); Spacer(Modifier.height(8.dp)); Text(title, color = PBlue, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
    }
}

@Composable
private fun AudioSub(enabled: Boolean, onBack: () -> Unit, onApply: (Boolean) -> Unit) {
    var active by rememberSaveable { mutableStateOf(enabled) }
    PrepScaffold("Áudio", "Defina se pretende receber orientação áudio durante a caminhada.", onBack) {
        Button(onClick = { active = !active }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if (active) PGreen else PBlue)) { Text(if (active) "ÁUDIO ATIVO" else "ÁUDIO DESATIVADO") }
        Text("A preferência fica guardada no plano de caminhada.", color = PMuted)
        Button(onClick = { onApply(active) }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PGreen)) { Text("APLICAR") }
    }
}

@Composable
private fun SupportsSub(onBack: () -> Unit) {
    PrepScaffold("Apoios", "Consulte os tipos de apoio disponíveis ao longo do percurso.", onBack) {
        listOf("Água", "Alimentação", "WC", "Alojamento").forEach { label -> Card(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp), border = BorderStroke(1.dp, PBorder), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Place, null, tint = PBlue); Text(label, Modifier.padding(start = 12.dp), color = PBlue, fontWeight = FontWeight.Bold) } } }
        Text("A localização detalhada dos apoios é apresentada durante a caminhada.", color = PMuted)
    }
}

@Composable
private fun NotesSub(existing: List<String>, onBack: () -> Unit, onApply: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    PrepScaffold("Notas", "Guarde uma nota pessoal associada ao seu plano.", onBack) {
        existing.forEach { note -> Card(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, PBorder)) { Text(note, Modifier.padding(16.dp), color = PBlue) } }
        OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth(), label = { Text("Nova nota") }, minLines = 3)
        Button(onClick = { onApply(text) }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PGreen)) { Text("GUARDAR NOTA") }
    }
}

@Composable
private fun BreaksSub(config: WalkingPreparationConfig, onBack: () -> Unit, onApply: (WalkingPreparationConfig) -> Unit) {
    var minutes by rememberSaveable { mutableStateOf(config.customBreakTimeMinutes?.toString() ?: "") }
    var distance by rememberSaveable { mutableStateOf(config.customBreakDistanceKm?.let { fmt(it) } ?: "") }
    var intelligent by rememberSaveable { mutableStateOf(config.intelligentBreaksEnabled) }
    PrepScaffold("Pausas inteligentes", "Defina quando pretende parar durante a caminhada.", onBack) {
        Text("Pausas automáticas", color = PBlue, fontWeight = FontWeight.Bold)
        Button(onClick = { intelligent = !intelligent }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if (intelligent) PGreen else PBlue)) { Text(if (intelligent) "PAUSAS INTELIGENTES ATIVAS" else "PAUSAS INTELIGENTES DESATIVADAS") }
        OutlinedTextField(minutes, { minutes = it }, Modifier.fillMaxWidth(), label = { Text("Parar a cada X minutos") }, singleLine = true)
        OutlinedTextField(distance, { distance = it }, Modifier.fillMaxWidth(), label = { Text("Parar a cada X km") }, singleLine = true)
        Button(onClick = { onApply(config.copy(intelligentBreaksEnabled = intelligent, customBreakTimeMinutes = minutes.toIntOrNull(), customBreakDistanceKm = distance.replace(',', '.').toDoubleOrNull())) }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PGreen)) { Text("APLICAR PAUSAS", fontWeight = FontWeight.ExtraBold) }
    }
}

@Composable
private fun PrepScaffold(title: String, description: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().background(PSurface).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Voltar", tint = PBlue) }; Text(title, color = PBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge) }
        Text(description, color = PMuted); content()
    }
}

@Composable
private fun RangeSub(total: Double, start: Double, destination: Double, onBack: () -> Unit, onApply: (Double, Double) -> Unit) {
    var s by rememberSaveable { mutableStateOf(fmt(start)) }; var d by rememberSaveable { mutableStateOf(fmt(destination)) }; var error by rememberSaveable { mutableStateOf<String?>(null) }
    PrepScaffold("Início e fim", "Defina livremente o início e destino no traçado do percurso.", onBack) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { OutlinedTextField(s, { s = it }, Modifier.weight(1f), label = { Text("Início (km)") }, singleLine = true); OutlinedTextField(d, { d = it }, Modifier.weight(1f), label = { Text("Destino (km)") }, singleLine = true) }
        Text("Percurso: 0,00 → ${fmt(total)} km", color = PMuted); error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = { val sv = s.replace(',', '.').toDoubleOrNull(); val dv = d.replace(',', '.').toDoubleOrNull(); error = when { sv == null || dv == null -> "Indique números válidos."; sv < 0.0 || dv > total -> "Os valores têm de ficar dentro do percurso."; sv >= dv -> "O destino tem de ficar depois do início."; else -> null }; if (error == null && sv != null && dv != null) onApply(sv, dv) }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PGreen)) { Text("APLICAR") }
    }
}

private fun fmt(v: Double): String = String.format(Locale("pt", "PT"), "%.2f", v.coerceAtLeast(0.0))
