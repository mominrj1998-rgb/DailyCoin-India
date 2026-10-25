package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpinWheelView(
    remainingSpins: Int,
    maxDailySpins: Int = 100,
    isSpinning: Boolean,
    onSpinRequested: (onSpinReady: (rewardWon: Long, onAnimationComplete: () -> Unit) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val rewards = listOf(5L, 10L, 15L, 30L, 40L, 50L, 75L, 100L)
    val sliceColors = listOf(
        Color(0xFF2563EB), // Blue
        Color(0xFFD97706), // Gold
        Color(0xFF059669), // Emerald
        Color(0xFF7C3AED), // Violet
        Color(0xFFDC2626), // Red
        Color(0xFF0891B2), // Cyan
        Color(0xFFEA580C), // Orange
        Color(0xFF4F46E5)  // Indigo
    )

    val currentRotation = remember { Animatable(0f) }
    var wonReward by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Daily Spins Remaining Badge
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (remainingSpins > 0) TgSurfaceElevated else ErrorRed.copy(alpha = 0.2f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (remainingSpins > 0) TgBlueLight.copy(alpha = 0.5f) else ErrorRed
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = null,
                    tint = if (remainingSpins > 0) CoinGoldLight else ErrorRed,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Spins remaining: $remainingSpins/$maxDailySpins",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Wheel Container
        Box(
            modifier = Modifier
                .size(290.dp)
                .testTag("spin_wheel_box"),
            contentAlignment = Alignment.Center
        ) {
            // The Rotating Canvas Wheel
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(currentRotation.value)
            ) {
                val canvasSize = size.minDimension
                val radius = canvasSize / 2f
                val center = Offset(canvasSize / 2f, canvasSize / 2f)
                val sliceAngle = 360f / rewards.size

                // Draw slices
                for (i in rewards.indices) {
                    val startAngle = i * sliceAngle
                    drawArc(
                        color = sliceColors[i % sliceColors.size],
                        startAngle = startAngle,
                        sweepAngle = sliceAngle,
                        useCenter = true,
                        topLeft = Offset(0f, 0f),
                        size = Size(canvasSize, canvasSize)
                    )

                    // Draw separator lines
                    val angleRad = Math.toRadians(startAngle.toDouble())
                    val endX = center.x + radius * cos(angleRad).toFloat()
                    val endY = center.y + radius * sin(angleRad).toFloat()
                    drawLine(
                        color = Color.White.copy(alpha = 0.3f),
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 3f
                    )

                    // Draw text label on each slice
                    val textAngleRad = Math.toRadians((startAngle + sliceAngle / 2f).toDouble())
                    val textRadius = radius * 0.68f
                    val textX = center.x + textRadius * cos(textAngleRad).toFloat()
                    val textY = center.y + textRadius * sin(textAngleRad).toFloat()

                    drawContext.canvas.nativeCanvas.apply {
                        save()
                        rotate((startAngle + sliceAngle / 2f + 90f), textX, textY)
                        val paint = Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 34f
                            textAlign = Paint.Align.CENTER
                            isFakeBoldText = true
                            typeface = Typeface.DEFAULT_BOLD
                            setShadowLayer(4f, 0f, 2f, android.graphics.Color.BLACK)
                        }
                        drawText("${rewards[i]}", textX, textY + 10f, paint)
                        restore()
                    }
                }

                // Outer border ring
                drawCircle(
                    color = Color(0xFFFBBF24),
                    radius = radius,
                    style = Stroke(width = 8f)
                )
            }

            // Center Hub
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(CoinGoldLight, CoinGoldDark)
                        )
                    )
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "₹",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
            }

            // Top Pointer Needle
            Canvas(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(36.dp)
                    .offset(y = (-8).dp)
            ) {
                val path = Path().apply {
                    moveTo(size.width / 2f, size.height) // Pointing down into the wheel
                    lineTo(0f, 0f)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(
                    path = path,
                    color = Color(0xFFEF4444),
                    style = Fill
                )
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Spin Action Button
        Button(
            onClick = {
                if (remainingSpins > 0 && !isSpinning) {
                    wonReward = null // Clear any previous winning banner while spinning
                    onSpinRequested { rewardWon, onAnimationComplete ->
                        val rewardIndex = rewards.indexOf(rewardWon).coerceAtLeast(0)
                        val sliceAngle = 360f / rewards.size
                        // Target angle where slice lands at top pointer (270 degrees)
                        val targetSliceAngle = (rewardIndex * sliceAngle + sliceAngle / 2f)
                        val currentBase = (currentRotation.value % 360f + 360f) % 360f
                        val deltaToTarget = (270f - targetSliceAngle - currentBase + 360f) % 360f
                        val finalAngle = currentRotation.value + (360f * 5) + deltaToTarget

                        coroutineScope.launch {
                            currentRotation.animateTo(
                                targetValue = finalAngle,
                                animationSpec = tween(
                                    durationMillis = 3500,
                                    easing = FastOutSlowInEasing
                                )
                            )
                            // The wheel has completely stopped!
                            // 1. Show the winning banner
                            wonReward = rewardWon
                            // 2. Trigger the one-time coin credit to the user ledger
                            onAnimationComplete()
                        }
                    }
                }
            },
            enabled = remainingSpins > 0 && !isSpinning,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(52.dp)
                .testTag("btn_spin_wheel"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CoinGold,
                contentColor = Color.Black,
                disabledContainerColor = Color(0xFF334155),
                disabledContentColor = Color(0xFF94A3B8)
            )
        ) {
            if (isSpinning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Spinning...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (remainingSpins > 0) "SPIN TO WIN" else "DAILY LIMIT REACHED",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        if (wonReward != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SuccessGreen.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
            ) {
                Text(
                    text = "🎉 Won $wonReward Coins! Credited to your ledger.",
                    color = SuccessGreenLight,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}
