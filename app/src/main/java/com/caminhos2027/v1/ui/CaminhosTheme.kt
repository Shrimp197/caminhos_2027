package com.caminhos2027.v1.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CaminhosForest = Color(0xFF0E6546)
private val CaminhosSand = Color(0xFFF6F3EC)
private val CaminhosInk = Color(0xFF1C2520)

@Composable
internal fun CaminhosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = CaminhosForest,
            onPrimary = Color.White,
            secondary = CaminhosForest,
            onSecondary = Color.White,
            background = CaminhosSand,
            surface = Color.White,
            onSurface = CaminhosInk,
            onBackground = CaminhosInk
        ),
        content = content
    )
}