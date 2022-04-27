package com.lyhv.timetable_sample

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen


class CalendarApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}