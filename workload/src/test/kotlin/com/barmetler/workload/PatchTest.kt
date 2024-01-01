package com.barmetler.workload

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import com.barmetler.workload.util.patchInstance
import java.util.*
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class PatchTest {

    @Test
    fun patchNothing() {
        val a = A("x", 123, B(true))
        val ac = AC()
        val expected = a.copy(b = a.b!!.copy())
        patchInstance(a, ac)
        assertEquals(expected, a)
    }

    @Test
    fun patchAll() {
        val a = A("x", 123, B(true))
        val ac = AC("x2".some(), BC(Optional.of(false)))
        val expected = A("x2", 123, B(false))
        patchInstance(a, ac)
        assertEquals(expected, a)
    }

    @Test
    fun patchPartial() {
        val a = A("x", 123, B(true))
        val ac = AC(null, BC(Optional.of(false)))
        val expected = A("x", 123, B(false))
        patchInstance(a, ac)
        assertEquals(expected, a)
    }

    @Test
    fun unsetAll() {
        val a = A("x", 123, B(true))
        val ac = AC(None, BC(Optional.empty()))
        val expected = A(null, 123, B(null))
        patchInstance(a, ac)
        assertEquals(expected, a)
    }

    @Test
    fun unsetPartial() {
        val a = A("x", 123, B(true))
        val ac = AC(None, BC(null))
        val expected = A(null, 123, B(true))
        patchInstance(a, ac)
        assertEquals(expected, a)
    }
}

data class A(var x: String? = null, var y: Int? = null, var b: B? = null)

data class B(var b1: Boolean? = null)

data class AC(var x: Option<String>? = null, var b: BC? = null)

data class BC(var b1: Optional<Boolean>? = null)
