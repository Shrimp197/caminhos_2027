package com.caminhos2027.v1.ui

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CaminhosGreen = Color(0xFF159447)
private val CaminhosGreenDark = Color(0xFF0E6E36)
private val CaminhosGreenSoft = Color(0xFFE8F4ED)
private val CaminhosBlue = Color(0xFF164B63)
private val CaminhosGold = Color(0xFFC28A16)
private val CaminhosSand = Color(0xFFF7F7F4)
private val CaminhosInk = Color(0xFF18211C)
private val CaminhosMuted = Color(0xFF68736D)
private val CaminhosLine = Color(0xFFDCE3DE)
private val CaminhosDanger = Color(0xFFD92D20)
private val CaminhosWarning = Color(0xFFA15C00)
private val CaminhosWarningSoft = Color(0xFFFFF1D8)

private val CaminhosTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontSize = 25.sp, lineHeight = 31.sp, fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontSize = 20.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold),
        bodyLarge = bodyLarge.copy(fontSize = 16.sp, lineHeight = 22.sp),
        bodyMedium = bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
        labelLarge = labelLarge.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
        labelMedium = labelMedium.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
    )
}

private val CaminhosShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
internal fun CaminhosTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity
            activity?.window?.let { window ->
                window.statusBarColor = Color.White.toArgb()
                window.navigationBarColor = Color.White.toArgb()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    window.decorView.systemUiVisibility =
                        window.decorView.systemUiVisibility or android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    window.decorView.systemUiVisibility =
                        window.decorView.systemUiVisibility or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = CaminhosGreen,
            onPrimary = Color.White,
            primaryContainer = CaminhosGreenSoft,
            onPrimaryContainer = CaminhosGreenDark,
            secondary = CaminhosBlue,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFE8F0F3),
            onSecondaryContainer = Color(0xFF103B4E),
            tertiary = CaminhosGold,
            onTertiary = Color.White,
            background = CaminhosSand,
            onBackground = CaminhosInk,
            surface = Color.White,
            onSurface = CaminhosInk,
            surfaceVariant = Color(0xFFF0F1EE),
            onSurfaceVariant = CaminhosMuted,
            outline = CaminhosLine,
            error = CaminhosDanger,
            errorContainer = Color(0xFFFFE7E4),
            onErrorContainer = Color(0xFF7A1B12)
        ),
        typography = CaminhosTypography,
        shapes = CaminhosShapes,
        content = content
    )
}

internal object CaminhosVisual {
    val Green = CaminhosGreen
    val GreenDark = CaminhosGreenDark
    val GreenSoft = CaminhosGreenSoft
    val Blue = CaminhosBlue
    val Gold = CaminhosGold
    val Sand = CaminhosSand
    val Ink = CaminhosInk
    val Muted = CaminhosMuted
    val Line = CaminhosLine
    val Warning = CaminhosWarning
    val WarningSoft = CaminhosWarningSoft
}
