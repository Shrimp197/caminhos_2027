package com.caminhos2027.v1.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.diary.DiaryEntry
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.walking.WalkingState
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val NavBlue = Color(0xFF164B63)
private val NavGreen = Color(0xFF159447)
private val NavSurface = Color(0xFFF7F8F6)
private val NavMuted = Color(0xFF687278)
private val EmergencyRed = Color(0xFFB3261E)

@Composable
internal fun BottomNavBarV1(selected: WalkingSurface, onNavigate: (WalkingSurface) -> Unit) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        navItem(WalkingSurface.SUMMARY, "Resumo", Icons.Filled.Home, selected, onNavigate)
        navItem(WalkingSurface.ACTIVE, "Mapa", Icons.Filled.Map, selected, onNavigate)
        navItem(WalkingSurface.APOI_BROWSER, "Apoios", Icons.Filled.Place, selected, onNavigate)
        navItem(WalkingSurface.DIARY, "Diário", Icons.Filled.Book, selected, onNavigate)
        navItem(WalkingSurface.MORE, "Mais", Icons.Filled.MoreHoriz, selected, onNavigate)
    }
}

@Composable
private fun RowScope.navItem(destination: WalkingSurface, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: WalkingSurface, onNavigate: (WalkingSurface) -> Unit) {
    NavigationBarItem(
        selected = selected == destination,
        onClick = { onNavigate(destination) },
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = NavBlue,
            selectedTextColor = NavBlue,
            indicatorColor = Color(0xFFF4E8CF),
            unselectedIconColor = NavMuted,
            unselectedTextColor = NavMuted
        )
    )
}

@Composable
internal fun SummarySurfaceV1(state: WalkingState?, route: Route, onMap: () -> Unit, onApoi: () -> Unit, onDiary: () -> Unit, onMore: () -> Unit) {
    Scaffold(
        containerColor = NavSurface,
        bottomBar = { BottomNavBarV1(WalkingSurface.SUMMARY) { destination ->
            when (destination) {
                WalkingSurface.ACTIVE -> onMap()
                WalkingSurface.APOI_BROWSER -> onApoi()
                WalkingSurface.DIARY -> onDiary()
                WalkingSurface.MORE -> onMore()
                else -> Unit
            }
        } }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Resumo", color = NavBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
            if (state == null) {
                EmptyProductCard("Ainda não iniciou uma caminhada. Prepare o percurso para começar.")
            } else {
                val current = state.routePosition?.routeKm ?: state.progress?.currentRouteKm ?: 0.0
                val destination = state.walk.plannedDestinationKm ?: route.totalDistanceKm
                val progress = (state.progress?.progressRatio ?: 0.0).coerceIn(0.0, 1.0)
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(route.officialName, color = NavBlue, fontWeight = FontWeight.ExtraBold)
                        Text("${fmtKm(current)} km", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                        Text("${fmtKm((destination - current).coerceAtLeast(0.0))} km para o destino planeado", color = NavMuted)
                        LinearProgressIndicator(progress = { progress.toFloat() }, Modifier.fillMaxWidth().height(10.dp), color = NavGreen)
                        Text("${(progress * 100.0).toInt()}% do percurso planeado", color = NavGreen, fontWeight = FontWeight.Bold)
                    }
                }
                val currentStage = route.stages.firstOrNull { stage ->
                    current >= stage.startRouteKm && current < stage.endRouteKm
                } ?: route.stages.lastOrNull { current >= it.startRouteKm }
                currentStage?.let { stage ->
                    Card(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF5F1))) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text("ETAPA ATUAL", color = NavBlue, fontWeight = FontWeight.ExtraBold)
                            Text(stage.name, color = NavBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                            Text(stage.distanceKm.toString().replace(".", ",") + " km · " + stage.startName + " → " + stage.endName, color = NavMuted)
                            Text(
                                if (current >= stage.endRouteKm) "Etapa concluída" else "Em curso",
                                color = if (current >= stage.endRouteKm) NavMuted else NavGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                state.nextApoi?.let { apoi ->
                    Card(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE9F6EE))) {
                        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Place, null, tint = NavGreen)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) { Text("Próximo APOI", color = NavGreen, fontWeight = FontWeight.ExtraBold); Text(apoi.name, fontWeight = FontWeight.Bold) }
                            Text(state.nextApoiDistanceKm?.let(::fmtDistance) ?: "—", color = NavGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onMap, Modifier.weight(1f)) { Text("ABRIR MAPA") }
                    OutlinedButton(onClick = onApoi, Modifier.weight(1f)) { Text("APOIOS") }
                }
                OutlinedButton(onClick = onDiary, Modifier.fillMaxWidth()) { Text("DIÁRIO DO PEREGRINO") }
            }
        }
    }
}

@Composable
internal fun Next10KmSurfaceV1(state: WalkingState?, results: List<com.caminhos2027.v1.core.apoi.ApoiAhead>, onMap: () -> Unit, onNavigate: (WalkingSurface) -> Unit) {
    Scaffold(containerColor = NavSurface, bottomBar = { BottomNavBarV1(WalkingSurface.NEXT_10_KM, onNavigate) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Próximos 10 km", color = NavBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
            Text("Apoios ordenados pela distância ao longo do percurso.", color = NavMuted)
            when {
                state == null -> EmptyProductCard("Inicie uma caminhada para consultar o contexto do percurso.")
                results.isEmpty() -> EmptyProductCard("Não existem APOI publicados nos próximos 10 km para os dados de produção disponíveis.")
                else -> results.forEachIndexed { index, item ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(36.dp)) {
                            Box(Modifier.size(28.dp).background(NavGreen, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text("${index + 1}", color = Color.White, fontWeight = FontWeight.ExtraBold) }
                            if (index < results.lastIndex) Box(Modifier.width(2.dp).height(55.dp).background(Color(0xFFD6E4DB)))
                        }
                        Card(Modifier.fillMaxWidth().weight(1f), RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) { Icon(categoryIconProduct(item.apoi.mainCategory), null, tint = categoryColorProduct(item.apoi.mainCategory), modifier = Modifier.size(22.dp)); Spacer(Modifier.width(7.dp)); Text(fmtDistance(item.distanceKm), color = categoryColorProduct(item.apoi.mainCategory), fontWeight = FontWeight.ExtraBold) }
                                Text(item.apoi.name, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) { Text(categoryTitle(item.apoi.mainCategory), color = categoryColorProduct(item.apoi.mainCategory), fontWeight = FontWeight.SemiBold) }
                                Text(item.apoi.cost.description ?: costLabel(item.apoi.cost.model), color = NavMuted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            OutlinedButton(onClick = onMap, Modifier.fillMaxWidth()) { Text("VOLTAR AO MAPA") }
        }
    }
}

private fun categoryIconProduct(category: com.caminhos2027.v1.core.model.ApoiCategory): androidx.compose.ui.graphics.vector.ImageVector = when (category) {
    com.caminhos2027.v1.core.model.ApoiCategory.AGUA -> Icons.Filled.WaterDrop
    com.caminhos2027.v1.core.model.ApoiCategory.ALIMENTACAO -> Icons.Filled.Restaurant
    com.caminhos2027.v1.core.model.ApoiCategory.PERNOITA -> Icons.Filled.Hotel
    com.caminhos2027.v1.core.model.ApoiCategory.EMERGENCIA -> Icons.Filled.LocalHospital
    com.caminhos2027.v1.core.model.ApoiCategory.CARREGAMENTO -> Icons.Filled.Bolt
    else -> Icons.Filled.Place
}

private fun categoryColorProduct(category: com.caminhos2027.v1.core.model.ApoiCategory): Color = when (category) {
    com.caminhos2027.v1.core.model.ApoiCategory.AGUA -> Color(0xFF1687C9)
    com.caminhos2027.v1.core.model.ApoiCategory.ALIMENTACAO -> Color(0xFFD77B18)
    com.caminhos2027.v1.core.model.ApoiCategory.PERNOITA -> Color(0xFF7561C9)
    com.caminhos2027.v1.core.model.ApoiCategory.EMERGENCIA -> Color(0xFFC33D35)
    com.caminhos2027.v1.core.model.ApoiCategory.CARREGAMENTO -> Color(0xFF2B8C67)
    else -> NavBlue
}

@Composable
internal fun DiarySurfaceV1(entries: List<DiaryEntry>, activeState: WalkingState?, photoUri: String?, onChoosePhoto: () -> Unit, onClearPhoto: () -> Unit, onAdd: (String) -> Unit, onNavigate: (WalkingSurface) -> Unit) {
    var text by remember { mutableStateOf("") }
    Scaffold(containerColor = NavSurface, bottomBar = { BottomNavBarV1(WalkingSurface.DIARY, onNavigate) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Diário", color = NavBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
            Text("Registe notas e fotografias associadas à caminhada.", color = NavMuted)
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    androidx.compose.material3.OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        label = { Text("Nova nota") }
                    )
                    activeState?.let { state ->
                        Text("Associada à caminhada" + (state.routePosition?.routeKm?.let { " · km ${fmtKm(it)}" } ?: ""), color = NavGreen, style = MaterialTheme.typography.bodySmall)
                    } ?: Text("Nota geral, sem caminhada ativa.", color = NavMuted, style = MaterialTheme.typography.bodySmall)
                    if (photoUri != null) {
                        DiaryPhotoThumbnail(photoUri)
                        OutlinedButton(onClick = onClearPhoto, Modifier.fillMaxWidth()) { Text("REMOVER FOTOGRAFIA") }
                    } else {
                        OutlinedButton(onClick = onChoosePhoto, Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Share, null)
                            Spacer(Modifier.width(8.dp))
                            Text("ADICIONAR FOTOGRAFIA")
                        }
                    }
                    Button(onClick = { if (text.isNotBlank()) { onAdd(text.trim()); text = "" } }, enabled = text.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("GUARDAR NOTA") }
                }
            }
            if (entries.isEmpty()) EmptyProductCard("Ainda não existem entradas no diário.")
            entries.forEach { entry ->
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(entry.createdAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm")), color = NavGreen, fontWeight = FontWeight.Bold)
                        entry.routeKm?.let { Text("Km ${fmtKm(it)}", color = NavMuted, style = MaterialTheme.typography.bodySmall) }
                        entry.location?.let { Text("Coordenadas " + formatCoord(it.latitude) + ", " + formatCoord(it.longitude), color = NavMuted, style = MaterialTheme.typography.bodySmall) }
                        Text(entry.content)
                        entry.photoUri?.let { DiaryPhotoThumbnail(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaryPhotoThumbnail(uriString: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, key1 = uriString) {
        value = runCatching { context.contentResolver.openInputStream(Uri.parse(uriString)).use { input -> if (input == null) null else BitmapFactory.decodeStream(input) } }.getOrNull()
    }
    bitmap?.let { value -> Image(
            painter = BitmapPainter(value.asImageBitmap()),
            contentDescription = "Fotografia do diário",
            modifier = Modifier.fillMaxWidth().height(190.dp),
            contentScale = ContentScale.Crop
        ) }
        ?: Text("Fotografia anexada.", color = NavMuted, style = MaterialTheme.typography.bodySmall)
}

@Composable
internal fun SosSurfaceV1(state: WalkingState?, emergencyApoi: List<com.caminhos2027.v1.core.apoi.ApoiAhead>, onNavigate: (WalkingSurface) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coordinates = state?.currentPhysicalPoint ?: state?.routePosition?.projectedPoint
    val coordinatesLabel = when {
        state?.currentPhysicalPoint != null -> "Última posição física conhecida"
        state?.routePosition?.projectedPoint != null -> "Última posição conhecida no Caminho"
        else -> "Sem posição conhecida"
    }
    val shareText = coordinates?.let { "Localização no Caminhos do Peregrino: " + formatCoord(it.latitude) + ", " + formatCoord(it.longitude) }
    Scaffold(containerColor = Color(0xFF141414), bottomBar = { BottomNavBarV1(WalkingSurface.SOS, onNavigate) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("EMERGÊNCIA", color = Color.White, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall); Text("Acesso direto às funções do dispositivo.", color = Color(0xFFE0E0E0)) }
                Icon(Icons.Filled.Warning, null, tint = Color(0xFFFFB4AB), modifier = Modifier.size(34.dp))
            }
            Button(onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))) }, Modifier.fillMaxWidth().height(68.dp), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = EmergencyRed), shape = RoundedCornerShape(18.dp)) {
                Icon(Icons.Filled.Call, null); Spacer(Modifier.width(10.dp)); Text("LIGAR 112", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
            }
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF242424))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("A minha localização", color = Color.White, fontWeight = FontWeight.ExtraBold)
                    if (coordinates == null) Text("Sem uma posição GPS válida neste momento.", color = Color(0xFFFFDAD6))
                    else {
                        Text(coordinatesLabel, color = Color(0xFFFFDAD6), fontWeight = FontWeight.Bold)
                        Text("Latitude: " + formatCoord6(coordinates.latitude), color = Color.White)
                        Text("Longitude: " + formatCoord6(coordinates.longitude), color = Color.White)
                    }
                }
            }
            Button(onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, shareText ?: "A minha localização não está disponível no momento.") }
                context.startActivity(Intent.createChooser(intent, "Partilhar localização"))
            }, Modifier.fillMaxWidth(), enabled = shareText != null) { Icon(Icons.Filled.Share, null); Spacer(Modifier.width(8.dp)); Text("PARTILHAR LOCALIZAÇÃO") }
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF242424))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Apoios de emergência", color = Color.White, fontWeight = FontWeight.ExtraBold)
                    if (emergencyApoi.isEmpty()) {
                        Text("Não existem apoios de emergência publicados no contexto atual. Não são apresentados dados fictícios.", color = Color(0xFFE0E0E0))
                    } else {
                        emergencyApoi.forEach { item ->
                            Text(item.apoi.name, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(fmtDistance(item.distanceKm), color = Color(0xFFFFB4AB))
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SmartwatchSurfaceV1(state: WalkingState?, onNavigate: (WalkingSurface) -> Unit, onMap: () -> Unit) {
    val currentKm = state?.routePosition?.routeKm ?: state?.progress?.currentRouteKm ?: 0.0
    val remainingKm = state?.progress?.remainingKm ?: 0.0
    val nextApoi = state?.nextApoi
    val nextApoiDistance = state?.nextApoiDistanceKm
    Scaffold(containerColor = NavSurface, bottomBar = { BottomNavBarV1(WalkingSurface.SMARTWATCH, onNavigate) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Smartwatch", color = NavBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(174.dp)) {
                    Box(Modifier.width(34.dp).height(62.dp).background(Color(0xFF1D1D1D), RoundedCornerShape(12.dp)))
                    Box(Modifier.width(156.dp).height(176.dp).background(Color(0xFF111111), RoundedCornerShape(36.dp)).padding(10.dp)) {
                        Column(Modifier.fillMaxSize().background(Color.Black, RoundedCornerShape(28.dp)).padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.DirectionsWalk, null, tint = NavGreen, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("CAMINHADA", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(fmtDistance(nextApoiDistance ?: remainingKm), color = Color(0xFF8DFF9A), fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineSmall)
                                Text(nextApoi?.name ?: "Próximo APOI", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2)
                            }
                            Text("Km " + fmtKm(currentKm), color = Color(0xFFB8B8B8), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Box(Modifier.width(34.dp).height(62.dp).background(Color(0xFF1D1D1D), RoundedCornerShape(12.dp)))
                }
            }
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Icon(Icons.Filled.Notifications, null, tint = NavGreen, modifier = Modifier.size(32.dp))
                    Text("Pré-visualização das notificações", fontWeight = FontWeight.ExtraBold)
                    Text("A aplicação usa notificações Android reais para eventos úteis. O relógio mostrado acima é apenas uma representação visual.", color = NavMuted)
                    Text("Não é apresentada como integração nativa com Huawei ou Amazfit.", color = NavGreen, fontWeight = FontWeight.Bold)
                }
            }
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3DF))) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("Compatibilidade externa", color = Color(0xFF7A4A00), fontWeight = FontWeight.ExtraBold)
                    Text("A integração específica de Huawei Watch GT 2 e Amazfit Active 2 depende das respetivas plataformas e não está integrada nativamente nesta aplicação.", color = Color(0xFF7A4A00))
                }
            }
            Button(onClick = onMap, Modifier.fillMaxWidth()) { Text("VOLTAR À CAMINHADA") }
        }
    }
}

@Composable
internal fun PilgrimModeSurfaceV1(state: WalkingState, onOpenApoi: () -> Unit, onOpenSos: () -> Unit, onExit: () -> Unit) {
    val routeKm = state.routePosition?.routeKm ?: state.progress?.currentRouteKm ?: 0.0
    val remaining = state.progress?.remainingKm ?: 0.0
    val progress = (state.progress?.progressRatio ?: 0.0).coerceIn(0.0, 1.0)
    Surface(Modifier.fillMaxSize().semantics { contentDescription = "MODO PEREGRINO — SUPERFÍCIE" }, color = Color.Black) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("MODO PEREGRINO", color = Color.White, fontWeight = FontWeight.ExtraBold); Text(if (state.isPaused) "PAUSADO" else "CAMINHADA EM CURSO", color = Color(0xFF8DFF9A), fontWeight = FontWeight.Bold) }
                IconButton(onClick = onExit) { Icon(Icons.Filled.Close, "Sair do Modo Peregrino", tint = Color.White) }
            }
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF202020))) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("PERCORRIDOS", color = Color(0xFFB8C8B8), fontWeight = FontWeight.Bold)
                    Text("${fmtKm(routeKm)} km", color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.displaySmall)
                    Text("RESTANTES", color = Color(0xFFB8C8B8), fontWeight = FontWeight.Bold)
                    Text("${fmtKm(remaining)} km", color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.displaySmall)
                    LinearProgressIndicator(progress = { progress.toFloat() }, Modifier.fillMaxWidth().height(12.dp), color = Color(0xFF36D56E), trackColor = Color(0xFF354238))
                }
            }
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF202020))) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, null, tint = Color(0xFF8DFF9A), modifier = Modifier.size(34.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text("PRÓXIMO APOIO", color = Color(0xFFB8C8B8), fontWeight = FontWeight.Bold); Text(state.nextApoi?.name ?: "Sem APOI publicado", color = Color.White, fontWeight = FontWeight.ExtraBold); state.nextApoiDistanceKm?.let { Text(fmtDistance(it), color = Color(0xFF8DFF9A), fontWeight = FontWeight.Bold) } }
                }
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = onOpenApoi, Modifier.fillMaxWidth().height(62.dp), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF36D56E))) { Text("APOIOS", color = Color.Black, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge) }
            Button(onClick = onOpenSos, Modifier.fillMaxWidth().height(62.dp), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = EmergencyRed)) { Text("SOS", color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge) }
        }
    }
}

@Composable
internal fun SettingsSurfaceV1(
    pilgrimModeOnStart: Boolean,
    onTogglePilgrimModeOnStart: (Boolean) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onNavigate: (WalkingSurface) -> Unit
) {
    Scaffold(containerColor = NavSurface, bottomBar = { BottomNavBarV1(WalkingSurface.MORE, onNavigate) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Definições", color = NavBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
            Text("Preferências da aplicação e do dispositivo.", color = NavMuted)

            Card(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Modo Peregrino", fontWeight = FontWeight.ExtraBold)
                    Text("Ao ativar, a aplicação reabre a próxima caminhada neste modo simplificado.", color = NavMuted)
                    androidx.compose.material3.Switch(
                        checked = pilgrimModeOnStart,
                        onCheckedChange = onTogglePilgrimModeOnStart
                    )
                }
            }

            Card(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Notificações", fontWeight = FontWeight.ExtraBold)
                    Text("As permissões e canais de notificação são geridos pelo Android.", color = NavMuted)
                    OutlinedButton(onClick = onOpenNotificationSettings, Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Notifications, null)
                        Spacer(Modifier.width(8.dp))
                        Text("ABRIR DEFINIÇÕES DE NOTIFICAÇÕES")
                    }
                }
            }

            Card(
                Modifier.fillMaxWidth(),
                RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Unidades", fontWeight = FontWeight.ExtraBold)
                    Text("Distâncias apresentadas em quilómetros e metros.", color = NavMuted)
                }
            }
        }
    }
}

@Composable
internal fun MoreSurfaceV1(onPrepare: () -> Unit, onMap: () -> Unit, onApoi: () -> Unit, onDiary: () -> Unit, onHelp: () -> Unit, onContact: () -> Unit, onAbout: () -> Unit, onSmartwatch: () -> Unit, onSos: () -> Unit, onPilgrimMode: () -> Unit, onSettings: () -> Unit, onNavigate: (WalkingSurface) -> Unit) {
    Scaffold(containerColor = NavSurface, bottomBar = { BottomNavBarV1(WalkingSurface.MORE, onNavigate) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Mais", color = NavBlue, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
            MoreAction("Percursos", Icons.Filled.DirectionsWalk, "Escolher e preparar um percurso", onPrepare)
            MoreAction("Caminhada", Icons.Filled.Map, "Abrir a caminhada atual", onMap)
            MoreAction("Apoios / POI", Icons.Filled.Place, "Consultar apoios", onApoi)
            MoreAction("Diário", Icons.Filled.Book, "Registar notas e fotografias", onDiary)
            MoreAction("Smartwatch", Icons.Filled.Smartphone, "Notificações e compatibilidade externa", onSmartwatch)
            MoreAction("Modo Peregrino", Icons.Filled.DirectionsWalk, "Interface simplificada para caminhar", onPilgrimMode)
            MoreAction("SOS / Emergência", Icons.Filled.ReportProblem, "Acesso a 112 e localização", onSos)
            MoreAction("Definições", Icons.Filled.Settings, "Preferências da aplicação e notificações", onSettings)
            MoreAction("Ajuda", Icons.Filled.HelpOutline, "Como utilizar o produto", onHelp)
            MoreAction("Contacto", Icons.Filled.Call, "Abrir aplicação de comunicação", onContact)
            MoreAction("Sobre", Icons.Filled.Info, "Versão e dados do produto", onAbout)
        }
    }
}

@Composable
private fun MoreAction(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, subtitle: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick).semantics { role = Role.Button; contentDescription = title }, RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = NavBlue, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.ExtraBold); Text(subtitle, color = NavMuted, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun EmptyProductCard(message: String) {
    Card(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(message, fontWeight = FontWeight.Bold)
            Text("Os dados publicados determinam o que a aplicação pode apresentar.", color = NavMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun categoryTitle(category: com.caminhos2027.v1.core.model.ApoiCategory): String =
    category.name.lowercase(Locale("pt", "PT")).replaceFirstChar { it.titlecase(Locale("pt", "PT")) }

private fun costLabel(model: com.caminhos2027.v1.core.model.ApoiCostModel): String = when (model) {
    com.caminhos2027.v1.core.model.ApoiCostModel.FREE -> "Gratuito"
    com.caminhos2027.v1.core.model.ApoiCostModel.OPTIONAL_CONTRIBUTION -> "Contribuição opcional"
    com.caminhos2027.v1.core.model.ApoiCostModel.PAID -> "Pago"
    com.caminhos2027.v1.core.model.ApoiCostModel.UNKNOWN -> "Custo desconhecido"
}

private fun fmtKm(value: Double) = String.format(Locale("pt", "PT"), "%.2f", value.coerceAtLeast(0.0))
private fun formatCoord(value: Double): String = String.format(Locale.US, "%.5f", value)
private fun formatCoord6(value: Double): String = String.format(Locale.US, "%.6f", value)
private fun fmtDistance(value: Double) = if (value < 1.0) String.format(Locale("pt", "PT"), "%.0f m", value * 1000.0) else String.format(Locale("pt", "PT"), "%.1f km", value)
