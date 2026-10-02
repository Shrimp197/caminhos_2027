package com.caminhos2027.v1.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import com.caminhos2027.R

/**
 * Approved offline hero artwork.
 *
 * The primary source is the bundled JPEG in assets. BitmapFactory is used because it can
 * decode the raw asset bytes without the stricter ImageDecoder source requirements.
 * A drawable-resource fallback prevents the approved artwork area from ever becoming empty
 * if one packaging path fails on a device.
 */
@Composable
internal fun ReferenceHeroArt(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember {
        runCatching {
            val bytes = context.assets.open("data/hero_centenario.jpg")
                .use { it.readBytes() }
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
        } ?: Image(
            painter = painterResource(R.drawable.hero_centenario),
            contentDescription = "Caminho do Centenário",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
