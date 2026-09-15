package com.caminhos2027.v1.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.apoi.ApoiAhead
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.PublicationStatus
import java.util.Locale

/** APOI discovery surface. Domain supplies already-filtered, route-ordered results. */
@Composable
fun ApoiBrowserScreenV1(
    query: String,
    selectedServices: Set<ApoiCategory>,
    items: List<ApoiAhead>,
    onQueryChanged: (String) -> Unit,
    onServiceToggled: (ApoiCategory) -> Unit,
    onApoiSelected: (ApoiAhead) -> Unit = {}
) {
    Surface {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Procurar APOI", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Encontre apoio ao longo do Caminho, por nome ou serviço.", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Procurar") }
            )
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ApoiCategory.entries.forEach { category ->
                    FilterChip(
                        selected = category in selectedServices,
                        onClick = { onServiceToggled(category) },
                        label = { Text(categoryLabel(category)) }
                    )
                }
            }
            if (items.isEmpty()) {
                Text("Não foram encontrados APOI com estes critérios.")
            } else {
                items.forEach { item ->
                    ApoiBrowserCard(item, onApoiSelected)
                }
            }
        }
    }
}

@Composable
private fun ApoiBrowserCard(item: ApoiAhead, onSelected: (ApoiAhead) -> Unit) {
    Card(onClick = { onSelected(item) }, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.apoi.name, fontWeight = FontWeight.Bold)
            Text("${formatKm(item.distanceKm)} km pelo caminho", style = MaterialTheme.typography.bodyMedium)
            Text(item.apoi.services.sortedBy { it.name }.joinToString(" · ") { categoryLabel(it) }, style = MaterialTheme.typography.bodySmall)
            if (item.apoi.publication.status == PublicationStatus.PUBLISHED_WITH_WARNING) {
                Text("⚠ Informação com ressalva", style = MaterialTheme.typography.bodySmall)
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
