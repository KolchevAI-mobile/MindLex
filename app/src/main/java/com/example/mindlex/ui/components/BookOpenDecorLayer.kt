package com.example.mindlex.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mindlex.R

private const val BookRotationDeg = -28f
private const val BookScale = 1.92f
private const val TintAlpha = 0.065f

/**
 * Крупный однотонный водяной знак разворота книги: диагональ, поворот, почти на весь экран, без акцента.
 */
@Composable
fun BookOpenDecorLayer(
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val density = LocalDensity.current
    val tint = MaterialTheme.colorScheme.primary.copy(alpha = TintAlpha)
    Image(
        painter = painterResource(R.drawable.book_open_decor),
        contentDescription = contentDescription,
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationZ = BookRotationDeg
                scaleX = BookScale
                scaleY = BookScale
                transformOrigin = TransformOrigin(1f, 1f)
                translationX = with(density) { 64.dp.toPx() }
                translationY = with(density) { 44.dp.toPx() }
            },
        contentScale = ContentScale.Fit,
        alignment = Alignment.BottomEnd,
        colorFilter = ColorFilter.tint(tint, BlendMode.SrcIn)
    )
}
