package methods.expand

import engine.context.Setting
import engine.expressions.Constants
import engine.expressions.DivideBy
import engine.expressions.Expression
import engine.expressions.Minus
import engine.expressions.Sum
import engine.expressions.equationOf
import engine.expressions.explicitProductOf
import engine.expressions.fractionOf
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.methods.Rule
import engine.methods.RuleResultBuilder
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.commutativeProductOf
import engine.patterns.commutativeSumOf
import engine.patterns.condition
import engine.patterns.fractionOf
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
    DistributeConstantNumerator(distributeConstantNumerator),
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

        val distributedTerms = if (context.isSet(Setting.CopySumSignsWhenDistributing)) {
            val toDistribute = copySign(optionalNegProduct, toDistribute)
            sumValue.terms.map {
                multiplyDistributedTerm(toDistribute, it, multiplyFromRight, extractMinus = true)
            }
        } else {
            sumValue.terms.map {
                copySign(optionalNegProduct, multiplyDistributedTerm(toDistribute, it, multiplyFromRight))
            }
        }
        ruleResult(
            toExpr = sumOf(distributedTerms),
            gmAction = drag(toDistribute, GmPathModifier.Group, sum, null, DragTargetPosition.LeftOf),
            explanation = metadata(
                Explanation.DistributeMultiplicationOverSum,
                copySign(optionalNegProduct, toDistribute),
            ),
        )
    }
}

private fun RuleResultBuilder.multiplyDistributedTerm(
    toDistribute: Expression,
    term: Expression,
    fromRight: Boolean,
    extractMinus: Boolean = false,
): Expression {
    val effectiveTerm = if (extractMinus && term is Minus && !term.hasVisibleBracket()) {
        term.argument
    } else {
        term
    }
    val product = if (fromRight) {
        explicitProductOf(move(effectiveTerm), toDistribute)
    } else {
        explicitProductOf(toDistribute, move(effectiveTerm))
    }
    return if (term === effectiveTerm) {
        product
    } else {
        negOf(product)
    }
}

/**
 * [x + 2 / 3] -> [x / 3] + [2 / 3]
 * [x - 2 / 3] -> [x / 3] - [2 / 3]
 *
 * We don't want to expand when the numerator is constant because it would break constant expression
 * simplification.  This probably should be thought through.
 */
private val distributeConstantNumerator = rule {
    val sum = condition(sumContaining()) { !it.isConstant() }
    val denominator = condition { it.isConstant() }
    val fraction = fractionOf(sum, denominator)

    onPattern(fraction) {

        val sumValue = get(sum) as Sum
        val toDistribute = distribute(denominator)

        // We always extract the minus in this case because we want
        // [x - 2 / 3] --> [x / 3] - [2 / 3]
        // Not
        // --> [x / 3] + [-2 / 3]
        val distributedTerms = sumValue.terms.map {
            divideDistributedTerm(toDistribute, it, extractMinus = true)
        }

        ruleResult(
            toExpr = sumOf(distributedTerms),
            gmAction = drag(toDistribute, GmPathModifier.Group, sum, null, DragTargetPosition.LeftOf),
            explanation = metadata(
                Explanation.DistributeConstantNumerator,
                toDistribute,
            ),
        )
    }
}

private fun RuleResultBuilder.divideDistributedTerm(
    toDistribute: Expression,
    term: Expression,
    extractMinus: Boolean = false,
): Expression {
    val effectiveTerm = if (extractMinus && term is Minus && !term.hasVisibleBracket()) {
        term.argument
    } else {
        term
    }
    val fraction = fractionOf(move(effectiveTerm), toDistribute)
    return if (term === effectiveTerm) {
        fraction
    } else {
        negOf(fraction)
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

            val fa = substitute(a, "a")
            val fb = substitute(b, "b")
            val ftwo = substitute(two)

            ruleResult(
                toExpr = sumOf(
                    powerOf(da, dtwo),
                    explicitProductOf(dtwo, da, db),
                    powerOf(db, dtwo),
                ),
                formula = equationOf(
                    powerOf(sumOf(fa, fb), ftwo),
                    sumOf(powerOf(fa, ftwo), productOf(ftwo, fa, fb), powerOf(fb, ftwo)),
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

            val fa = substitute(a, "a")
            val fb = substitute(b, "b")
            val fthree = substitute(three)

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
                formula = equationOf(
                    powerOf(sumOf(fa, fb), fthree),
                    sumOf(
                        powerOf(fa, fthree),
                        productOf(fthree, powerOf(fa, Constants.Two), fb),
                        productOf(fthree, fa, powerOf(fb, Constants.Three)),
                        powerOf(fb, fthree),
                    ),
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

            val fa = substitute(a, "a")
            val fb = substitute(b, "b")
            val fc = substitute(c, "c")
            val ftwo = substitute(two)

            ruleResult(
                toExpr = sumOf(
                    powerOf(da, dtwo),
                    powerOf(db, dtwo),
                    powerOf(dc, dtwo),
                    explicitProductOf(dtwo, da, db),
                    explicitProductOf(dtwo, db, dc),
                    explicitProductOf(dtwo, dc, da),
                ),
                formula = equationOf(
                    powerOf(sumOf(fa, fb, fc), ftwo),
                    sumOf(
                        powerOf(fa, ftwo),
                        powerOf(fb, ftwo),
                        powerOf(fc, ftwo),
                        productOf(ftwo, fa, fb),
                        productOf(ftwo, fb, fc),
                        productOf(ftwo, fc, fa),
                    ),
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
            val fa = substitute(a, "a")
            val fb = substitute(b, "b")

            ruleResult(
                toExpr = sumOf(
                    powerOf(move(a), introduce(Constants.Two)),
                    negOf(
                        powerOf(move(b), introduce(Constants.Two)),
                    ),
                ),
                formula = equationOf(
                    pattern.substitute(sumOf(fa, fb), sumOf(fa, negOf(fb))),
                    sumOf(powerOf(fa, Constants.Two), powerOf(fb, Constants.Two)),
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
                gmAction = doubleTap(sum2, GmPathModifier.OpenParens),
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
                gmAction = doubleTap(sum2, GmPathModifier.OpenParens),
                explanation = metadata(Explanation.ExpandDoubleBrackets),
            )
        }
    }
