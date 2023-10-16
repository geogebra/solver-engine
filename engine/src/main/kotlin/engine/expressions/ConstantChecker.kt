package engine.expressions

import engine.context.Context

/**
 * A ConstantChecker is able to check whether an expression is constant. There can be different notions of an
 * expression being constant (e.g. it has no variables or its only variables belong to a list of "constant variables").
 */
fun interface ConstantChecker {

    /**
     * Return true if [expression] is considered to be constant in the given [context]
     */
    fun isConstant(context: Context, expression: Expression): Boolean
}

/**
 * Checks for expressions without any variables
 */
val defaultConstantChecker = ConstantChecker { _, e -> e.isConstant() }

/**
 * Checks for expressions whose only variables are not in the list of solution variables
 */
val solutionVariableConstantChecker = ConstantChecker { ctx, e -> e.isConstantIn(ctx.solutionVariables) }
