package com.example.draw_canvas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun ToolSection(
    selectedTool: String,
    onToolSelected: (String) -> Unit,
    strokeWidth: Float,
    onStrokeWidthChange: (Float) -> Unit,
    eraserSize: Float,
    onEraserSizeChange: (Float) -> Unit,
    // Ruler controls
    rulerLength: Float = 1200f,
    onRulerLengthChange: (Float) -> Unit = {},
    rulerSnapEnabled: Boolean = true,
    onToggleRulerSnap: () -> Unit = {},
    onRulerReset: () -> Unit = {},
    rulerAngleDeg: Float = 0f,
    onRulerAngleChange: (Float) -> Unit = {}
) {
    var offsetX by remember { mutableStateOf(100f) }
    var offsetY by remember { mutableStateOf(100f) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .background(
                    Color(0xFFFFFFFF).copy(alpha = 0.98f), 
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tool buttons row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ToolButton("Pen", selectedTool == "Pen", onToolSelected)
                    ToolButton("Pencil", selectedTool == "Pencil", onToolSelected)
                    ToolButton("Eraser", selectedTool == "Eraser", onToolSelected)
                    ToolButton("Ruler", selectedTool == "Ruler", onToolSelected)
                }

                // Tool-specific controls
                when (selectedTool) {
                    "Ruler" -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Snap indicator
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "Snap: 0°, 30°, 45°, 60°, 90°", 
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF666666)
                                )
                            }
                            
                            // Controls row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Length controls
                                RulerControl(
                                    label = "Length",
                                    value = rulerLength.toInt().toString(),
                                    onDecrease = { onRulerLengthChange((rulerLength - 100f).coerceAtLeast(200f)) },
                                    onIncrease = { onRulerLengthChange((rulerLength + 100f).coerceAtMost(3000f)) }
                                )
                                
                                // Angle input
                                CompactTextField(
                                    value = rulerAngleDeg.toInt().toString(),
                                    onValueChange = { newText ->
                                        val v = newText.toFloatOrNull()
                                        if (v != null) onRulerAngleChange(v)
                                    },
                                    label = "Angle°",
                                    width = 70.dp
                                )
                                
                                // Snap toggle
                                ToggleButton(
                                    text = if (rulerSnapEnabled) "Snap" else "Free",
                                    enabled = rulerSnapEnabled,
                                    onClick = onToggleRulerSnap
                                )
                            }
                        }
                    }
                    "Pen", "Pencil" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var strokeText by remember { mutableStateOf(strokeWidth.toInt().toString()) }
                            LaunchedEffect(strokeWidth) {
                                strokeText = strokeWidth.toInt().toString()
                            }
                            
                            CompactTextField(
                                value = strokeText,
                                onValueChange = { newText ->
                                    strokeText = newText
                                    val v = newText.toFloatOrNull()
                                    if (v != null && v in 1f..50f) {
                                        onStrokeWidthChange(v)
                                    }
                                },
                                label = "Stroke",
                                width = 80.dp
                            )
                            
                            // Quick stroke presets
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(2f, 6f, 12f, 20f).forEach { value ->
                                    PresetButton(
                                        text = "${value.toInt()}",
                                        selected = kotlin.math.abs(strokeWidth - value) < 0.5f,
                                        onClick = { onStrokeWidthChange(value) }
                                    )
                                }
                            }
                        }
                    }
                    "Eraser" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var eraserText by remember { mutableStateOf(eraserSize.toInt().toString()) }
                            LaunchedEffect(eraserSize) {
                                eraserText = eraserSize.toInt().toString()
                            }
                            
                            CompactTextField(
                                value = eraserText,
                                onValueChange = { newText ->
                                    eraserText = newText
                                    val v = newText.toFloatOrNull()
                                    if (v != null && v >= 5f && v <= 200f) {
                                        onEraserSizeChange(v)
                                    }
                                },
                                label = "Size",
                                width = 80.dp
                            )
                            
                            // Quick eraser presets
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(10f, 30f, 60f, 100f).forEach { value ->
                                    PresetButton(
                                        text = "${value.toInt()}",
                                        selected = kotlin.math.abs(eraserSize - value) < 0.5f,
                                        onClick = { onEraserSizeChange(value) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToolButton(tool: String, selected: Boolean, onToolSelected: (String) -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(
                if (selected) Color(0xFF2196F3) else Color(0xFFF5F5F5), 
                RoundedCornerShape(12.dp)
            )
            .clickable { onToolSelected(tool) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when(tool) {
                "Pen" -> "✒"
                "Pencil" -> "✏"
                "Eraser" -> "⌫"
                "Ruler" -> "📐"
                else -> tool.take(1)
            },
            color = if (selected) Color.White else Color(0xFF333333),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    width: androidx.compose.ui.unit.Dp = 80.dp
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFFAFAFA),
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedBorderColor = Color(0xFF2196F3),
            unfocusedBorderColor = Color(0xFFE0E0E0)
        ),
        modifier = Modifier
            .width(width)
            .height(48.dp)
    )
}

@Composable
fun RulerControl(
    label: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF666666)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                    .clickable { onDecrease() },
                contentAlignment = Alignment.Center
            ) {
                Text("−", color = Color(0xFF333333), style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF333333),
                modifier = Modifier.width(40.dp)
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                    .clickable { onIncrease() },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color(0xFF333333), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun ToggleButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (enabled) Color(0xFF4CAF50) else Color(0xFFE0E0E0),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Color(0xFF666666),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun PresetButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(
                if (selected) Color(0xFF2196F3) else Color(0xFFF0F0F0),
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFF333333),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

