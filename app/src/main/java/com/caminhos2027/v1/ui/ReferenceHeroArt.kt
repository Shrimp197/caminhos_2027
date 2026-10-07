package com.caminhos2027.v1.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.caminhos2027.R

/**
 * Approved offline hero artwork.
 * The runtime uses the verified Android-compatible PNG bundled with the app.
 */
@Composable
internal fun ReferenceHeroArt(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.hero_centenario),
        contentDescription = "Caminho do Centenário",
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}
