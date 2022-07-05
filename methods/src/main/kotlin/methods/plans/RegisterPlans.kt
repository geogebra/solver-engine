package methods.plans

import engine.plans.PlanRegistry

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
