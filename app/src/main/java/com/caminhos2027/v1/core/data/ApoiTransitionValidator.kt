package com.caminhos2027.v1.core.data

/** Deterministic safety checks for the internal 2026 -> V1 APOI transition dataset. */
object ApoiTransitionValidator {
    fun validate(
        productionReady: Boolean,
        environment: String,
        sourceIds: List<String>
    ): List<String> {
        val errors = mutableListOf<String>()
        if (productionReady) errors += "production_ready must be false"
        if (environment != "review") errors += "environment must be review"
        if (sourceIds.any { !Regex("^apoio_2026_[0-9]{3}$").matches(it) }) {
            errors += "all source ids must use the 2026 source-id format"
        }
        val duplicates = sourceIds.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        duplicates.forEach { errors += "duplicate source id: $it" }
        return errors
    }
}
