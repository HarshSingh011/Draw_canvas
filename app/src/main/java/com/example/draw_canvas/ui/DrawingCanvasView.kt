package com.example.draw_canvas.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.max
import kotlin.math.min

class Stroke(val path: Path, val paint: Paint)

class DrawingCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val strokes = mutableListOf<Stroke>()
    private var currentPath: Path? = null
    private var currentPaint: Paint? = null

    private var selectedTool: Tool = Tool.PEN
    private var strokeColor: Int = Color.BLACK
    private var strokeWidth: Float = 6f
    enum class Tool { PEN, PENCIL, ERASER }

    fun setTool(tool: Tool) {
        selectedTool = tool
        when (tool) {
            Tool.ERASER -> {
                strokeColor = Color.WHITE
                // Keep strokeWidth as set by user, default to 30 if not set
                if (strokeWidth < 10f) strokeWidth = 30f
            }
            Tool.PEN -> {
                strokeColor = Color.BLACK
                if (strokeWidth < 2f || strokeWidth > 30f) strokeWidth = 6f
            }
            Tool.PENCIL -> {
                strokeColor = Color.DKGRAY
                if (strokeWidth < 2f || strokeWidth > 30f) strokeWidth = 4f
            }
        }
    }

    fun setStrokeWidth(width: Float) {
        strokeWidth = width
        // Update color if eraser is selected
        if (selectedTool == Tool.ERASER) {
            strokeColor = Color.WHITE
        }
    }

    fun setStrokeColor(color: Int) {
        strokeColor = color
    }

    private var scaleFactor = 1.0f
    private var lastFocusX = 0f
    private var lastFocusY = 0f
    private var posX = 0f
    private var posY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isPanning = false

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = max(0.5f, min(scaleFactor, 3.0f))
            invalidate()
            return true
        }
    })

    init {
        setBackgroundColor(Color.WHITE)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val saveCount = canvas.save()
        canvas.translate(posX, posY)
        canvas.scale(scaleFactor, scaleFactor)
        for (stroke in strokes) {
            canvas.drawPath(stroke.path, stroke.paint)
        }
        if (currentPath != null && currentPaint != null) {
            canvas.drawPath(currentPath!!, currentPaint!!)
        }
        canvas.restoreToCount(saveCount)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        val pointerCount = event.pointerCount

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (pointerCount == 1) {
                    currentPath = Path()
                    currentPath?.moveTo(event.x, event.y)
                    currentPaint = Paint().apply {
                        color = strokeColor
                        style = Paint.Style.STROKE
                        strokeWidth = strokeWidth
                        isAntiAlias = true
                        if (selectedTool == Tool.PENCIL) {
                            alpha = 120
                        }
                        if (selectedTool == Tool.ERASER) {
                            strokeJoin = Paint.Join.ROUND
                        }
                    }
                    isPanning = false
                }
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                isPanning = true
                lastFocusX = event.getX(0)
                lastFocusY = event.getY(0)
            }
            MotionEvent.ACTION_MOVE -> {
                if (pointerCount == 1 && !isPanning) {
                    currentPath?.lineTo(event.x, event.y)
                    invalidate()
                } else if (pointerCount >= 2) {
                    val focusX = event.getX(0)
                    val focusY = event.getY(0)
                    posX += focusX - lastFocusX
                    posY += focusY - lastFocusY
                    lastFocusX = focusX
                    lastFocusY = focusY
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!isPanning && currentPath != null && currentPaint != null) {
                    strokes.add(Stroke(currentPath!!, currentPaint!!))
                    currentPath = null
                    currentPaint = null
                    invalidate()
                }
                isPanning = false
            }
        }
        return true
    }

    fun clearCanvas() {
        strokes.clear()
        invalidate()
    }
}
