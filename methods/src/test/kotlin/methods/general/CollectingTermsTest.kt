package methods.general

import engine.patterns.UnsignedIntegerPattern
import engine.patterns.integerOrderRootOf
import engine.steps.metadata.MetadataKey
import methods.rules.testRule
import org.junit.jupiter.api.Test

class CollectingTermsTest {

    private val testMetadataKey = object : MetadataKey { override val keyName = "test" }

    @Test
    fun testCollectLikeRoots() {
        val collectLikeRoots = collectLikeTerms(integerOrderRootOf(UnsignedIntegerPattern()), testMetadataKey)

        testRule("sqrt[2] + 2*sqrt[2] + 2", collectLikeRoots, "(1 + 2)*sqrt[2] + 2")
        testRule("sqrt[3] + sqrt[3]", collectLikeRoots, "(1 + 1)*sqrt[3]")
        testRule(
            "sqrt[3] + sqrt[7] + sqrt[3] + sqrt[5]",
            collectLikeRoots,
            "(1 + 1) * sqrt[3] + sqrt[7] + sqrt[5]"
        )
        testRule(
            "sqrt[7] + sqrt[3] + sqrt[3] + sqrt[5]",
            collectLikeRoots,
            "sqrt[7] + (1 + 1) * sqrt[3] + sqrt[5]"
        )
        testRule(
            "sqrt[7] + root[5, 3] + sqrt[5] + root[5, 3]",
            collectLikeRoots,
            "sqrt[7] + (1 + 1) * root[5, 3] + sqrt[5]"
        )
        testRule(
            "[2 * sqrt[2] / 3] + [1 / 5] * sqrt[2] - 4 * sqrt[2]",
            collectLikeRoots,
            "([2 / 3] + [1 / 5] - 4) * sqrt[2]"
        )
    }
}
