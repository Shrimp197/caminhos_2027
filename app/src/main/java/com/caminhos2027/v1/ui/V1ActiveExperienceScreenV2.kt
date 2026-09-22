package com.caminhos2027.v1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebSettings
import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.TextView
import com.caminhos2027.v1.core.data.AndroidRouteOption
import com.caminhos2027.v1.core.model.AudioMode
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.MapOrientation
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.walking.WalkingState
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlin.math.cos

private val V2Forest = Color(0xFF0E6546)
private val V2ForestSoft = Color(0xFFE6F2EB)
private val V2Map = Color(0xFFF0F0E9)
private val V2Road = Color(0xFFC9C7BF)
private val V2MajorRoad = Color(0xFFB1AEA4)
private val V2Muted = Color(0xFF68736D)

@Composable
internal fun V1ActiveExperienceScreenV2(
    state: WalkingState,
    route: Route,
    routeOptions: List<AndroidRouteOption>,
    onStop: () -> Unit,
    onTogglePause: () -> Unit,
    onOpenApoi: () -> Unit,
    onOpenDecision: () -> Unit,
    onQaAdvance: () -> Unit,
    onQaToggleGps: (Boolean) -> Unit,
    onQaDeviation: () -> Unit
) {
    val currentKm = state.routePosition?.routeKm ?: 0.0
    val remainingKm = state.progress?.remainingKm ?: 0.0
    var nowMillis by remember(state.walk.startedAt) { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.walk.startedAt) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            kotlinx.coroutines.delay(1_000)
        }
    }
    val elapsed = elapsedWalkingTime(
        startedAt = state.walk.startedAt,
        nowMillis = nowMillis,
        pausedAt = state.pausedAt,
        pausedDurationSeconds = state.pausedDurationSeconds
    )
    val progress = state.progress?.progressRatio?.coerceIn(0.0, 1.0) ?: 0.0
    val destinationKm = state.walk.plannedDestinationKm ?: 0.0
    val projectedPoint = state.routePosition?.projectedPoint

    ActiveWalkingAudioFeedback(state)

    Column(Modifier.fillMaxSize().background(Color(0xFFF7F5EF))) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(route.officialName, color = V2Forest, fontWeight = FontWeight.ExtraBold)
                Text(
                    if (state.isPaused) "GPS em pausa" else gpsLabel(state.gpsState),
                    color = if (state.isPaused) Color(0xFF7A4A00) else gpsColor(state.gpsState),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "MAPA REAL · OPENSTREETMAP",
                    color = V2Muted,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            OutlinedButton(onClick = onStop) { Text("PARAR") }
        }

        RealOpenStreetMap(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp),
            geometry = route.geometry.points,
            projectedPoint = projectedPoint,
            gpsState = state.gpsState,
            mapOrientation = state.walk.preparation.mapOrientation,
            nextApoi = state.nextApoi
        )
        Text(
            "MAPA REAL · OPENSTREETMAP",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp),
            color = V2Muted,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall
        )

        Card(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .padding(12.dp),
            RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard("${fmt(currentKm)} km", "Percorridos", Modifier.weight(1f))
                    MetricCard("${fmt(remainingKm)} km", "Para o fim", Modifier.weight(1f))
                    MetricCard(elapsed, "Tempo", Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Progresso", color = V2Muted, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Text("${(progress * 100).toInt()}%", color = V2Forest, fontWeight = FontWeight.ExtraBold)
                }
                LinearProgressIndicator(progress = { progress.toFloat() }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(10.dp)), color = V2Forest, trackColor = V2ForestSoft)
                Text("Destino planeado · ${fmt(destinationKm)} km", color = V2Muted, style = MaterialTheme.typography.bodySmall)
                pauseRecommendationText(state)?.let { recommendation ->
                    Card(
                        Modifier.fillMaxWidth(),
                        RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = V2ForestSoft)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("PAUSA INTELIGENTE", color = V2Forest, fontWeight = FontWeight.ExtraBold)
                            Text(recommendation, color = V2Forest, fontWeight = FontWeight.SemiBold)
                            Text("Pode parar agora e retomar quando quiser.", color = V2Muted, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(Icons.Filled.LocationOn, null, tint = V2Forest)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("A minha posição", color = V2Forest, fontWeight = FontWeight.ExtraBold)
                        Text(positionDetail(state), color = V2Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
                state.nextApoi?.let { apoi ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(Icons.Filled.Place, null, tint = V2Forest)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Próximo APOI", color = V2Muted, style = MaterialTheme.typography.labelLarge)
                            Text(apoi.name, fontWeight = FontWeight.ExtraBold)
                        }
                        Text(state.nextApoiDistanceKm?.let(::fmtDistance) ?: "—", color = V2Forest, fontWeight = FontWeight.ExtraBold)
                    }
                }
                if (state.isPaused) {
                    Card(
                        Modifier.fillMaxWidth(),
                        RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1D9))
                    ) {
                        Text(
                            "CAMINHADA PAUSADA · a posição ficou guardada e o GPS não está a ser aceite.",
                            Modifier.padding(12.dp),
                            color = Color(0xFF7A4A00),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onTogglePause, Modifier.weight(1f)) {
                        Text(if (state.isPaused) "RETOMAR CAMINHADA" else "PAUSAR CAMINHADA")
                    }
                    OutlinedButton(onClick = onOpenApoi, Modifier.weight(1f)) { Text("VER APOIOS") }
                }
                OutlinedButton(onClick = onOpenDecision, Modifier.fillMaxWidth()) { Text("OPÇÕES") }
                if (routeOptions.firstOrNull { it.id == route.id }?.testOnly == true) {
                    HorizontalDivider()
                    Text("QA · percurso de teste", color = V2Muted, fontWeight = FontWeight.ExtraBold)
                    Text(
                        "Controlos apenas para validar GPS simulado. Não aparecem no percurso de produção.",
                        color = V2Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onClick = onQaAdvance, Modifier.weight(1f)) { Text("AVANÇAR GPS") }
                        OutlinedButton(onClick = { onQaToggleGps(false) }, Modifier.weight(1f)) { Text("PERDER GPS") }
                        OutlinedButton(onClick = { onQaToggleGps(true) }, Modifier.weight(1f)) { Text("RECUPERAR GPS") }
                    }
                    OutlinedButton(onClick = onQaDeviation, Modifier.fillMaxWidth()) { Text("SIMULAR DESVIO") }
                }
            }
        }
    }
}

@Composable
private fun ActiveWalkingAudioFeedback(state: WalkingState) {
    val mode = state.walk.preparation.audioMode
    if (mode == AudioMode.SILENT) return

    val context = LocalContext.current
    val feedback = remember(context) { AndroidWalkingAudioFeedback(context) }
    var previousGps by remember(state.walk.id) { mutableStateOf<GpsState?>(null) }
    var previousPaused by remember(state.walk.id) { mutableStateOf(state.isPaused) }
    var previousApoiId by remember(state.walk.id) { mutableStateOf(state.nextApoi?.id) }
    var previousPauseRecommended by remember(state.walk.id) { mutableStateOf(false) }

    DisposableEffect(feedback) {
        onDispose { feedback.release() }
    }

    val pauseRecommended = pauseRecommendationText(state) != null
    LaunchedEffect(state.walk.id, state.gpsState, state.isPaused, state.nextApoi?.id, pauseRecommended) {
        val message = when {
            !previousPaused && state.isPaused -> "Caminhada pausada."
            previousPaused && !state.isPaused -> "Caminhada retomada."
            previousGps == GpsState.ON_ROUTE && state.gpsState == GpsState.NO_SIGNAL ->
                "Sinal GPS perdido. A última posição foi mantida."
            previousGps == GpsState.NO_SIGNAL && state.gpsState == GpsState.ON_ROUTE ->
                "Sinal GPS recuperado. Está no percurso."
            state.gpsState == GpsState.POSSIBLE_DEVIATION && previousGps != GpsState.POSSIBLE_DEVIATION ->
                "Possível desvio do percurso."
            state.gpsState == GpsState.PROBABLE_DEVIATION && previousGps != GpsState.PROBABLE_DEVIATION ->
                "Provável desvio do percurso."
            !previousPauseRecommended && pauseRecommended ->
                "Pausa recomendada. Pode parar agora e retomar quando quiser."
            mode == AudioMode.IMMERSIVE && previousApoiId != null && previousApoiId != state.nextApoi?.id && state.nextApoi != null ->
                "Próximo APOI: " + state.nextApoi.name + ". " +
                    (state.nextApoiDistanceKm?.let(::fmtDistance) ?: "distância não disponível") + "."
            else -> null
        }
        message?.let { feedback.speak(it, mode) }
        previousGps = state.gpsState
        previousPaused = state.isPaused
        previousApoiId = state.nextApoi?.id
        previousPauseRecommended = pauseRecommended
    }
}

@Composable
private fun RealOpenStreetMap(
    modifier: Modifier,
    geometry: List<GeoPoint>,
    projectedPoint: GeoPoint?,
    gpsState: GpsState,
    mapOrientation: MapOrientation,
    nextApoi: com.caminhos2027.v1.core.model.Apoi?
) {
    val points = geometry.filter { it.latitude.isFinite() && it.longitude.isFinite() }
    val routeJs = remember(points) { points.joinToString(prefix = "[", postfix = "]") { point -> "[" + point.latitude + "," + point.longitude + "]" } }
    val projectedJs = projectedPoint?.takeIf { it.latitude.isFinite() && it.longitude.isFinite() }?.let { "[" + it.latitude + "," + it.longitude + "]" } ?: "null"
    val nextApoiJs = nextApoi?.location?.let { location ->
        if (location.latitude?.isFinite() == true && location.longitude?.isFinite() == true) {
            "[" + location.latitude + "," + location.longitude + "]"
        } else null
    } ?: "null"
    val rotation = if (mapOrientation == MapOrientation.WALK_DIRECTION) routeBearingDegrees(points, projectedPoint) else 0f
    val html = remember(routeJs, projectedJs, nextApoiJs, rotation) { openStreetMapHtml(routeJs, projectedJs, nextApoiJs, rotation) }
    Card(modifier, RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = V2Map), elevation = CardDefaults.cardElevation(2.dp)) {
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context -> WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    settings.loadsImagesAutomatically = true
                    settings.userAgentString = "${settings.userAgentString} CaminhosDoPeregrino/1.0"
                    tag = html
                    loadDataWithBaseURL("https://tile.openstreetmap.org/", html, "text/html", "UTF-8", null)
                }},
                update = { webView ->
                    if (webView.tag != html) {
                        webView.tag = html
                        webView.loadDataWithBaseURL("https://tile.openstreetmap.org/", html, "text/html", "UTF-8", null)
                    }
                }
            )
            Card(Modifier.align(Alignment.TopStart).padding(12.dp), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .96f))) {
                Column(Modifier.padding(12.dp)) {
                    Text("A MINHA POSIÇÃO", color = V2Forest, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium)
                    Text(gpsLabel(gpsState), color = gpsColor(gpsState), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                }
            }
            AndroidView(
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                factory = { context ->
                    TextView(context).apply {
                        text = "MAPA REAL · OPENSTREETMAP\n© OpenStreetMap contributors"
                        contentDescription = "MAPA REAL · OPENSTREETMAP"
                        gravity = Gravity.START
                        setTextColor(AndroidColor.rgb(14, 101, 70))
                        setTextSize(12f)
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                        setPadding(24, 16, 24, 16)
                        background = GradientDrawable().apply {
                            setColor(AndroidColor.argb(245, 255, 255, 255))
                            cornerRadius = 28f
                        }
                        isFocusable = false
                        importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_YES
                    }
                }
            )
        }
    }
}

private fun openStreetMapHtml(routeJs: String, projectedJs: String, nextApoiJs: String, rotation: Float): String = """
<!doctype html><html><head><meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no"><style>
html,body,#map{margin:0;width:100%;height:100%;overflow:hidden;background:#eef0eb}#tiles{position:absolute;inset:0;transform-origin:50% 50%}.tile{position:absolute;width:256px;height:256px}svg{position:absolute;inset:0;width:100%;height:100%;overflow:visible}.route{fill:none;stroke:white;stroke-width:12;stroke-linecap:round;stroke-linejoin:round}.route2{fill:none;stroke:#0e6546;stroke-width:7;stroke-linecap:round;stroke-linejoin:round}.marker{stroke:white;stroke-width:5}
</style></head><body><div id="map"><div id="tiles"></div><svg id="overlay"></svg></div><script>
const route=$routeJs,current=$projectedJs,nextApoi=$nextApoiJs,rotation=$rotation,size=256;const map=document.getElementById('map'),tiles=document.getElementById('tiles'),svg=document.getElementById('overlay');
function mercator(lat,lon,z){const n=Math.pow(2,z),x=(lon+180)/360*n,r=lat*Math.PI/180,y=(1-Math.asinh(Math.tan(r))/Math.PI)/2*n;return[x,y]}
function chooseZoom(){if(!route.length)return 12;let minLat=route[0][0],maxLat=route[0][0],minLon=route[0][1],maxLon=route[0][1];for(const q of route){minLat=Math.min(minLat,q[0]);maxLat=Math.max(maxLat,q[0]);minLon=Math.min(minLon,q[1]);maxLon=Math.max(maxLon,q[1])}const s=Math.max(maxLat-minLat,maxLon-minLon);return s>8?7:s>4?8:s>2?9:s>1?10:s>.5?11:12}
function render(){const z=chooseZoom(),world=Math.pow(2,z),lat=route.reduce((a,p)=>a+p[0],0)/route.length,lon=route.reduce((a,p)=>a+p[1],0)/route.length,c=mercator(lat,lon,z),w=map.clientWidth,h=map.clientHeight;tiles.innerHTML='';const bx=Math.floor(c[0]),by=Math.floor(c[1]),left=w/2-(c[0]-bx)*size,top=h/2-(c[1]-by)*size;for(let dx=-5;dx<=5;dx++)for(let dy=-4;dy<=4;dy++){let tx=((bx+dx)%world+world)%world,ty=by+dy;if(ty<0||ty>=world)continue;let img=document.createElement('img');img.className='tile';img.src='https://tile.openstreetmap.org/'+z+'/'+tx+'/'+ty+'.png';img.style.left=(left+dx*size)+'px';img.style.top=(top+dy*size)+'px';tiles.appendChild(img)}if(!route.length)return;svg.setAttribute('viewBox','0 0 '+w+' '+h);function p(q){let m=mercator(q[0],q[1],z);return[w/2+(m[0]-c[0])*size,h/2+(m[1]-c[1])*size]}let d=route.map((q,i)=>{let p0=p(q);return(i?'L':'M')+p0[0].toFixed(1)+' '+p0[1].toFixed(1)}).join(' '),s=p(route[0]),e=p(route[route.length-1]);svg.innerHTML='<path class="route" d="'+d+'"/><path class="route2" d="'+d+'"/><circle class="marker" fill="#0e6546" r="9" cx="'+s[0]+'" cy="'+s[1]+'"/><circle class="marker" fill="#c28a16" r="9" cx="'+e[0]+'" cy="'+e[1]+'"/>';if(current){let q=p(current);svg.innerHTML+='<circle class="marker" fill="#1464c8" r="11" cx="'+q[0]+'" cy="'+q[1]+'"/>'}if(nextApoi){let q=p(nextApoi);svg.innerHTML+='<circle class="marker" fill="#8b5cf6" r="10" cx="'+q[0]+'" cy="'+q[1]+'"/><circle fill="white" r="4" cx="'+q[0]+'" cy="'+q[1]+'"/>'}tiles.style.transform='rotate('+(-rotation)+'deg)';svg.style.transform='rotate('+(-rotation)+'deg)';svg.style.transformOrigin='50% 50%';}window.addEventListener('resize',render);setTimeout(render,40);
</script></body></html>
""".trimIndent()

private fun routeBearingDegrees(route: List<GeoPoint>, projected: GeoPoint?): Float {
    if (route.size < 2 || projected == null) return 0f
    val index = route.indices.minByOrNull { index ->
        val point = route[index]
        val dLat = point.latitude - projected.latitude
        val dLon = point.longitude - projected.longitude
        dLat * dLat + dLon * dLon
    } ?: return 0f
    val from = if (index < route.lastIndex) route[index] else route[index - 1]
    val to = if (index < route.lastIndex) route[index + 1] else route[index]
    val meanLat = (from.latitude + to.latitude) / 2.0
    val east = (to.longitude - from.longitude) * cos(meanLat * Math.PI / 180.0)
    val north = to.latitude - from.latitude
    if (east == 0.0 && north == 0.0) return 0f
    return Math.toDegrees(kotlin.math.atan2(east, north)).toFloat()
}

@Composable private fun MetricCard(value: String, label: String, modifier: Modifier) {
    Card(\n        modifier.semantics { contentDescription = label },\n        RoundedCornerShape(16.dp),\n        colors = CardDefaults.cardColors(containerColor = V2ForestSoft)\n    ) {
        Column(Modifier.padding(12.dp)) { Text(value, color = V2Forest, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge); Text(label, color = V2Muted, style = MaterialTheme.typography.bodySmall) }
    }
}

private fun gpsLabel(state: GpsState) = when (state) {
    GpsState.NO_SIGNAL -> "GPS sem sinal"
    GpsState.ACQUIRING -> "A obter sinal GPS"
    GpsState.ON_ROUTE -> "GPS no percurso"
    GpsState.POSSIBLE_DEVIATION -> "Possível desvio"
    GpsState.PROBABLE_DEVIATION -> "Provável desvio"
}

private fun gpsColor(state: GpsState) = when (state) {
    GpsState.ON_ROUTE -> V2Forest
    GpsState.NO_SIGNAL, GpsState.POSSIBLE_DEVIATION, GpsState.PROBABLE_DEVIATION -> Color(0xFF9A5A00)
    GpsState.ACQUIRING -> V2Muted
}

private fun positionDetail(state: WalkingState): String {
    val routeKm = state.routePosition?.routeKm?.let(::fmt) ?: "—"
    val routeDistance = state.routePosition?.distanceToRouteMeters?.let(::fmtMeters) ?: "—"
    val confidence = state.routePosition?.confidence?.name?.lowercase(Locale("pt", "PT")) ?: "desconhecida"
    return "Km no percurso: $routeKm · distância ao traçado: $routeDistance · confiança: $confidence"
}

private fun pauseRecommendationText(state: WalkingState): String? {
    if (state.isPaused) return null
    state.pauseRecommendation?.let { return it }
    val startedAt = state.walk.startedAt ?: return null
    val now = Instant.now()
    val currentPauseSeconds = state.pausedAt?.let { Duration.between(it, now).seconds.coerceAtLeast(0L) } ?: 0L
    val activeSeconds = (
        Duration.between(startedAt, now).seconds -
            state.pausedDurationSeconds.coerceAtLeast(0L) -
            currentPauseSeconds
        ).coerceAtLeast(0L)
    val minutes = activeSeconds / 60L
    val threshold = state.walk.preparation.customBreakTimeMinutes?.takeIf { it > 0 } ?: 60
    return if (state.walk.preparation.intelligentBreaksEnabled && minutes >= threshold) {
        "Já passaram $minutes min de caminhada."
    } else null
}
private fun elapsedWalkingTime(
    startedAt: Instant?,
    nowMillis: Long,
    pausedAt: Instant?,
    pausedDurationSeconds: Long
): String {
    if (startedAt == null) return "—"
    val now = Instant.ofEpochMilli(nowMillis)
    val currentPauseSeconds = pausedAt?.let { Duration.between(it, now).seconds.coerceAtLeast(0L) } ?: 0L
    val seconds = (
        Duration.between(startedAt, now).seconds -
            pausedDurationSeconds.coerceAtLeast(0L) -
            currentPauseSeconds
        ).coerceAtLeast(0L)
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) String.format(Locale("pt", "PT"), "%dh %02dm", hours, minutes)
    else String.format(Locale("pt", "PT"), "%02dm", minutes)
}

private fun fmt(value: Double) = String.format(Locale("pt", "PT"), "%.2f", value)
private fun fmtDistance(value: Double) = if (value < 1.0) String.format(Locale("pt", "PT"), "%.0f m", value * 1000.0) else String.format(Locale("pt", "PT"), "%.1f km", value)
private fun fmtMeters(value: Double) = if (value >= 1000.0) String.format(Locale("pt", "PT"), "%.1f km", value / 1000.0) else String.format(Locale("pt", "PT"), "%.0f m", value)
