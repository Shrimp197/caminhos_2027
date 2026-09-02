package com.caminhos2027.v1.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.apoi.ApoiAhead
import com.caminhos2027.v1.core.model.ApoiCategory
import java.util.Locale

/** Compact list of publishable APOI ahead of the current route position. */
@Composable
fun NextApoiScreenV1(
    items: List<ApoiAhead>,
    onApoiSelected: (ApoiAhead) -> Unit = {}
) {
    Surface {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Próximos APOI", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Apoios ordenados pela distância ao longo do Caminho.", style = MaterialTheme.typography.bodyMedium)
            if (items.isEmpty()) {
                Text("Não há APOI publicados à frente nesta posição.")
            } else {
                items.forEach { item ->
                    ApoiAheadCard(item, onApoiSelected)
                }
            }
        }
    }
}

@Composable
private fun ApoiAheadCard(item: ApoiAhead, onApoiSelected: (ApoiAhead) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onApoiSelected(item) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(item.apoi.name, fontWeight = FontWeight.Bold)
                Text(categoryLabel(item.apoi.mainCategory), style = MaterialTheme.typography.bodySmall)
                Text("${formatKm(item.distanceKm)} km pelo caminho", style = MaterialTheme.typography.bodyMedium)
                item.apoi.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
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
