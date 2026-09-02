package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiAvailability
import com.caminhos2027.v1.core.model.ApoiAvailabilityStatus
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation
import org.junit.Assert.assertEquals
import org.junit.Test

class ApoiQualificationTest {
    private fun base(status: PublicationStatus = PublicationStatus.REVIEW,
                      availability: ApoiAvailabilityStatus = ApoiAvailabilityStatus.CURRENT,
                      precision: LocationPrecision = LocationPrecision.EXACT,
                      relation: RouteRelation = RouteRelation.ON_ROUTE) = Apoi(
        id = "a", name = "SR APOI", description = null,
        mainCategory = ApoiCategory.AGUA, services = setOf(ApoiCategory.AGUA),
        location = ApoiLocation(40.0, -8.0, precision, null, null, null, "sr", 1.0, 0.0, null, relation),
        publication = ApoiPublication(status, null),
        availability = ApoiAvailability(status = availability)
    )

    @Test fun currentReviewedRecordNeedsReview() = assertEquals(PublicationStatus.REVIEW, ApoiQualification.evaluate(base()))
    @Test fun awaitingConfirmationPublishesWithWarning() = assertEquals(PublicationStatus.PUBLISHED_WITH_WARNING, ApoiQualification.evaluate(base(availability = ApoiAvailabilityStatus.AWAITING_CONFIRMATION)))
    @Test fun historicalIsNotPublishedAsCurrent() = assertEquals(PublicationStatus.HISTORICAL, ApoiQualification.evaluate(base(availability = ApoiAvailabilityStatus.HISTORICAL)))
    @Test fun outsideRouteIsExcluded() = assertEquals(PublicationStatus.EXCLUDED, ApoiQualification.evaluate(base(relation = RouteRelation.OUTSIDE_ROUTE)))
    @Test fun uncertainLocationNeedsReview() = assertEquals(PublicationStatus.REVIEW, ApoiQualification.evaluate(base(precision = LocationPrecision.UNKNOWN)))
}
