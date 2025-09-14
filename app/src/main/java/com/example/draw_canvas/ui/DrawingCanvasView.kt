package com.example.draw_canvas.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
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
    private var bitmap: Bitmap? = null
    private var bitmapCanvas: Canvas? = null
    private val bitmapPaint = Paint(Paint.DITHER_FLAG)
    private lateinit var eraserPaint: Paint

    private var selectedTool: Tool = Tool.PEN
    private var strokeColor: Int = Color.BLACK
    private var strokeWidth: Float = 6f
    private var eraserSize: Float = 30f
    enum class Tool { PEN, PENCIL, ERASER }

    fun setTool(tool: Tool) {
        selectedTool = tool
        when (tool) {
            Tool.ERASER -> {
                strokeColor = Color.WHITE
                // If strokeWidth is not set by user, default to 30
                if (strokeWidth < 2f || strokeWidth > 30f) strokeWidth = 30f
            }
            Tool.PEN -> {
                strokeColor = Color.BLACK
                // Do not override strokeWidth, just set color
            }
            Tool.PENCIL -> {
                strokeColor = Color.DKGRAY
                // Do not override strokeWidth, just set color
            }
        }
    }

    fun setStrokeWidth(width: Float) {
        strokeWidth = width
        // Apply width to eraser paint and current paint immediately
        if (this::eraserPaint.isInitialized) eraserPaint.strokeWidth = strokeWidth
        currentPaint?.strokeWidth = strokeWidth
        // Update color if eraser is selected
        if (selectedTool == Tool.ERASER) {
            strokeColor = Color.WHITE
        }
    }

    fun setStrokeColor(color: Int) {
        strokeColor = color
    }

    fun setEraserSize(size: Float) {
        eraserSize = size
        if (this::eraserPaint.isInitialized) eraserPaint.strokeWidth = size
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
        eraserPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = this@DrawingCanvasView.strokeWidth
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bitmapCanvas = Canvas(bitmap!!)
            for (stroke in strokes) {
                bitmapCanvas?.drawPath(stroke.path, stroke.paint)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val saveCount = canvas.save()
        canvas.translate(posX, posY)
        canvas.scale(scaleFactor, scaleFactor)
        bitmap?.let { bmp ->
            canvas.drawBitmap(bmp, 0f, 0f, bitmapPaint)
        }
        // Do not redraw committed strokes here; bitmap holds persistent pixels
        if (currentPath != null) {
            if (selectedTool == Tool.ERASER) {
                val rectPaint = Paint().apply {
                    color = Color.RED
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    isAntiAlias = true
                }
                // draw a rectangle representing eraser area around last touch
                val lastX = lastTouchX
                val lastY = lastTouchY
                val half = eraserSize / 2f
                canvas.drawRect(lastX - half, lastY - half, lastX + half, lastY + half, rectPaint)
            } else if (currentPaint != null) {
                canvas.drawPath(currentPath!!, currentPaint!!)
            }
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
                    if (selectedTool == Tool.ERASER) {
                        currentPaint = null
                        eraserPaint.strokeWidth = strokeWidth
                    } else {
                        currentPaint = Paint().apply {
                            color = strokeColor
                            style = Paint.Style.STROKE
                            this.strokeWidth = this@DrawingCanvasView.strokeWidth
                            isAntiAlias = true
                            strokeCap = Paint.Cap.ROUND
                            strokeJoin = Paint.Join.ROUND
                            if (selectedTool == Tool.PENCIL) {
                                alpha = 120
                            }
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
                    if (selectedTool == Tool.ERASER) {
                        val x = event.x
                        val y = event.y
                        val half = eraserSize / 2f
                        bitmapCanvas?.drawRect(x - half, y - half, x + half, y + half, eraserPaint)
                    }
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
                if (!isPanning && currentPath != null) {
                    if (selectedTool == Tool.ERASER) {
                        invalidate()
                    } else if (currentPaint != null) {
                        bitmapCanvas?.drawPath(currentPath!!, currentPaint!!)
                        strokes.add(Stroke(Path(currentPath), Paint(currentPaint)))
                        invalidate()
                    }
                    currentPath = null
                    currentPaint = null
                }
                isPanning = false
            }
        }
        return true
    }

    fun clearCanvas() {
        strokes.clear()
        bitmap?.eraseColor(android.graphics.Color.TRANSPARENT)
        bitmapCanvas = bitmap?.let { Canvas(it) }
        invalidate()
    }
}
