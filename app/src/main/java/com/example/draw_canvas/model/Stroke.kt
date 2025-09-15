package com.example.draw_canvas.model

import android.graphics.Path
import android.graphics.Paint

data class Stroke(
    val path: Path,
    val paint: Paint,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float
)
