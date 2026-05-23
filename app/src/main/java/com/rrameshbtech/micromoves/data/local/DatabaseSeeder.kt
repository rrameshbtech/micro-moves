package com.rrameshbtech.micromoves.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rrameshbtech.micromoves.data.Break
import com.rrameshbtech.micromoves.data.BreakSchedule

private data class BreakSeedItem(
    val name: String,
    val frequencyMinutes: Int = 15,
    val activeStartHour: Int = 9,
    val activeEndHour: Int = 17,
)

private fun BreakSeedItem.toBreak() = Break(
    name = name,
    schedule = BreakSchedule(
        frequencyMinutes = frequencyMinutes,
        activeStartHour = activeStartHour,
        activeEndHour = activeEndHour,
    )
)

object DatabaseSeeder {

    suspend fun seed(context: Context, dao: BreakDao) {
        val json = context.assets.open("init_breaks.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<BreakSeedItem>>() {}.type
        val items: List<BreakSeedItem> = Gson().fromJson(json, type)
        dao.insertAll(items.map { it.toBreak() })
    }
}
