package com.gadfaria.nimtv.data

import org.junit.Assert.assertEquals
import org.junit.Test

class FavoritesTest {

    private val all = setOf("a", "b", "c", "d")

    @Test
    fun `toggle adds at the end and removes`() {
        assertEquals(listOf("a", "b"), listOf("a").toggled("b"))
        assertEquals(listOf("b"), listOf("a", "b").toggled("a"))
    }

    @Test
    fun `move swaps with the neighbor`() {
        assertEquals(listOf("b", "a", "c"), listOf("a", "b", "c").moved("a", 1, all))
        assertEquals(listOf("a", "c", "b"), listOf("a", "b", "c").moved("c", -1, all))
    }

    @Test
    fun `move past the ends is a no-op`() {
        val list = listOf("a", "b", "c")
        assertEquals(list, list.moved("a", -1, all))
        assertEquals(list, list.moved("c", 1, all))
    }

    @Test
    fun `move skips keys of apps that are not installed`() {
        // "x" is stored but not visible: "a" moving down must land after "b".
        val stored = listOf("a", "x", "b", "c")
        assertEquals(listOf("b", "x", "a", "c"), stored.moved("a", 1, all))
    }

    @Test
    fun `move of an unknown key is a no-op`() {
        val list = listOf("a", "b")
        assertEquals(list, list.moved("z", 1, all))
    }
}
