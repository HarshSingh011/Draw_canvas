package com.example.draw_canvas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    onStrokeWidthChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            ToolButton("Pen", selectedTool == "Pen", onToolSelected)
            ToolButton("Pencil", selectedTool == "Pencil", onToolSelected)
            ToolButton("Eraser", selectedTool == "Eraser", onToolSelected)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Stroke Width: ${strokeWidth.toInt()}")
        Slider(
            value = strokeWidth,
            onValueChange = onStrokeWidthChange,
            valueRange = 2f..30f,
            steps = 7,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
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
