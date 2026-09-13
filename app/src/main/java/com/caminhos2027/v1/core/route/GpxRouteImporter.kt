package com.caminhos2027.v1.core.route

import com.caminhos2027.v1.core.model.GeoPoint
import com.caminhos2027.v1.core.model.RouteGeometry
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

/** Parses GPX track points without mutating or repairing the source geometry. */
object GpxRouteImporter {
    private const val GPX_NAMESPACE = "http://www.topografix.com/GPX/1/1"

    fun parse(input: InputStream): RouteGeometry {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        }
        val document = factory.newDocumentBuilder().parse(input)

        val tracks = document.getElementsByTagNameNS(GPX_NAMESPACE, "trk")
        require(tracks.length == 1) { "GPX must contain exactly one track" }

        val segments = document.getElementsByTagNameNS(GPX_NAMESPACE, "trkseg")
        require(segments.length == 1) { "GPX must contain exactly one track segment" }

        val points = buildList {
            val trackPoints = document.getElementsByTagNameNS(GPX_NAMESPACE, "trkpt")
            for (index in 0 until trackPoints.length) {
                val node = trackPoints.item(index)
                val lat = node.attributes.getNamedItem("lat")?.nodeValue?.toDoubleOrNull()
                    ?: error("GPX track point is missing a numeric latitude")
                val lon = node.attributes.getNamedItem("lon")?.nodeValue?.toDoubleOrNull()
                    ?: error("GPX track point is missing a numeric longitude")
                add(GeoPoint(lat, lon))
            }
        }

        val geometry = RouteGeometry(points)
        RouteGeometryValidator.validate(geometry)
        return geometry
    }
}
