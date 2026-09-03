package com.rrameshbtech.micromoves.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "break_occurrences",
    foreignKeys = [
        ForeignKey(entity = Break::class, parentColumns = ["id"], childColumns = ["breakId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("breakId"), Index("triggeredAt")],
)
data class BreakOccurrence(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val breakId: Long,
    val triggeredAt: Long,
    val exerciseIds: List<Long> = emptyList(),
    val completedAt: Long? = null,
    val skippedAt: Long? = null,
)
