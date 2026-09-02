package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.apoi.ApoiCatalog
import com.caminhos2027.v1.core.apoi.ApoiQualificationEvidence
import com.caminhos2027.v1.core.model.ApoiAvailabilityStatus
import com.caminhos2027.v1.core.model.PublicationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApoiPublicationPipelineTest {
    @Test
    fun `parsed master record becomes publishable only when 2027 evidence is explicit`() {
        val records = ApoiJsonParser.parseDataset(
            """
            {
              "items": [
                {
                  "id": "pipeline-001",
                  "name": "Apoio 2027",
                  "main_category": "AGUA",
                  "services": ["AGUA"],
                  "location": {"precision":"EXACT","route_id":"caminho-centenario","route_km":10.0,"route_relation":"ON_ROUTE"},
                  "availability": {"status":"CURRENT"},
                  "publication": {"status":"REVIEW","reason":"awaiting editorial confirmation"}
                }
              ]
            }
            """.trimIndent()
        )

        val catalog = ApoiCatalog(
            targetYear = 2027,
            records = records,
            evidenceByApoiId = mapOf(
                "pipeline-001" to ApoiQualificationEvidence(
                    pilgrimSupportConfirmed = true,
                    confirmedForYear = 2027
                )
            )
        )

        val published = catalog.published()

        assertEquals(1, published.size)
        assertEquals(PublicationStatus.PUBLISHED, published.single().publication.status)
        assertEquals(ApoiAvailabilityStatus.CURRENT, published.single().availability.status)
        assertTrue(catalog.allQualified().single().status == PublicationStatus.PUBLISHED)
    }

    @Test
    fun `2026 evidence without 2027 confirmation stays out of published catalog`() {
        val records = ApoiJsonParser.parseDataset(
            """
            {
              "items": [
                {
                  "id": "pipeline-002",
                  "name": "Apoio 2026",
                  "main_category": "PERNOITA",
                  "services": ["PERNOITA"],
                  "location": {"precision":"EXACT","route_id":"caminho-centenario","route_km":20.0,"route_relation":"ON_ROUTE"},
                  "availability": {"status":"AWAITING_CONFIRMATION"},
                  "publication": {"status":"REVIEW","reason":"2027 confirmation pending"}
                }
              ]
            }
            """.trimIndent()
        )

        val catalog = ApoiCatalog(
            targetYear = 2027,
            records = records,
            evidenceByApoiId = mapOf(
                "pipeline-002" to ApoiQualificationEvidence(
                    pilgrimSupportConfirmed = true,
                    confirmedForYear = 2026
                )
            )
        )

        assertTrue(catalog.published().isEmpty())
        assertEquals(PublicationStatus.REVIEW, catalog.allQualified().single().status)
    }
}
