package com.cbrew.unify

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * abstractOver turns a semantic variable into a λ-bound one. Each case is
 * checked twice: against the expected de Bruijn term, and by applying the
 * abstraction, which must give the term with the argument in the
 * variable's place.
 */
class AbstractOverTest {

    private val x = FstructVar("@x")
    private val john = Constant("john")

    private fun app(f: String, vararg args: Lambda): Lambda =
            args.fold(Constant(f) as Lambda) { acc, a -> App(acc, a) }

    // applying the abstraction to a constant is substitution of that constant
    private fun assertAbstraction(expected: Lambda, term: Lambda) {
        val abstraction = term.abstractOver(x)
        assertEquals(expected, abstraction)
        assertEquals(term.renameVariables(mapOf<CharSequence, Unifiable>("@x" to john)), createApp(abstraction, john))
    }

    @Test
    fun testUnderNoBinder() {
        assertAbstraction(Lam(app("walk", Var(1))), app("walk", x))
    }

    @Test
    fun testUnderOneLam() {
        // λy.chase(y, @x)
        assertAbstraction(Lam(Lam(app("chase", Var(1), Var(2)))),
                Lam(app("chase", Var(1), x)))
    }

    @Test
    fun testUnderTwoLams() {
        // λy.λz.give(y, z, @x)
        assertAbstraction(Lam(Lam(Lam(app("give", Var(2), Var(1), Var(3))))),
                Lam(Lam(app("give", Var(2), Var(1), x))))
    }

    @Test
    fun testFreeVarsMoveUp() {
        // the new binder is outside the free Var's, so they move up by one
        assertEquals(Lam(App(Var(2), Var(1))), App(Var(1), x).abstractOver(x))
        assertEquals(Lam(Lam(App(Var(3), Var(2)))), Lam(App(Var(2), x)).abstractOver(x))
        // a bound Var stays
        assertEquals(Lam(Lam(App(Var(1), Var(2)))), Lam(App(Var(1), x)).abstractOver(x))
    }

    @Test
    fun testUnderQuantifier() {
        // ∃y.(dog(y) ∧ chase(@x, y)): quantifiers bind QVars, so @x becomes
        // Var(1) as at the top
        val body = createAnd(app("dog", QVar(1)), app("chase", x, QVar(1)))
        assertAbstraction(
                Lam(Exists(createAnd(app("dog", QVar(1)), app("chase", Var(1), QVar(1))))),
                Exists(body))
    }

    @Test
    fun testQuantifiedArgumentIsShifted() {
        // ∀z.((λ@x.∃y.chase(@x, y))(z)) = ∀z.∃y.chase(z, y): applying the
        // abstraction to a quantified variable shifts it over the ∃
        val abstraction = Exists(app("chase", x, QVar(1))).abstractOver(x)
        assertEquals(Forall(Exists(app("chase", QVar(2), QVar(1)))),
                Forall(createApp(abstraction, QVar(1))))
    }

    @Test
    fun testOtherVariablesAndSharing() {
        val y = FstructVar("@y")
        val sub = Exists(app("dog", QVar(1)))
        val result = app("r", sub, y, x).abstractOver(x)
        assertEquals(Lam(app("r", sub, y, Var(1))), result)
        // the subterm without @x is shared, not copied
        val r = ((result as Lam).body as App).e1 as App
        assertSame(sub, (r.e1 as App).e2)
        // a term without @x or free Vars comes back inside the Lam unchanged
        val t = app("walk", y)
        assertSame(t, (t.abstractOver(x) as Lam).body)
    }
}
