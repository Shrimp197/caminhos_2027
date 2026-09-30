package com.caminhos2027.v1.ui

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.caminhos2027.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayInputStream

/**
 * Approved offline hero artwork.
 * The reference artwork is stored as a portable base64 asset so the Android build does not
 * depend on an invalid placeholder JPEG in the resource tree.
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
            BitmapFactory.decodeStream(ByteArrayInputStream(Base64.decode(encoded, Base64.DEFAULT)))
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
            painter = painterResource(R.drawable.caminho_centenario_hero_vector),
            contentDescription = "Caminho do Centenário",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
