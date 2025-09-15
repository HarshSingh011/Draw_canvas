package com.example.draw_canvas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
    onEraserSizeChange: (Float) -> Unit
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
                    Color(0xFFEEEEEE).copy(alpha = 0.95f), 
                    shape = RoundedCornerShape(999.dp)
                )
                .padding(12.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                ToolButton("Pen", selectedTool == "Pen", onToolSelected)
                Spacer(modifier = Modifier.width(8.dp))
                ToolButton("Pencil", selectedTool == "Pencil", onToolSelected)
                Spacer(modifier = Modifier.width(8.dp))
                ToolButton("Eraser", selectedTool == "Eraser", onToolSelected)
                Spacer(modifier = Modifier.width(16.dp))

                if (selectedTool == "Pen" || selectedTool == "Pencil") {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Text(
                            "Stroke: ${strokeWidth.toInt()}",
                            modifier = Modifier
                                .clickable { expanded = true }
                                .background(Color.White, shape = RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf(2f, 4f, 6f, 8f, 12f, 16f, 20f, 24f, 30f).forEach { value ->
                                DropdownMenuItem(
                                    text = { Text("${value.toInt()}") },
                                    onClick = {
                                        onStrokeWidthChange(value)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    var strokeText by remember { mutableStateOf(strokeWidth.toInt().toString()) }
                    LaunchedEffect(strokeWidth) {
                        strokeText = strokeWidth.toInt().toString()
                    }
                    
                    OutlinedTextField(
                        value = strokeText,
                        onValueChange = { newText ->
                            strokeText = newText
                            val v = newText.toFloatOrNull()
                            if (v != null && v in 2f..30f) {
                                onStrokeWidthChange(v)
                            }
                        },
                        label = { Text("Stroke", style = MaterialTheme.typography.labelSmall) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        modifier = Modifier.width(80.dp).height(56.dp)
                    )
                } else if (selectedTool == "Eraser") {
                    var eraserText by remember { mutableStateOf(eraserSize.toInt().toString()) }
                    LaunchedEffect(eraserSize) {
                        eraserText = eraserSize.toInt().toString()
                    }
                    
                    OutlinedTextField(
                        value = eraserText,
                        onValueChange = { newText ->
                            eraserText = newText
                            val v = newText.toFloatOrNull()
                            if (v != null && v >= 2f && v <= 200f) {
                                onEraserSizeChange(v)
                            }
                        },
                        label = { Text("Eraser", style = MaterialTheme.typography.labelSmall) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        modifier = Modifier.width(100.dp).height(56.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ToolButton(tool: String, selected: Boolean, onToolSelected: (String) -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(if (selected) Color(0xFF1976D2) else Color.White, CircleShape)
            .clickable { onToolSelected(tool) },
        contentAlignment = Alignment.Center
    ) {
        Text(tool.take(1), color = if (selected) Color.White else Color.Black)
    }
}
