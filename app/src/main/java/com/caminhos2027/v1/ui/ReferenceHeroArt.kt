package com.caminhos2027.v1.ui

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

/**
 * Approved offline hero artwork.
 *
 * The source JPEG is stored in the repository as base64 text because the original JPEG encoding
 * is not reliably decoded by Android's resource pipeline. Decoding the approved bytes explicitly
 * keeps the exact artwork while avoiding the incompatible resource path.
 */
@Composable
internal fun ReferenceHeroArt(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember {
        runCatching {
            val encoded = context.assets.open("data/hero_centenario.jpg.b64")
                .bufferedReader()
                .use { it.readText() }
                .replace("\\s".toRegex(), "")
            val bytes = Base64.decode(encoded, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
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
