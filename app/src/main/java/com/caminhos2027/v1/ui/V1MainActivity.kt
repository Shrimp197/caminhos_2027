package com.caminhos2027.v1.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Forest = Color(0xFF165B43)
private val Sand = Color(0xFFF7F4EE)
private val Ink = Color(0xFF1E2521)
private val Muted = Color(0xFF68736D)
private val RouteLine = Color(0xFF6E8E7F)

class V1MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CaminhosTheme { WalkingScreenV1() } }
    }
}

@Composable
private fun CaminhosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Forest,
            onPrimary = Color.White,
            background = Sand,
            surface = Color.White,
            onSurface = Ink,
            onBackground = Ink
        ),
        content = content
    )
}

@Composable
private fun WalkingScreenV1() {
    Surface(modifier = Modifier.fillMaxSize(), color = Sand) {
        Box(modifier = Modifier.fillMaxSize()) {
            RouteMapPreview(modifier = Modifier.fillMaxSize())

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = Forest, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(7.dp))
                        Column {
                            Text("Caminhada atual", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text("Etapa de referência", style = MaterialTheme.typography.labelSmall, color = Muted)
                        }
                    }
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Ink)
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                NextSupportCard()
                Spacer(Modifier.height(8.dp))
                ProgressCard()
            }
        }
    }
}

@Composable
private fun NextSupportCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F2ED)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Forest)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Próximo APOI", style = MaterialTheme.typography.labelMedium, color = Muted)
                    Text("Ponto de apoio ao peregrino", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Água · descanso", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = "Ver detalhe", tint = Muted)
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Forest, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(5.dp))
                Text("1,2 km pelo caminho", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text("Informação a confirmar", style = MaterialTheme.typography.labelSmall, color = Muted)
            }
        }
    }
}

@Composable
private fun ProgressCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Forest)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("12,5 km percorridos", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("199,4 km até ao objetivo", color = Color.White.copy(alpha = .82f), style = MaterialTheme.typography.bodySmall)
                }
                Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(9.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = .25f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(.06f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                )
            }
        }
    }
}

@Composable
private fun RouteMapPreview(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(Color(0xFFE9E7E0))) {
        val route = Path().apply {
            moveTo(size.width * .15f, size.height * .25f)
            cubicTo(size.width * .32f, size.height * .42f, size.width * .46f, size.height * .18f, size.width * .60f, size.height * .38f)
            cubicTo(size.width * .72f, size.height * .56f, size.width * .65f, size.height * .70f, size.width * .84f, size.height * .82f)
        }
        drawPath(route, color = RouteLine, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
        drawCircle(color = Forest, radius = 12.dp.toPx(), center = Offset(size.width * .47f, size.height * .30f))
        drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(size.width * .47f, size.height * .30f))
    }
}
