package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.ui.components.TelegramHeader
import com.example.ui.theme.*

data class MathQuestion(
    val equation: String,
    val options: List<Int>,
    val correctAnswer: Int
)

fun generateMathQuestion(): MathQuestion {
    val type = (0..2).random()
    val (num1, num2, op, answer) = when (type) {
        0 -> { // Addition
            val a = (2..20).random()
            val b = (2..20).random()
            listOf(a, b, "+", a + b)
        }
        1 -> { // Subtraction
            val a = (10..30).random()
            val b = (1..a).random()
            listOf(a, b, "-", a - b)
        }
        else -> { // Multiplication
            val a = (2..9).random()
            val b = (2..9).random()
            listOf(a, b, "×", a * b)
        }
    }

    val correct = answer as Int
    val wrongOptions = mutableSetOf<Int>()
    while (wrongOptions.size < 3) {
        val delta = listOf(-3, -2, -1, 1, 2, 3, 4, -4).random()
        val candidate = correct + delta
        if (candidate != correct && candidate >= 0) {
            wrongOptions.add(candidate)
        }
    }
    val allOptions = (wrongOptions + correct).shuffled()
    return MathQuestion(
        equation = "$num1 $op $num2 = ?",
        options = allOptions,
        correctAnswer = correct
    )
}

@Composable
fun MathPizzaScreen(
    user: UserEntity?,
    isOwner: Boolean = false,
    dailyGamesUsed: Int,
    dailyGameLimit: Int = 30,
    onSubmitAnswer: (question: String, isCorrect: Boolean, onResult: (Long) -> Unit) -> Unit,
    onBack: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenBotSpec: () -> Unit
) {
    var currentQuestion by remember { mutableStateOf(generateMathQuestion()) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var answerState by remember { mutableStateOf<Boolean?>(null) } // true: correct, false: wrong
    var comboStreak by remember { mutableIntStateOf(0) }

    val remaining = (dailyGameLimit - dailyGamesUsed).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TgBgDark)
    ) {
        TelegramHeader(
            user = user,
            isOwner = isOwner,
            title = "🍕 Pizza Math Challenge",
            onBack = onBack,
            onOpenAdmin = onOpenAdmin,
            onOpenBotSpec = onOpenBotSpec
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stats Row: Daily Limit & Streak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TgSurfaceElevated
                ) {
                    Text(
                        text = "Challenges: $dailyGamesUsed/$dailyGameLimit today",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "🔥 Streak: $comboStreak",
                        color = Color(0xFFFCA5A5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Pizza Math Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("math_pizza_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = TgSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, TgBorderDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pizza Icon Badge
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFF97316), Color(0xFFEA580C))
                                )
                            )
                            .border(3.dp, Color(0xFFFED7AA), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalPizza,
                            contentDescription = "Pizza",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "PIZZA CHEF'S PUZZLE",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentQuestion.equation,
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Correct answer earns +5 Coins instantly",
                        color = CoinGoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Multiple Choice Options (4 Buttons in 2x2 Grid)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (row in currentQuestion.options.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (option in row) {
                            val isSelected = selectedAnswer == option
                            val isCorrect = option == currentQuestion.correctAnswer

                            val btnColor = when {
                                answerState != null && isCorrect -> SuccessGreen
                                answerState != null && isSelected && !isCorrect -> ErrorRed
                                else -> TgSurfaceElevated
                            }

                            Button(
                                onClick = {
                                    if (answerState == null && remaining > 0) {
                                        selectedAnswer = option
                                        val correct = option == currentQuestion.correctAnswer
                                        answerState = correct
                                        if (correct) {
                                            comboStreak += 1
                                        } else {
                                            comboStreak = 0
                                        }

                                        onSubmitAnswer(currentQuestion.equation, correct) {
                                            // Result callback
                                        }
                                    }
                                },
                                enabled = answerState == null && remaining > 0,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .testTag("math_option_$option"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = btnColor,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "$option",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Answer Result Banner
            AnimatedVisibility(
                visible = answerState != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (answerState == true) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (answerState == true) SuccessGreen else ErrorRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (answerState == true) Icons.Default.CheckCircle else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (answerState == true) SuccessGreenLight else ErrorRed
                            )
                            Column {
                                Text(
                                    text = if (answerState == true) "Correct! +5 Coins" else "Wrong! 0 Coins",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (answerState == true) "Added to your coin ledger" else "Correct answer was: ${currentQuestion.correctAnswer}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                selectedAnswer = null
                                answerState = null
                                currentQuestion = generateMathQuestion()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TgBlue),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Next 🍕", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (remaining <= 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ErrorRed.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed)
                ) {
                    Text(
                        text = "Daily limit of $dailyGameLimit challenges reached. Come back tomorrow!",
                        color = ErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
