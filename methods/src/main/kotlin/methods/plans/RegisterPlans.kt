package methods.plans

import engine.plans.PlanRegistry

fun registerPlans() {
    PlanRegistry.addPlan(simplifyArithmeticExpression)
    PlanRegistry.addPlan(combineFractionsInExpression)
    PlanRegistry.addPlan(addMixedNumbers)
    PlanRegistry.addPlan(simplifyNumericFraction)
    PlanRegistry.addPlan(evaluatePositiveFractionSum)
    PlanRegistry.addPlan(evaluatePositiveFractionProduct)
    PlanRegistry.addPlan(evaluatePositiveFractionPower)
    PlanRegistry.addPlan(evaluatePowerOfFraction)
    PlanRegistry.addPlan(evaluateSignedIntegerPower)
}
