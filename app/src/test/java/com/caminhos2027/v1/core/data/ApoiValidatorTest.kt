package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class ApoiValidatorTest {
    private fun valid(status: PublicationStatus = PublicationStatus.PUBLISHED, reason: String? = null) = Apoi(
        id = "TEST-FICTITIOUS-001",
        name = "TEST/FICTITIOUS APOI",
        description = null,
        mainCategory = ApoiCategory.PERNOITA,
        services = setOf(ApoiCategory.PERNOITA, ApoiCategory.DUCHES, ApoiCategory.CARREGAMENTO),
        location = ApoiLocation(40.0, -8.0, LocationPrecision.APPROXIMATE, "TEST", "TEST", null, "TEST-ROUTE", 10.0, 20.0, 50.0, RouteRelation.NEAR_ROUTE),
        publication = ApoiPublication(status, reason)
    )

    @Test fun validMultiServiceApoiPasses() {
        assertTrue(ApoiValidator.validate(valid(), "production").isEmpty())
    }

    @Test fun duplicateIdsAreRejected() {
        val errors = ApoiValidator.validateDataset("production", listOf(valid(), valid()))
        assertTrue(errors.any { it.contains("duplicate APOI id") })
    }

    @Test fun mainCategoryMustBeAService() {
        val apoi = valid().copy(services = setOf(ApoiCategory.DUCHES))
        assertTrue(ApoiValidator.validate(apoi, "production").any { it.contains("main category") })
    }

    @Test fun unknownLocationCannotCarryCoordinates() {
        val apoi = valid().copy(location = valid().location.copy(precision = LocationPrecision.UNKNOWN))
        assertTrue(ApoiValidator.validate(apoi, "production").any { it.contains("unknown location precision") })
    }

    @Test fun productionRejectsReviewAndHistoricalStatuses() {
        val review = ApoiValidator.validate(valid(PublicationStatus.REVIEW), "production")
        val historical = ApoiValidator.validate(valid(PublicationStatus.HISTORICAL), "production")
        assertFalse(review.isEmpty())
        assertFalse(historical.isEmpty())
    }

    @Test fun warningPublicationRequiresReason() {
        val errors = ApoiValidator.validate(valid(PublicationStatus.PUBLISHED_WITH_WARNING), "production")
        assertTrue(errors.any { it.contains("requires a reason") })
    }

    @Test fun uncertainLocationCannotBePublishedNormally() {
        val apoi = valid().copy(location = valid().location.copy(routeRelation = RouteRelation.LOCATION_UNCERTAIN))
        assertTrue(ApoiValidator.validate(apoi, "production").any { it.contains("location relation") })
    }

    @Test fun productionAcceptsOnlyPublishableStatuses() {
        assertTrue(ApoiValidator.validate(valid(PublicationStatus.PUBLISHED), "production").isEmpty())
        assertTrue(ApoiValidator.validate(valid(PublicationStatus.PUBLISHED_WITH_WARNING, "2027 confirmation pending"), "production").isEmpty())
    }
}
