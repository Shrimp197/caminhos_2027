package com.caminhos2027.v1.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.caminhos2027.v1.core.data.AndroidRouteOption
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.route.GpsState
import com.caminhos2027.v1.core.walking.WalkingState
import java.util.Locale
import kotlin.math.max

private val V2Forest = Color(0xFF0E6546)
private val V2ForestSoft = Color(0xFFE6F2EB)
private val V2Sand = Color(0xFFF6F3EC)
private val V2Muted = Color(0xFF68736D)
private val V2Warning = Color(0xFF9A5A00)

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
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(route.officialName, color = V2Forest, fontWeight = FontWeight.ExtraBold)
                Text(gpsLabel(state.gpsState), color = gpsColor(state.gpsState), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = onStop) { Text("PARAR") }
        }

        CartographicRouteMap(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp),
            geometry = route.geometry.points,
            projectedPoint = projectedPoint,
            gpsState = state.gpsState
        )

        Card(Modifier.fillMaxWidth().padding(12.dp), RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("${fmt(currentKm)} km", "Percorridos", Modifier.weight(1f))
                    MetricCard("${fmt(remainingKm)} km", "Para o fim", Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Progresso", color = V2Muted, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Text("${(progress * 100).toInt()}%", color = V2Forest, fontWeight = FontWeight.ExtraBold)
                }
                LinearProgressIndicator(progress = progress.toFloat(), modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(10.dp)), color = V2Forest, trackColor = V2ForestSoft)
                Text("Destino planeado · ${fmt(destinationKm)} km", color = V2Muted, style = MaterialTheme.typography.bodySmall)
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
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onOpenApoi, Modifier.weight(1f)) { Text("VER APOIOS") }
                    OutlinedButton(onClick = onOpenDecision, Modifier.weight(1f)) { Text("OPÇÕES") }
                }
                if (isTest) {
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("QA · controlo do percurso", color = V2Warning, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                        if (state.gpsState != GpsState.ON_ROUTE) androidx.compose.material3.Icon(Icons.Filled.WarningAmber, null, tint = V2Warning)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onQaAdvance, Modifier.weight(1f)) { Text("AVANÇAR") }
                        OutlinedButton({ onQaToggleGps(false) }, Modifier.weight(1f)) { Text("PERDER GPS") }
                        OutlinedButton({ onQaToggleGps(true) }, Modifier.weight(1f)) { Text("RECUPERAR") }
                    }
                    OutlinedButton(onQaDeviation, Modifier.fillMaxWidth()) { Text("SIMULAR DESVIO") }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun CartographicRouteMap(modifier: Modifier, geometry: List<GeoPoint>, projectedPoint: GeoPoint?, gpsState: GpsState) {
    val points = geometry.filter { it.latitude.isFinite() && it.longitude.isFinite() }
    val html = cartographicMapHtml(points, projectedPoint)
    Card(modifier, RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        webViewClient = WebViewClient()
                        loadDataWithBaseURL("https://www.openstreetmap.org/", html, "text/html", "UTF-8", null)
                    }
                },
                update = { view -> view.loadDataWithBaseURL("https://www.openstreetmap.org/", html, "text/html", "UTF-8", null) }
            )
            Card(Modifier.align(Alignment.TopStart).padding(12.dp), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .96f))) {
                Column(Modifier.padding(12.dp)) {
                    Text("A MINHA POSIÇÃO", color = V2Forest, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium)
                    Text(gpsLabel(gpsState), color = gpsColor(gpsState), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                }
            }
            Card(Modifier.align(Alignment.BottomStart).padding(12.dp), RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = .96f))) {
                Text("MAPA CARTOGRÁFICO · OPENSTREETMAP", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = V2Forest, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun cartographicMapHtml(points: List<GeoPoint>, projected: GeoPoint?): String {
    if (points.isEmpty()) return "<html><body style='margin:0;background:#eee'><div style='padding:24px;font:16px sans-serif'>Percurso sem geometria cartográfica.</div></body></html>"
    val coords = points.joinToString(",") { "[${it.latitude},${it.longitude}]" }
    val marker = projected?.takeIf { it.latitude.isFinite() && it.longitude.isFinite() }?.let { "[${it.latitude},${it.longitude}]" } ?: "null"
    return """
<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1,user-scalable=no'><style>html,body,#m{margin:0;width:100%;height:100%;overflow:hidden;background:#e8e5dc}canvas{display:block;width:100%;height:100%}.a{position:absolute;right:8px;bottom:5px;background:rgba(255,255,255,.9);padding:3px 6px;border-radius:4px;font:10px sans-serif;color:#444}</style></head>
<body><canvas id='m'></canvas><div class='a'>© OpenStreetMap contributors</div><script>
const route=[$coords]; const me=$marker; const c=document.getElementById('m'),x=c.getContext('2d');
const tile=256, R=6378137;
function world(lat,lon,z){let s=tile*Math.pow(2,z);let X=(lon+180)/360*s;let y=(1-Math.log(Math.tan(lat*Math.PI/180)+1/Math.cos(lat*Math.PI/180))/Math.PI)/2*s;return [X,y]}
function zoomFit(){let minLat=90,maxLat=-90,minLon=180,maxLon=-180;route.forEach(p=>{minLat=Math.min(minLat,p[0]);maxLat=Math.max(maxLat,p[0]);minLon=Math.min(minLon,p[1]);maxLon=Math.max(maxLon,p[1])});let span=Math.max(maxLat-minLat,(maxLon-minLon)*Math.max(.25,Math.cos(((minLat+maxLat)/2)*Math.PI/180)));let z=span<.35?12:span<.8?11:span<1.6?10:span<3.2?9:8;return {z,lat:(minLat+maxLat)/2,lon:(minLon+maxLon)/2}}
function draw(){let d=devicePixelRatio||1,w=c.clientWidth,h=c.clientHeight;c.width=w*d;c.height=h*d;x.setTransform(d,0,0,d,0,0);x.fillStyle='#dfe7d5';x.fillRect(0,0,w,h);let f=zoomFit(),z=f.z,center=world(f.lat,f.lon,z),imgs=[];let cols=Math.ceil(w/tile)+2,rows=Math.ceil(h/tile)+2;let ox=w/2-center[0],oy=h/2-center[1];for(let tx=Math.floor((center[0]-w/2)/tile);tx<Math.floor((center[0]+w/2)/tile)+1;tx++)for(let ty=Math.floor((center[1]-h/2)/tile);ty<Math.floor((center[1]+h/2)/tile)+1;ty++){let n=1<<z,wx=((tx%n)+n)%n,wy=ty;if(wy<0||wy>=n)continue;let im=new Image();im.crossOrigin='anonymous';im.onload=()=>{x.drawImage(im,tx*tile+ox,ty*tile+oy,tile,tile);drawRoute(ox,oy,z);};im.src='https://tile.openstreetmap.org/'+z+'/'+wx+'/'+wy+'.png';imgs.push(im)}drawRoute(ox,oy,z)}
function drawRoute(ox,oy,z){x.save();x.lineJoin='round';x.lineCap='round';x.beginPath();route.forEach((p,i)=>{let q=world(p[0],p[1],z);let X=q[0]+ox,Y=q[1]+oy;i?x.lineTo(X,Y):x.moveTo(X,Y)});x.strokeStyle='white';x.lineWidth=10;x.stroke();x.strokeStyle='#0e6546';x.lineWidth=5;x.stroke();let a=world(route[0][0],route[0][1],z),b=world(route[route.length-1][0],route[route.length-1][1],z);dot(a[0]+ox,a[1]+oy,'#0e6546');dot(b[0]+ox,b[1]+oy,'#c28a16');if(me){let q=world(me[0],me[1],z);dot(q[0]+ox,q[1]+oy,'#0e6546',10,true)}x.restore()}
function dot(X,Y,col,r=7,ring=false){x.beginPath();x.arc(X,Y,r+(ring?7:0),0,Math.PI*2);x.fillStyle='white';x.fill();x.beginPath();x.arc(X,Y,r,0,Math.PI*2);x.fillStyle=col;x.fill()}
window.addEventListener('resize',draw);draw();
</script></body></html>
""".trimIndent()
}

@Composable private fun MetricCard(value: String, label: String, modifier: Modifier) {
    Card(modifier, RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = V2ForestSoft)) {
        Column(Modifier.padding(12.dp)) { androidx.compose.material3.Text(value, color = V2Forest, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge); androidx.compose.material3.Text(label, color = V2Muted, style = MaterialTheme.typography.bodySmall) }
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
