package com.caminhos2027.v1.core.data

import android.content.Context
import com.caminhos2027.v1.core.model.ApoiDataset

interface ApoiDataSource {
    fun loadDataset(): ApoiDataset
}

/** Loads only a caller-selected local asset; it never downloads data during build. */
class AssetApoiDataSource(
    private val context: Context,
    private val assetPath: String
) : ApoiDataSource {
    override fun loadDataset(): ApoiDataset {
        val json = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        return ApoiJsonParser.parse(json)
    }
}

class ValidatingApoiRepository(
    private val dataSource: ApoiDataSource
) : ApoiRepository {
    override fun getDataset(environment: String): Result<ApoiDataset> = runCatching {
        val dataset = dataSource.loadDataset()
        require(dataset.environment == environment) {
            "APOI environment mismatch: expected $environment, got ${dataset.environment}"
        }
        dataset
    }
}
