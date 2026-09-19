package com.caminhos2027.v1.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.walking.WalkingDecisionContext
import com.caminhos2027.v1.core.walking.WalkingDecisionOption
import java.util.Locale

/** Neutral decision surface: shows consequences and available support without choosing for the pilgrim. */
@Composable
fun WalkingDecisionScreenV1(
    context: WalkingDecisionContext,
    onApoiSelected: (Apoi) -> Unit = {}
) {
    Surface {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Decidir", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Está em ${formatKm(context.currentRouteKm)} km do caminho. Veja o que cada opção implica.",
                style = MaterialTheme.typography.bodyLarge
            )

            DecisionOptionCard(context.stopNow, onApoiSelected)
            DecisionOptionCard(context.continueWalking, onApoiSelected)

            Text(
                "A decisão é sua. A aplicação apresenta informação e alternativas; não determina quando deve parar ou continuar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DecisionOptionCard(
    option: WalkingDecisionOption,
    onApoiSelected: (Apoi) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(option.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(formatDistance(option.distanceKm), fontWeight = FontWeight.SemiBold)
            }

            if (option.relevantApoi.isEmpty()) {
                Text("Sem APOI adicional associado a esta opção.", style = MaterialTheme.typography.bodySmall)
            } else {
                Text("APOI que pode encontrar pelo caminho:", style = MaterialTheme.typography.bodyMedium)
                option.relevantApoi.forEach { apoi ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onApoiSelected(apoi) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(apoi.name, fontWeight = FontWeight.SemiBold)
                            apoi.location.routeKm?.let { km ->
                                Text("${formatKm(km)} km no caminho", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                apoi.services.sortedBy { it.ordinal }.joinToString(" · ") { it.name.lowercase(Locale("pt", "PT")) },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDistance(value: Double): String =
    if (value == 0.0) "Agora" else "${formatKm(value)} km"

private fun formatKm(value: Double): String =
    String.format(Locale("pt", "PT"), "%.1f", value)
