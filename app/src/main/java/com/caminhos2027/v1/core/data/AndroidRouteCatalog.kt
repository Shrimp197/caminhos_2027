package com.caminhos2027.v1.core.data

import android.content.Context
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.walking.AndroidWalkRepository

/** Route choices exposed by the Android V1 preparation flow. */
data class AndroidRouteOption(
    val id: String,
    val title: String,
    val description: String,
    val testOnly: Boolean
)

object AndroidRouteCatalog {
    const val CENTENARIO_ID = "caminho-do-centenario"
    const val SR_ID = "sr-test"
    const val HF_ID = "hf-test"

    val options: List<AndroidRouteOption> = listOf(
        AndroidRouteOption(
            id = CENTENARIO_ID,
            title = "Caminho do Centenário",
            description = "Percurso real de referência; os dados 2026 permanecem históricos enquanto não existirem os de 2027.",
            testOnly = false
        ),
        AndroidRouteOption(
            id = SR_ID,
            title = "SR",
            description = "Trajeto de teste SR (GPX casa/trabalho). Ambiente de QA; não é percurso de produção.",
            testOnly = true
        ),
        AndroidRouteOption(
            id = HF_ID,
            title = "HF",
            description = "Trajeto de teste HF (GPX fornecido). Ambiente de QA; não é percurso de produção.",
            testOnly = true
        )
    )

    fun loadRoute(context: Context, routeId: String): Route = when (routeId) {
        CENTENARIO_ID -> AssetRouteDataSource(
            context = context,
            assetPath = "data/route.geojson",
            metadata = RouteJsonMetadata(
                officialDistanceKm = 211.87,
                officialName = "Caminho do Centenário"
            )
        ).loadRoute()
        SR_ID -> AssetGpxRouteDataSource(
            context = context,
            assetPath = "data/percurso-teste-casa-trabalho.gpx",
            routeId = SR_ID,
            name = "SR — trajeto de teste",
            source = "GPX fornecido para cenário QA SR"
        ).loadRoute()
        HF_ID -> AssetGpxRouteDataSource(
            context = context,
            assetPath = "data/percurso-teste-hf.gpx",
            routeId = HF_ID,
            name = "HF — trajeto de teste",
            source = "GPX fornecido para cenário QA HF"
        ).loadRoute()
        else -> error("Unknown V1 route: $routeId")
    }

    /** Prefer an existing active/planed route so Android recreation never silently switches route. */
    fun preferredPersistedRouteId(context: Context): String? =
        AndroidWalkRepository(context.applicationContext)
            .list()
            .asReversed()
            .firstOrNull { it.status == WalkStatus.ACTIVE || it.status == WalkStatus.PLANNED }
            ?.routeId
}
