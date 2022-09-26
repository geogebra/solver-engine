package engine.utility

import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class TestBigIntegerExtensions {

    private fun i(n: Int) = n.toBigInteger()

    private fun p(p: Int, m: Int) = Pair(i(p), i(m))

    @Test
    fun testIsPowerOfDegree() {
        assert(i(1000000).isPowerOfDegree(2))
        assert(i(1000000).isPowerOfDegree(3))
        assert(!i(1000000).isPowerOfDegree(4))
        assert(i(76543).isPowerOfDegree(1))
        assert(i(1000).isPowerOfDegree(3))
        assert(!i(125).isPowerOfDegree(2))
        assert(i(1).isPowerOfDegree(6))
        assert(i(0).isPowerOfDegree(10))
        assert(i(11).isPrime())
    }

    @Test
    fun testPrimeFactorDecomposition() {
        assertContentEquals(
            listOf(p(2, 1), p(3, 2)),
            i(18).primeFactorDecomposition()
        )
        assertContentEquals(
            listOf(p(2, 8), p(5, 8), p(7, 1)),
            i(700000000).primeFactorDecomposition()
        )
        assertContentEquals(listOf(p(23, 1)), i(23).primeFactorDecomposition())
    }
}
