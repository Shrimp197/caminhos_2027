package com.caminhos2027.v1.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
internal fun ReferenceHeroArt(modifier: Modifier = Modifier) {
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFB9D9E8), Color(0xFFE7E5D7), Color(0xFF6D9B65))
                )
            )

            drawCircle(
                color = Color(0xFFFFD86B),
                radius = w * 0.11f,
                center = Offset(w * 0.78f, h * 0.22f)
            )

            val distant = Path().apply {
                moveTo(0f, h * 0.58f)
                cubicTo(w * .18f, h * .47f, w * .34f, h * .61f, w * .50f, h * .51f)
                cubicTo(w * .67f, h * .41f, w * .84f, h * .56f, w, h * .43f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(distant, color = Color(0xFF8FB18A), style = Fill)

            val foreground = Path().apply {
                moveTo(0f, h * .72f)
                cubicTo(w * .22f, h * .60f, w * .38f, h * .75f, w * .55f, h * .64f)
                cubicTo(w * .70f, h * .55f, w * .83f, h * .68f, w, h * .54f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(foreground, color = Color(0xFF47794B), style = Fill)

            val road = Path().apply {
                moveTo(-w * .08f, h * .88f)
                cubicTo(w * .20f, h * .73f, w * .40f, h * .86f, w * .58f, h * .72f)
                cubicTo(w * .72f, h * .61f, w * .86f, h * .69f, w * 1.08f, h * .55f)
            }
            drawPath(
                road,
                color = Color(0xFFF0E5D0),
                style = Stroke(width = w * .055f)
            )
            drawPath(
                road,
                color = Color(0xFFB08B62),
                style = Stroke(width = w * .008f)
            )

            // Simplified pilgrimage landmark silhouette.
            val tower = Path().apply {
                moveTo(w * .15f, h * .66f)
                lineTo(w * .15f, h * .48f)
                lineTo(w * .20f, h * .42f)
                lineTo(w * .25f, h * .48f)
                lineTo(w * .25f, h * .66f)
                close()
            }
            drawPath(tower, color = Color(0xFFEDE7D5), style = Fill)
            drawLine(Color(0xFFEDE7D5), Offset(w*.20f, h*.42f), Offset(w*.20f, h*.33f), w*.018f)
            drawLine(Color(0xFFEDE7D5), Offset(w*.17f, h*.36f), Offset(w*.23f, h*.36f), w*.014f)
        }
    }
}
