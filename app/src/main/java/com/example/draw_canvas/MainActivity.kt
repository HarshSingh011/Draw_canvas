package com.example.draw_canvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.draw_canvas.ui.theme.Draw_CanvasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Draw_CanvasTheme {
                DrawingScreen()
            }
        }
    }
}

@Composable
fun DrawingScreen() {
    var selectedTool by remember { mutableStateOf("Pen") }
    var strokeWidth by remember { mutableStateOf(6f) }
    var drawingView: com.example.draw_canvas.ui.DrawingCanvasView? by remember { mutableStateOf(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            com.example.draw_canvas.ui.ToolSection(
                selectedTool = selectedTool,
                onToolSelected = {
                    selectedTool = it
                    drawingView?.let { view ->
                        when (it) {
                            "Pen" -> view.setTool(com.example.draw_canvas.ui.DrawingCanvasView.Tool.PEN)
                            "Pencil" -> view.setTool(com.example.draw_canvas.ui.DrawingCanvasView.Tool.PENCIL)
                            "Eraser" -> view.setTool(com.example.draw_canvas.ui.DrawingCanvasView.Tool.ERASER)
                        }
                    }
                },
                strokeWidth = strokeWidth,
                onStrokeWidthChange = {
                    strokeWidth = it
                    drawingView?.setStrokeWidth(it)
                }
            )
        }
    ) { innerPadding ->
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { context ->
                com.example.draw_canvas.ui.DrawingCanvasView(context).also { view ->
                    drawingView = view
                    when (selectedTool) {
                        "Pen" -> view.setTool(com.example.draw_canvas.ui.DrawingCanvasView.Tool.PEN)
                        "Pencil" -> view.setTool(com.example.draw_canvas.ui.DrawingCanvasView.Tool.PENCIL)
                        "Eraser" -> view.setTool(com.example.draw_canvas.ui.DrawingCanvasView.Tool.ERASER)
                    }
                    view.setStrokeWidth(strokeWidth)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}
