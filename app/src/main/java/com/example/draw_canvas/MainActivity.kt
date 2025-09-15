package com.example.draw_canvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.example.draw_canvas.ui.DrawingCanvasView
import com.example.draw_canvas.ui.ToolSection
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
    val eraserSize = remember { mutableStateOf(30f) }
    val rulerLength = remember { mutableStateOf(1200f) }
    val rulerSnapEnabled = remember { mutableStateOf(true) }
    val rulerAngleDeg = remember { mutableStateOf(0f) }
    val drawingViewState = remember { mutableStateOf<DrawingCanvasView?>(null) }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Canvas takes full screen
        AndroidView(
            factory = { context ->
                DrawingCanvasView(context).also { view ->
                    drawingViewState.value = view
                    when (selectedTool.value) {
                        "Pen" -> view.setTool(DrawingCanvasView.Tool.PEN)
                        "Pencil" -> view.setTool(DrawingCanvasView.Tool.PENCIL)
                        "Eraser" -> view.setTool(DrawingCanvasView.Tool.ERASER)
                        "Ruler" -> view.setTool(DrawingCanvasView.Tool.RULER)
                        else -> view.setTool(DrawingCanvasView.Tool.PEN)
                    }
                    view.setStrokeWidth(strokeWidth.value)
                    view.setEraserSize(eraserSize.value)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // ToolSection floating on top of canvas
        ToolSection(
            selectedTool = selectedTool.value,
            onToolSelected = {
                selectedTool.value = it
                drawingViewState.value?.let { view ->
                    when (it) {
                        "Pen" -> view.setTool(DrawingCanvasView.Tool.PEN)
                        "Pencil" -> view.setTool(DrawingCanvasView.Tool.PENCIL)
                        "Eraser" -> view.setTool(DrawingCanvasView.Tool.ERASER)
                        "Ruler" -> view.setTool(DrawingCanvasView.Tool.RULER)
                        else -> view.setTool(DrawingCanvasView.Tool.PEN)
                    }
                }
            },
            strokeWidth = strokeWidth.value,
            onStrokeWidthChange = {
                strokeWidth.value = it
                drawingViewState.value?.setStrokeWidth(it)
            },
            eraserSize = eraserSize.value,
            onEraserSizeChange = {
                eraserSize.value = it
                drawingViewState.value?.setEraserSize(it)
            }
,
            rulerLength = rulerLength.value,
            onRulerLengthChange = { newLen ->
                rulerLength.value = newLen
                drawingViewState.value?.setRulerLength(newLen)
            },
            rulerAngleDeg = rulerAngleDeg.value,
            onRulerAngleChange = { deg ->
                rulerAngleDeg.value = deg
                drawingViewState.value?.setRulerAngle(deg)
            },
            rulerSnapEnabled = rulerSnapEnabled.value,
            onToggleRulerSnap = { rulerSnapEnabled.value = !rulerSnapEnabled.value },
            onRulerReset = { drawingViewState.value?.resetRuler() }
        )
    }
}
