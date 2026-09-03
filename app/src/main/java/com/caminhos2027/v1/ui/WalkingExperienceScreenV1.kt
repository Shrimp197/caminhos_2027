package com.caminhos2027.v1.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.AppState
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.walking.WalkingDecisionContext

/**
 * Shared V1 walking experience seam.
 *
 * The screen consumes the single AppState read model and delegates mutations to the host.
 * It intentionally contains no GPS, APOI qualification, distance, or decision rules.
 */
@Composable
fun WalkingExperienceScreenV1(
    state: AppState,
    onOpenApoi: (Apoi) -> Unit = {},
    onOpenDecision: () -> Unit = {},
    onBackToWalking: () -> Unit = {}
) {
    val walking = state.walking
    val browser = state.apoiBrowser
    val decision = state.decision

    Surface {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Caminhada", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            if (walking == null) {
                Text("Nenhuma caminhada ativa.")
                return@Column
            }

            walking.routePosition?.let { position ->
                Text("Está aqui: %.1f km no caminho".format(position.routeKm))
            } ?: Text("A posição atual ainda não está disponível.")

            walking.nextApoi?.let { next ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Próximo APOI", style = MaterialTheme.typography.labelMedium)
                        Text(next.name, fontWeight = FontWeight.Bold)
                        walking.nextApoiDistanceKm?.let { Text("%.1f km pelo caminho".format(it)) }
                        Button(onClick = { onOpenApoi(next) }) { Text("Ver detalhe") }
                    }
                }
            }

            browser?.results?.let { results ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("APOI à frente", fontWeight = FontWeight.Bold)
                        results.forEach { item ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(item.apoi.name, modifier = Modifier.weight(1f))
                                Button(onClick = { onOpenApoi(item.apoi) }) { Text("Abrir") }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onOpenDecision,
                modifier = Modifier.fillMaxWidth(),
                enabled = walking.routePosition != null
            ) {
                Text("Decidir: parar ou continuar")
            }

            decision?.let { context -> DecisionSummary(context) }

            Button(onClick = onBackToWalking, modifier = Modifier.fillMaxWidth()) {
                Text("Voltar à caminhada")
            }
        }
    }
}

@Composable
private fun DecisionSummary(context: WalkingDecisionContext) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Decisão", fontWeight = FontWeight.Bold)
            Text("Parar agora — sem distância adicional.")
            Text("Continuar — %.1f km até ao destino planeado.".format(context.remainingToPlannedDestinationKm))
            Text(
                "A aplicação apresenta as opções e os apoios encontrados; a decisão continua a ser do peregrino.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
