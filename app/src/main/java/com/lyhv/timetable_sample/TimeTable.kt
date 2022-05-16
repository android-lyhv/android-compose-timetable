package com.lyhv.timetable_sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.timetable.MainTable

internal class TimeTable

@Composable
fun TimeTable(
    modifier: Modifier = Modifier,
    onEvent: (String) -> Unit
) {
    MainTable(onEvent = onEvent)
}
