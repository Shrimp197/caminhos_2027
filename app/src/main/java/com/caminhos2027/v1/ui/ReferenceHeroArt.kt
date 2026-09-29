package com.caminhos2027.v1.ui

import android.graphics.BitmapFactory
import com.caminhos2027.R
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
 * Production hero artwork used by the preparation surface.
 *
 * The approved product reference uses a real Caminho do Centenário photograph.
 * Keep the source in the already validated offline asset bundle so the preparation
 * screen remains deterministic and works without network access.
 */
@Composable
internal fun ReferenceHeroArt(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.caminho_centenario_hero)
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
