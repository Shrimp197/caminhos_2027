package com.caminhos2027.v1.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

/**
 * Approved offline hero artwork.
 *
 * The approved JPEG is bundled as a binary asset so Android does not have to parse it through
 * the drawable resource pipeline. The bytes remain local and no network access is involved.
 */
@Composable
internal fun ReferenceHeroArt(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember {
        runCatching {
            context.assets.open("data/hero_centenario.jpg")
                .use { BitmapFactory.decodeStream(it) }
        }.getOrNull()
    }

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "Caminho do Centenário",
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
