package com.lyhv.timetable_sample

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import com.example.timetable.MainTable
import com.lyhv.timetable_sample.ui.theme.TimeTableTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TimeTableTheme {
                Scaffold {
                    Surface {
                        MainTable()
                    }
                }
            }
        }
    }
}