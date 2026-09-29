package com.caminhos2027.v1.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.caminhos2027.R

/**
 * Production hero artwork used by the preparation surface.
 *
 * The visual reference calls for a real Caminho do Centenário photograph rather than
 * an illustrative placeholder. Keep the artwork in resources so the screen is fully
 * offline and deterministic.
 */
@Composable
internal fun ReferenceHeroArt(modifier: Modifier = Modifier) {
    Box(modifier) {
        Image(
            painter = painterResource(R.drawable.caminho_centenario_hero),
            contentDescription = "Caminho do Centenário",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
