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
import com.caminhos2027.v1.core.model.PublicationStatus

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
    val presentation = ApoiAheadPresentationMapper.map(item.apoi, item.distanceKm)
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onApoiSelected(item) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(presentation.name, fontWeight = FontWeight.Bold)
                Text(presentation.categoryLabel, style = MaterialTheme.typography.bodySmall)
                Text(presentation.distanceLabel + " pelo caminho", style = MaterialTheme.typography.bodyMedium)
                Text(presentation.availabilityLabel, style = MaterialTheme.typography.bodySmall)
                Text(presentation.confidenceLabel, style = MaterialTheme.typography.bodySmall)
                if (item.apoi.publication.status == PublicationStatus.PUBLISHED_WITH_WARNING) {
                    presentation.warningLabel?.let {
                        Text("⚠ $it", style = MaterialTheme.typography.bodySmall)
                    }
                }
                item.apoi.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
