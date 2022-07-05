package engine.plans

enum class PlanId {
    SimplifyArithmeticExpression,
    CombineFractionsInExpression,
    AddMixedNumbers,
    SimplifyNumericFraction,
    EvaluatePositiveFractionSum,
    EvaluatePositiveFractionProduct,
    EvaluatePowerOfFraction,
    EvaluateSignedIntegerPower,
    EvaluatePositiveFractionPower;
}

class PlanRegistry {

    private var plans = HashMap<PlanId, Plan>()

    fun addPlan(plan: Plan) {
        if (plan.planId != null) {
            plans[plan.planId] = plan
        }
    }

    fun getPlan(planId: PlanId): Plan? {
        return plans[planId]
    }

    fun getPlan(planIdString: String): Plan? {
        return try {
            val planId = PlanId.valueOf(planIdString)
            plans[planId]
        } catch (@Suppress("SwallowedException") e: IllegalArgumentException) {
            null
        }
    }

    fun allPlans(): Sequence<Plan> = plans.values.asSequence()
}
