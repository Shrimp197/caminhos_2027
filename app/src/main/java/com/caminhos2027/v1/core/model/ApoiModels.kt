package com.caminhos2027.v1.core.model

enum class ApoiCategory {
    ALIMENTACAO, AGUA, DESCANSO, PERNOITA, DUCHES, CARREGAMENTO, TRANSPORTE, EMERGENCIA
}

enum class LocationPrecision { EXACT, APPROXIMATE, LOCALITY_ONLY, UNKNOWN }
enum class RouteRelation { ON_ROUTE, NEAR_ROUTE, ACCESSIBLE_WITH_DETOUR, DISTANT_POTENTIAL_SUPPORT, LOCATION_UNCERTAIN, OUTSIDE_ROUTE }
enum class PublicationStatus { CANDIDATE, REVIEW, PUBLISHED, PUBLISHED_WITH_WARNING, HISTORICAL, CLOSED, EXCLUDED }
enum class AvailabilityStatus { CURRENT, FUTURE_CONFIRMED, RECURRING, HISTORICAL, EXPIRED, AWAITING_CONFIRMATION, CLOSED }
enum class CostModel { FREE, OPTIONAL_CONTRIBUTION, PAID, UNKNOWN }
enum class ReservationPolicy { NOT_REQUIRED, RECOMMENDED, REQUIRED, UNKNOWN }
enum class ConfidenceLevel { HIGH, MEDIUM, LOW, UNKNOWN }
enum class SleepingType { BED, MATTRESS, FLOOR, TENT, OUTDOOR, OTHER, UNKNOWN }

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

data class ApoiAvailability(
    val status: AvailabilityStatus = AvailabilityStatus.AWAITING_CONFIRMATION,
    val validFrom: String? = null,
    val validUntil: String? = null,
    val recurrence: String? = null,
    val season: String? = null,
    val openingHours: String? = null,
    val notes: String? = null
)

data class ApoiCost(
    val model: CostModel = CostModel.UNKNOWN,
    val amount: Double? = null,
    val currency: String? = null,
    val description: String? = null
)

data class ApoiReservation(
    val policy: ReservationPolicy = ReservationPolicy.UNKNOWN,
    val contact: String? = null,
    val url: String? = null,
    val notes: String? = null
)

data class ApoiConfidence(
    val overall: ConfidenceLevel = ConfidenceLevel.UNKNOWN,
    val location: ConfidenceLevel = ConfidenceLevel.UNKNOWN,
    val support: ConfidenceLevel = ConfidenceLevel.UNKNOWN,
    val availability: ConfidenceLevel = ConfidenceLevel.UNKNOWN,
    val criticalInformation: ConfidenceLevel = ConfidenceLevel.UNKNOWN
)

data class ApoiSupport(
    val pilgrimSupportConfirmed: Boolean? = null,
    val capacity: Int? = null,
    val sleepingType: SleepingType? = null,
    val shower: Boolean? = null,
    val hotWater: Boolean? = null,
    val waterAvailable: Boolean? = null,
    val waterPotable: Boolean? = null,
    val waterPotableConfirmed: Boolean? = null,
    val foodAvailable: Boolean? = null,
    val chargingAvailable: Boolean? = null,
    val transportAvailable: Boolean? = null,
    val emergencyAvailable: Boolean? = null,
    val wc: Boolean? = null,
    val laundry: Boolean? = null,
    val drying: Boolean? = null
)

data class ApoiSource(
    val id: String,
    val type: String,
    val name: String,
    val reference: String? = null,
    val url: String? = null,
    val collectedAt: String? = null,
    val publishedAt: String? = null,
    val verifiedAt: String? = null,
    val method: String? = null,
    val notes: String? = null
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
    val availability: ApoiAvailability = ApoiAvailability(),
    val cost: ApoiCost = ApoiCost(),
    val reservation: ApoiReservation = ApoiReservation(),
    val support: ApoiSupport = ApoiSupport(),
    val confidence: ApoiConfidence = ApoiConfidence(),
    val sources: List<ApoiSource> = emptyList()
)
