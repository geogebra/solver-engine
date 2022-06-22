package methods.plans

import engine.plans.PlanRegistry

fun registerPlans() {
    PlanRegistry.addPlan(simplifyArithmeticExpression)
    PlanRegistry.addPlan(combineFractionsInExpression)
    PlanRegistry.addPlan(addMixedNumbers)
}
