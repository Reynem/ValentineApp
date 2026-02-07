package com.reynem.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.util.Pair
import com.reynem.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

val wishingsList = listOf(
    "Ты очень классная!", "Эпштейн гордится тобой!", "Ты очень милая!", "Дидди бы тебя смазал!",
    "Дарю цветочки 🌼🌼🌼", "Вот еще цветы 🌸🌸🌸"
)
const val myWishesString = "Со святым валентином!"

val emojiParticles = listOf(
    "💘", "💗", "💝", "❤️", "💖"
)

val pinkColor = Triple(254, 192, 255)
val pinkColorBorder = Triple(253, 229, 247)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier
                    .fillMaxSize(),
                    containerColor = Color(0xFFFFE4E1)
                )
                { innerPadding ->
                    WishesAnimationScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

enum class AnimationStage {
    Initial,
    Appearing,
    Gathering,
    Finished
}

@Composable
fun WishesAnimationScreen(modifier: Modifier = Modifier) {
    // Current stage of animation
    var stage by remember { mutableStateOf(AnimationStage.Initial) }

    val appearingPeriodMillis = 500L
    val gatheringPeriodMillis = 5000L
    val finishedPeriodMillis = 1500L

    LaunchedEffect(Unit) {
        delay(appearingPeriodMillis)
        stage = AnimationStage.Appearing

        delay(gatheringPeriodMillis)
        stage = AnimationStage.Gathering

        delay(finishedPeriodMillis)
        stage = AnimationStage.Finished
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
    ) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx() }.toInt()
        val maxHeightPx = with(density) { maxHeight.toPx() }.toInt()
        val centerX = maxWidthPx / 2
        val centerY = maxHeightPx / 2

        val randomPositions = remember {
            wishingsList.map {
                val randomX = Random.nextInt(10, maxWidthPx - 300)
                val randomY = Random.nextInt(10, maxHeightPx - 300)
                IntOffset(randomX, randomY)
            }
        }

        val heartScale by animateFloatAsState(
            targetValue = when (stage) {
                AnimationStage.Initial -> 0f
                AnimationStage.Appearing -> 0.4f
                AnimationStage.Gathering -> 1.0f
                else -> 1.5f
            },
            animationSpec = if (stage === AnimationStage.Gathering) {
                    tween(
                        durationMillis = gatheringPeriodMillis.toInt(),
                        easing = FastOutLinearInEasing
                    )
                } else spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
            label = "HeartScale"
        )

        val infiniteTransition = rememberInfiniteTransition("HeartBeat")

        val heartBeatScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(300, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "HeartBeatScale"
        )

        var explosionParticles by remember { mutableStateOf(listOf<EmojiParticle>()) }

        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            EmojiExplosion(particles = explosionParticles) { idToRemove ->
                explosionParticles = explosionParticles.filter { it.id != idToRemove }
            }

            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Heart",
                tint = Color.Red,
                modifier = Modifier
                    .size(150.dp)
                    .scale(if (stage == AnimationStage.Finished) heartScale * heartBeatScale
                           else heartScale
                    )
                    .clickable(
                        enabled = stage == AnimationStage.Finished,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            val newParticles = List(15) {
                                EmojiParticle(
                                    id = System.nanoTime() + it,
                                    emoji = emojiParticles[Random.nextInt(0, emojiParticles.size)],
                                    targetX = Random.nextInt(-250, 250).toFloat(),
                                    targetY = Random.nextInt(-400, 100).toFloat(),
                                    duration = Random.nextInt(800, 1500)
                                )
                            }
                            explosionParticles = explosionParticles + newParticles
                        }
                    )
            )
        }

        wishingsList.forEachIndexed { index, text ->
            val targetPosition = if (stage == AnimationStage.Gathering || stage == AnimationStage.Finished) {
                val textCenterPos = Pair(centerX - 150, centerY - 40)
                IntOffset(textCenterPos.first, textCenterPos.second)
            } else {
                randomPositions[index]
            }

            val animatedOffset by animateIntOffsetAsState(
                targetValue = targetPosition,
                animationSpec = tween(durationMillis = 1500),
                label = "MoveToCenter"
            )

            val alpha by animateFloatAsState(
                targetValue = if (stage == AnimationStage.Finished) 0f else 1f,
                animationSpec = tween(300),
                label = "Alpha"
            )

            if (stage != AnimationStage.Initial) {
                WishingContainer(
                    text = text,
                    offset = animatedOffset,
                    alpha = alpha
                )
            }
        }

        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = 120.dp)
            ) {
                AnimatedVisibility(
                    visible = stage == AnimationStage.Finished,
                    enter = fadeIn(animationSpec = tween(1000)) + slideInVertically()
                ) {
                    Text(
                        text = myWishesString,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

@Composable
fun WishingContainer(
    text: String,
    offset: IntOffset,
    alpha: Float
) {
    Box(
        modifier = Modifier
            .offset { offset }
            .alpha(alpha)
            .background(Color(pinkColor.first, pinkColor.second, pinkColor.third),
                RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, Color(pinkColorBorder.first, pinkColorBorder.second, pinkColorBorder.third)),
                shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black
        )
    }
}