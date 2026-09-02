package com.caminhos2027.v1.core.model

/** Versioned APOI dataset consumed by the V1 app. */
data class ApoiDataset(
    val datasetVersion: String,
    val environment: String,
    val apoi: List<Apoi>
)
