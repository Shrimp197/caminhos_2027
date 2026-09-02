package com.caminhos2027.v1.core.data

import org.junit.Assert.assertTrue
import org.junit.Test

class ApoiTransitionValidatorTest {
    @Test
    fun transitionDatasetMustNeverBeProductionReady() {
        val errors = ApoiTransitionValidator.validate(
            productionReady = true,
            environment = "review",
            sourceIds = listOf("apoio_2026_001")
        )
        assertTrue(errors.any { it.contains("production_ready") })
    }

    @Test
    fun transitionDatasetRejectsProductionEnvironment() {
        val errors = ApoiTransitionValidator.validate(
            productionReady = false,
            environment = "production",
            sourceIds = listOf("apoio_2026_001")
        )
        assertTrue(errors.any { it.contains("environment") })
    }

    @Test
    fun transitionDatasetRejectsDuplicateSourceIds() {
        val errors = ApoiTransitionValidator.validate(
            productionReady = false,
            environment = "review",
            sourceIds = listOf("apoio_2026_001", "apoio_2026_001")
        )
        assertTrue(errors.any { it.contains("duplicate") })
    }

    @Test
    fun transitionDatasetAcceptsValidReviewRecords() {
        val errors = ApoiTransitionValidator.validate(
            productionReady = false,
            environment = "review",
            sourceIds = listOf("apoio_2026_001", "apoio_2026_012")
        )
        assertTrue(errors.isEmpty())
    }
}
