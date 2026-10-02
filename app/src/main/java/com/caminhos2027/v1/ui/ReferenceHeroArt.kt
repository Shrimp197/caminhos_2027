package com.caminhos2027.v1.ui

import android.graphics.ImageDecoder
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import java.nio.ByteBuffer

/**
 * Approved offline hero artwork.
 * Decodes the bundled JPEG through ImageDecoder from its raw asset bytes.
 */
@Composable
internal fun ReferenceHeroArt(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember {
        runCatching {
            val bytes = context.assets.open("data/hero_centenario.jpg")
                .use { it.readBytes() }
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(ByteBuffer.wrap(bytes)))
        }.getOrNull()
    }
    Box(modifier) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Caminho do Centenário",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
