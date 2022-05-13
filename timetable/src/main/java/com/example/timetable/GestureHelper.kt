package com.example.timetable

import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import androidx.compose.ui.geometry.Offset
import kotlin.math.pow
import kotlin.math.sqrt

object GestureHelper {

    fun getScale(matrix: Matrix): Float {
        return sqrt(
            (getValue(matrix, Matrix.MSCALE_X).toDouble().pow(2.0).toFloat() + getValue(
                matrix,
                Matrix.MSKEW_Y
            ).toDouble().pow(2.0).toFloat()).toDouble()
        ).toFloat()
    }

    fun getValue(matrix: Matrix, whichValue: Int): Float {
        val values = FloatArray(9)
        matrix.getValues(values)
        return values[whichValue]
    }
}

fun Matrix.mapOffset(offset: Offset): Offset {
    val absolute = FloatArray(2)
    absolute[0] = offset.x
    absolute[1] = offset.y
    this.mapPoints(absolute)
    return Offset(absolute[0], absolute[1])
}

fun Matrix.limitScale(
    minScale: Float,
    maxScale: Float,
    focusX: Float,
    focusY: Float
) {
    val values = FloatArray(9)
    this.getValues(values)
    val scaleX = values[Matrix.MSCALE_X]
    val scaleY = values[Matrix.MSCALE_Y]
    if (scaleX >= maxScale) {
        this.postScale(maxScale / scaleX, maxScale / scaleY, focusX, focusY)
    } else if (scaleX <= minScale) {
        this.postScale(minScale / scaleX, minScale / scaleY, focusX, focusY)
    }
}

fun Matrix.bound(boundRect: RectF, areaRect: RectF) {
    val displayRect = this.getRectF(boundRect)
    val height = displayRect.height()
    val width = displayRect.width()
    var deltaX = 0f
    var deltaY = 0f
    val viewHeight: Float = areaRect.height()
    val viewWidth: Float = areaRect.width()
    when {
        height <= viewHeight -> {
            deltaY = (viewHeight - height) / 2 - displayRect.top
        }

        displayRect.top > 0 -> {
            deltaY = -displayRect.top
        }

        displayRect.bottom < viewHeight -> {
            deltaY = viewHeight - displayRect.bottom
        }

        else -> {
            // No-op
        }
    }
    when {
        width <= viewWidth -> {
            deltaX = (viewWidth - width) / 2 - displayRect.left
        }

        displayRect.left > 0 -> {
            deltaX = -displayRect.left
        }

        displayRect.right < viewWidth -> {
            deltaX = viewWidth - displayRect.right
        }

        else -> {
            // No-op
        }
    }
    // Finally actually translate the matrix
    Log.d("TAG", "bound: $deltaX, $deltaY")
    this.postTranslate(deltaX, deltaY)
}

fun Matrix.getRectF(rectF: RectF): RectF {
    val newRectF = RectF(rectF.left, rectF.top, rectF.right, rectF.bottom)
    this.mapRect(newRectF)
    return newRectF
}