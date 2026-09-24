package com.caminhos2027.v1.ui

import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.offline.OfflineGeometryRegionDefinition
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.MapOrientation
import com.caminhos2027.v1.core.route.GpsState
import kotlin.math.cos

internal const val WALKING_MAP_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val OFFLINE_METADATA = "{\"routeId\":\"caminho-do-centenario\",\"kind\":\"walking-route-map\"}"
private const val OFFLINE_MIN_ZOOM = 9.0
private const val OFFLINE_MAX_ZOOM = 15.0

private data class OfflineUiState(
    val complete: Boolean = false,
    val active: Boolean = false,
    val percent: Int = 0,
    val message: String? = null
)

@Composable
internal fun RealWalkingMap(
    modifier: Modifier,
    routeId: String,
    geometry: List<GeoPoint>,
    projectedPoint: GeoPoint?,
    currentKm: Double,
    totalKm: Double,
    gpsState: GpsState,
    mapOrientation: MapOrientation,
    nextApoi: Apoi?
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember(context) { MapView(context) }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var styleReady by remember { mutableStateOf(false) }
    var offlineRegion by remember { mutableStateOf<OfflineRegion?>(null) }
    var cameraInitialized by remember { mutableStateOf(false) }
    var offlineState by remember { mutableStateOf(OfflineUiState()) }
    var currentMarker by remember { mutableStateOf<org.maplibre.android.annotations.Marker?>(null) }
    var nextApoiMarker by remember { mutableStateOf<org.maplibre.android.annotations.Marker?>(null) }
    val points = remember(geometry) { geometry.filter { it.latitude.isFinite() && it.longitude.isFinite() } }
    val currentPoint = projectedPoint ?: pointAtRouteKmForMap(points, currentKm, totalKm)

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.clip(RoundedCornerShape(24.dp)).background(Color(0xFFE8EDE7))
    )

    LaunchedEffect(mapView) {
        mapView.getMapAsync { loaded ->
            map = loaded
            loaded.uiSettings.isZoomGesturesEnabled = true
            loaded.uiSettings.isScrollGesturesEnabled = true
            loaded.uiSettings.isRotateGesturesEnabled = true
            loaded.uiSettings.isCompassEnabled = true
            loaded.setStyle(WALKING_MAP_STYLE_URL) { styleReady = true }
        }
    }

    LaunchedEffect(Unit) {
        inspectOfflineRegion(context, routeId) { region, state ->
            offlineRegion = region
            offlineState = state
        }
    }

    LaunchedEffect(map, styleReady, points) {
        val loaded = map ?: return@LaunchedEffect
        if (!styleReady || points.size < 2 || cameraInitialized) return@LaunchedEffect
        runCatching {
            val bounds = LatLngBounds.Builder()
            points.forEach { bounds.include(LatLng(it.latitude, it.longitude)) }
            loaded.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 70))
            cameraInitialized = true
        }
    }

    LaunchedEffect(map, styleReady, points) {
        val loaded = map ?: return@LaunchedEffect
        if (!styleReady || points.size < 2) return@LaunchedEffect
        runCatching {
            loaded.addPolyline(
                PolylineOptions()
                    .addAll(points.map { LatLng(it.latitude, it.longitude) })
                    .color(AndroidColor.rgb(31, 107, 74))
                    .width(7f)
            )
            loaded.addMarker(
                MarkerOptions()
                    .position(LatLng(points.first().latitude, points.first().longitude))
                    .title("Início")
            )
            loaded.addMarker(
                MarkerOptions()
                    .position(LatLng(points.last().latitude, points.last().longitude))
                    .title("Destino")
            )
        }
    }

    LaunchedEffect(map, styleReady, currentPoint, nextApoi, gpsState, mapOrientation) {
        val loaded = map ?: return@LaunchedEffect
        if (!styleReady || points.size < 2) return@LaunchedEffect
        runCatching {
            if (mapOrientation == MapOrientation.WALK_DIRECTION) {
                val bearing = routeBearing(points, currentPoint)
                org.maplibre.android.camera.CameraPosition.Builder(loaded.cameraPosition)
                    .bearing(bearing)
                    .build()
                    .also { loaded.setCameraPosition(it) }
            }
            currentMarker?.let { loaded.removeMarker(it) }
            currentMarker = currentPoint?.let {
                loaded.addMarker(
                    MarkerOptions()
                        .position(LatLng(it.latitude, it.longitude))
                        .title("A minha posição")
                        .snippet(gpsLabelForMap(gpsState))
                )
            }
            nextApoiMarker?.let { loaded.removeMarker(it) }
            nextApoiMarker = nextApoi?.location?.latitude?.let { lat ->
                nextApoi.location.longitude?.let { lon ->
                    loaded.addMarker(
                        MarkerOptions()
                            .position(LatLng(lat, lon))
                            .title("Próximo APOI")
                            .snippet(nextApoi.name)
                    )
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        MapControls(
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            onZoomIn = { map?.let { loaded -> loaded.cameraPosition.target?.let { target -> loaded.animateCamera(CameraUpdateFactory.newLatLngZoom(target, (loaded.cameraPosition.zoom + 1.0).coerceAtMost(17.0))) } } },
            onZoomOut = { map?.let { loaded -> loaded.cameraPosition.target?.let { target -> loaded.animateCamera(CameraUpdateFactory.newLatLngZoom(target, (loaded.cameraPosition.zoom - 1.0).coerceAtLeast(7.0))) } } },
            onLocate = {
                currentPoint?.let { p ->
                    map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(p.latitude, p.longitude), 14.0))
                }
            }
        )

        Card(
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f))
        ) {
            Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("MAPA · CARTOGRAFIA REAL", fontWeight = FontWeight.ExtraBold)
                Text("Traçado oficial local", color = Color(0xFF68736D))
                Text(gpsLabelForMap(gpsState), color = gpsColorForMap(gpsState))
                when {
                    offlineState.complete -> Text("MAPA OFFLINE · DISPONÍVEL", color = Color(0xFF0E6546), fontWeight = FontWeight.Bold)
                    offlineState.active -> Text("A preparar mapa offline · ${offlineState.percent}%", color = Color(0xFF7A4A00), fontWeight = FontWeight.Bold)
                    else -> Text("Mapa offline ainda não guardado", color = Color(0xFF68736D))
                }
                offlineState.message?.let { Text(it, color = Color(0xFF9A3A00)) }
            }
        }

        if (!offlineState.complete && !offlineState.active && points.size >= 2) {
            OutlinedButton(
                onClick = {
                    offlineState = OfflineUiState(message = "A preparar o mapa offline…")
                    downloadOfflineRegion(
                        context,
                        points,
                        routeId,
                        onRegion = {
                            offlineRegion = it
                            offlineState = OfflineUiState(active = true)
                        },
                        onStatus = { status ->
                            val percent = if (status.requiredResourceCount > 0L) {
                                ((status.completedResourceCount.toDouble() / status.requiredResourceCount.toDouble()) * 100.0).toInt().coerceIn(0, 100)
                            } else 0
                            offlineState = OfflineUiState(
                                complete = status.isComplete,
                                active = !status.isComplete,
                                percent = percent,
                                message = if (status.isComplete) "Cartografia guardada para utilização sem rede na região do percurso." else null
                            )
                            if (status.isComplete) offlineRegion?.setDownloadState(OfflineRegion.STATE_INACTIVE)
                        },
                        onError = { offlineState = OfflineUiState(message = "Download offline: $it") }
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .semantics { contentDescription = "GUARDAR MAPA OFFLINE" }
            ) {
                Text("GUARDAR MAPA OFFLINE")
            }
        }

        Card(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
            RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.90f))
        ) {
            Text(
                "© OpenStreetMap contributors · OpenMapTiles · OpenFreeMap",
                Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                color = Color(0xFF4F5852),
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun MapControls(
    modifier: Modifier,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onLocate: () -> Unit
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        MapButton("+", "Aumentar zoom", onZoomIn)
        MapButton("−", "Diminuir zoom", onZoomOut)
        MapButton("⌖", "Recentrar na minha posição", onLocate)
    }
}

@Composable
private fun MapButton(text: String, description: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp).semantics { contentDescription = description },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        shape = RoundedCornerShape(12.dp)
    ) { Text(text, fontWeight = FontWeight.ExtraBold) }
}

private fun inspectOfflineRegion(
    context: Context,
    routeId: String,
    onResult: (OfflineRegion?, OfflineUiState) -> Unit
) {
    val metadata = offlineMetadata(routeId)
    OfflineManager.getInstance(context).listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
        override fun onList(regions: Array<OfflineRegion>?) {
            val region = regions?.firstOrNull { runCatching { String(it.metadata, Charsets.UTF_8) == metadata }.getOrDefault(false) }
            if (region == null) {
                onResult(null, OfflineUiState())
                return
            }
            region.setDeliverInactiveMessages(true)
            region.setObserver(offlineObserver(region, onResult))
            region.getStatus(object : OfflineRegion.OfflineRegionStatusCallback {
                override fun onStatus(status: OfflineRegionStatus?) {
                    if (status == null) return
                    onResult(region, status.toUiState())
                }
                override fun onError(error: String?) { onResult(region, OfflineUiState(message = error)) }
            })
        }
        override fun onError(error: String) { onResult(null, OfflineUiState(message = error)) }
    })
}

private fun offlineObserver(region: OfflineRegion, onResult: (OfflineRegion?, OfflineUiState) -> Unit) =
    object : OfflineRegion.OfflineRegionObserver {
        override fun onStatusChanged(status: OfflineRegionStatus) {
            onResult(region, status.toUiState())
        }
        override fun onError(error: OfflineRegionError) {
            onResult(region, OfflineUiState(message = error.message))
        }
        override fun mapboxTileCountLimitExceeded(limit: Long) {
            onResult(region, OfflineUiState(message = "Limite de mapas offline atingido: $limit tiles."))
        }
    }

private fun OfflineRegionStatus.toUiState(): OfflineUiState {
    val percent = if (requiredResourceCount > 0L) {
        ((completedResourceCount.toDouble() / requiredResourceCount.toDouble()) * 100.0).toInt().coerceIn(0, 100)
    } else 0
    return OfflineUiState(
        complete = isComplete,
        active = !isComplete && downloadState == OfflineRegion.STATE_ACTIVE,
        percent = percent
    )
}

private fun downloadOfflineRegion(
    context: Context,
    points: List<GeoPoint>,
    routeId: String,
    onRegion: (OfflineRegion) -> Unit,
    onStatus: (OfflineRegionStatus) -> Unit,
    onError: (String) -> Unit
) {
    val line = LineString.fromLngLats(points.map { Point.fromLngLat(it.longitude, it.latitude) })
    val definition = OfflineGeometryRegionDefinition(
        WALKING_MAP_STYLE_URL,
        line,
        OFFLINE_MIN_ZOOM,
        OFFLINE_MAX_ZOOM,
        1f
    )
    OfflineManager.getInstance(context).createOfflineRegion(
        definition,
        offlineMetadata(routeId).toByteArray(Charsets.UTF_8),
        object : OfflineManager.CreateOfflineRegionCallback {
            override fun onCreate(region: OfflineRegion) {
                onRegion(region)
                region.setDeliverInactiveMessages(true)
                region.setObserver(object : OfflineRegion.OfflineRegionObserver {
                    override fun onStatusChanged(status: OfflineRegionStatus) { onStatus(status) }
                    override fun onError(error: OfflineRegionError) { onError(error.message ?: "erro desconhecido") }
                    override fun mapboxTileCountLimitExceeded(limit: Long) { onError("Limite de mapas offline atingido: $limit tiles.") }
                })
                region.setDownloadState(OfflineRegion.STATE_ACTIVE)
            }
            override fun onError(error: String) { onError(error) }
        }
    )
}

private fun offlineMetadata(routeId: String): String =
    org.json.JSONObject()
        .put("routeId", routeId)
        .put("kind", "walking-route-map")
        .toString()

private fun gpsLabelForMap(state: GpsState): String = when (state) {
    GpsState.NO_SIGNAL -> "GPS sem sinal"
    GpsState.ACQUIRING -> "A obter sinal GPS"
    GpsState.ON_ROUTE -> "GPS no percurso"
    GpsState.POSSIBLE_DEVIATION -> "Possível desvio"
    GpsState.PROBABLE_DEVIATION -> "Provável desvio"
}

private fun routeBearing(points: List<GeoPoint>, point: GeoPoint?): Double {
    if (points.size < 2 || point == null) return 0.0
    val index = points.indices.minByOrNull { i ->
        val a = points[i]
        val lat = a.latitude - point.latitude
        val lon = a.longitude - point.longitude
        lat * lat + lon * lon
    } ?: return 0.0
    val from = if (index < points.lastIndex) points[index] else points[index - 1]
    val to = if (index < points.lastIndex) points[index + 1] else points[index]
    val meanLat = Math.toRadians((from.latitude + to.latitude) / 2.0)
    val east = (to.longitude - from.longitude) * kotlin.math.cos(meanLat)
    val north = to.latitude - from.latitude
    return if (east == 0.0 && north == 0.0) 0.0 else Math.toDegrees(kotlin.math.atan2(east, north))
}

private fun gpsColorForMap(state: GpsState): Color = when (state) {
    GpsState.ON_ROUTE -> Color(0xFF0E6546)
    GpsState.ACQUIRING -> Color(0xFF7A4A00)
    GpsState.NO_SIGNAL, GpsState.POSSIBLE_DEVIATION, GpsState.PROBABLE_DEVIATION -> Color(0xFF9A5A00)
}

private fun pointAtRouteKmForMap(points: List<GeoPoint>, routeKm: Double, totalKm: Double): GeoPoint? {
    if (points.isEmpty()) return null
    if (points.size == 1) return points.first()
    val target = routeKm.coerceIn(0.0, totalKm.coerceAtLeast(0.0))
    if (target <= 0.0) return points.first()
    var accumulated = 0.0
    for (i in 1 until points.size) {
        val a = points[i - 1]
        val b = points[i]
        val segment = distanceKm(a, b)
        if (accumulated + segment >= target) {
            val fraction = if (segment <= 0.0) 0.0 else ((target - accumulated) / segment).coerceIn(0.0, 1.0)
            return GeoPoint(
                latitude = a.latitude + (b.latitude - a.latitude) * fraction,
                longitude = a.longitude + (b.longitude - a.longitude) * fraction
            )
        }
        accumulated += segment
    }
    return points.last()
}

private fun distanceKm(a: GeoPoint, b: GeoPoint): Double {
    val radius = 6371.0088
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)
    val dLat = lat2 - lat1
    val dLon = Math.toRadians(b.longitude - a.longitude)
    val s1 = kotlin.math.sin(dLat / 2.0)
    val s2 = kotlin.math.sin(dLon / 2.0)
    val h = s1 * s1 + cos(lat1) * cos(lat2) * s2 * s2
    return 2.0 * radius * kotlin.math.asin(kotlin.math.sqrt(h.coerceIn(0.0, 1.0)))
}
