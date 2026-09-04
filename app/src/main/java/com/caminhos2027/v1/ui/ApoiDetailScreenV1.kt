package com.caminhos2027.v1.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.AutoMirrored
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caminhos2027.v1.core.model.Apoi
import com.caminhos2027.v1.core.model.ApoiAvailabilityStatus
import com.caminhos2027.v1.core.model.ApoiCategory
import com.caminhos2027.v1.core.model.ApoiCostModel
import com.caminhos2027.v1.core.model.ApoiReservationPolicy
import com.caminhos2027.v1.core.model.LocationPrecision
import com.caminhos2027.v1.core.model.PositionConfidence
import com.caminhos2027.v1.core.model.PublicationStatus
import java.util.Locale

/** V1 APOI detail presentation. Missing values are omitted instead of being shown as "não informado". */
@Composable
fun ApoiDetailScreenV1(apoi: Apoi, onBack: () -> Unit = {}) {
    Surface {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onBack) {
                    Icon(AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("APOI", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(apoi.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }
            apoi.description?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
            StatusCard(apoi)
            LocationCard(apoi)
            ServicesCard(apoi)
            OperationalCard(apoi)
            ContactCard(apoi)
            ConfidenceCard(apoi)
            Text(
                "Os dados apresentados dependem da informação disponível e da sua data de confirmação. Uma informação publicada não é, por si só, uma garantia de funcionamento em 2027.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable private fun StatusCard(apoi: Apoi) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(publicationLabel(apoi.publication.status), fontWeight = FontWeight.Bold)
            Text("Estado de disponibilidade: ${availabilityLabel(apoi.availability.status)}")
            availabilityCaveat(apoi.availability.status)?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            apoi.publication.reason?.takeIf { it.isNotBlank() }?.let { Text(it) }
            apoi.availability.validFrom?.takeIf { it.isNotBlank() }?.let { Text("Válido desde: $it") }
            apoi.availability.validUntil?.takeIf { it.isNotBlank() }?.let { Text("Válido até: $it") }
            apoi.availability.recurrence?.takeIf { it.isNotBlank() }?.let { Text("Recorrência: $it") }
            apoi.availability.season?.takeIf { it.isNotBlank() }?.let { Text("Época: $it") }
            apoi.availability.openingHours?.takeIf { it.isNotBlank() }?.let { Text("Horário: $it") }
            apoi.availability.notes?.takeIf { it.isNotBlank() }?.let { Text(it) }
        }
    }
}

@Composable private fun LocationCard(apoi: Apoi) {
    Card {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(androidx.compose.material.icons.Icons.Default.LocationOn, contentDescription = null)
                Text("Localização", fontWeight = FontWeight.Bold)
            }
            apoi.location.routeKm?.let { Text("${formatKm(it)} km no caminho") }
            apoi.location.accessDistanceM?.let { Text("Acesso: ${formatMeters(it)}") }
            listOfNotNull(apoi.location.locality, apoi.location.municipality, apoi.location.reference)
                .joinToString(" · ").takeIf { it.isNotBlank() }?.let { Text(it) }
            Text(locationPrecisionLabel(apoi.location.precision), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable private fun ServicesCard(apoi: Apoi) {
    if (apoi.services.isEmpty()) return
    Card {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Serviços", fontWeight = FontWeight.Bold)
            Text(apoi.services.sortedBy { it.ordinal }.joinToString(" · ") { categoryLabel(it) })
        }
    }
}

@Composable private fun OperationalCard(apoi: Apoi) {
    val rows = buildList {
        when (apoi.cost.model) {
            ApoiCostModel.FREE -> add("Custo" to "Gratuito")
            ApoiCostModel.OPTIONAL_CONTRIBUTION -> add("Custo" to "Contribuição opcional")
            ApoiCostModel.PAID -> add("Custo" to (apoi.cost.amount?.let { "Pago — ${formatAmount(it)} ${apoi.cost.currency.orEmpty()}" } ?: "Pago"))
            ApoiCostModel.UNKNOWN -> Unit
        }
        when (apoi.reservation.policy) {
            ApoiReservationPolicy.NOT_REQUIRED -> add("Reserva" to "Não necessária")
            ApoiReservationPolicy.RECOMMENDED -> add("Reserva" to "Recomendada")
            ApoiReservationPolicy.REQUIRED -> add("Reserva" to "Necessária")
            ApoiReservationPolicy.UNKNOWN -> Unit
        }
        apoi.capacity.sleeping?.let { add("Pernoita" to "Capacidade: $it") }
        apoi.characteristics.sleepingType?.takeIf { it.name != "UNKNOWN" }?.let { add("Tipo" to it.name.lowercase(Locale("pt", "PT"))) }
        if (apoi.characteristics.shower == true) add("Duches" to "Disponíveis")
        if (apoi.characteristics.hotWater == true) add("Água quente" to "Disponível")
        if (apoi.characteristics.wc == true) add("WC" to "Disponível")
        if (apoi.characteristics.laundry == true) add("Lavandaria" to "Disponível")
        if (apoi.characteristics.drying == true) add("Secagem" to "Disponível")
        apoi.capacity.notes?.takeIf { it.isNotBlank() }?.let { add("Capacidade" to it) }
        apoi.cost.description?.takeIf { it.isNotBlank() }?.let { add("Nota de custo" to it) }
        apoi.reservation.notes?.takeIf { it.isNotBlank() }?.let { add("Nota de reserva" to it) }
    }
    if (rows.isEmpty()) return
    Card {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("Informação útil", fontWeight = FontWeight.Bold)
            rows.forEach { (label, value) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(label, modifier = Modifier.weight(0.38f), fontWeight = FontWeight.SemiBold)
                    Text(value, modifier = Modifier.weight(0.62f))
                }
            }
        }
    }
}

@Composable private fun ContactCard(apoi: Apoi) {
    val contact = buildList {
        apoi.contact.responsible?.takeIf { it.isNotBlank() }?.let { add("Responsável" to it) }
        apoi.contact.organization?.takeIf { it.isNotBlank() }?.let { add("Entidade" to it) }
        apoi.contact.phone?.takeIf { it.isNotBlank() }?.let { add("Telefone" to it) }
        apoi.contact.email?.takeIf { it.isNotBlank() }?.let { add("Email" to it) }
        apoi.contact.website?.takeIf { it.isNotBlank() }?.let { add("Website" to it) }
        apoi.contact.social?.takeIf { it.isNotBlank() }?.let { add("Rede social" to it) }
    }
    if (contact.isEmpty()) return
    Card {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Contacto", fontWeight = FontWeight.Bold)
            contact.forEach { (label, value) -> Text("$label: $value") }
        }
    }
}

@Composable private fun ConfidenceCard(apoi: Apoi) {
    Card {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Confiança da informação", fontWeight = FontWeight.Bold)
            confidenceLine("Global", apoi.confidence.overall)
            confidenceLine("Localização", apoi.confidence.location)
            confidenceLine("Apoio", apoi.confidence.support)
            confidenceLine("Disponibilidade", apoi.confidence.availability)
            confidenceLine("Informação crítica", apoi.confidence.criticalInformation)
        }
    }
}

@Composable private fun confidenceLine(label: String, value: PositionConfidence) = Text(
    "$label: ${confidenceLabel(value)}",
    style = MaterialTheme.typography.bodySmall
)

private fun confidenceLabel(value: PositionConfidence): String = when (value) {
    PositionConfidence.HIGH -> "alta"
    PositionConfidence.MEDIUM -> "média"
    PositionConfidence.LOW -> "baixa"
    PositionConfidence.UNKNOWN -> "por confirmar"
}

private fun availabilityCaveat(status: ApoiAvailabilityStatus): String? = when (status) {
    ApoiAvailabilityStatus.CURRENT -> "Informação indicada como válida atualmente; confirme antes de depender deste apoio."
    ApoiAvailabilityStatus.FUTURE_CONFIRMED -> "Há confirmação para o período indicado; isso não substitui a confirmação de 2027 quando o período for outro."
    ApoiAvailabilityStatus.RECURRING -> "A recorrência é informativa e pode não cobrir a data concreta da caminhada."
    ApoiAvailabilityStatus.AWAITING_CONFIRMATION -> "A informação ainda aguarda confirmação."
    ApoiAvailabilityStatus.HISTORICAL -> "Informação histórica: não deve ser tratada como disponibilidade atual."
    ApoiAvailabilityStatus.EXPIRED -> "Informação expirada: não deve ser tratada como disponibilidade atual."
    ApoiAvailabilityStatus.CLOSED -> "Este APOI está indicado como encerrado."
}

private fun publicationLabel(status: PublicationStatus): String = when (status) {
    PublicationStatus.PUBLISHED -> "Informação publicada"
    PublicationStatus.PUBLISHED_WITH_WARNING -> "Informação publicada com aviso"
    PublicationStatus.HISTORICAL -> "Informação histórica"
    PublicationStatus.CLOSED -> "APOI encerrado"
    else -> "Informação em revisão"
}

private fun availabilityLabel(status: ApoiAvailabilityStatus): String = when (status) {
    ApoiAvailabilityStatus.CURRENT -> "atual"
    ApoiAvailabilityStatus.FUTURE_CONFIRMED -> "futura confirmada"
    ApoiAvailabilityStatus.RECURRING -> "recorrente"
    ApoiAvailabilityStatus.HISTORICAL -> "histórica"
    ApoiAvailabilityStatus.EXPIRED -> "expirada"
    ApoiAvailabilityStatus.AWAITING_CONFIRMATION -> "aguarda confirmação"
    ApoiAvailabilityStatus.CLOSED -> "encerrada"
}

private fun locationPrecisionLabel(value: LocationPrecision): String = when (value) {
    LocationPrecision.EXACT -> "Localização exata"
    LocationPrecision.APPROXIMATE -> "Localização aproximada"
    LocationPrecision.LOCALITY_ONLY -> "Localização indicada pela localidade"
    LocationPrecision.UNKNOWN -> "Localização por confirmar"
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
private fun formatMeters(value: Double): String = if (value >= 1000) "${formatKm(value / 1000)} km" else "${value.toInt()} m"
private fun formatAmount(value: Double): String = String.format(Locale("pt", "PT"), "%.2f", value)
