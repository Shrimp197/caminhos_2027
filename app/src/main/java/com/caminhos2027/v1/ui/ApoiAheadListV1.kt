package com.caminhos2027.v1.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.apoi.ApoiAhead
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.PublicationStatus
import java.util.Locale

/** Compact, information-first list of published APOI ahead of the pilgrim. */
@Composable
fun ApoiAheadListV1(
    results: List<ApoiAhead>,
    onOpen: (ApoiAhead) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("APOI à frente", fontWeight = FontWeight.Bold)
            if (results.isEmpty()) {
                Text(
                    "Não há APOI publicados à frente com estes critérios.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                results.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(item.apoi.name, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${categoryLabel(item.apoi.mainCategory)} · ${formatKm(item.distanceKm)} km",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (item.apoi.publication.status == PublicationStatus.PUBLISHED_WITH_WARNING) {
                                Text(
                                    "⚠ Informação com aviso",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Button(onClick = { onOpen(item) }) { Text("Abrir") }
                    }
                }
            }
        }
    }
}

private fun categoryLabel(category: ApoiCategory): String = when (category) {
    ApoiCategory.ALIMENTACAO -> "Alimentação"
    ApoiCategory.AGUA -> "Água"
    ApoiCategory.DESCANSO -> "Descanso"
    ApoiCategory.PERNOITA -> "Pernoita"
    ApoiCategory.DUCHES -> "Duches"
    ApoiCategory.CARREGAMENTO -> "Carregamento"
    ApoiCategory.TRANSPORTE -> "Transporte"
    ApoiCategory.EMERGENCIA -> "Emergência"
}

private fun formatKm(value: Double): String = String.format(Locale("pt", "PT"), "%.1f", value)
