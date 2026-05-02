package com.example.mindlex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mindlex.R

private const val PrimaryBookRotationDeg = -28f
private const val SecondaryBookRotationDeg = 19f
private const val PrimaryBookScale = 1.92f
private const val SecondaryBookScale = 1.25f
private const val PrimaryTintAlpha = 0.085f
private const val SecondaryTintAlpha = 0.045f

@Composable
fun BookOpenDecorLayer(
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val density = LocalDensity.current
    val primaryTint = MaterialTheme.colorScheme.primary.copy(alpha = PrimaryTintAlpha)
    val secondaryTint = MaterialTheme.colorScheme.secondary.copy(alpha = SecondaryTintAlpha)

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-72).dp, y = (-56).dp)
                .blur(34.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(240.dp)
                .offset(x = 34.dp, y = (-30).dp)
                .blur(42.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.13f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0f)
                        )
                    )
                )
        )

        Image(
            painter = painterResource(R.drawable.book_open_decor),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = PrimaryBookRotationDeg
                    scaleX = PrimaryBookScale
                    scaleY = PrimaryBookScale
                    transformOrigin = TransformOrigin(1f, 1f)
                    translationX = with(density) { 64.dp.toPx() }
                    translationY = with(density) { 44.dp.toPx() }
                },
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomEnd,
            colorFilter = ColorFilter.tint(primaryTint, BlendMode.SrcIn)
        )

        Image(
            painter = painterResource(R.drawable.book_open_decor),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = SecondaryBookRotationDeg
                    scaleX = SecondaryBookScale
                    scaleY = SecondaryBookScale
                    alpha = 0.9f
                    transformOrigin = TransformOrigin(0f, 0f)
                    translationX = with(density) { (-54).dp.toPx() }
                    translationY = with(density) { (-36).dp.toPx() }
                },
            contentScale = ContentScale.Fit,
            alignment = Alignment.TopStart,
            colorFilter = ColorFilter.tint(secondaryTint, BlendMode.SrcIn)
        )
    }
}
