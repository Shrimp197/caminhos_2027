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
import androidx.compose.material.icons.filled.PauseCircle
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
    var showRange by rememberSaveable(selectedRouteId) { mutableStateOf(false) }
    var showRoutes by rememberSaveable(selectedRouteId) { mutableStateOf(false) }
    var startKm by rememberSaveable(selectedRouteId) { mutableStateOf(0.0) }
    var destinationKm by rememberSaveable(selectedRouteId, route.totalDistanceKm) { mutableStateOf(route.totalDistanceKm) }
    var config by remember(selectedRouteId) { mutableStateOf(WalkingPreparationConfig()) }
    val notes = remember(selectedRouteId) { mutableStateListOf<String>() }

    when {
        showRoutes -> PrepScaffold("Selecionar percurso", "Escolha o percurso que pretende preparar.", onBack = { showRoutes = false }) {
            routeOptions.forEach { option ->
                val selected = option.id == selectedRouteId
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onSelectRoute(option.id); showRoutes = false },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFFE9F6EE) else Color.White),
                    border = BorderStroke(1.dp, PBorder)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(option.title, color = PBlue, fontWeight = FontWeight.ExtraBold)
                        Text(option.description, color = PMuted)
                    }
                }
            }
        }
        showRange -> RangeSub(route.totalDistanceKm, startKm, destinationKm, onBack = { showRange = false }) { s, d ->
            startKm = s
            destinationKm = d
            showRange = false
        }
        else -> PreparationHomeV3(
            route = route,
            onRoutes = { showRoutes = true },
            onRange = { showRange = true },
            onConfirm = {
                if (startKm >= 0.0 && destinationKm <= route.totalDistanceKm && startKm < destinationKm) {
                    onConfirm(startKm, destinationKm, config.copy(notes = notes.toList()))
                }
            },
            onBack = onBack
        )
    }
}

@Composable
private fun PreparationHomeV3(
    route: Route,
    onRoutes: () -> Unit,
    onRange: () -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(PSurface).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = PBlue) }
            Icon(Icons.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(38.dp), tint = Color(0xFFC28A16))
            Column(Modifier.padding(start = 9.dp)) {
                Text("CAMINHOS", color = PBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                Text("DO PEREGRINO", color = PBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
            }
        }
        Text("Prepare a sua caminhada", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = PBlue, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, PBorder),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Box(Modifier.fillMaxWidth().height(250.dp)) {
                if (route.id == "caminho-do-centenario") {
                    Image(
                        painter = painterResource(R.drawable.caminho_centenario_hero),
                        contentDescription = "Imagem do Caminho do Centenário",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF6F9DB0), Color(0xFF1E6247)))))
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.White)
                        Text("PERCURSO DE TESTE", color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xD9000000)))))
                Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
                    Text(route.officialName, color = Color.White, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        if (route.id == "caminho-do-centenario") "212 km · Porto → Fátima" else "${fmt(route.totalDistanceKm)} km · Percurso selecionado",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = onRoutes,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PGreen)
                ) { Text("PREPARAR", fontWeight = FontWeight.ExtraBold) }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tile(Icons.Filled.LocationOn, "Início e fim", Modifier.weight(1f), onRange)
            Tile(Icons.Filled.Headphones, "Áudio", Modifier.weight(1f), {})
            Tile(Icons.Filled.Map, "Orientação", Modifier.weight(1f), {})
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tile(Icons.Filled.PauseCircle, "Pausas", Modifier.weight(1f), {})
            Tile(Icons.Filled.Place, "Apoios", Modifier.weight(1f), {})
            Tile(Icons.Filled.Notes, "Notas", Modifier.weight(1f), {})
        }
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PGreen)
        ) { Text("INICIAR CAMINHADA", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium) }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun Tile(icon: ImageVector, title: String, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, PBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().height(126.dp).padding(horizontal = 6.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(31.dp), tint = PBlue)
            Spacer(Modifier.height(8.dp))
            Text(title, color = PBlue, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun PrepScaffold(title: String, description: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(PSurface).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Voltar", tint = PBlue) }
            Text(title, color = PBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
        }
        Text(description, color = PMuted)
        content()
    }
}

@Composable
private fun RangeSub(total: Double, start: Double, destination: Double, onBack: () -> Unit, onApply: (Double, Double) -> Unit) {
    var s by rememberSaveable { mutableStateOf(fmt(start)) }
    var d by rememberSaveable { mutableStateOf(fmt(destination)) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    PrepScaffold("Início e fim", "Defina livremente o início e destino no traçado do percurso.", onBack) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(value = s, onValueChange = { s = it }, modifier = Modifier.weight(1f), label = { Text("Início (km)") }, singleLine = true)
            OutlinedTextField(value = d, onValueChange = { d = it }, modifier = Modifier.weight(1f), label = { Text("Destino (km)") }, singleLine = true)
        }
        Text("Percurso: 0,00 → ${fmt(total)} km", color = PMuted)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = {
                val sv = s.replace(',', '.').toDoubleOrNull()
                val dv = d.replace(',', '.').toDoubleOrNull()
                error = when {
                    sv == null || dv == null -> "Indique números válidos."
                    sv < 0.0 || dv > total -> "Os valores têm de ficar dentro do percurso."
                    sv >= dv -> "O destino tem de ficar depois do início."
                    else -> null
                }
                if (error == null && sv != null && dv != null) onApply(sv, dv)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PGreen)
        ) { Text("APLICAR") }
    }
}

private fun fmt(v: Double): String = String.format(Locale("pt", "PT"), "%.2f", v.coerceAtLeast(0.0))
