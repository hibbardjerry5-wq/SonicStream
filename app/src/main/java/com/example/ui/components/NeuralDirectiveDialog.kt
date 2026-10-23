package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MusicPlayerViewModel
import com.example.ui.viewmodel.PlayerUiState

/**
 * Super-Intelligent Neural Directive Console
 * Allows users to command the Gemini AI acoustic engine with creative natural language
 * or instant futuristic neural audio directives.
 */
@Composable
fun NeuralDirectiveDialog(
    viewModel: MusicPlayerViewModel,
    uiState: PlayerUiState,
    onDismiss: () -> Unit
) {
    var promptInput by remember { mutableStateOf(uiState.neuralPromptInput) }

    val quickDirectives = listOf(
        "Deep Cyber Sub-Warp" to "Heavy 808 sub-harmonics & chest punch",
        "Hyper-Spatial 360°" to "3D holographic binaural soundstage",
        "Neural Room Inversion" to "Real-time acoustic reflection cancellation",
        "Crystal Laser Vocals" to "Clutter notch with high-frequency exciter",
        "Cyberpunk Highway" to "Driving loudness curve with sub-woofer boost",
        "Warm Analog Tape" to "Vintage valve warmth and gentle rolled-off highs"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.5.dp,
                    brush = Brush.sweepGradient(listOf(NeonCyan, ElectricViolet, AmberGlow, NeonCyan)),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("neural_directive_dialog"),
            color = Color(0xF2080D18)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(NeonCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NEURAL ACOUSTIC COPILOT",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Command the Gemini AI sound engine with natural language or autonomous quantum presets.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Telemetry Matrix Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("MIC SPL", fontSize = 9.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                        Text("${uiState.ambientSplDb.toInt()} dB", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Column {
                        Text("MASKING", fontSize = 9.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                        Text("${uiState.psychoacousticMaskingPercent}%", fontSize = 12.sp, color = NeonCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Column {
                        Text("CONFIDENCE", fontSize = 9.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                        Text("${uiState.aiConfidencePercent}%", fontSize = 12.sp, color = EmeraldPulse, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Column {
                        Text("ENGINE", fontSize = 9.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                        Text("GEMINI 3.5", fontSize = 12.sp, color = ElectricViolet, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Natural Language Input Field
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = {
                        promptInput = it
                        viewModel.setNeuralPromptInput(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("neural_prompt_input_field"),
                    placeholder = {
                        Text("e.g., Deep cyber sub-bass in a rainy neon city...", fontSize = 12.sp, color = TextMuted)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Execute Command Button
                Button(
                    onClick = {
                        viewModel.executeNeuralSonicDirective(promptInput)
                    },
                    enabled = !uiState.isNeuralProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("execute_neural_prompt_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isNeuralProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SYNTHESIZING...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SYNTHESIZE NEURAL PROFILE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Directive Chips
                Text(
                    text = "QUANTUM SOUNDSTAGE DIRECTIVES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    quickDirectives.forEach { (title, subtitle) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    promptInput = title
                                    viewModel.setNeuralPromptInput(title)
                                    viewModel.executeNeuralSonicDirective(title)
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.04f),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(SurfaceBorder, NeonCyan.copy(alpha = 0.25f))))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(subtitle, fontSize = 10.sp, color = TextSecondary)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                if (uiState.aiExplanation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ElectricViolet.copy(alpha = 0.12f),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(ElectricViolet.copy(alpha = 0.4f), NeonCyan.copy(alpha = 0.4f))))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Insights, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.aiExplanation,
                                fontSize = 11.sp,
                                color = Color.White,
                                maxLines = 3
                            )
                        }
                    }
                }
            }
        }
    }
}
