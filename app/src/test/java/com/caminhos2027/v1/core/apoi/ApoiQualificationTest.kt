package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiAvailability
import com.caminhos2027.v1.core.model.ApoiAvailabilityStatus
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation
import org.junit.Assert.assertEquals
import org.junit.Test

class ApoiQualificationTest {
    private fun base(
        availability: ApoiAvailabilityStatus = ApoiAvailabilityStatus.CURRENT,
        precision: LocationPrecision = LocationPrecision.EXACT,
        relation: RouteRelation = RouteRelation.ON_ROUTE,
        services: Set<ApoiCategory> = setOf(ApoiCategory.AGUA)
    ) = Apoi(
        id = "a", name = "SR APOI", description = null,
        mainCategory = ApoiCategory.AGUA, services = services,
        location = ApoiLocation(40.0, -8.0, precision, null, null, null, "sr", 1.0, 0.0, null, relation),
        publication = com.caminhos2027.v1.core.model.ApoiPublication(PublicationStatus.REVIEW, null),
        availability = ApoiAvailability(status = availability)
    )

    @Test fun supportUnknownNeedsReview() = assertEquals(
        PublicationStatus.REVIEW,
        ApoiQualification.evaluate(base(), ApoiQualificationEvidence(null), 2027).status
    )

    @Test fun supportExplicitlyFalseIsExcluded() = assertEquals(
        PublicationStatus.EXCLUDED,
        ApoiQualification.evaluate(base(), ApoiQualificationEvidence(false), 2027).status
    )

    @Test fun supportConfirmedAndCurrentIsPublished() = assertEquals(
        PublicationStatus.PUBLISHED,
        ApoiQualification.evaluate(base(), ApoiQualificationEvidence(true), 2027).status
    )

    @Test fun awaitingConfirmationWithConfirmedSupportIsWarning() = assertEquals(
        PublicationStatus.PUBLISHED_WITH_WARNING,
        ApoiQualification.evaluate(base(ApoiAvailabilityStatus.AWAITING_CONFIRMATION), ApoiQualificationEvidence(true), 2027).status
    )

    @Test fun historicalIsNotPublishedAsCurrent() = assertEquals(
        PublicationStatus.HISTORICAL,
        ApoiQualification.evaluate(base(ApoiAvailabilityStatus.HISTORICAL), ApoiQualificationEvidence(true), 2027).status
    )

    @Test fun closedIsNotPublishedAsCurrent() = assertEquals(
        PublicationStatus.CLOSED,
        ApoiQualification.evaluate(base(ApoiAvailabilityStatus.CLOSED), ApoiQualificationEvidence(true), 2027).status
    )

    @Test fun outsideAndDistantSupportAreExcluded() {
        assertEquals(
            PublicationStatus.EXCLUDED,
            ApoiQualification.evaluate(base(relation = RouteRelation.OUTSIDE_ROUTE), ApoiQualificationEvidence(true), 2027).status
        )
        assertEquals(
            PublicationStatus.EXCLUDED,
            ApoiQualification.evaluate(base(relation = RouteRelation.DISTANT_POTENTIAL_SUPPORT), ApoiQualificationEvidence(true), 2027).status
        )
    }

    @Test fun emptyServicesAreExcluded() = assertEquals(
        PublicationStatus.EXCLUDED,
        ApoiQualification.evaluate(base(services = emptySet()), ApoiQualificationEvidence(true), 2027).status
    )

    @Test fun criticalConflictBlocksPublication() = assertEquals(
        PublicationStatus.REVIEW,
        ApoiQualification.evaluate(base(), ApoiQualificationEvidence(true, criticalConflict = true), 2027).status
    )

    @Test fun confirmationFrom2026DoesNotBecomeCurrentFor2027() = assertEquals(
        PublicationStatus.REVIEW,
        ApoiQualification.evaluate(base(), ApoiQualificationEvidence(true, confirmedForYear = 2026), 2027).status
    )

    @Test fun matchingConfirmationYearCanPublish() = assertEquals(
        PublicationStatus.PUBLISHED,
        ApoiQualification.evaluate(base(), ApoiQualificationEvidence(true, confirmedForYear = 2027), 2027).status
    )

    @Test fun uncertainLocationNeedsReviewBeforePublication() = assertEquals(
        PublicationStatus.REVIEW,
        ApoiQualification.evaluate(base(precision = LocationPrecision.UNKNOWN), ApoiQualificationEvidence(true), 2027).status
    )
}
