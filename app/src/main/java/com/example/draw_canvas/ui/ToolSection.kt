package com.example.draw_canvas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun ToolSection(
    selectedTool: String,
    onToolSelected: (String) -> Unit,
    strokeWidth: Float,
    onStrokeWidthChange: (Float) -> Unit,
    eraserSize: Float,
    onEraserSizeChange: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color(0xFFEEEEEE), shape = androidx.compose.foundation.shape.RoundedCornerShape(40.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            ToolButton("Pen", selectedTool == "Pen", onToolSelected)
            ToolButton("Pencil", selectedTool == "Pencil", onToolSelected)
            ToolButton("Eraser", selectedTool == "Eraser", onToolSelected)
            Spacer(modifier = Modifier.width(16.dp))
            var expanded by remember { mutableStateOf(false) }
            Box {
                Text(
                    "Stroke: ${strokeWidth.toInt()}",
                    modifier = Modifier
                        .clickable { expanded = true }
                        .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
                androidx.compose.material3.DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf(2f, 4f, 6f, 8f, 12f, 16f, 20f, 24f, 30f).forEach { value ->
                        androidx.compose.material3.DropdownMenuItem(
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
            var eraserText by remember { mutableStateOf(eraserSize.toInt().toString()) }
            LaunchedEffect(eraserSize) {
                val t = eraserSize.toInt().toString()
                if (t != eraserText) eraserText = t
            }
            OutlinedTextField(
                value = eraserText,
                onValueChange = {
                    eraserText = it
                    val v = it.toFloatOrNull()
                    if (v != null && v >= 2f && v <= 200f) {
                        onEraserSizeChange(v)
                    }
                },
                label = { Text("Eraser Size") },
                singleLine = true,
                modifier = Modifier.width(100.dp)
            )
            var strokeText by remember { mutableStateOf(strokeWidth.toInt().toString()) }
            LaunchedEffect(strokeWidth) {
                val text = strokeWidth.toInt().toString()
                if (text != strokeText) strokeText = text
            }
            OutlinedTextField(
                value = strokeText,
                onValueChange = {
                    strokeText = it
                    val v = it.toFloatOrNull()
                    if (v != null && v in 2f..30f) {
                        onStrokeWidthChange(v)
                    }
                },
                label = { Text("Stroke Size") },
                singleLine = true,
                modifier = Modifier.width(80.dp)
            )
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
