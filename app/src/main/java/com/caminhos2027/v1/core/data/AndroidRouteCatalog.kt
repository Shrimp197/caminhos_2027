package com.caminhos2027.v1.core.data

import android.content.Context
import com.caminhos2027.BuildConfig
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.model.WalkStatus
import com.caminhos2027.v1.core.walking.AndroidWalkRepository

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

    private val production = AndroidRouteOption(
        id = CENTENARIO_ID,
        title = "Caminho do Centenário",
        description = "Porto – Fátima · cerca de 212 km · 8 etapas",
        testOnly = false
    )

    private val qa = listOf(
        AndroidRouteOption(
            id = SR_ID,
            title = "Trajeto SR",
            description = "Percurso exclusivamente de teste para validação da experiência.",
            testOnly = true
        ),
        AndroidRouteOption(
            id = HF_ID,
            title = "Trajeto HF",
            description = "Percurso exclusivamente de teste para validação da experiência.",
            testOnly = true
        )
    )

    /** QA routes exist only in debug builds; the catalogue remains extensible for future routes. */
    val options: List<AndroidRouteOption>
        get() = if (BuildConfig.DEBUG) listOf(production) + qa else listOf(production)

    fun isTestRoute(routeId: String): Boolean =
        BuildConfig.DEBUG && (routeId == SR_ID || routeId == HF_ID)

    fun loadRoute(context: Context, routeId: String): Route {
        val route = when (routeId) {
            CENTENARIO_ID -> AssetRouteDataSource(
                context = context,
                assetPath = "data/route.geojson",
                metadata = RouteJsonMetadata(
                    officialDistanceKm = 211.87,
                    officialName = "Caminho do Centenário"
                )
            ).loadRoute()
            SR_ID -> {
                check(BuildConfig.DEBUG) { "QA route is available only in debug builds" }
                AssetGpxRouteDataSource(
                    context = context,
                    assetPath = "data/percurso-teste-casa-trabalho.gpx",
                    routeId = SR_ID,
                    name = "Trajeto SR",
                    source = "GPX fornecido para cenário QA SR"
                ).loadRoute()
            }
            HF_ID -> {
                check(BuildConfig.DEBUG) { "QA route is available only in debug builds" }
                AssetGpxRouteDataSource(
                    context = context,
                    assetPath = "data/percurso-teste-hf.gpx",
                    routeId = HF_ID,
                    name = "Trajeto HF",
                    source = "GPX fornecido para cenário QA HF"
                ).loadRoute()
            }
            else -> error("Unknown V1 route: $routeId")
        }
        return RouteStageAssetLoader(context).enrich(route)
    }

    fun preferredPersistedRouteId(context: Context): String? =
        AndroidWalkRepository(context.applicationContext)
            .list()
            .asReversed()
            .firstOrNull {
                (it.status == WalkStatus.ACTIVE || it.status == WalkStatus.PLANNED) &&
                    (it.routeId == CENTENARIO_ID || isTestRoute(it.routeId))
            }
            ?.routeId
}
