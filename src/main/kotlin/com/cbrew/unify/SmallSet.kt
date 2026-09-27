package com.cbrew.unify

/**
 * An immutable, insertion-ordered set backed by an array, for the element
 * sets of And and Or, which almost always hold two or three terms.
 *
 * Membership is a linear scan that compares hash codes (cached on composite
 * terms) before calling equals. For a handful of elements that is far cheaper
 * to build and to query than a LinkedHashSet, which allocates a table and an
 * entry per element. [of] falls back to a LinkedHashSet for large sets, so
 * nothing becomes quadratic.
 */
class SmallSet<T> private constructor(private val items: Array<Any?>) : AbstractSet<T>() {

    override val size: Int get() = items.size

    override fun isEmpty(): Boolean = items.isEmpty()

    override fun contains(element: T): Boolean = indexOf(items, items.size, element) >= 0

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        private var i = 0
        override fun hasNext(): Boolean = i < items.size

        @Suppress("UNCHECKED_CAST")
        override fun next(): T = if (i < items.size) items[i++] as T else throw NoSuchElementException()
    }

    companion object {
        /** Sets larger than this are stored in a LinkedHashSet instead. */
        const val MAX_SIZE = 16

        /** The distinct [elements], first occurrence first. */
        fun <T> of(elements: Collection<T>): Set<T> {
            if (elements.size > MAX_SIZE) return LinkedHashSet(elements)
            val buf = arrayOfNulls<Any?>(elements.size)
            var n = 0
            for (x in elements) if (indexOf(buf, n, x) < 0) buf[n++] = x
            return SmallSet(if (n == buf.size) buf else buf.copyOf(n))
        }

        private fun indexOf(items: Array<Any?>, n: Int, x: Any?): Int {
            val h = x.hashCode()
            for (i in 0 until n) {
                val y = items[i]
                if (y === x || (y.hashCode() == h && y == x)) return i
            }
            return -1
        }
    }
}
