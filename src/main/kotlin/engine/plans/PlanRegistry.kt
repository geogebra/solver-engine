package engine.plans

enum class PlanId {
    SimplifyArithmeticExpression,
    CombineFractionsInExpression,
    AddMixedNumbers;
}

object PlanRegistry {

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
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
