package methods

import engine.plans.PlanRegistry
import methods.fractionarithmetic.combineFractionsInExpression
import methods.fractionarithmetic.evaluatePositiveFractionPower
import methods.fractionarithmetic.evaluatePositiveFractionProduct
import methods.fractionarithmetic.evaluatePositiveFractionSum
import methods.fractionarithmetic.evaluatePowerOfFraction
import methods.fractionarithmetic.simplifyNumericFraction
import methods.integerarithmetic.evaluateSignedIntegerPower
import methods.integerarithmetic.simplifyArithmeticExpression
import methods.mixednumbers.addMixedNumbers

val planRegistry = run {
    val planRegistry = PlanRegistry()
    planRegistry.addPlan(simplifyArithmeticExpression)
    planRegistry.addPlan(combineFractionsInExpression)
    planRegistry.addPlan(addMixedNumbers)
    planRegistry.addPlan(simplifyNumericFraction)
    planRegistry.addPlan(evaluatePositiveFractionSum)
    planRegistry.addPlan(evaluatePositiveFractionProduct)
    planRegistry.addPlan(evaluatePositiveFractionPower)
    planRegistry.addPlan(evaluatePowerOfFraction)
    planRegistry.addPlan(evaluateSignedIntegerPower)

    planRegistry
}
