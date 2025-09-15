package com.example.draw_canvas.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Bitmap
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
    private lateinit var eraserStrokePaint: Paint

    private var selectedTool: Tool = Tool.PEN
    private var strokeColor: Int = Color.BLACK
    private var strokeWidth: Float = 6f
    private var eraserSize: Float = 30f
    enum class Tool { PEN, PENCIL, ERASER }

    fun setTool(tool: Tool) {
        selectedTool = tool
        when (tool) {
            Tool.ERASER -> {
                // Eraser paints the canvas background color (white) to simulate erasing
            }
            Tool.PEN -> {
                strokeColor = Color.BLACK
            }
            Tool.PENCIL -> {
                strokeColor = Color.DKGRAY
            }
        }
    }

    fun setStrokeWidth(width: Float) {
        strokeWidth = width
        currentPaint?.strokeWidth = strokeWidth
    }

    fun setStrokeColor(color: Int) {
        strokeColor = color
    }

    fun setEraserSize(size: Float) {
        eraserSize = size
        if (this::eraserPaint.isInitialized) {
            eraserPaint.strokeWidth = size
            if (this::eraserStrokePaint.isInitialized) eraserStrokePaint.strokeWidth = size
        }
    }

    private var scaleFactor = 1.0f
    private var lastFocusX = 0f
    private var lastFocusY = 0f
    private var lastCanvasFocusX = 0f
    private var lastCanvasFocusY = 0f
    private var posX = 0f
    private var posY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isPanning = false
    
    // Canvas dimensions - make it larger than screen
    private var canvasWidth = 0f
    private var canvasHeight = 0f
    private var actualCanvasWidth = 2000f  // Large canvas width
    private var actualCanvasHeight = 3000f // Large canvas height
    
    // Matrix for coordinate transformation
    private val transformMatrix = Matrix()
    private val inverseMatrix = Matrix()
    
    // Border paint for canvas boundary
    private val borderPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    // Eraser preview paints
    private val eraserPreviewFill = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val eraserPreviewBorder = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }

    // Debug overlay state
    private var lastScreenX = 0f
    private var lastScreenY = 0f
    private var lastMappedX = 0f
    private var lastMappedY = 0f
    private var isTouching = false
    private val debugTextPaint = Paint().apply {
        color = Color.BLACK
        textSize = 36f
        isAntiAlias = true
    }

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val prevScale = scaleFactor
            var newScale = prevScale * detector.scaleFactor
            newScale = max(0.5f, min(newScale, 3.0f))
            val focusX = detector.focusX
            val focusY = detector.focusY
            // Preserve the canvas point under the focus by computing it using the
            // current inverse matrix, then recomputing pos so the same canvas point
            // maps to the same screen focus after scaling.
            val before = floatArrayOf(focusX, focusY)
            if (!inverseMatrix.isIdentity()) {
                inverseMatrix.mapPoints(before)
            } else {
                // Fallback to manual mapping
                before[0] = focusX / prevScale - posX
                before[1] = focusY / prevScale - posY
            }
            val canvasPointX = before[0]
            val canvasPointY = before[1]
            scaleFactor = newScale
            // Compute new pos so that focusX = scaleFactor * (canvasPoint + pos')
            posX = focusX / scaleFactor - canvasPointX
            posY = focusY / scaleFactor - canvasPointY
            updateTransformMatrix()
            invalidate()
            return true
        }
    })
    
    private fun updateTransformMatrix() {
        // Build a matrix that maps canvas coordinates to screen coordinates:
        // screen = scaleFactor * p + scaleFactor * pos
        transformMatrix.reset()
        transformMatrix.setScale(scaleFactor, scaleFactor)
        transformMatrix.postTranslate(scaleFactor * posX, scaleFactor * posY)
        // Create inverse matrix for screen-to-canvas coordinate conversion
        if (!transformMatrix.invert(inverseMatrix)) {
            inverseMatrix.reset()
        }
    }
    
    private fun screenToCanvasCoords(screenX: Float, screenY: Float): FloatArray {
        val pts = floatArrayOf(screenX, screenY)
        inverseMatrix.mapPoints(pts)
        return pts
    }
    
    private fun isWithinCanvasBounds(x: Float, y: Float): Boolean {
        return x >= 0f && x <= actualCanvasWidth && y >= 0f && y <= actualCanvasHeight
    }

    init {
        setBackgroundColor(Color.WHITE)
        eraserPaint = Paint().apply {
            style = Paint.Style.FILL
            strokeWidth = eraserSize
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = Color.WHITE
        }
        eraserStrokePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = eraserSize
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = Color.WHITE
        }
        updateTransformMatrix()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            canvasWidth = w.toFloat()
            canvasHeight = h.toFloat()
            
            // Create a large bitmap canvas for drawing
            bitmap = Bitmap.createBitmap(actualCanvasWidth.toInt(), actualCanvasHeight.toInt(), Bitmap.Config.ARGB_8888)
            bitmapCanvas = Canvas(bitmap!!)
            bitmapCanvas?.drawColor(Color.WHITE) // White canvas background
            
            // Redraw existing strokes on the new bitmap
            for (stroke in strokes) {
                bitmapCanvas?.drawPath(stroke.path, stroke.paint)
            }
            
            // Center the large canvas on screen initially
            // posX/posY are in canvas units; compute so bitmap is centered on screen
            posX = (canvasWidth / scaleFactor - actualCanvasWidth) / 2f
            posY = (canvasHeight / scaleFactor - actualCanvasHeight) / 2f
            
            updateTransformMatrix()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Fill background with gray to show area outside canvas
        canvas.drawColor(Color.LTGRAY)
        
    val saveCount = canvas.save()
    // Apply the same transform via matrix so mapping and drawing use identical math
    canvas.concat(transformMatrix)
        
        // Draw the large canvas bitmap
        bitmap?.let { bmp ->
            canvas.drawBitmap(bmp, 0f, 0f, bitmapPaint)
        }
        
        // Draw border around the canvas
        canvas.drawRect(0f, 0f, actualCanvasWidth, actualCanvasHeight, borderPaint)
        
        // Draw current path being drawn (only if inside canvas bounds)
        if (currentPath != null && currentPaint != null) {
            if (selectedTool == Tool.ERASER) {
                val rectPaint = Paint().apply {
                    color = Color.RED
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    isAntiAlias = true
                }
                // Draw eraser rectangle at last touch position
                val half = eraserSize / 2f
                canvas.drawRect(lastTouchX - half, lastTouchY - half, lastTouchX + half, lastTouchY + half, rectPaint)
            } else {
                // Draw current pen/pencil path
                canvas.drawPath(currentPath!!, currentPaint!!)
            }
        }

        // Eraser preview: show white-filled rect with red border at mapped location
        if (selectedTool == Tool.ERASER && isTouching) {
            // restore to draw overlay in view coordinates
            canvas.restoreToCount(saveCount)
            val half = eraserSize / 2f
            // Map mapped canvas coords back to screen coordinates for overlay using the transformMatrix
            val pts = floatArrayOf(lastMappedX, lastMappedY)
            transformMatrix.mapPoints(pts)
            val screenX = pts[0]
            val screenY = pts[1]
            val left = screenX - half * scaleFactor
            val top = screenY - half * scaleFactor
            val right = screenX + half * scaleFactor
            val bottom = screenY + half * scaleFactor
            // draw fill and border
            canvas.drawRect(left, top, right, bottom, eraserPreviewFill)
            canvas.drawRect(left, top, right, bottom, eraserPreviewBorder)
            // draw debug labels below preview
            val label1 = "screen: ${lastScreenX.toInt()}, ${lastScreenY.toInt()}"
            val label2 = "mapped: ${lastMappedX.toInt()}, ${lastMappedY.toInt()}"
            canvas.drawText(label1, left, bottom + 40f, debugTextPaint)
            canvas.drawText(label2, left, bottom + 80f, debugTextPaint)
        } else if (isTouching) {
            // Debug overlay: show screen vs mapped coordinates
            canvas.restoreToCount(saveCount)
            val label1 = "screen: ${lastScreenX.toInt()}, ${lastScreenY.toInt()}"
            val label2 = "mapped: ${lastMappedX.toInt()}, ${lastMappedY.toInt()}"
            canvas.drawText(label1, 16f, 40f, debugTextPaint)
            canvas.drawText(label2, 16f, 80f, debugTextPaint)
        }
        
        canvas.restoreToCount(saveCount)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        val pointerCount = event.pointerCount

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (pointerCount == 1) {
                    val canvasCoords = screenToCanvasCoords(event.x, event.y)
                    val canvasX = canvasCoords[0]
                    val canvasY = canvasCoords[1]
                    // Update debug overlay (always show preview following touch)
                    lastScreenX = event.x
                    lastScreenY = event.y
                    lastMappedX = canvasX
                    lastMappedY = canvasY
                    isTouching = true
                    
                    // Only start drawing if touch is within canvas bounds
                    if (isWithinCanvasBounds(canvasX, canvasY)) {
                        currentPath = Path()
                        currentPath?.moveTo(canvasX, canvasY)
                        
                        if (selectedTool == Tool.ERASER) {
                            // For eraser, do not set currentPaint; erase directly to bitmap
                            bitmapCanvas?.drawCircle(canvasX, canvasY, eraserSize / 2f, eraserPaint)
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
                        lastTouchX = canvasX
                        lastTouchY = canvasY
                    }
                    isPanning = false
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                isPanning = true
                // Use centroid of all pointers as the focus to avoid drift
                var sumX = 0f
                var sumY = 0f
                val count = event.pointerCount
                for (i in 0 until count) {
                    sumX += event.getX(i)
                    sumY += event.getY(i)
                }
                lastFocusX = sumX / count
                lastFocusY = sumY / count
                // Store the canvas coordinate under the fingers so we can keep it
                // stationary relative to the fingers during pan.
                val canvasFocus = screenToCanvasCoords(lastFocusX, lastFocusY)
                lastCanvasFocusX = canvasFocus[0]
                lastCanvasFocusY = canvasFocus[1]
            }
            MotionEvent.ACTION_MOVE -> {
                if (pointerCount == 1 && !isPanning && currentPath != null) {
                    val canvasCoords = screenToCanvasCoords(event.x, event.y)
                    val canvasX = canvasCoords[0]
                    val canvasY = canvasCoords[1]
                    // keep preview updated while moving
                    lastScreenX = event.x
                    lastScreenY = event.y
                    lastMappedX = canvasX
                    lastMappedY = canvasY
                    isTouching = true
                    
                    // Only continue drawing if within canvas bounds
                    if (isWithinCanvasBounds(canvasX, canvasY)) {
                        currentPath?.lineTo(canvasX, canvasY)

                        if (selectedTool == Tool.ERASER) {
                            // Erase by drawing a line between last and current points on the bitmap
                            val half = eraserSize / 2f
                            bitmapCanvas?.drawLine(lastTouchX, lastTouchY, canvasX, canvasY, eraserStrokePaint)
                            // Also draw circles to ensure no gaps
                            bitmapCanvas?.drawCircle(canvasX, canvasY, half, eraserPaint)
                        } else {
                            // For pen/pencil we just update preview; onDraw will render currentPath
                        }

                        lastTouchX = canvasX
                        lastTouchY = canvasY
                        invalidate()
                    }
                } else if (pointerCount >= 2) {
                    // Compute centroid/focus of all pointers to pan accurately
                    var sumX = 0f
                    var sumY = 0f
                    val count = event.pointerCount
                    for (i in 0 until count) {
                        sumX += event.getX(i)
                        sumY += event.getY(i)
                    }
                    val focusX = sumX / count
                    val focusY = sumY / count
                    // Recompute pos so that the stored canvas focus stays under the fingers
                    posX = focusX / scaleFactor - lastCanvasFocusX
                    posY = focusY / scaleFactor - lastCanvasFocusY
                    lastFocusX = focusX
                    lastFocusY = focusY
                    updateTransformMatrix()
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!isPanning && currentPath != null) {
                    if (selectedTool == Tool.ERASER) {
                        // Eraser already applied directly to bitmap during move; nothing to commit
                        invalidate()
                    } else if (currentPaint != null) {
                        // Commit pen/pencil path to the bitmap
                        bitmapCanvas?.drawPath(currentPath!!, currentPaint!!)
                        // Save stroke for potential undo; store a copy
                        strokes.add(Stroke(Path(currentPath), Paint(currentPaint)))
                        invalidate()
                    }
                    currentPath = null
                    currentPaint = null
                }
                isPanning = false
                isTouching = false
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
