package com.caminhos2027.v1.core.data

import android.content.Context
import com.caminhos2027.v1.core.model.Apoi

interface ApoiDataSource {
    fun load(): List<Apoi>
}

/** Loads the validated, bundled APOI publication dataset. */
class AssetApoiDataSource(
    private val context: Context,
    private val assetPath: String = "data/published/apoi-production.json"
) : ApoiDataSource {
    override fun load(): List<Apoi> =
        context.assets.open(assetPath).bufferedReader().use { reader ->
            ApoiJsonParser.parseDataset(reader.readText())
        }
}

/** Repository boundary used by application code; UI never reads JSON directly. */
class ApoiRepository(
    private val dataSource: ApoiDataSource
) {
    fun getAll(): List<Apoi> = dataSource.load()
}
