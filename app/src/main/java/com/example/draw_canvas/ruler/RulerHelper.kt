package com.example.draw_canvas.ruler

import com.example.draw_canvas.model.Stroke
import kotlin.math.*

object RulerHelper {
    fun projectPointToRuler(
        px: Float,
        py: Float,
        centerX: Float,
        centerY: Float,
        angleRad: Float,
        length: Float
    ): FloatArray {
        val half = length / 2f
        val cosA = cos(angleRad.toDouble()).toFloat()
        val sinA = sin(angleRad.toDouble()).toFloat()

        val rx1 = centerX - half * cosA
        val ry1 = centerY - half * sinA
        val rx2 = centerX + half * cosA
        val ry2 = centerY + half * sinA

        val vx = rx2 - rx1
        val vy = ry2 - ry1
        val wx = px - rx1
        val wy = py - ry1

        val c1 = vx * wx + vy * wy
        val c2 = vx * vx + vy * vy
        val t = if (c2 == 0f) 0f else (c1 / c2).coerceIn(0f, 1f)

        val projx = rx1 + t * vx
        val projy = ry1 + t * vy
        return floatArrayOf(projx, projy)
    }

    fun computeRulerCorners(
        centerX: Float,
        centerY: Float,
        angleRad: Float,
        length: Float,
        width: Float
    ): FloatArray {
        val halfLen = length / 2f
        val cosA = cos(angleRad.toDouble()).toFloat()
        val sinA = sin(angleRad.toDouble()).toFloat()
        val dx = halfLen * cosA
        val dy = halfLen * sinA
        val wx = (width / 2f) * -sinA
        val wy = (width / 2f) * cosA

        val p1x = centerX - dx - wx
        val p1y = centerY - dy - wy
        val p2x = centerX + dx - wx
        val p2y = centerY + dy - wy
        val p3x = centerX + dx + wx
        val p3y = centerY + dy + wy
        val p4x = centerX - dx + wx
        val p4y = centerY - dy + wy

        return floatArrayOf(p1x, p1y, p2x, p2y, p3x, p3y, p4x, p4y)
    }

    fun snapAngleToCommon(angleDeg: Double, snapAngles: FloatArray, thresholdDeg: Float): Double {
        var deg = ((angleDeg % 360) + 360) % 360
        for (a in snapAngles) {
            val diff = abs(deg - a)
            val wrapped = if (diff > 180) 360 - diff else diff
            if (wrapped <= thresholdDeg) {
                return a.toDouble()
            }
        }
        return angleDeg
    }

    fun findNearestAnchor(x: Float, y: Float, strokes: List<Stroke>, anchorSnapDistancePx: Float): FloatArray? {
        var bestDist = Float.MAX_VALUE
        var best: FloatArray? = null
        val threshSq = anchorSnapDistancePx * anchorSnapDistancePx
        for (s in strokes) {
            val sx = s.startX
            val sy = s.startY
            val ex = s.endX
            val ey = s.endY
            val mx = (sx + ex) / 2f
            val my = (sy + ey) / 2f
            val candidates = arrayOf(floatArrayOf(sx, sy), floatArrayOf(ex, ey), floatArrayOf(mx, my))
            for (c in candidates) {
                val dx = c[0] - x
                val dy = c[1] - y
                val d2 = dx * dx + dy * dy
                if (d2 < bestDist) {
                    bestDist = d2
                    best = c
                }
            }
        }
        if (best != null && bestDist <= threshSq) return best
        return null
    }
}