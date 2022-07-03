package com.example.timetable

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import android.view.View
import android.widget.OverScroller
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.positionChangeConsumed
import androidx.compose.ui.input.pointer.positionChanged
import androidx.core.view.ViewCompat
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
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

/**
 * @param boundRect Rect is contain the graphic view
 * @param viewRect Rect show on screen
 */
fun Matrix.bound(boundRect: RectF, viewRect: RectF) {
    val displayRect = this.getRectF(boundRect)
    val height = displayRect.height()
    val width = displayRect.width()
    var deltaX = 0f
    var deltaY = 0f
    val viewHeight: Float = viewRect.height()
    val viewWidth: Float = viewRect.width()
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
    this.postTranslate(deltaX, deltaY)
}

fun Matrix.getRectF(rectF: RectF): RectF {
    val newRectF = RectF(rectF.left, rectF.top, rectF.right, rectF.bottom)
    this.mapRect(newRectF)
    return newRectF
}

class FlingRunnable(context: Context, val flingDental: (Float, Float) -> Unit) :
    Runnable {
    private val scroller: OverScroller = OverScroller(context)
    private var currentX = 0
    private var currentY = 0

    fun fling(
        boundRect: RectF,
        velocityX: Float,
        velocityY: Float
    ) {
        Log.d("TAG", "fling: $velocityX, $velocityY ")
        scroller.forceFinished(true)
        val viewWidth = boundRect.width()
        val viewHeight = boundRect.height()
        val startX = (-boundRect.left).roundToInt()
        val minX: Int
        val maxX: Int
        val minY: Int
        val maxY: Int
        if (viewWidth < boundRect.width()) {
            minX = 0
            maxX = (boundRect.width() - viewWidth).roundToInt()
        } else {
            maxX = startX
            minX = maxX
        }
        val startY = (-boundRect.top).roundToInt()
        if (viewHeight < boundRect.height()) {
            minY = 0
            maxY = (boundRect.height() - viewHeight).roundToInt()
        } else {
            maxY = startY
            minY = maxY
        }
        currentX = startX
        currentY = startY
        // If we actually can move, fling the scroller
        if (startX != maxX || startY != maxY) {
            scroller.fling(
                startX, startY, velocityX.roundToInt(), velocityY.toInt(), minX,
                maxX, minY, maxY, 0, 0
            )
        }
    }

    override fun run() {
        if (scroller.isFinished) {
            return  // remaining post that should not be handled
        }
        if (scroller.computeScrollOffset()) {
            val newX = scroller.currX
            val newY = scroller.currY
            flingDental((currentX - newX).toFloat(), (currentY - newY).toFloat())
            currentX = newX
            currentY = newY
//            // Post On animation
//            ViewCompat.postInvalidateOnAnimation(view)
        }
    }
}

suspend fun PointerInputScope.detectTransformGesturesAndPointer(
    panZoomLock: Boolean = false,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float, rotation: Float, change: PointerInputChange?) -> Unit
) {
    forEachGesture {
        awaitPointerEventScope {
            var rotation = 0f
            var zoom = 1f
            var pan = Offset.Zero
            var pastTouchSlop = false
            val touchSlop = viewConfiguration.touchSlop
            var lockedToPanZoom = false

            awaitFirstDown(requireUnconsumed = false)
            do {
                val event = awaitPointerEvent()
                val canceled = event.changes.any { it.positionChangeConsumed() }
                if (!canceled) {
                    val zoomChange = event.calculateZoom()
                    val rotationChange = event.calculateRotation()
                    val panChange = event.calculatePan()

                    if (!pastTouchSlop) {
                        zoom *= zoomChange
                        rotation += rotationChange
                        pan += panChange

                        val centroidSize = event.calculateCentroidSize(useCurrent = false)
                        val zoomMotion = abs(1 - zoom) * centroidSize
                        val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
                        val panMotion = pan.getDistance()

                        if (zoomMotion > touchSlop ||
                            rotationMotion > touchSlop ||
                            panMotion > touchSlop
                        ) {
                            pastTouchSlop = true
                            lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
                        }
                    }

                    if (pastTouchSlop) {
                        val centroid = event.calculateCentroid(useCurrent = false)
                        val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
                        if (effectiveRotation != 0f ||
                            zoomChange != 1f ||
                            panChange != Offset.Zero
                        ) {
                            onGesture(centroid, panChange, zoomChange, effectiveRotation, event.changes.lastOrNull())
                        }
                        event.changes.forEach {
                            if (it.positionChanged()) {
                                it.consumeAllChanges()
                            }
                        }
                    }
                }
            } while (!canceled && event.changes.any { it.pressed })
        }
    }
}