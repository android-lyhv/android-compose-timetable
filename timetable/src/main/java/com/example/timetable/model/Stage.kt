package com.example.timetable.model

data class Stage(
    val id: Int,
    val name: String,
    val events: List<Event>,
    val bgColor: Long,
    val eventColor: Long,
) {
    companion object {
        val default = listOf(
            Stage(
                id = 1,
                name = "Stage A",
                events = listOf(
                    Event(0, "Event A0", "03:10", "04:20"),
                    Event(1, "Event A1", "01:10", "02:00")
                ),
                bgColor = 0x5269A8FA,
                eventColor = 0xFF0052CC
            ), Stage(
                id = 2,
                name = "Stage B",
                events = listOf(Event(3, "Event B0", "01:30", "02:30")),
                bgColor = 0xB479F2C0,
                eventColor = 0xFF00875A
            ),
            Stage(
                id = 3,
                name = "Stage C",
                events = listOf(Event(3, "Event C0", "02:50", "04:00")),
                bgColor = 0x81BB86FC,
                eventColor = 0xFFBB86FC
            )
        )
    }
}