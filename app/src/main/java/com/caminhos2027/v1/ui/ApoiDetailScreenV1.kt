package com.caminhos2027.v1.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PublicationStatus
import java.util.Locale

/** Essential-first APOI detail. Missing fields are omitted instead of displayed as unknown text. */
@Composable
fun ApoiDetailScreenV1(
    apoi: Apoi,
    distanceFromWalkKm: Double? = null,
    onBack: () -> Unit = {}
) {
    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("Voltar") }
        Text(apoi.name, style = MaterialTheme.typography.headlineSmall)
        Text(categoryLabel(apoi.mainCategory), style = MaterialTheme.typography.labelLarge)
        apoi.description?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                distanceFromWalkKm?.let { Text("${formatKm(it)} km pelo caminho") }
                apoi.location.routeKm?.let { Text("Km do caminho: ${formatKm(it)}") }
                apoi.location.accessDistanceM?.let { Text("Acesso/desvio: ${it.toInt()} m") }
                if (apoi.services.isNotEmpty()) {
                    Text("Serviços")
                    Text(apoi.services.joinToString(" · ") { categoryLabel(it) })
                }
                if (apoi.location.precision != LocationPrecision.EXACT) {
                    Text("⚠️ Localização ${precisionLabel(apoi.location.precision)}")
                }
                if (apoi.publication.status == PublicationStatus.PUBLISHED_WITH_WARNING) {
                    Text("⚠️ ${apoi.publication.reason ?: "Informação a confirmar"}")
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

private fun precisionLabel(precision: LocationPrecision): String = when (precision) {
    LocationPrecision.APPROXIMATE -> "aproximada"
    LocationPrecision.LOCALITY_ONLY -> "indicativa"
    LocationPrecision.UNKNOWN -> "não confirmada"
    LocationPrecision.EXACT -> "exata"
}

private fun formatKm(value: Double): String = String.format(Locale("pt", "PT"), "%.1f", value)
