package engine.utility

import java.math.BigInteger

fun List<BigInteger>.gcd(): BigInteger = this.fold(BigInteger.ZERO, BigInteger::gcd)
