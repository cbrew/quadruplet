package com.cbrew.unify

/**
 * An immutable, insertion-ordered map backed by an array of entries, for
 * feature maps, which hold a handful of features.
 *
 * Lookup is a linear scan comparing (cached) key hash codes before calling
 * equals, which for a few entries is much cheaper to build and to query than
 * a LinkedHashMap with its table and linked nodes. Equality and hash codes
 * follow the Map contract, so a SmallMap equals a LinkedHashMap with the same
 * entries. [Builder] falls back to a LinkedHashMap for large maps.
 */
class SmallMap<K, V> private constructor(private val cells: Array<Cell<K, V>>) : AbstractMap<K, V>() {

    private class Cell<K, V>(override val key: K, override val value: V) : Map.Entry<K, V> {
        override fun equals(other: Any?): Boolean =
                other is Map.Entry<*, *> && key == other.key && value == other.value

        override fun hashCode(): Int = key.hashCode() xor value.hashCode()

        override fun toString(): String = "$key=$value"
    }

    override val size: Int get() = cells.size

    override fun isEmpty(): Boolean = cells.isEmpty()

    override fun get(key: K): V? = find(key)?.value

    override fun containsKey(key: K): Boolean = find(key) != null

    private fun find(key: K): Cell<K, V>? {
        val h = key.hashCode()
        for (c in cells) if (c.key === key || (c.key.hashCode() == h && c.key == key)) return c
        return null
    }

    override val entries: Set<Map.Entry<K, V>>
        get() = object : AbstractSet<Map.Entry<K, V>>() {
            override val size: Int get() = cells.size
            override fun iterator(): Iterator<Map.Entry<K, V>> = cells.iterator()
        }

    /**
     * Collects entries in order. The caller adds each key at most once, as
     * the unifier and substitution naturally do; this is not checked.
     */
    class Builder<K, V>(private val capacity: Int) {
        private val cells = arrayOfNulls<Cell<K, V>>(minOf(capacity, MAX_SIZE))
        private var large: LinkedHashMap<K, V>? = if (capacity > MAX_SIZE) LinkedHashMap(capacity * 2) else null
        private var n = 0

        fun put(key: K, value: V) {
            val big = large
            if (big != null) big[key] = value else cells[n++] = Cell(key, value)
        }

        @Suppress("UNCHECKED_CAST")
        fun build(): Map<K, V> =
                large ?: SmallMap((if (n == cells.size) cells else cells.copyOf(n)) as Array<Cell<K, V>>)
    }

    companion object {
        /** Maps larger than this are stored in a LinkedHashMap instead. */
        const val MAX_SIZE = 16

        /** A copy of [map], reusing it if it already is a SmallMap. */
        fun <K, V> of(map: Map<K, V>): Map<K, V> {
            if (map is SmallMap<K, V>) return map
            val b = Builder<K, V>(map.size)
            for ((k, v) in map) b.put(k, v)
            return b.build()
        }
    }
}
