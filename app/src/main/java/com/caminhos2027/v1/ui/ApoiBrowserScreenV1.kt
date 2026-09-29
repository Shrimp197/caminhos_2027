package com.caminhos2027.v1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.apoi.ApoiAhead
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.PublicationStatus
import java.util.Locale

private val ApoioBlue = Color(0xFF16516B)
private val ApoioGreen = Color(0xFF159447)
private val ApoioBg = Color(0xFFF7F8F6)
private val ApoioMuted = Color(0xFF687278)

@Composable
fun ApoiBrowserScreenV1(
    query: String,
    selectedServices: Set<ApoiCategory>,
    items: List<ApoiAhead>,
    onQueryChanged: (String) -> Unit,
    onServiceToggled: (ApoiCategory) -> Unit,
    onApoiSelected: (ApoiAhead) -> Unit = {}
) {
    Surface(color = ApoioBg) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Apoios", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = ApoioBlue)
                    Text("Serviços disponíveis ao longo do percurso", color = ApoioMuted)
                }
                Icon(Icons.Filled.Place, null, tint = ApoioGreen, modifier = Modifier.size(30.dp))
            }

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                label = { Text("Procurar apoio") },
                shape = RoundedCornerShape(14.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                ApoiCategory.entries.forEach { category ->
                    FilterChip(
                        selected = category in selectedServices,
                        onClick = { onServiceToggled(category) },
                        label = { Text(categoryLabel(category)) }
                    )
                }
            }

            if (items.isEmpty()) {
                Card(
                    Modifier.fillMaxWidth(),
                    RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        "Não foram encontrados APOI com estes critérios.",
                        modifier = Modifier.padding(18.dp),
                        color = ApoioMuted
                    )
                }
            } else {
                items.forEach { item -> ApoiBrowserCard(item, onApoiSelected) }
            }
        }
    }
}

@Composable
private fun ApoiBrowserCard(item: ApoiAhead, onSelected: (ApoiAhead) -> Unit) {
    val category = item.apoi.mainCategory
    Card(
        onClick = { onSelected(item) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(46.dp).background(categoryColor(category), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Place, null, tint = Color.White, modifier = Modifier.size(25.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(categoryLabel(category).uppercase(Locale("pt", "PT")), color = categoryColor(category), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium)
                Text(item.apoi.name, fontWeight = FontWeight.ExtraBold, color = ApoioBlue)
                Text(
                    "${formatKm(item.distanceKm)} km · " +
                        item.apoi.services.sortedBy { it.name }.joinToString(" · ") { categoryLabel(it) },
                    color = ApoioMuted,
                    style = MaterialTheme.typography.bodySmall
                )
                if (item.apoi.publication.status == PublicationStatus.PUBLISHED_WITH_WARNING) {
                    Text("⚠ Informação com ressalva", color = Color(0xFF9A5A00), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun categoryColor(category: ApoiCategory): Color = when (category) {
    ApoiCategory.AGUA -> Color(0xFF1687C9)
    ApoiCategory.ALIMENTACAO -> Color(0xFFD77B18)
    ApoiCategory.PERNOITA -> Color(0xFF7561C9)
    ApoiCategory.EMERGENCIA -> Color(0xFFC33D35)
    ApoiCategory.CARREGAMENTO -> Color(0xFF2B8C67)
    ApoiCategory.DESCANSO, ApoiCategory.DUCHES, ApoiCategory.TRANSPORTE -> ApoioBlue
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
