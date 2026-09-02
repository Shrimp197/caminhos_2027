package com.caminhos2027.v1.core.model

enum class ApoiAvailabilityStatus { CURRENT, FUTURE_CONFIRMED, RECURRING, HISTORICAL, EXPIRED, AWAITING_CONFIRMATION, CLOSED }
enum class ApoiCostModel { FREE, OPTIONAL_CONTRIBUTION, PAID, UNKNOWN }
enum class ApoiReservationPolicy { NOT_REQUIRED, RECOMMENDED, REQUIRED, UNKNOWN }
enum class SleepingType { BED, MATTRESS, FLOOR, TENT, OUTDOOR, OTHER, UNKNOWN }

data class ApoiCost(val model: ApoiCostModel = ApoiCostModel.UNKNOWN, val amount: Double? = null, val currency: String? = null, val description: String? = null)
data class ApoiReservation(val policy: ApoiReservationPolicy = ApoiReservationPolicy.UNKNOWN, val contact: String? = null, val url: String? = null, val notes: String? = null)
data class ApoiAvailability(val status: ApoiAvailabilityStatus = ApoiAvailabilityStatus.AWAITING_CONFIRMATION, val validFrom: String? = null, val validUntil: String? = null, val recurrence: String? = null, val season: String? = null, val openingHours: String? = null, val notes: String? = null)
data class ApoiCapacity(val total: Int? = null, val sleeping: Int? = null, val notes: String? = null)
data class ApoiCharacteristics(val sleepingType: SleepingType? = null, val shower: Boolean? = null, val hotWater: Boolean? = null, val wc: Boolean? = null, val laundry: Boolean? = null, val drying: Boolean? = null, val accessibility: Boolean? = null, val notes: String? = null)
data class ApoiContact(val responsible: String? = null, val organization: String? = null, val phone: String? = null, val email: String? = null, val website: String? = null, val social: String? = null)
data class ApoiConfidence(val overall: PositionConfidence = PositionConfidence.UNKNOWN, val location: PositionConfidence = PositionConfidence.UNKNOWN, val support: PositionConfidence = PositionConfidence.UNKNOWN, val availability: PositionConfidence = PositionConfidence.UNKNOWN, val criticalInformation: PositionConfidence = PositionConfidence.UNKNOWN)
