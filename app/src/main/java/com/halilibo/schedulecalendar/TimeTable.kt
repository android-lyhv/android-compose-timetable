package com.halilibo.schedulecalendar

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas

@Composable
fun TimeLineDivider(modifier: Modifier) {
    val houseSpace = 360f
    val minusSpace = houseSpace / 6
    val textOffset = 200f
    val paint = Paint()
    paint.textAlign = Paint.Align.RIGHT
    paint.textSize = 50f
    paint.color = android.graphics.Color.BLACK
    val offsetTextY = (paint.descent() + paint.ascent())/2
    Canvas(modifier = modifier) {
        (0..23).forEach { house ->
            drawIntoCanvas {
                it.nativeCanvas.drawText("$house:00", textOffset - 40f, houseSpace * house - offsetTextY, paint)
            }
            drawLine(
                color = Color.Gray,
                strokeWidth = 10f,
                start = Offset(textOffset -20f, houseSpace * house),
                end = Offset(50f + textOffset, houseSpace * house),
            )
            val startSpace = houseSpace * house + minusSpace
            (0..4).forEach {
                val space = startSpace + it * minusSpace
                drawLine(
                    color = Color.Gray,
                    strokeWidth = 5f,
                    start = Offset(textOffset, space),
                    end = Offset(50f + textOffset, space),
                )
            }

        }
    }
}