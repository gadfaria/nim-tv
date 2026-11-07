package com.gadfaria.nimtv.data

/**
 * Pure list operations on favorite keys, kept apart from DataStore so they can
 * be tested on the JVM.
 */

fun List<String>.toggled(key: String): List<String> =
    if (key in this) this - key else this + key

/**
 * Moves [key] by [delta] positions, clamped to the list bounds.
 *
 * [visible] is the order the user sees: keys of uninstalled apps stay stored
 * (an app can come back after an update) but don't count as positions, or
 * "move down" would sometimes look like it did nothing.
 */
fun List<String>.moved(key: String, delta: Int, visible: Set<String>): List<String> {
    val shown = filter { it in visible }
    val from = shown.indexOf(key)
    if (from == -1) return this
    val to = (from + delta).coerceIn(0, shown.lastIndex)
    if (to == from) return this

    val reordered = shown.toMutableList().apply { add(to, removeAt(from)) }
    // Hidden keys keep their slots; the visible ones are refilled in the new order.
    val iterator = reordered.iterator()
    return map { if (it in visible) iterator.next() else it }
}
