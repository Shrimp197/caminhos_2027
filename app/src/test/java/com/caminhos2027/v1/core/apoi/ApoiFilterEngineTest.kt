package com.caminhos2027.v1.core.apoi

import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiLocation
import com.caminhos2027.v1.core.model.ApoiPublication
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import com.caminhos2027.v1.core.model.RouteRelation
import org.junit.Assert.assertEquals
import org.junit.Test

class ApoiFilterEngineTest {
    private fun apoi(id: String, km: Double, services: Set<ApoiCategory>, status: PublicationStatus = PublicationStatus.PUBLISHED) =
        Apoi(id, id, null, services.first(), services, ApoiLocation(40.0, -8.0, LocationPrecision.EXACT, null, null, null, "r", km, 0.0, 0.0, RouteRelation.ON_ROUTE), ApoiPublication(status, null))

    @Test fun filtersByAllSelectedServicesAndKeepsRouteOrder() {
        val records = listOf(
            apoi("late", 5.0, setOf(ApoiCategory.PERNOITA, ApoiCategory.DUCHES)),
            apoi("early", 2.0, setOf(ApoiCategory.PERNOITA, ApoiCategory.DUCHES)),
            apoi("water", 1.0, setOf(ApoiCategory.AGUA))
        )
        val result = ApoiFilterEngine.apply(records, ApoiFilter(setOf(ApoiCategory.PERNOITA, ApoiCategory.DUCHES)))
        assertEquals(listOf("early", "late"), result.map { it.id })
    }

    @Test fun warningsCanBeExcluded() {
        val records = listOf(apoi("warning", 1.0, setOf(ApoiCategory.AGUA), PublicationStatus.PUBLISHED_WITH_WARNING))
        assertEquals(emptyList<Apoi>(), ApoiFilterEngine.apply(records, ApoiFilter(includeWarnings = false)))
    }
}
