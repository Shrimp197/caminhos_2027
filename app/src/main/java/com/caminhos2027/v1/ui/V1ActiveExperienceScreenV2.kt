package com.caminhos2027.v1.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.caminhos2027.v1.core.data.AndroidRouteOption
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.walking.WalkingState
import java.util.Locale

private val V2Forest = Color(0xFF0E6546)
private val V2ForestSoft = Color(0xFFE6F2EB)
private val V2Sand = Color(0xFFF6F3EC)
private val V2Muted = Color(0xFF68736D)
private val V2Warning = Color(0xFF9A5A00)
private val V2WarningSoft = Color(0xFFFFF1D9)

@Composable
internal fun V1ActiveExperienceScreenV2(
    state: WalkingState,
    route: Route,
    routeOptions: List<AndroidRouteOption>,
    onStop: () -> Unit,
    onOpenApoi: () -> Unit,
    onOpenDecision: () -> Unit,
    onQaAdvance: () -> Unit,
    onQaToggleGps: (Boolean) -> Unit,
    onQaDeviation: () -> Unit
) {
    val currentKm = state.routePosition?.routeKm ?: 0.0
    val remainingKm = state.progress?.remainingKm ?: 0.0
    val progress = state.progress?.progressRatio?.coerceIn(0.0, 1.0) ?: 0.0
    val destinationKm = state.walk.plannedDestinationKm ?: 0.0
    val projectedPoint = state.routePosition?.projectedPoint
    val isTest = routeOptions.firstOrNull { it.id == route.id }?.testOnly == true

    Column(Modifier.fillMaxSize().background(V2Sand)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(route.officialName, color = V2Forest, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                Text(gpsLabel(state.gpsState), color = gpsColor(state.gpsState), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = onStop) { Text("PARAR") }
        }

        RealMapCard(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp),
            geometry = route.geometry.points,
            projectedPoint = projectedPoint,
            routeKm = currentKm,
            gpsState = state.gpsState
        )

        Card(
            Modifier.fillMaxWidth().padding(12.dp),
            RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("${fmt(currentKm)} km", "Percorridos", Modifier.weight(1f))
                    MetricCard("${fmt(remainingKm)} km", "Para o fim", Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Progresso", color = V2Muted, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Text("${(progress * 100).toInt()}%", color = V2Forest, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                }
                LinearProgressIndicator(
                    progress = progress.toFloat(),
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(10.dp)),
                    color = V2Forest,
                    trackColor = V2ForestSoft
                )
                Text("Destino planeado · ${fmt(destinationKm)} km", color = V2Muted, style = MaterialTheme.typography.bodySmall)
                HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = V2Forest)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("A minha posição", color = V2Forest, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                        Text(positionDetail(state), color = V2Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
                state.nextApoi?.let { apoi ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Place, contentDescription = null, tint = V2Forest)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Próximo APOI", color = V2Muted, style = MaterialTheme.typography.labelLarge)
                            Text(apoi.name, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                        }
                        Text(state.nextApoiDistanceKm?.let(::fmtDistance) ?: "—", color = V2Forest, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onOpenApoi, Modifier.weight(1f)) { Text("VER APOIOS") }
                    OutlinedButton(onClick = onOpenDecision, Modifier.weight(1f)) { Text("OPÇÕES") }
                }
                if (isTest) {
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("QA · controlo do percurso", color = V2Warning, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                        if (state.gpsState != GpsState.ON_ROUTE) Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = V2Warning)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onQaAdvance, Modifier.weight(1f)) { Text("AVANÇAR") }
                        OutlinedButton({ onQaToggleGps(false) }, Modifier.weight(1f)) { Text("PERDER GPS") }
                        OutlinedButton({ onQaToggleGps(true) }, Modifier.weight(1f)) { Text("RECUPERAR") }
                    }
                    OutlinedButton(onQaDeviation, Modifier.fillMaxWidth()) { Text("SIMULAR DESVIO") }
                    Card(Modifier.fillMaxWidth(), RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = V2WarningSoft)) {
                        Text("Ambiente de teste: estes controlos permitem validar GPS, desvio e progressão sem depender do movimento físico.", Modifier.padding(10.dp), color = V2Warning, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun RealMapCard(
    modifier: Modifier,
    geometry: List<GeoPoint>,
    projectedPoint: GeoPoint?,
    routeKm: Double,
    gpsState: GpsState
) {
    val points = remember(geometry) { geometry.filter { it.latitude.isFinite() && it.longitude.isFinite() } }
    val routeJs = remember(points) { points.toJsPoints() }
    val startJs = remember(points) { points.firstOrNull()?.toJsPoint() ?: "null" }
    val projectedJs = projectedPoint?.takeIf { it.latitude.isFinite() && it.longitude.isFinite() }?.toJsPoint() ?: "null"
    val html = remember { realMapHtml() }

    Card(
        modifier,
        RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)),
                factory = { context ->
                    WebView(context).apply {
                        setBackgroundColor(android.graphics.Color.WHITE)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        settings.userAgentString = "Caminhos2027/1.0 (Android; OpenStreetMap)"
                        webViewClient = WebViewClient()
                        loadDataWithBaseURL("https://appassets.androidplatform.net/", html, "text/html", "UTF-8", null)
                    }
                },
                update = { webView ->
                    if (webView.url != null) {
                        webView.evaluateJavascript("setMapData($routeJs,$startJs,$projectedJs,${routeKm},'${gpsState.name}');", null)
                    }
                }
            )
            Card(
                Modifier.align(Alignment.TopStart).padding(12.dp),
                RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .95f))
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("A MINHA POSIÇÃO", color = V2Forest, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium)
                    Text("${fmt(routeKm)} km", style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                    Text(gpsLabel(gpsState), color = gpsColor(gpsState), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                }
            }
            Card(
                Modifier.align(Alignment.BottomStart).padding(12.dp).border(1.dp, V2ForestSoft, RoundedCornerShape(14.dp)),
                RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .95f))
            ) {
                Text("Percurso oficial · OpenStreetMap", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = V2Forest, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            }
        }
    }
}

private fun List<GeoPoint>.toJsPoints(): String = joinToString(prefix = "[", postfix = "]") { it.toJsPoint() }

private fun GeoPoint.toJsPoint(): String = "{lat:${latitude},lon:${longitude}}"

private fun realMapHtml(): String = """
<!doctype html>
<html lang="pt">
<head>
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
<style>
html,body,#map{margin:0;width:100%;height:100%;overflow:hidden;background:#f7f5ef;font-family:Arial,sans-serif}
#tiles{position:absolute;inset:0;overflow:hidden;background:#e6e2d8}
.tile{position:absolute;width:256px;height:256px;image-rendering:auto}
#route{position:absolute;inset:0;pointer-events:none}
.road{fill:none;stroke:white;stroke-width:12;stroke-linecap:round;stroke-linejoin:round}
.route{fill:none;stroke:#0e6546;stroke-width:7;stroke-linecap:round;stroke-linejoin:round}
.halo{fill:#e6f2eb;stroke:white;stroke-width:5}
.marker{fill:#0e6546;stroke:white;stroke-width:4}
#credit{position:absolute;right:6px;bottom:5px;padding:2px 5px;background:rgba(255,255,255,.82);font-size:10px;color:#45534c;border-radius:4px}
#status{position:absolute;right:10px;top:10px;padding:6px 8px;background:rgba(255,255,255,.9);font-size:11px;color:#4f5b55;border-radius:8px;display:none}
</style>
</head>
<body>
<div id="map"><div id="tiles"></div><svg id="route" preserveAspectRatio="none"></svg><div id="status"></div><div id="credit">© OpenStreetMap contributors</div></div>
<script>
let points=[], start=null, user=null, km=0, gps='ACQUIRING';
let center={lat:39.65,lon:-8.0}, zoom=10;
const tileSize=256;
const $=id=>document.getElementById(id);
const clamp=(v,a,b)=>Math.max(a,Math.min(b,v));
function world(p,z){const s=tileSize*Math.pow(2,z),x=(p.lon+180)/360*s,lat=Math.max(-85.05112878,Math.min(85.05112878,p.lat));const r=lat*Math.PI/180;const y=(1-Math.log(Math.tan(r)+1/Math.cos(r))/Math.PI)/2*s;return{x,y};}
function screen(p){const w=world(p,zoom),c=world(center,zoom);return{x:w.x-c.x+window.innerWidth/2,y:w.y-c.y+window.innerHeight/2};}
function fit(p){if(!p.length)return;let minLat=1e9,maxLat=-1e9,minLon=1e9,maxLon=-1e9;p.forEach(q=>{minLat=Math.min(minLat,q.lat);maxLat=Math.max(maxLat,q.lat);minLon=Math.min(minLon,q.lon);maxLon=Math.max(maxLon,q.lon)});center={lat:(minLat+maxLat)/2,lon:(minLon+maxLon)/2};const spans=Math.max(maxLat-minLat,maxLon-minLon,0.02);zoom=clamp(Math.floor(Math.log2(50/spans)),7,15);}
function renderTiles(){const box=$('tiles');box.innerHTML='';const c=world(center,zoom),cols=Math.ceil(window.innerWidth/tileSize)+2,rows=Math.ceil(window.innerHeight/tileSize)+2;const tx0=Math.floor((c.x-window.innerWidth/2)/tileSize)-1,ty0=Math.floor((c.y-window.innerHeight/2)/tileSize)-1;const n=Math.pow(2,zoom);for(let ty=0;ty<rows;ty++)for(let tx=0;tx<cols;tx++){let x=tx0+tx,y=ty0+ty;while(x<0)x+=n;while(x>=n)x-=n;if(y<0||y>=n)continue;const img=document.createElement('img');img.className='tile';img.src=`https://tile.openstreetmap.org/${zoom}/${x}/${y}.png`;img.alt='';img.style.left=(x*tileSize-c.x+window.innerWidth/2)+'px';img.style.top=(y*tileSize-c.y+window.innerHeight/2)+'px';box.appendChild(img)}}
function renderRoute(){const svg=$('route');svg.setAttribute('viewBox',`0 0 ${window.innerWidth} ${window.innerHeight}`);svg.innerHTML='';if(!points.length)return;const path=points.map((p,i)=>{const s=screen(p);return(i?'L':'M')+s.x.toFixed(1)+' '+s.y.toFixed(1)}).join(' ');const a=document.createElementNS('http://www.w3.org/2000/svg','path');a.setAttribute('class','road');a.setAttribute('d',path);svg.appendChild(a);const b=document.createElementNS('http://www.w3.org/2000/svg','path');b.setAttribute('class','route');b.setAttribute('d',path);svg.appendChild(b);if(user){const s=screen(user);const h=document.createElementNS('http://www.w3.org/2000/svg','circle');h.setAttribute('class','halo');h.setAttribute('cx',s.x);h.setAttribute('cy',s.y);h.setAttribute('r','19');svg.appendChild(h);const m=document.createElementNS('http://www.w3.org/2000/svg','circle');m.setAttribute('class','marker');m.setAttribute('cx',s.x);m.setAttribute('cy',s.y);m.setAttribute('r','10');svg.appendChild(m)}}
function render(){renderTiles();renderRoute();const st=$('status');st.textContent=gps==='NO_SIGNAL'?'GPS sem sinal':gps==='ON_ROUTE'?'GPS no percurso':gps==='POSSIBLE_DEVIATION'?'Possível desvio':gps==='PROBABLE_DEVIATION'?'Provável desvio':'A obter sinal GPS';st.style.display='block';}
function setMapData(route,startPoint,userPoint,currentKm,gpsState){points=route||[];start=startPoint;user=userPoint;km=currentKm;gps=gpsState||'ACQUIRING';if(points.length)fit(points);render();}
window.addEventListener('resize',render);window.setMapData=setMapData;
</script>
</body>
</html>
""".trimIndent()

@Composable
private fun MetricCard(value: String, label: String, modifier: Modifier) {
    Card(modifier, RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = V2ForestSoft)) {
        Column(Modifier.padding(12.dp)) {
            Text(value, color = V2Forest, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
            Text(label, color = V2Muted, style = MaterialTheme.typography.bodySmall)
        }
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
    GpsState.NO_SIGNAL, GpsState.POSSIBLE_DEVIATION, GpsState.PROBABLE_DEVIATION -> V2Warning
    GpsState.ACQUIRING -> V2Muted
}

private fun positionDetail(state: WalkingState): String {
    val routeKm = state.routePosition?.routeKm?.let(::fmt) ?: "—"
    val routeDistance = state.routePosition?.distanceToRouteMeters?.let(::fmtMeters) ?: "—"
    val confidence = state.routePosition?.confidence?.name?.lowercase(Locale("pt", "PT")) ?: "desconhecida"
    return "Km no percurso: $routeKm · distância ao traçado: $routeDistance · confiança: $confidence"
}

private fun fmt(value: Double) = String.format(Locale("pt", "PT"), "%.2f", value)
private fun fmtDistance(value: Double) = if (value < 1.0) String.format(Locale("pt", "PT"), "%.0f m", value * 1000.0) else String.format(Locale("pt", "PT"), "%.1f km", value)
private fun fmtMeters(value: Double) = if (value >= 1000.0) String.format(Locale("pt", "PT"), "%.1f km", value / 1000.0) else String.format(Locale("pt", "PT"), "%.0f m", value)
