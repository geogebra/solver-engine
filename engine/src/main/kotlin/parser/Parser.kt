/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package parser

import engine.expressions.Constants
import engine.expressions.Contradiction
import engine.expressions.Decorator
import engine.expressions.Equation
import engine.expressions.Expression
import engine.expressions.ExpressionWithConstraint
import engine.expressions.FiniteSet
import engine.expressions.Identity
import engine.expressions.ImplicitSolution
import engine.expressions.Inequation
import engine.expressions.ListExpression
import engine.expressions.Power
import engine.expressions.Product
import engine.expressions.SetDifference
import engine.expressions.SetExpression
import engine.expressions.SetSolution
import engine.expressions.Variable
import engine.expressions.VariableList
import engine.expressions.VoidExpression
import engine.expressions.cartesianProductOf
import engine.expressions.expressionOf
import engine.expressions.greaterThanEqualOf
import engine.expressions.greaterThanOf
import engine.expressions.lessThanEqualOf
import engine.expressions.lessThanOf
import engine.expressions.matrixOf
import engine.expressions.mixedNumber
import engine.expressions.nameXp
import engine.expressions.negOf
import engine.expressions.productSignRequired
import engine.expressions.setUnionOf
import engine.expressions.xp
import engine.operators.AddEquationsOperator
import engine.operators.BinaryExpressionOperator
import engine.operators.Comparator
import engine.operators.DefaultProductOperator
import engine.operators.DefiniteIntegralOperator
import engine.operators.DerivativeOperator
import engine.operators.DoubleComparisonOperator
import engine.operators.ExpressionWithConstraintOperator
import engine.operators.IndefiniteIntegralOperator
import engine.operators.IntervalOperator
import engine.operators.Operator
import engine.operators.StatementSystemOperator
import engine.operators.StatementUnionOperator
import engine.operators.SubtractEquationsOperator
import engine.operators.SumOperator
import engine.operators.TrigonometricFunctionOperator
import engine.operators.TrigonometricFunctionType
import engine.operators.TupleOperator
import engine.operators.UnaryExpressionOperator
import engine.operators.VectorOperator
import engine.utility.RecurringDecimal
import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Token
import parser.antlr.ExpressionBaseVisitor
import parser.antlr.ExpressionLexer
import parser.antlr.ExpressionParser
import java.math.BigDecimal
import java.security.InvalidParameterException

fun parseExpression(text: String): Expression {
    val lexer = ExpressionLexer(CharStreams.fromString(text))
    val parser = ExpressionParser(CommonTokenStream(lexer))
    parser.errorHandler = BailErrorStrategy()
    val visitor = ExpressionVisitor()
    return visitor.visit(parser.wholeInput())
}

private fun adjustBracket(operator: Operator, i: Int, operand: Expression) =
    when {
        operand.hasBracket() || operator.nthChildAllowed(i, operand.operator) -> operand
        else -> operand.decorate(Decorator.MissingBracket)
    }

private fun makeExpression(operator: Operator, operands: List<Expression>) =
    expressionOf(
        operator,
        operands.mapIndexed { i, operand -> adjustBracket(operator, i, operand) },
    )

private fun makeExpression(operator: Operator, vararg operands: Expression) =
    makeExpression(
        operator,
        operands.asList(),
    )

@Suppress("TooManyFunctions")
private class ExpressionVisitor : ExpressionBaseVisitor<Expression>() {
    override fun visitWholeInput(ctx: ExpressionParser.WholeInputContext): Expression {
        return visit(ctx.getChild(0))
    }

    override fun visitInputList(ctx: ExpressionParser.InputListContext): Expression {
        return ListExpression(ctx.elements.map { visit(it) })
    }

    override fun visitStatementUnion(ctx: ExpressionParser.StatementUnionContext): Expression {
        val statements = ctx.statements.map { visit(it) }
        return if (statements.size == 1) {
            statements[0]
        } else {
            makeExpression(StatementUnionOperator, statements)
        }
    }

    override fun visitStatementWithConstraints(ctx: ExpressionParser.StatementWithConstraintsContext): Expression {
        return ctx.constraints.fold(visit(ctx.stmt)) { stmt, constraint ->
            makeExpression(ExpressionWithConstraintOperator, listOf(stmt, visit(constraint)))
        }
    }

    override fun visitStatementSystem(ctx: ExpressionParser.StatementSystemContext): Expression {
        val statements = ctx.statements.map { visit(it) }
        return if (statements.size == 1) {
            statements[0]
        } else {
            makeExpression(StatementSystemOperator, statements)
        }
    }

    override fun visitEquationAddition(ctx: ExpressionParser.EquationAdditionContext): Expression {
        return makeExpression(AddEquationsOperator, listOf(visit(ctx.eq1), visit(ctx.eq2)))
    }

    override fun visitEquationSubtraction(ctx: ExpressionParser.EquationSubtractionContext): Expression {
        return makeExpression(SubtractEquationsOperator, listOf(visit(ctx.eq1), visit(ctx.eq2)))
    }

    override fun visitEquation(ctx: ExpressionParser.EquationContext): Expression {
        return Equation(visit(ctx.lhs), visit((ctx.rhs)))
    }

    override fun visitInequation(ctx: ExpressionParser.InequationContext): Expression {
        return Inequation(visit(ctx.lhs), visit(ctx.rhs))
    }

    override fun visitInequality(ctx: ExpressionParser.InequalityContext): Expression {
        val lhs = visit(ctx.lhs)
        val rhs = visit(ctx.rhs)

        return when (ctx.comparator.text) {
            "<" -> lessThanOf(lhs, rhs)
            "<=" -> lessThanEqualOf(lhs, rhs)
            ">" -> greaterThanOf(lhs, rhs)
            ">=" -> greaterThanEqualOf(lhs, rhs)
            else -> throw InvalidParameterException("Comparator ${ctx.comparator.text} not recognized")
        }
    }

    override fun visitTuple(ctx: ExpressionParser.TupleContext): Expression {
        return makeExpression(TupleOperator, listOf(visit(ctx.first)) + ctx.rest.map { visit(it) })
    }

    override fun visitDoubleInequality(ctx: ExpressionParser.DoubleInequalityContext): Expression {
        val leftOp = Comparator.fromReadableString(ctx.left.text)!!
        val rightOp = Comparator.fromReadableString(ctx.right.text)!!
        return makeExpression(
            DoubleComparisonOperator(leftOp, rightOp),
            listOf(visit(ctx.first), visit(ctx.second), visit(ctx.third)),
        )
    }

    override fun visitExpr(ctx: ExpressionParser.ExprContext): Expression {
        return visit(ctx.getChild(0))
    }

    override fun visitIdentity(ctx: ExpressionParser.IdentityContext): Expression {
        val vars = if (ctx.vars == null) VariableList(emptyList()) else visitVariables(ctx.vars)
        return Identity(vars, visit(ctx.stmt))
    }

    override fun visitContradiction(ctx: ExpressionParser.ContradictionContext): Expression {
        val vars = if (ctx.vars == null) VariableList(emptyList()) else visitVariables(ctx.vars)
        val statement = if (ctx.stmt == null) Constants.Undefined else visit(ctx.stmt)
        return Contradiction(vars, statement)
    }

    override fun visitImplicitSolution(ctx: ExpressionParser.ImplicitSolutionContext): Expression {
        return ImplicitSolution(visitVariables(ctx.vars), visit(ctx.stmt))
    }

    override fun visitSetSolution(ctx: ExpressionParser.SetSolutionContext): Expression {
        return SetSolution(visitVariables(ctx.vars), visit(ctx.solutionSet))
    }

    override fun visitCartesianProduct(ctx: ExpressionParser.CartesianProductContext): Expression {
        val first = visit(ctx.first)
        val rest = ctx.rest.map { visit(it) }
        if (rest.isEmpty()) {
            return first
        }
        return cartesianProductOf(listOf(first) + rest)
    }

    override fun visitSetUnion(ctx: ExpressionParser.SetUnionContext): Expression {
        val first = visit(ctx.first)
        val rest = ctx.rest.map { visit(it) }
        if (rest.isEmpty()) {
            return first
        }
        return setUnionOf(listOf(first) + rest)
    }

    override fun visitSetDifference(ctx: ExpressionParser.SetDifferenceContext): SetExpression {
        return SetDifference(visitSimpleSet(ctx.left), visitSimpleSet(ctx.right))
    }

    override fun visitSimpleSet(ctx: ExpressionParser.SimpleSetContext): SetExpression {
        // I don't think the Kotlin + Antlr combo is strong enough to do this without explicit casting.
        // Prove me wrong!
        return super.visitSimpleSet(ctx) as SetExpression
    }

    override fun visitEmptySet(ctx: ExpressionParser.EmptySetContext): Expression {
        return Constants.EmptySet
    }

    override fun visitFiniteSet(ctx: ExpressionParser.FiniteSetContext): SetExpression {
        return FiniteSet(listOf(visit(ctx.first)) + ctx.rest.map { visit(it) })
    }

    override fun visitReals(ctx: ExpressionParser.RealsContext): Expression {
        return Constants.Reals
    }

    override fun visitInterval(ctx: ExpressionParser.IntervalContext): Expression {
        val leftClosed = ctx.leftBracket.text == "["
        val rightClosed = ctx.rightBracket.text == "]"
        return makeExpression(IntervalOperator(leftClosed, rightClosed), visit(ctx.left), visit(ctx.right))
    }

    override fun visitInfinity(ctx: ExpressionParser.InfinityContext): Expression {
        return Constants.Infinity
    }

    override fun visitMinusInfinity(ctx: ExpressionParser.MinusInfinityContext): Expression {
        return negOf(Constants.Infinity)
    }

    override fun visitExpressionWithConstraint(ctx: ExpressionParser.ExpressionWithConstraintContext): Expression {
        return ExpressionWithConstraint(visit(ctx.sum()), visit(ctx.simpleStatement()))
    }

    override fun visitSum(ctx: ExpressionParser.SumContext): Expression {
        val first = visit(ctx.first)
        val rest = ctx.rest.map { visit(it) }
        if (rest.isEmpty()) {
            return first
        }
        val terms = mutableListOf<Expression>(first)
        terms.addAll(rest)
        return makeExpression(SumOperator, terms)
    }

    override fun visitRealFirstTerm(ctx: ExpressionParser.RealFirstTermContext): Expression {
        val p = visit(ctx.term())
        return if (ctx.sign == null) p else makeExpression(getAdditiveOperator(ctx.sign), p)
    }

    override fun visitFirstPartialSum(ctx: ExpressionParser.FirstPartialSumContext): Expression {
        return visit(ctx.sum()).decorate(Decorator.PartialBracket)
    }

    override fun visitRealOtherTerm(ctx: ExpressionParser.RealOtherTermContext): Expression {
        val p = visit(ctx.term())
        return when (val op = getAdditiveOperator(ctx.sign)) {
            UnaryExpressionOperator.Plus -> if (op.childAllowed(p.operator) || p.hasBracket()) {
                p
            } else {
                p.decorate(
                    Decorator.MissingBracket,
                )
            }
            else -> makeExpression(op, p)
        }
    }

    override fun visitOtherPartialSum(ctx: ExpressionParser.OtherPartialSumContext): Expression {
        return visit(ctx.sum()).decorate(Decorator.PartialBracket)
    }

    override fun visitProduct(ctx: ExpressionParser.ProductContext): Expression {
        val factors = mutableListOf<Expression>(visit(ctx.first))
        val forcedSigns = mutableListOf<Int>()

        for (otherFactor in ctx.rest) {
            val (factor, hasSign) = when (otherFactor) {
                is ExpressionParser.FactorWithNoOperatorContext -> Pair(visit(otherFactor.factor()), false)
                is ExpressionParser.FactorWithProductOperatorContext -> Pair(visit(otherFactor.signedFactor()), true)
                is ExpressionParser.FactorWithDivisionOperatorContext -> {
                    val implicitProduct = visit(otherFactor.implicitProduct())
                    val divisor = makeExpression(UnaryExpressionOperator.DivideBy, implicitProduct)
                    Pair(divisor, false)
                }
                is ExpressionParser.PartialProductWithNoOperatorContext ->
                    Pair(visit(otherFactor.product()).decorate(Decorator.PartialBracket), false)
                is ExpressionParser.PartialProductWithProductOperatorContext ->
                    Pair(visit(otherFactor.product()).decorate(Decorator.PartialBracket), true)
                else -> throw IllegalArgumentException("Invalid factor $otherFactor in product")
            }

            val productSignRequired = productSignRequired(factors.last(), factor)

            when {
                productSignRequired && !hasSign ->
                    println("Warning: product sign needed between ${factors.last()} and $factor")
                !productSignRequired && hasSign ->
                    forcedSigns.add(factors.size)
            }

            factors.add(factor)
        }

        if (factors.size == 1) {
            return factors[0]
        }

        return Product(
            factors.mapIndexed { i, op -> adjustBracket(DefaultProductOperator, i, op) },
            forcedSigns,
        )
    }

    override fun visitPartialFirstFactor(ctx: ExpressionParser.PartialFirstFactorContext): Expression {
        return visit(ctx.product()).decorate(Decorator.PartialBracket)
    }

    override fun visitImplicitProduct(ctx: ExpressionParser.ImplicitProductContext): Expression {
        val first = visit(ctx.first)
        val rest = ctx.others.map { visit(it) }
        if (rest.isEmpty()) {
            return first
        }
        val factors = mutableListOf<Expression>(first)
        factors.addAll(rest)
        return Product(factors.mapIndexed { i, op -> adjustBracket(DefaultProductOperator, i, op) })
    }

    override fun visitSignedFactor(ctx: ExpressionParser.SignedFactorContext): Expression {
        return ctx.signs.foldRight(visit(ctx.factor())) {
                sign, acc ->
            makeExpression(getAdditiveOperator(sign), acc)
        }
    }

    override fun visitPercentage(ctx: ExpressionParser.PercentageContext): Expression {
        return makeExpression(UnaryExpressionOperator.Percentage, visit(ctx.atom()))
    }

    override fun visitPercentageOf(ctx: ExpressionParser.PercentageOfContext): Expression {
        return makeExpression(BinaryExpressionOperator.PercentageOf, visit(ctx.part), visit(ctx.base))
    }

    override fun visitFraction(ctx: ExpressionParser.FractionContext): Expression {
        return makeExpression(BinaryExpressionOperator.Fraction, visit(ctx.num), visit(ctx.den))
    }

    override fun visitPower(ctx: ExpressionParser.PowerContext): Expression {
        return makeExpression(BinaryExpressionOperator.Power, visit(ctx.base), visit(ctx.exp))
    }

    override fun visitSqrt(ctx: ExpressionParser.SqrtContext): Expression {
        return makeExpression(UnaryExpressionOperator.SquareRoot, visit(ctx.radicand))
    }

    override fun visitRoot(ctx: ExpressionParser.RootContext): Expression {
        return makeExpression(BinaryExpressionOperator.Root, visit(ctx.radicand), visit(ctx.order))
    }

    override fun visitNaturalLog(ctx: ExpressionParser.NaturalLogContext): Expression {
        return makeExpression(UnaryExpressionOperator.NaturalLog, visit(ctx.argument))
    }

    override fun visitLogBase10(ctx: ExpressionParser.LogBase10Context): Expression {
        return makeExpression(UnaryExpressionOperator.LogBase10, visit(ctx.argument))
    }

    override fun visitLog(ctx: ExpressionParser.LogContext): Expression {
        return makeExpression(BinaryExpressionOperator.Log, visit(ctx.base), visit(ctx.argument))
    }

    // to parse "trigFunction argument" format
    override fun visitSimpleTrigFunction(ctx: ExpressionParser.SimpleTrigFunctionContext): Expression {
        val functionName = ctx.TRIG_FUNCTION().text.replaceFirstChar { it.uppercase() }
        val function = TrigonometricFunctionOperator(TrigonometricFunctionType.valueOf(functionName))
        return makeExpression(function, visit(ctx.argument))
    }

    // to parse "[trigonometricFunction ^ power] argument" format
    override fun visitPowerTrigFunction(ctx: ExpressionParser.PowerTrigFunctionContext): Expression {
        val baseTrigFunctionType = TrigonometricFunctionType.valueOf(ctx.base.text.replaceFirstChar { it.uppercase() })
        val exponent = visit(ctx.exp)!!

        return if (exponent == Constants.MinusOne) {
            val inverseTrigFunction = TrigonometricFunctionOperator(
                baseTrigFunctionType.inverse,
                inverseNotation = "superscript",
            )
            makeExpression(inverseTrigFunction, visit(ctx.argument))
        } else {
            val baseTrigFunction = TrigonometricFunctionOperator(
                baseTrigFunctionType,
                powerInside = true,
            )
            val baseExpression = makeExpression(baseTrigFunction, visit(ctx.argument))
            makeExpression(BinaryExpressionOperator.Power, baseExpression, exponent)
        }
    }

    override fun visitRealVariablePower(ctx: ExpressionParser.RealVariablePowerContext): Expression {
        return Power(visitVariable(ctx.base), visitNaturalNumber(ctx.exp))
    }

    override fun visitFirstDerivative(ctx: ExpressionParser.FirstDerivativeContext): Expression {
        return makeExpression(DerivativeOperator, Constants.One, visit(ctx.product()), visit(ctx.variablePower()))
    }

    override fun visitNthDerivative(ctx: ExpressionParser.NthDerivativeContext): Expression {
        val arguments = listOf(visit(ctx.order), visit(ctx.product())) + ctx.vars.map { visit(it) }
        return makeExpression(DerivativeOperator, arguments)
    }

    override fun visitIndefiniteIntegral(ctx: ExpressionParser.IndefiniteIntegralContext): Expression {
        return makeExpression(IndefiniteIntegralOperator, visit(ctx.function), visit(ctx.variable()))
    }

    override fun visitDefiniteIntegral(ctx: ExpressionParser.DefiniteIntegralContext): Expression {
        return makeExpression(
            DefiniteIntegralOperator,
            visit(ctx.lowerBound),
            visit(ctx.upperBound),
            visit(ctx.function),
            visit(ctx.variable()),
        )
    }

    override fun visitVector(ctx: ExpressionParser.VectorContext): Expression {
        return makeExpression(VectorOperator, ctx.expr().map { visit(it) })
    }

    override fun visitMatrix(ctx: ExpressionParser.MatrixContext): Expression {
        val matrixRows = ctx.matrixRow().map { row -> row.expr().map { visit(it) } }
        return matrixOf(matrixRows)
    }

    override fun visitAbsoluteValue(ctx: ExpressionParser.AbsoluteValueContext): Expression {
        return makeExpression(UnaryExpressionOperator.AbsoluteValue, visit(ctx.argument))
    }

    override fun visitRoundBracket(ctx: ExpressionParser.RoundBracketContext): Expression {
        return visit(ctx.expr()).decorate(Decorator.RoundBracket)
    }

    override fun visitSquareBracket(ctx: ExpressionParser.SquareBracketContext): Expression {
        return visit(ctx.expr()).decorate(Decorator.SquareBracket)
    }

    override fun visitCurlyBracket(ctx: ExpressionParser.CurlyBracketContext): Expression {
        return visit(ctx.expr()).decorate(Decorator.CurlyBracket)
    }

    override fun visitMixedNumber(ctx: ExpressionParser.MixedNumberContext): Expression {
        return mixedNumber(
            ctx.integer.text.toBigInteger(),
            ctx.num.text.toBigInteger(),
            ctx.den.text.toBigInteger(),
        )
    }

    override fun visitNaturalNumber(ctx: ExpressionParser.NaturalNumberContext): Expression {
        return xp(ctx.NATNUM().text.toBigInteger())
    }

    override fun visitDecimalNumber(ctx: ExpressionParser.DecimalNumberContext): Expression {
        return xp(ctx.DECNUM().text.toBigDecimal())
    }

    override fun visitRecurringDecimalNumber(ctx: ExpressionParser.RecurringDecimalNumberContext): Expression {
        val text = ctx.RECURRING_DECNUM().text
        val startRep = text.indexOf('[')
        val endRep = text.indexOf(']')
        val decimal = BigDecimal(
            text.substring(0 until startRep) +
                text.substring(startRep + 1 until endRep),
        )
        return xp(RecurringDecimal(decimal, endRep - startRep - 1))
    }

    override fun visitVariables(ctx: ExpressionParser.VariablesContext): VariableList {
        return VariableList(listOf(visitVariable(ctx.first)) + ctx.rest.map { visitVariable(it) })
    }

    override fun visitVariable(ctx: ExpressionParser.VariableContext): Variable {
        val variableText = ctx.text
        val underscorePos = variableText.indexOf('_')
        return if (underscorePos < 0) {
            xp(variableText)
        } else {
            xp(variableText.substring(0, underscorePos), variableText.substring(underscorePos + 1))
        }
    }

    override fun visitPi(ctx: ExpressionParser.PiContext?): Expression {
        return Constants.Pi
    }

    override fun visitE(ctx: ExpressionParser.EContext?): Expression {
        return Constants.E
    }

    override fun visitImaginaryUnit(ctx: ExpressionParser.ImaginaryUnitContext?): Expression {
        return Constants.ImaginaryUnit
    }

    override fun visitName(ctx: ExpressionParser.NameContext): Expression {
        val text = ctx.NAME().text
        return nameXp(ctx.NAME().text.substring(1, text.length - 1))
    }

    override fun visitVoid(ctx: ExpressionParser.VoidContext): Expression {
        return VoidExpression()
    }

    override fun visitUndefined(ctx: ExpressionParser.UndefinedContext?): Expression {
        return Constants.Undefined
    }

    private fun getAdditiveOperator(tok: Token): UnaryExpressionOperator =
        when (tok.text) {
            "+" -> UnaryExpressionOperator.Plus
            "-" -> UnaryExpressionOperator.Minus
            "+/-" -> UnaryExpressionOperator.PlusMinus
            else -> throw IllegalArgumentException(tok.text)
        }
}
