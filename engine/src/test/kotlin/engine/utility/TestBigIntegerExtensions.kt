package engine.utility

import org.junit.jupiter.api.Test

class TestBigIntegerExtensions {

    @Test
    fun testIsPowerOfDegree() {
        assert(1000000.toBigInteger().isPowerOfDegree(2))
        assert(1000000.toBigInteger().isPowerOfDegree(3))
        assert(!1000000.toBigInteger().isPowerOfDegree(4))
        assert(76543.toBigInteger().isPowerOfDegree(1))
        assert(1000.toBigInteger().isPowerOfDegree(3))
        assert(!125.toBigInteger().isPowerOfDegree(2))
        assert(1.toBigInteger().isPowerOfDegree(6))
        assert(0.toBigInteger().isPowerOfDegree(10))
        assert(11.toBigInteger().isPrime())
    }
}
