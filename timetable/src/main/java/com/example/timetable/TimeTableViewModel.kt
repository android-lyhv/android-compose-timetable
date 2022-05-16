package com.example.timetable

import android.graphics.Matrix
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.lifecycle.ViewModel

class TimeTableViewModel : ViewModel() {
    private var _matrix = mutableStateOf(Matrix(), neverEqualPolicy())
    val matrix get(): Matrix = _matrix.value
    fun setMatrix(matrix: Matrix){
        _matrix.value = matrix
    }
}