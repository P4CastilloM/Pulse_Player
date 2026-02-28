package com.example.pulseplayer.data.entity

import androidx.room.ColumnInfo

data class NamedPlayStat(
    @ColumnInfo(name = "label") val label: String,
    @ColumnInfo(name = "play_count") val playCount: Int
)
