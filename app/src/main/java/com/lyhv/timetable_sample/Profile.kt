package com.lyhv.timetable_sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

internal class Profile

@Composable
fun Profile(
    modifier: Modifier = Modifier
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Profile",
            color = Color.Black,
            maxLines = 1,
            fontSize = 50.sp,
            textAlign = TextAlign.Center,
            modifier = modifier.align(Alignment.Center)
        )
    }
}
