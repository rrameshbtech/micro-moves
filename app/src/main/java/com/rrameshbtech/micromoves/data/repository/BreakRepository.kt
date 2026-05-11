package com.rrameshbtech.micromoves.data.repository

import com.rrameshbtech.micromoves.data.ActiveBreak
import com.rrameshbtech.micromoves.data.Break
import com.rrameshbtech.micromoves.data.BreakWithState
import com.rrameshbtech.micromoves.data.Slide
import com.rrameshbtech.micromoves.data.local.ActiveBreakDao
import com.rrameshbtech.micromoves.data.local.BreakDao
import com.rrameshbtech.micromoves.data.local.SlideDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Repository for all break-related data operations.
 * Abstracts away database and data source implementation details.
 */
class BreakRepository(
    private val breakDao: BreakDao,
    private val slideDao: SlideDao,
    private val activeBreakDao: ActiveBreakDao
) {

    // ============= Break Operations =============

    suspend fun createBreak(
        name: String,
        description: String,
        frequency: Int,
        startHour: Int,
        endHour: Int,
        slides: List<Slide>
    ): Long {
        val breakItem = Break(
            name = name,
            description = description,
            frequency = frequency,
            activeStartHour = startHour,
            activeEndHour = endHour
        )
        val breakId = breakDao.insert(breakItem)

        // Insert associated slides
        slides.forEach { slide ->
            slideDao.insert(slide.copy(breakId = breakId))
        }

        return breakId
    }

    suspend fun updateBreak(breakItem: Break) {
        breakDao.update(breakItem)
    }

    suspend fun deleteBreak(breakId: Long) {
        slideDao.deleteSlidesByBreakId(breakId)
        activeBreakDao.delete(breakId)
        breakDao.deleteBreak(breakId)
    }

    suspend fun getBreakById(id: Long): Break? {
        return breakDao.getBreakById(id)
    }

    fun getBreakByIdFlow(id: Long): Flow<Break?> {
        return breakDao.getBreakByIdFlow(id)
    }

    fun getAllBreaks(): Flow<List<Break>> {
        return breakDao.getAllBreaks()
    }

    fun getAllEnabledBreaks(): Flow<List<Break>> {
        return breakDao.getAllEnabledBreaks()
    }

    // ============= Slide Operations =============

    suspend fun getSlidesByBreakId(breakId: Long): List<Slide> {
        return slideDao.getSlidesByBreakId(breakId)
    }

    fun getSlidesByBreakIdFlow(breakId: Long): Flow<List<Slide>> {
        return slideDao.getSlidesByBreakIdFlow(breakId)
    }

    suspend fun insertSlide(slide: Slide): Long {
        return slideDao.insert(slide)
    }

    suspend fun updateSlide(slide: Slide) {
        slideDao.update(slide)
    }

    suspend fun deleteSlide(slideId: Long) {
        slideDao.delete(slideId)
    }

    // ============= ActiveBreak Operations =============

    suspend fun createActiveBreak(breakItem: Break): ActiveBreak {
        val activeBreak = ActiveBreak(
            breakId = breakItem.id,
            breakName = breakItem.name,
            minutesUntilNext = breakItem.frequency
        )
        activeBreakDao.insert(activeBreak)
        return activeBreak
    }

    suspend fun getActiveBreakById(breakId: Long): ActiveBreak? {
        return activeBreakDao.getActiveBreakById(breakId)
    }

    fun getActiveBreakByIdFlow(breakId: Long): Flow<ActiveBreak?> {
        return activeBreakDao.getActiveBreakByIdFlow(breakId)
    }

    fun getAllActiveBreaks(): Flow<List<ActiveBreak>> {
        return activeBreakDao.getAllActiveBreaks()
    }

    fun getAllUnpausedActiveBreaks(): Flow<List<ActiveBreak>> {
        return activeBreakDao.getAllUnpausedActiveBreaks()
    }

    fun getAllPausedActiveBreaks(): Flow<List<ActiveBreak>> {
        return activeBreakDao.getAllPausedActiveBreaks()
    }

    suspend fun pauseActiveBreak(breakId: Long, cycles: Int = 1) {
        val activeBreak = activeBreakDao.getActiveBreakById(breakId) ?: return
        activeBreakDao.updatePauseState(
            breakId = breakId,
            isPaused = true,
            cycles = activeBreak.pausedForCycles + cycles,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun resumeActiveBreak(breakId: Long) {
        activeBreakDao.updatePauseState(
            breakId = breakId,
            isPaused = false,
            cycles = 0,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun updateActiveBreakTrigger(breakId: Long, minutesUntilNext: Int) {
        activeBreakDao.updateTriggerInfo(
            breakId = breakId,
            minutes = minutesUntilNext,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun deleteActiveBreak(breakId: Long) {
        activeBreakDao.delete(breakId)
    }

    // ============= Combined Operations =============

    /**
     * Get combined view of all active breaks with their definitions and slides.
     * Useful for UI screens that need complete break information.
     */
    fun getAllBreaksWithState(): Flow<List<BreakWithState>> {
        return combine(
            getAllBreaks(),
            getAllActiveBreaks()
        ) { breaks, activeBreaks ->
            breaks.map { breakItem ->
                val activeBreak = activeBreaks.find { it.breakId == breakItem.id }
                    ?: ActiveBreak(
                        breakId = breakItem.id,
                        breakName = breakItem.name,
                        minutesUntilNext = breakItem.frequency
                    )
                BreakWithState(breakDef = breakItem, activeBreak = activeBreak)
            }
        }
    }
}





