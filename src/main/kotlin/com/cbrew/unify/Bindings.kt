package com.cbrew.unify

/**
 * Variable bindings as an immutable association list: each binding is a cell
 * pointing at the bindings that existed before it.
 *
 * Adding a binding is O(1) and shares the existing list, so the bindings in
 * force before a unification step stay valid after it, and abandoning a
 * failed attempt is just dropping the reference. Lookup is linear, which for
 * the handful of bindings a single unification makes is faster than hashing.
 *
 * Implements the read-only [Map] interface, so bindings can be inspected,
 * printed and compared with ordinary maps.
 */
class Bindings private constructor(
        private val name: String?,
        private val value: Unifiable?,
        private val rest: Bindings?,
        override val size: Int) : AbstractMap<CharSequence, Unifiable>() {

    /**
     * These bindings plus [name] bound to [value]. The caller ensures that
     * [name] is not already bound (unification only binds unbound variables).
     */
    fun bind(name: CharSequence, value: Unifiable): Bindings = Bindings(name.toString(), value, this, size + 1)

    override fun get(key: CharSequence): Unifiable? {
        val k = key.toString()
        var b: Bindings = this
        while (true) {
            val next = b.rest ?: return null
            if (b.name == k) return b.value
            b = next
        }
    }

    override fun containsKey(key: CharSequence): Boolean = get(key) != null

    override fun isEmpty(): Boolean = size == 0

    // oldest binding first
    override val entries: Set<Map.Entry<CharSequence, Unifiable>>
        get() {
            val cells = ArrayList<Bindings>(size)
            var b: Bindings = this
            while (b.rest != null) {
                cells.add(b)
                b = b.rest!!
            }
            val m = LinkedHashMap<CharSequence, Unifiable>(size)
            for (c in cells.asReversed()) m[c.name!!] = c.value!!
            return m.entries
        }

    companion object {
        val EMPTY = Bindings(null, null, null, 0)

        /** [map] as Bindings, reusing it if it already is one. */
        fun of(map: Map<CharSequence, Unifiable>): Bindings =
                map as? Bindings ?: map.entries.fold(EMPTY) { b, (k, v) -> b.bind(k, v) }
    }
}
