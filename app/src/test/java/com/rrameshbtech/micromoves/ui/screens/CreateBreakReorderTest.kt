package com.rrameshbtech.micromoves.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class CreateBreakReorderTest {

    @Test
    fun movingMiddleItemUp_swapsWithPrevious() {
        assertEquals(listOf("a", "b", "c"), listOf("b", "a", "c").moved(1, 0))
    }

    @Test
    fun movingMiddleItemDown_swapsWithNext() {
        assertEquals(listOf("a", "c", "b"), listOf("a", "b", "c").moved(1, 2))
    }

    @Test
    fun movingFirstItemUp_isNoOp() {
        val items = listOf("a", "b", "c")

        assertEquals(items, items.moved(0, -1))
    }

    @Test
    fun movingLastItemDown_isNoOp() {
        val items = listOf("a", "b", "c")

        assertEquals(items, items.moved(2, 3))
    }
}
