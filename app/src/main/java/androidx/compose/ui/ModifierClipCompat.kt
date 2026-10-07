package androidx.compose.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip as drawClip

/** Compatibility bridge for legacy imports used by the V1 UI. */
fun Modifier.clip(shape: RoundedCornerShape): Modifier = this.drawClip(shape)
