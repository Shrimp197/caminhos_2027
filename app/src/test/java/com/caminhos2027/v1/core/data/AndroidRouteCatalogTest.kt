package com.caminhos2027.v1.core.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AndroidRouteCatalogTest {
    @Test
    fun preparationCatalogExposesProductionAndBothQaRoutes() {
        assertEquals(
            listOf("caminho-do-centenario", "sr-test", "hf-test"),
            AndroidRouteCatalog.options.map { it.id }
        )
        assertEquals(2, AndroidRouteCatalog.options.count { it.testOnly })
        assertTrue(AndroidRouteCatalog.options.first { it.id == "sr-test" }.description.contains("QA"))
        assertTrue(AndroidRouteCatalog.options.first { it.id == "hf-test" }.description.contains("QA"))
    }
}
