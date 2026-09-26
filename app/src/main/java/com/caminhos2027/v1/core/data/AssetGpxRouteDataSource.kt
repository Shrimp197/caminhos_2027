package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.RouteGeometry
import com.caminhos2027.v1.core.model.Stage
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

/** Loads a controlled GPX test track from a local asset without importing it into production data. */
class AssetGpxRouteDataSource(
    private val context: android.content.Context,
    private val assetPath: String,
    private val routeId: String,
    private val name: String,
    private val source: String
) : RouteDataSource {
    override fun loadRoute(): Route {
        val xml = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        return GpxRouteParser.parse(xml, routeId, name, source)
    }
}

/** Deterministic parser for the local SR/HF QA GPX tracks. */
object GpxRouteParser {
    fun parse(xml: String, routeId: String, name: String, source: String): Route {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = false
        val document = factory.newDocumentBuilder().parse(ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8)))
        val trackPoints = document.getElementsByTagName("trkpt")
        require(trackPoints.length >= 2) { "Test GPX route must contain at least two track points" }

        val points = buildList(trackPoints.length) {
            for (index in 0 until trackPoints.length) {
                val point = trackPoints.item(index)
                val latitude = point.attributes.getNamedItem("lat")?.nodeValue?.toDoubleOrNull()
                val longitude = point.attributes.getNamedItem("lon")?.nodeValue?.toDoubleOrNull()
                require(latitude != null && longitude != null && latitude.isFinite() && longitude.isFinite()) {
                    "Test GPX track point[$index] must contain finite lat/lon"
                }
                add(GeoPoint(latitude, longitude))
            }
        }

        val distanceKm = points.zipWithNext().sumOf { (from, to) -> distanceKm(from, to) }
        require(distanceKm.isFinite() && distanceKm > 0.0) { "Test GPX route distance must be positive" }

        return Route(
            id = routeId,
            name = name,
            officialName = name,
            totalDistanceKm = distanceKm,
            source = source,
            updatedAt = null,
            geometry = RouteGeometry(points),
            stages = emptyList<Stage>()
        )
    }

    private fun distanceKm(from: GeoPoint, to: GeoPoint): Double {
        val earthRadiusKm = 6371.0088
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val deltaLat = lat2 - lat1
        val deltaLon = Math.toRadians(to.longitude - from.longitude)
        val a = kotlin.math.sin(deltaLat / 2.0) * kotlin.math.sin(deltaLat / 2.0) +
            kotlin.math.cos(lat1) * kotlin.math.cos(lat2) *
            kotlin.math.sin(deltaLon / 2.0) * kotlin.math.sin(deltaLon / 2.0)
        return 2.0 * earthRadiusKm * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1.0 - a))
    }
}
