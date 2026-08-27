package com.rrameshbtech.micromoves.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "routine_steps",
    primaryKeys = ["breakId", "position"],
    foreignKeys = [
        ForeignKey(entity = Break::class, parentColumns = ["id"], childColumns = ["breakId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Exercise::class, parentColumns = ["id"], childColumns = ["exerciseId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("breakId"), Index("exerciseId")],
)
data class RoutineStep(
    val breakId: Long,
    val exerciseId: Long,
    val position: Int,
    val pauseAfterStep: Boolean = false,
)
