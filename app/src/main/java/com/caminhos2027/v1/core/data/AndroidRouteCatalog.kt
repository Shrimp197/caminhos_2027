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
            description = "Percurso exclusivamente de teste para validação da experiência · QA.",
            testOnly = true
        ),
        AndroidRouteOption(
            id = HF_ID,
            title = "Trajeto HF",
            description = "Percurso exclusivamente de teste para validação da experiência · QA.",
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

    fun persistSelectedRouteId(context: Context, routeId: String) {
        require(routeId == CENTENARIO_ID || isTestRoute(routeId)) {
            "Cannot persist an unknown or unavailable V1 route"
        }
        context.applicationContext
            .getSharedPreferences(ROUTE_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(SELECTED_ROUTE_KEY, routeId)
            .apply()
    }

    /**
     * Restores a route without silently switching between unrelated prepared plans.
     * An active walk always wins; otherwise the last route the user explicitly selected wins.
     * Older installations without the selection preference fall back to their newest planned route.
     */
    fun preferredPersistedRouteId(context: Context): String? {
        val applicationContext = context.applicationContext
        val repository = AndroidWalkRepository(applicationContext)
        val valid = { routeId: String -> routeId == CENTENARIO_ID || isTestRoute(routeId) }
        val activeRoute = repository.list()
            .asReversed()
            .firstOrNull { it.status == WalkStatus.ACTIVE && valid(it.routeId) }
            ?.routeId
        if (activeRoute != null) return activeRoute

        val selected = applicationContext
            .getSharedPreferences(ROUTE_PREFS, Context.MODE_PRIVATE)
            .getString(SELECTED_ROUTE_KEY, null)
            ?.takeIf(valid)
        if (selected != null) return selected

        return repository.list()
            .asReversed()
            .firstOrNull { it.status == WalkStatus.PLANNED && valid(it.routeId) }
            ?.routeId
    }

    private const val ROUTE_PREFS = "walking_v1_route_selection"
    private const val SELECTED_ROUTE_KEY = "selected_route_id"
}
