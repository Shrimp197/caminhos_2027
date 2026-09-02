package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.ApoiCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class ApoiRepositoryTest {
    @Test
    fun `repository delegates to data source and does not expose JSON concerns`() {
        val source = object : ApoiDataSource {
            override fun load() = listOf(
                ApoiJsonParser.parseDataset(
                    """
                    {"items":[{"id":"a1","name":"Água","main_category":"AGUA","services":["AGUA"],"location":{"precision":"EXACT","route_relation":"ON_ROUTE"}}]}
                    """.trimIndent()
                ).single()
            )
        }

        val records = ApoiRepository(source).getAll()

        assertEquals(1, records.size)
        assertEquals("a1", records.single().id)
        assertEquals(ApoiCategory.AGUA, records.single().mainCategory)
    }
}
