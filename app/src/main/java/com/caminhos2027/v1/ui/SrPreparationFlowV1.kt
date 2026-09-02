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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.model.Route
import com.caminhos2027.v1.core.walking.WalkingPreparation
import com.caminhos2027.v1.core.walking.WalkingPreparationService

/** SR-only preparation flow. It deliberately uses route kilometres rather than automatic stage planning. */
@Composable
fun SrPreparationFlowV1(
    route: Route,
    preparationService: WalkingPreparationService,
    onSaved: (WalkingPreparation) -> Unit = {},
    onStart: (WalkingPreparation) -> Unit = {}
) {
    var startKm by remember { mutableStateOf(0.0) }
    var destinationKm by remember { mutableStateOf(route.totalDistanceKm) }
    var preparation by remember { mutableStateOf<WalkingPreparation?>(null) }

    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(route.officialName)
        Text("Preparação SR — ambiente de teste")
        Text("As etapas oficiais servem de referência; o início e o destino podem ficar dentro de uma etapa.")

        KmEditor("Início", startKm, 0.0, route.totalDistanceKm) { startKm = it }
        KmEditor("Destino", destinationKm, 0.0, route.totalDistanceKm) { destinationKm = it }

        Button(onClick = {
            preparation = preparationService.preview("sr-walk", startKm, destinationKm)
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Rever plano")
        }

        preparation?.let { plan ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Plano: %.1f km".format(plan.walk.plannedDestinationKm!! - plan.walk.plannedStartKm!!))
                    Text("Etapas: ${plan.stages.joinToString { it.name }}")
                    Text("APOI relevantes: ${plan.relevantApoi.size}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onSaved(preparationService.save("sr-walk", startKm, destinationKm)) }) {
                            Text("Guardar plano")
                        }
                        Button(onClick = { onStart(plan) }) {
                            Text("Iniciar caminhada")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KmEditor(label: String, value: Double, min: Double, max: Double, onChange: (Double) -> Unit) {
    // SR deliberately keeps this control simple and deterministic; a production slider/input
    // will be designed only after the end-to-end flow is validated with users.
    Column {
        Text("$label: %.1f km".format(value))
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onChange((value - 1.0).coerceIn(min, max)) }) { Text("−1") }
            Button(onClick = { onChange((value + 1.0).coerceIn(min, max)) }) { Text("+1") }
        }
    }
}
