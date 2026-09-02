package com.caminhos2027.v1.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.walking.WalkingPreparation
import java.util.Locale

/**
 * V1 preparation presentation. It receives an already validated preparation model;
 * it does not load route data, invent coordinates, or perform automatic planning.
 */
@Composable
fun PreparationScreenV1(
    preparation: WalkingPreparation?,
    onSavePlan: () -> Unit,
    onStartWalk: () -> Unit,
    onBack: () -> Unit
) {
    Surface {
        if (preparation == null) {
            EmptyPreparationState(onBack)
        } else {
            PreparationContent(preparation, onSavePlan, onStartWalk, onBack)
        }
    }
}

@Composable
private fun EmptyPreparationState(onBack: () -> Unit) {
    Column(modifier = Modifier.padding(20.dp)) {
        Text("Preparar caminhada", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("Escolha o início e o destino para criar um plano.")
        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = onBack) { Text("Voltar") }
    }
}

@Composable
private fun PreparationContent(
    preparation: WalkingPreparation,
    onSavePlan: () -> Unit,
    onStartWalk: () -> Unit,
    onBack: () -> Unit
) {
    val walk = preparation.walk
    val start = walk.plannedStartKm ?: 0.0
    val destination = walk.plannedDestinationKm ?: 0.0
    val distance = destination - start

    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Preparar caminhada", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(preparation.route.officialName, style = MaterialTheme.typography.titleMedium)
        Text("As etapas oficiais são uma referência. Pode começar ou terminar a meio de uma etapa.")

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Plano", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Início: ${formatKm(start)} km")
                Text("Destino: ${formatKm(destination)} km")
                Text("Distância planeada: ${formatKm(distance)} km")
            }
        }

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Etapas de referência", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                preparation.stages.forEach { stage ->
                    Text("${stage.number}. ${stage.name}")
                }
            }
        }

        if (preparation.relevantApoi.isNotEmpty()) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("APOI no percurso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    preparation.relevantApoi.take(5).forEach { apoi ->
                        val km = apoi.location.routeKm
                        Text("${apoi.name}${km?.let { " — ${formatKm(it)} km" } ?: ""}")
                    }
                    if (preparation.relevantApoi.size > 5) {
                        Text("+ ${preparation.relevantApoi.size - 5} APOI", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Voltar") }
            Button(onClick = onSavePlan, modifier = Modifier.weight(1f)) { Text("Guardar plano") }
        }
        Button(onClick = onStartWalk, modifier = Modifier.fillMaxWidth()) { Text("Iniciar caminhada") }
    }
}

private fun formatKm(value: Double): String = String.format(Locale("pt", "PT"), "%.1f", value)
