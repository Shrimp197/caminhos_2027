package com.caminhos2027.v1.core.model

enum class ApoiCategory {
    ALIMENTACAO, AGUA, DESCANSO, PERNOITA, DUCHES, CARREGAMENTO, TRANSPORTE, EMERGENCIA
}

enum class LocationPrecision { EXACT, APPROXIMATE, LOCALITY_ONLY, UNKNOWN }

enum class RouteRelation { ON_ROUTE, NEAR_ROUTE, ACCESSIBLE_WITH_DETOUR, DISTANT_POTENTIAL_SUPPORT, LOCATION_UNCERTAIN, OUTSIDE_ROUTE }

enum class PublicationStatus { CANDIDATE, REVIEW, PUBLISHED, PUBLISHED_WITH_WARNING, HISTORICAL, CLOSED, EXCLUDED }

data class ApoiLocation(
    val latitude: Double?,
    val longitude: Double?,
    val precision: LocationPrecision,
    val locality: String?,
    val municipality: String?,
    val reference: String?,
    val routeId: String?,
    val routeKm: Double?,
    val distanceToRouteM: Double?,
    val accessDistanceM: Double?,
    val routeRelation: RouteRelation
)

data class ApoiPublication(
    val status: PublicationStatus,
    val reason: String?
)

data class Apoi(
    val id: String,
    val name: String,
    val description: String?,
    val mainCategory: ApoiCategory,
    val services: Set<ApoiCategory>,
    val location: ApoiLocation,
    val publication: ApoiPublication,
    val cost: ApoiCost = ApoiCost(),
    val reservation: ApoiReservation = ApoiReservation(),
    val availability: ApoiAvailability = ApoiAvailability(),
    val capacity: ApoiCapacity = ApoiCapacity(),
    val characteristics: ApoiCharacteristics = ApoiCharacteristics(),
    val contact: ApoiContact = ApoiContact(),
    val confidence: ApoiConfidence = ApoiConfidence()
)
