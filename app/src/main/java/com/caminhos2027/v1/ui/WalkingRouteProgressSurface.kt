package com.caminhos2027.v1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.route.WalkingProgress
import java.util.Locale

internal data class WalkingRouteProgressPresentation(
    val ratio: Float,
    val currentLabel: String,
    val remainingLabel: String,
    val destinationLabel: String
)

internal object WalkingRouteProgressPresenter {
    fun present(progress: WalkingProgress): WalkingRouteProgressPresentation =
        WalkingRouteProgressPresentation(
            ratio = progress.progressRatio.coerceIn(0.0, 1.0).toFloat(),
            currentLabel = "${formatKm(progress.currentRouteKm)} km",
            remainingLabel = "${formatKm(progress.remainingKm)} km restantes",
            destinationLabel = "Destino ${formatKm(progress.targetRouteKm)} km"
        )

    private fun formatKm(value: Double): String =
        String.format(Locale.US, "%.1f", value)
}

@Composable
internal fun WalkingRouteProgressSurface(progress: WalkingProgress?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Text(
                "Progresso no percurso",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Traçado local e progresso disponíveis sem rede.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF68736D)
            )
            Spacer(Modifier.height(14.dp))
            if (progress == null) {
                Text(
                    "Aguardando uma posição de percurso fiável.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF68736D)
                )
            } else {
                val presentation = WalkingRouteProgressPresenter.present(progress)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFE8F2ED))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(presentation.ratio)
                            .height(18.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF165B43))
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Início", style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        presentation.currentLabel,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(presentation.destinationLabel, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    presentation.remainingLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF68736D)
                )
            }
        }
    }
}
