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
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Bolt
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
                FilterChip(
                    selected = selectedServices.isEmpty(),
                    onClick = {
                        selectedServices.toList().forEach(onServiceToggled)
                    },
                    label = { Text("Todos") }
                )
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
                Modifier.size(48.dp).background(
                    categoryColor(category).copy(alpha = 0.12f),
                    RoundedCornerShape(14.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(categoryIcon(category), null, tint = categoryColor(category), modifier = Modifier.size(25.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    categoryLabel(category).uppercase(Locale("pt", "PT")),
                    color = categoryColor(category),
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    item.apoi.name,
                    fontWeight = FontWeight.ExtraBold,
                    color = ApoioBlue,
                    maxLines = 2
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${formatKm(item.distanceKm)} km",
                        color = ApoioBlue,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        costLabelBrowser(item.apoi),
                        color = ApoioMuted,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
                Text(
                    availabilityLabelBrowser(item.apoi),
                    color = availabilityColorBrowser(item.apoi),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
                reservationLabelBrowser(item.apoi)?.let {
                    Text(it, color = ApoioMuted, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
                if (item.apoi.publication.status == PublicationStatus.PUBLISHED_WITH_WARNING) {
                    Text(
                        "⚠ Informação com ressalva",
                        color = Color(0xFF9A5A00),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

private fun categoryIcon(category: ApoiCategory): androidx.compose.ui.graphics.vector.ImageVector = when (category) {
    ApoiCategory.AGUA -> Icons.Filled.WaterDrop
    ApoiCategory.ALIMENTACAO -> Icons.Filled.Restaurant
    ApoiCategory.PERNOITA -> Icons.Filled.Hotel
    ApoiCategory.EMERGENCIA -> Icons.Filled.LocalHospital
    ApoiCategory.CARREGAMENTO -> Icons.Filled.Bolt
    else -> Icons.Filled.Place
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

private fun costLabelBrowser(apoi: com.caminhos2027.v1.core.model.Apoi): String = when (apoi.cost.model) {
    com.caminhos2027.v1.core.model.ApoiCostModel.FREE -> "Custo · gratuito"
    com.caminhos2027.v1.core.model.ApoiCostModel.OPTIONAL_CONTRIBUTION -> "Custo · contribuição opcional"
    com.caminhos2027.v1.core.model.ApoiCostModel.PAID -> {
        val amount = apoi.cost.amount
        val currency = apoi.cost.currency.orEmpty()
        if (amount != null && currency.isNotBlank()) "Custo · pago · " + String.format(Locale("pt", "PT"), "%.2f", amount) + " " + currency
        else "Custo · pago"
    }
    com.caminhos2027.v1.core.model.ApoiCostModel.UNKNOWN -> "Custo · por confirmar"
}

private fun reservationLabelBrowser(apoi: com.caminhos2027.v1.core.model.Apoi): String? = when (apoi.reservation.policy) {
    com.caminhos2027.v1.core.model.ApoiReservationPolicy.NOT_REQUIRED -> "Reserva · não necessária"
    com.caminhos2027.v1.core.model.ApoiReservationPolicy.RECOMMENDED -> "Reserva · recomendada"
    com.caminhos2027.v1.core.model.ApoiReservationPolicy.REQUIRED -> "Reserva · necessária"
    com.caminhos2027.v1.core.model.ApoiReservationPolicy.UNKNOWN -> null
}

private fun availabilityLabelBrowser(apoi: com.caminhos2027.v1.core.model.Apoi): String = when (apoi.availability.status) {
    com.caminhos2027.v1.core.model.ApoiAvailabilityStatus.CURRENT -> "Disponibilidade · indicada como atual"
    com.caminhos2027.v1.core.model.ApoiAvailabilityStatus.RECURRING -> "Disponibilidade · recorrente"
    com.caminhos2027.v1.core.model.ApoiAvailabilityStatus.FUTURE_CONFIRMED -> "Disponibilidade · futura confirmada"
    com.caminhos2027.v1.core.model.ApoiAvailabilityStatus.AWAITING_CONFIRMATION -> "Disponibilidade · aguarda confirmação"
    com.caminhos2027.v1.core.model.ApoiAvailabilityStatus.HISTORICAL -> "Disponibilidade · histórica"
    com.caminhos2027.v1.core.model.ApoiAvailabilityStatus.EXPIRED -> "Disponibilidade · expirada"
    com.caminhos2027.v1.core.model.ApoiAvailabilityStatus.CLOSED -> "Disponibilidade · encerrada"
}

private fun availabilityColorBrowser(apoi: com.caminhos2027.v1.core.model.Apoi): Color = when (apoi.availability.status) {
    com.caminhos2027.v1.core.model.ApoiAvailabilityStatus.CURRENT,
    com.caminhos2027.v1.core.model.ApoiAvailabilityStatus.RECURRING,
    com.caminhos2027.v1.core.model.ApoiAvailabilityStatus.FUTURE_CONFIRMED -> ApoioGreen
    com.caminhos2027.v1.core.model.ApoiAvailabilityStatus.AWAITING_CONFIRMATION -> Color(0xFF9A5A00)
    com.caminhos2027.v1.core.model.ApoiAvailabilityStatus.HISTORICAL,
    com.caminhos2027.v1.core.model.ApoiAvailabilityStatus.EXPIRED,
    com.caminhos2027.v1.core.model.ApoiAvailabilityStatus.CLOSED -> Color(0xFF9A2F2F)
}