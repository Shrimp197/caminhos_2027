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
 * Approved offline hero artwork.
 * Uses the bundled Android drawable so the image is available without network access.
 */
@Composable
internal fun ReferenceHeroArt(modifier: Modifier = Modifier) {
    Box(modifier) {
        Image(
            painter = painterResource(R.drawable.caminho_centenario_photo),
            contentDescription = "Caminho do Centenário",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
