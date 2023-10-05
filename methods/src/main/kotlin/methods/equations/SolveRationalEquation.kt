
import engine.context.Context
import engine.context.Curriculum
import engine.expressions.Constants
import engine.expressions.Contradiction
import engine.expressions.DivideBy
import engine.expressions.Equation
import engine.expressions.Expression
import engine.expressions.Fraction
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.patterns.condition
import engine.steps.metadata.metadata
import methods.algebra.AlgebraExplanation
import methods.algebra.AlgebraPlans
import methods.algebra.algebraicSimplificationSteps
import methods.equations.EquationsPlans
import methods.equations.EquationsRules
import methods.equations.Explanation
import methods.equations.checkSolutionsAgainstConstraint
import methods.rationalexpressions.RationalExpressionsPlans

private val simplifyRationalEquation = plan {
    explanation = Explanation.SimplifyEquation
    steps {
        apply(algebraicSimplificationSteps(addRationalExpressions = false))
    }
}

private val factorDenominatorsOfFractions = plan {
    explanation = Explanation.FactorDenominatorOfFraction
    steps {
        whilePossible {
            deeply(RationalExpressionsPlans.FactorDenominatorOfFraction)
        }
    }
}

private val simplifyToPolynomialEquation = steps {
    firstOf {
        option {
            /**
             * An equation where all the denominators are the same is called a rational equation with a trivial LCD
             * Here we don't want to cancel the fractions (because the cancellation would have to be undone)
             */
            apply(EquationsRules.MultiplyBothSidesOfRationalEquationWithTrivialLCD)
            apply(simplifyRationalEquation)
        }
        option {
            optionally(simplifyRationalEquation)
            optionally(factorDenominatorsOfFractions)
            apply(EquationsRules.MultiplyBothSidesOfRationalEquation)
            optionally(simplifyRationalEquation)
        }
    }
}

private fun Expression.containsVariableDenominator(context: Context): Boolean {
    val variableDenominator = when (this) {
        is Fraction -> !denominator.isConstantIn(context.solutionVariables)
        is DivideBy -> !divisor.isConstantIn(context.solutionVariables)
        else -> false
    }
    return variableDenominator || children.any { it.containsVariableDenominator(context) }
}

internal val solveRationalEquation = taskSet {
    explanation = Explanation.SolveEquation
    pattern = condition { it is Equation && it.containsVariableDenominator(this) }

    tasks {
        val constraint = when (context.curriculum) {
            Curriculum.US -> expression
            else -> task(
                startExpr = expression,
                explanation = metadata(AlgebraExplanation.ComputeDomainOfAlgebraicExpression),
                stepsProducer = AlgebraPlans.ComputeDomainOfAlgebraicExpression,
            )?.result ?: return@tasks null
        }

        val simplifyToPolynomialExpression = task(
            startExpr = expression,
            explanation = metadata(Explanation.SimplifyToPolynomialEquation),
            stepsProducer = simplifyToPolynomialEquation,
        )!!.result

        val solvePolynomialEquation = task(
            startExpr = simplifyToPolynomialExpression,
            explanation = metadata(Explanation.SolveEquation),
            stepsProducer = EquationsPlans.SolveEquation,
        ) ?: return@tasks null

        val solution = solvePolynomialEquation.result

        // no need to check if the constraint(s) is/are satisfied if solution
        // is an empty set
        if (solution !is Contradiction || solution == Constants.EmptySet) {
            checkSolutionsAgainstConstraint(solution, constraint) ?: return@tasks null
        }

        allTasks()
    }
}
