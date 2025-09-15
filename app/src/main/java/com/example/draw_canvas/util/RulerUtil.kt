package com.example.draw_canvas.util

object RulerUtil {
    fun projectPointToLine(px: Float, py: Float, ax: Float, ay: Float, bx: Float, by: Float): FloatArray {
        val vx = bx - ax
        val vy = by - ay
        val wx = px - ax
        val wy = py - ay
        val vlen2 = vx * vx + vy * vy
        if (vlen2 == 0f) return floatArrayOf(ax, ay)
        val t = ((wx * vx + wy * vy) / vlen2).coerceIn(0f, 1f)
        return floatArrayOf(ax + t * vx, ay + t * vy)
    }

    fun distanceSq(ax: Float, ay: Float, bx: Float, by: Float): Float {
        val dx = ax - bx
        val dy = ay - by
        return dx*dx + dy*dy
    }
}
