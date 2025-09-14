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
import com.example.draw_canvas.ui.DrawingCanvasView
import com.example.draw_canvas.ui.theme.Draw_CanvasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrawingScreen()
        }
    }
}

@Composable
fun DrawingScreen() {
    val selectedTool = remember { mutableStateOf("Pen") }
    val strokeWidth = remember { mutableStateOf(6f) }
    var drawingView: DrawingCanvasView? = null

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            ToolSection(
                selectedTool = selectedTool.value,
                onToolSelected = {
                    selectedTool.value = it
                    drawingView?.let { view ->
                        when (it) {
                            "Pen" -> view.setTool(DrawingCanvasView.Tool.PEN)
                            "Pencil" -> view.setTool(DrawingCanvasView.Tool.PENCIL)
                            "Eraser" -> view.setTool(DrawingCanvasView.Tool.ERASER)
                        }
                    }
                },
                strokeWidth = strokeWidth.value,
                onStrokeWidthChange = {
                    strokeWidth.value = it
                    drawingView?.setStrokeWidth(it)
                }
            )
        }
    ) { innerPadding ->
        AndroidView(
            factory = { context ->
                DrawingCanvasView(context).also { view ->
                    drawingView = view
                    when (selectedTool.value) {
                        "Pen" -> view.setTool(DrawingCanvasView.Tool.PEN)
                        "Pencil" -> view.setTool(DrawingCanvasView.Tool.PENCIL)
                        "Eraser" -> view.setTool(DrawingCanvasView.Tool.ERASER)
                    }
                    view.setStrokeWidth(strokeWidth.value)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}
