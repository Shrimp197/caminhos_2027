package com.caminhos2027.v1.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.caminhos2027.R

/**
 * Approved offline hero artwork for the Centenário preparation surface.
 * The product surface must use the supplied photographic asset, not a generated placeholder.
 */
@Composable
internal fun ReferenceHeroArt(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.caminho_centenario_photo),
        contentDescription = "Caminho do Centenário",
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}
