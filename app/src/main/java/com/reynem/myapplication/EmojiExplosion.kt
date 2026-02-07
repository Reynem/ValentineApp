package com.reynem.myapplication

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch


data class EmojiParticle(
    val id: Long,
    val emoji: String,
    val targetX: Float,
    val targetY: Float,
    val duration: Int
)

@Composable
fun EmojiExplosion(particles: List<EmojiParticle>, onParticleEnd: (Long) -> Unit) {
    particles.forEach { particle ->
        key(particle.id) {
            MovingEmoji(particle, onParticleEnd)
        }
    }
}

@Composable
fun MovingEmoji(particle: EmojiParticle, onFinished: (Long) -> Unit) {
    val animX = remember { Animatable(0f) }
    val animY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    val scale = remember { Animatable(0.5f) }

    LaunchedEffect(Unit) {
        launch {
            animX.animateTo(particle.targetX, animationSpec = tween(particle.duration))
        }
        launch {
            // Stave effect
            animY.animateTo(particle.targetY, animationSpec = tween(particle.duration))
        }
        launch {
            scale.animateTo(1.2f, animationSpec = tween(particle.duration))
            alpha.animateTo(0f, animationSpec = tween(500)) // Fade in the end
            onFinished(particle.id) // Remove from list when animation is made
        }
    }

    Text(
        text = particle.emoji,
        fontSize = 24.sp,
        modifier = Modifier
            .offset(x = animX.value.dp, y = animY.value.dp)
            .alpha(alpha.value)
            .scale(scale.value)
    )
}