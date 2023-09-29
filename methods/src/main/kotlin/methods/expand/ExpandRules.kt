package methods.expand

import engine.expressions.Constants
import engine.expressions.DivideBy
import engine.expressions.Minus
import engine.expressions.Sum
import engine.expressions.explicitProductOf
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.commutativeProductOf
import engine.patterns.commutativeSumOf
import engine.patterns.condition
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.productOf
import engine.patterns.stickyOptionalNegOf
import engine.patterns.sumContaining
import engine.patterns.sumOf
import engine.steps.metadata.DragTargetPosition
import engine.steps.metadata.GmPathModifier
import engine.steps.metadata.metadata

enum class ExpandRules(override val runner: Rule) : RunnerMethod {
    DistributeMultiplicationOverSum(distributeMultiplicationOverSum),
    DistributeNegativeOverBracket(distributeNegativeOverBracket),
    ExpandBinomialSquaredUsingIdentity(expandBinomialSquaredUsingIdentity),
    ExpandBinomialCubedUsingIdentity(expandBinomialCubedUsingIdentity),
    ExpandTrinomialSquaredUsingIdentity(expandTrinomialSquaredUsingIdentity),
    ApplyFoilMethod(applyFoilMethod),
    ExpandDoubleBrackets(expandDoubleBrackets),
    ExpandProductOfSumAndDifference(expandProductOfSumAndDifference),
}

/**
 * a * (b + c) -> a * b + a * c
 * - a * (b + c) -> -a * b - a * c
 * (b + c) * a -> b * a + c * a
 * - (b + c) * a -> rule doesn't apply
 * a * (b + c) * d -> rule doesn't apply
 * a * (b + c) * (d + e) -> rule doesn't apply
 */
private val distributeMultiplicationOverSum = rule {
    val sum = sumContaining()
    val product = productContaining(sum) { rest -> rest !is DivideBy && rest !is Sum }
    val optionalNegProduct = stickyOptionalNegOf(product)

    onPattern(optionalNegProduct) {
        val factors = get(product).children

        val multiplyFromRight = when {
            factors.first() is Sum -> if (expression is Minus) return@onPattern null else true
            factors.last() is Sum -> false
            else -> return@onPattern null
        }

        val sumValue = get(sum) as Sum
        val toDistribute = distribute(restOf(product))

        // variableExpression * (c1 + c2 + ... + cn) --> shouldn't be expanded
        if (sumValue.isConstant() && !toDistribute.isConstant()) return@onPattern null

        ruleResult(
            toExpr = sumOf(
                if (multiplyFromRight) {
                    sumValue.terms.map { copySign(optionalNegProduct, explicitProductOf(move(it), toDistribute)) }
                } else {
                    sumValue.terms.map { copySign(optionalNegProduct, explicitProductOf(toDistribute, move(it))) }
                },
            ),
            gmAction = drag(toDistribute, GmPathModifier.Group, sum, null, DragTargetPosition.LeftOf),
            explanation = metadata(
                Explanation.DistributeMultiplicationOverSum,
                copySign(optionalNegProduct, toDistribute),
            ),
        )
    }
}

/**
 * -(x + y) --> -x - y
 */
private val distributeNegativeOverBracket =
    rule {
        val sumTerm = sumContaining()
        val negSumTerm = negOf(sumTerm)
        val sumContainingNegTerm = sumContaining(negSumTerm)
        val sum = oneOf(sumContainingNegTerm, negSumTerm)

        onPattern(sum) {
            // Note: we can't have distribute path mappings for this as things are because there is no node for "-"
            // itself
            val terms = get(sumTerm).children
            val negDistributedTerm = sumOf(
                terms.map {
                    if (it is Minus) move(it.firstChild) else negOf(move(it))
                },
            )

            val toExpr = if (isBound(sumContainingNegTerm)) {
                sumContainingNegTerm.substitute(negDistributedTerm)
            } else {
                negDistributedTerm
            }

            ruleResult(
                toExpr = toExpr,
                gmAction = drag(negSumTerm, GmPathModifier.Operator, sumTerm, null, DragTargetPosition.Onto),
                explanation = metadata(Explanation.DistributeNegativeOverBracket),
            )
        }
    }

/**
 * [(a + b)^2] --> [a^2] + 2ab + [b^2]
 *
 * NOTE: @Simona explicitly mentioned to not use the formula:
 * [(a - b)^2] --> [a^2] - 2ab + [b^2]
 */
private val expandBinomialSquaredUsingIdentity =
    rule {
        val a = AnyPattern()
        val b = AnyPattern()
        val two = FixedPattern(Constants.Two)
        val pattern = powerOf(sumOf(a, b), two)

        onPattern(pattern) {
            val (da, db, dtwo) = distribute(a, b, two)

            ruleResult(
                toExpr = sumOf(
                    powerOf(da, dtwo),
                    explicitProductOf(dtwo, da, db),
                    powerOf(db, dtwo),
                ),
                gmAction = doubleTap(two),
                explanation = metadata(Explanation.ExpandBinomialSquaredUsingIdentity),
            )
        }
    }

/**
 * [(a + b)^3] --> [a^3] + 3 [a^2] b + 3 a [b^2] + [b^3]
 *
 * NOTE: @Simona explicitly mentioned to not use the formula:
 * [(a - b)^3] --> [a^3] - 3 [a^2] b + 3 a [b^2] - [b^3]
 */
private val expandBinomialCubedUsingIdentity =
    rule {
        val a = AnyPattern()
        val b = AnyPattern()
        val three = FixedPattern(Constants.Three)
        val pattern = powerOf(sumOf(a, b), three)

        onPattern(pattern) {
            val (da, db, dthree) = distribute(a, b, three)

            ruleResult(
                toExpr = sumOf(
                    powerOf(da, dthree),
                    explicitProductOf(
                        dthree,
                        powerOf(da, introduce(Constants.Two)),
                        db,
                    ),
                    explicitProductOf(
                        dthree,
                        da,
                        powerOf(db, introduce(Constants.Two)),
                    ),
                    powerOf(db, dthree),
                ),
                gmAction = doubleTap(three),
                explanation = metadata(Explanation.ExpandBinomialCubedUsingIdentity),
            )
        }
    }

/**
 * [(a + b + c)^2] --> [a^2] + [b^2] + [c^2] + 2ab + 2bc + 2ca
 */
private val expandTrinomialSquaredUsingIdentity =
    rule {
        val a = AnyPattern()
        val b = AnyPattern()
        val c = AnyPattern()
        val two = FixedPattern(Constants.Two)

        val sum = sumOf(a, b, c)
        val trinomialSquared = powerOf(sum, two)

        onPattern(trinomialSquared) {
            val (da, db, dc, dtwo) = distribute(a, b, c, two)

            ruleResult(
                toExpr = sumOf(
                    powerOf(da, dtwo),
                    powerOf(db, dtwo),
                    powerOf(dc, dtwo),
                    explicitProductOf(dtwo, move(a), move(b)),
                    explicitProductOf(dtwo, move(b), move(c)),
                    explicitProductOf(dtwo, move(c), move(a)),
                ),
                gmAction = doubleTap(two),
                explanation = metadata(Explanation.ExpandTrinomialSquaredUsingIdentity),
            )
        }
    }

/**
 * (a +- b) * (a -+ b) --> [a^2] - [b^2]
 */
private val expandProductOfSumAndDifference =
    rule {
        val a = AnyPattern()
        val b = condition { it !is Minus }
        val pattern = commutativeProductOf(commutativeSumOf(a, b), commutativeSumOf(a, negOf(b)))

        onPattern(pattern) {
            ruleResult(
                toExpr = sumOf(
                    powerOf(move(a), introduce(Constants.Two)),
                    negOf(
                        powerOf(move(b), introduce(Constants.Two)),
                    ),
                ),
                gmAction = applyFormula(pattern, "Difference of Squares"),
                explanation = metadata(Explanation.ExpandProductOfSumAndDifference),
            )
        }
    }

/**
 * (a + b) * (c + d) --> a*c + a*d + b*c + b*d
 */
private val applyFoilMethod =
    rule {
        val a = AnyPattern()
        val b = AnyPattern()
        val c = AnyPattern()
        val d = AnyPattern()
        val sum1 = sumOf(a, b)
        val sum2 = sumOf(c, d)
        val prod = productOf(sum1, sum2)

        onPattern(prod) {
            val (da, db, dc, dd) = distribute(a, b, c, d)

            val toExpr = sumOf(
                explicitProductOf(da, dc),
                explicitProductOf(da, dd),
                explicitProductOf(db, dc),
                explicitProductOf(db, dd),
            )

            ruleResult(
                toExpr = toExpr,
                gmAction = doubleTap(sum2, GmPathModifier.OuterOperator),
                explanation = metadata(Explanation.ApplyFoilMethod),
            )
        }
    }

/**
 * (a1 + a2 + ... + aj) * (b1 + b2 + ... + bk) -->
 * a1*b1 + a1*b2 + ... + a1*bk   +   a2*b1 + a2*b2 + ... + a2*bk   + ... +   aj*bk
 */
private val expandDoubleBrackets =
    rule {
        val sum1 = sumContaining()
        val sum2 = sumContaining()
        val prod = productOf(sum1, sum2)

        onPattern(prod) {
            val terms1 = get(sum1).children.map { distribute(it) }
            val terms2 = get(sum2).children.map { distribute(it) }

            val toExpr = sumOf(
                terms1.map { term1 ->
                    sumOf(terms2.map { term2 -> explicitProductOf(term1, term2) })
                },
            )

            ruleResult(
                toExpr = toExpr,
                gmAction = doubleTap(sum2, GmPathModifier.OuterOperator),
                explanation = metadata(Explanation.ExpandDoubleBrackets),
            )
        }
    }
