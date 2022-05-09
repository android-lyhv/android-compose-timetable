package com.example.timetable.model

import android.graphics.Matrix
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