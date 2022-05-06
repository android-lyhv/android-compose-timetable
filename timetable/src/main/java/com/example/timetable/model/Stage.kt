package com.example.timetable.model

data class Stage(
    val id: Int,
    val name: String,
    val events: List<Event>
)