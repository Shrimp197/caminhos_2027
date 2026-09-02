package com.caminhos2027.v1.core.data

import com.caminhos2027.v1.core.model.ApoiDataset
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatingApoiRepositoryTest {
    @Test
    fun acceptsMatchingEnvironment() {
        val repository = ValidatingApoiRepository(
            object : ApoiDataSource {
                override fun loadDataset() = ApoiDataset("TEST-FICTITIOUS", "sr", emptyList())
            }
        )
        assertTrue(repository.getDataset("sr").isSuccess)
    }

    @Test
    fun rejectsEnvironmentMismatch() {
        val repository = ValidatingApoiRepository(
            object : ApoiDataSource {
                override fun loadDataset() = ApoiDataset("TEST-FICTITIOUS", "sr", emptyList())
            }
        )
        assertTrue(repository.getDataset("production").isFailure)
    }
}
