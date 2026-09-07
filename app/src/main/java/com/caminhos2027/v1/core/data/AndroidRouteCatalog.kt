package com.caminhos2027.v1.core.data

import android.content.Context
import com.caminhos2027.BuildConfig
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

    private val production = AndroidRouteOption(
        id = CENTENARIO_ID,
        title = "Caminho do Centenário",
        description = "Porto – Fátima · 212 km · percurso de produção; informação oficial do caminho.",
        testOnly = false
    )

    private val qa = listOf(
        AndroidRouteOption(
            id = SR_ID,
            title = "Trajeto SR",
            description = "Percurso de teste SR. Ambiente QA; não é percurso de produção.",
            testOnly = true
        ),
        AndroidRouteOption(
            id = HF_ID,
            title = "Trajeto HF",
            description = "Percurso de teste HF. Ambiente QA; não é percurso de produção.",
            testOnly = true
        )
    )

    /** QA route choices exist only in debug builds; release navigation remains production-only. */
    val options: List<AndroidRouteOption>
        get() = if (BuildConfig.DEBUG) listOf(production) + qa else listOf(production)

    fun isTestRoute(routeId: String): Boolean =
        BuildConfig.DEBUG && (routeId == SR_ID || routeId == HF_ID)

    fun loadRoute(context: Context, routeId: String): Route = when (routeId) {
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
                name = "SR — trajeto de teste",
                source = "GPX fornecido para cenário QA SR"
            ).loadRoute()
        }
        HF_ID -> {
            check(BuildConfig.DEBUG) { "QA route is available only in debug builds" }
            AssetGpxRouteDataSource(
                context = context,
                assetPath = "data/percurso-teste-hf.gpx",
                routeId = HF_ID,
                name = "HF — trajeto de teste",
                source = "GPX fornecido para cenário QA HF"
            ).loadRoute()
        }
        else -> error("Unknown V1 route: $routeId")
    }

    /** Prefer an existing active/planned route only when that route is available in this build. */
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
