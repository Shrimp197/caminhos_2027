package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.ApoiDataset

interface ApoiRepository {
    fun getDataset(environment: String): Result<ApoiDataset>
}
