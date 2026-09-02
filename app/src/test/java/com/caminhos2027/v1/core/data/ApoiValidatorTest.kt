package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.ApoiSupport
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApoiValidatorTest {
    private fun valid(status: PublicationStatus = PublicationStatus.PUBLISHED, reason: String? = "TEST/FICTITIOUS") = Apoi(
        id = "TEST-FICTITIOUS-001", name = "TEST/FICTITIOUS APOI", description = null,
        mainCategory = ApoiCategory.PERNOITA,
        services = setOf(ApoiCategory.PERNOITA, ApoiCategory.DUCHES, ApoiCategory.CARREGAMENTO),
        location = ApoiLocation(40.0, -8.0, LocationPrecision.APPROXIMATE, "TEST", "TEST", null, "TEST-ROUTE", 10.0, 20.0, 50.0, RouteRelation.NEAR_ROUTE),
        publication = ApoiPublication(status, reason),
        support = ApoiSupport(pilgrimSupportConfirmed = true)
    )

    @Test fun validMultiServiceApoiPasses() = assertTrue(ApoiValidator.validate(valid(), "production").isEmpty())

    @Test fun duplicateIdsAreRejected() {
        val errors = ApoiValidator.validateDataset("production", listOf(valid(), valid()))
        assertTrue(errors.any { it.contains("duplicate APOI id") })
    }

    @Test fun mainCategoryMustBeAService() {
        val errors = ApoiValidator.validate(valid().copy(services = setOf(ApoiCategory.DUCHES)), "production")
        assertTrue(errors.any { it.contains("main category") })
    }

    @Test fun unknownLocationCannotCarryCoordinates() {
        val errors = ApoiValidator.validate(valid().copy(location = valid().location.copy(precision = LocationPrecision.UNKNOWN)), "production")
        assertTrue(errors.any { it.contains("unknown location precision") })
    }

    @Test fun productionRejectsReviewAndHistoricalStatuses() {
        assertFalse(ApoiValidator.validate(valid(PublicationStatus.REVIEW), "production").isEmpty())
        assertFalse(ApoiValidator.validate(valid(PublicationStatus.HISTORICAL), "production").isEmpty())
    }

    @Test fun warningPublicationRequiresReason() {
        val errors = ApoiValidator.validate(valid(PublicationStatus.PUBLISHED_WITH_WARNING, null), "production")
        assertTrue(errors.any { it.contains("requires a reason") })
    }

    @Test fun uncertainLocationCannotBePublishedNormally() {
        val errors = ApoiValidator.validate(valid().copy(location = valid().location.copy(routeRelation = RouteRelation.LOCATION_UNCERTAIN)), "production")
        assertTrue(errors.any { it.contains("location relation") })
    }

    @Test fun publishedApoiRequiresConfirmedPilgrimSupport() {
        val errors = ApoiValidator.validate(valid().copy(support = ApoiSupport()), "production")
        assertTrue(errors.any { it.contains("confirmed pilgrim support") })
    }

    @Test fun potableWaterRequiresExplicitConfirmation() {
        val errors = ApoiValidator.validate(valid().copy(support = ApoiSupport(waterAvailable = true, waterPotable = true)), "sr")
        assertTrue(errors.any { it.contains("explicitly confirmed") })
    }

    @Test fun freeCostCannotHaveAmount() {
        val errors = ApoiValidator.validate(valid().copy(cost = com.caminhos2027.v1.core.model.ApoiCost(com.caminhos2027.v1.core.model.CostModel.FREE, 5.0, "EUR")), "sr")
        assertTrue(errors.any { it.contains("free cost") })
    }

    @Test fun requiredReservationNeedsContactOrUrl() {
        val errors = ApoiValidator.validate(valid().copy(reservation = com.caminhos2027.v1.core.model.ApoiReservation(com.caminhos2027.v1.core.model.ReservationPolicy.REQUIRED)), "sr")
        assertTrue(errors.any { it.contains("required reservation") })
    }
}
