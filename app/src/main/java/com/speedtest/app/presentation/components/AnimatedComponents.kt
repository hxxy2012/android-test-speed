package com.speedtest.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Animated number counter
 */
@Composable
fun AnimatedCounter(
    targetValue: Double,
    modifier: Modifier = Modifier,
    format: (Double) -> String = { String.format("%.1f", it) }
) {
    var currentValue by remember { mutableStateOf(0.0) }

    LaunchedEffect(targetValue) {
        animate(
            initialValue = currentValue,
            targetValue = targetValue,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        ) { value, _ ->
            currentValue = value
        }
    }

    Text(
        text = format(currentValue),
        modifier = modifier,
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold
    )
}

/**
 * Pulsating indicator
 */
@Composable
fun PulsatingIndicator(
    isActive: Boolean,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    if (isActive) {
        Box(
            modifier = modifier
                .scale(scale)
                .alpha(alpha)
                .size(16.dp)
                .background(color, CircleShape)
        )
    }
}

/**
 * Ripple effect
 */
@Composable
fun RippleEffect(
    isActive: Boolean,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val infiniteTransition = rememberInfiniteTransition(label = "ripple")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
                .size(100.dp)
                .background(color.copy(alpha = 0.3f), CircleShape)
        )
    }
}

/**
 * Fade in animation
 */
@Composable
fun FadeIn(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        content()
    }
}

/**
 * Slide in from bottom animation
 */
@Composable
fun SlideInFromBottom(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
    ) {
        content()
    }
}

/**
 * Expand/Collapse animation
 */
@Composable
fun ExpandableContent(
    expanded: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(
            animationSpec = tween(300),
            expandFrom = Alignment.Top
        ) + fadeIn(animationSpec = tween(300)),
        exit = shrinkVertically(
            animationSpec = tween(300),
            shrinkTowards = Alignment.Top
        ) + fadeOut(animationSpec = tween(300))
    ) {
        content()
    }
}

/**
 * Shimmer loading effect
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .alpha(alpha)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

/**
 * Bounce animation
 */
@Composable
fun BounceAnimation(
    targetScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce"
    )

    Box(modifier = Modifier.scale(scale)) {
        content()
    }
}
