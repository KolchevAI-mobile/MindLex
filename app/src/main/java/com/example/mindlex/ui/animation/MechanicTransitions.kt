package com.example.mindlex.ui.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

fun mechanicStateTransition(): ContentTransform {
    val inAnim = slideInHorizontally(
        animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing),
        initialOffsetX = { it / 12 }
    )
    val outAnim = slideOutHorizontally(
        animationSpec = tween(durationMillis = 120, easing = FastOutLinearInEasing),
        targetOffsetX = { -it / 20 }
    )
    return inAnim togetherWith outAnim
}

