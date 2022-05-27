package plans
//
//import patterns.Pattern
//import patterns.UnsignedIntegerPattern
//import patterns.fractionOf
//import patterns.sumContaining
//import rules.*
//import steps.ExplanationMaker
//import steps.SkillMaker
//
//class PlanBuilder {
//
//    var steps: MutableList<Plan> = mutableListOf()
//    var explanationMaker: ExplanationMaker? = null
//    var skillMakers: MutableList<SkillMaker> = mutableListOf()
//    var pattern: Pattern? = null
//
//    fun step(init: PlanBuilder.() -> Unit) {
//        val stepBuilder = PlanBuilder()
//        stepBuilder.init()
//        steps.add(stepBuilder.buildPlan())
//    }
//
//    fun step(step: Plan) {
//        steps.add(step)
//    }
//
//    fun firstOf(init: PlanBuilder.() -> Unit) {
//
//    }
//
//    fun buildPlan(): Plan {
//
//    }
//}
//
//fun plan(init: PlanBuilder.() -> Unit): Plan {
//    val planBuilder = PlanBuilder()
//    planBuilder.init()
//    return planBuilder.buildPlan()
//}
//
//val addUnlikeFractions2 = plan {
//    val f1 = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())
//    val f2 = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())
//
//    pattern = sumContaining(f1, f2)
//
//    step(commonDenominator)
//    step {
//        whilePossible {
//            deeply(evaluateIntegerProduct)
//        }
//    }
//    step {
//        apply(add)
//    }
//}
//
//val simplifyArithmeticExpression = plan {
//
//    explanation = "simplify arithmetic expression"
//
//    whilePossible {
//        deeply {
//            deepFirst()
//            firstOf {
//                option(removeBracketAroundSignedIntegerInSum)
//                option(simplifyDoubleNeg)
//                option {
//                    explanation = "simplify integer product"
//                    whilePossible(evaluateSignedIntegerProduct)
//                }
//                option {
//                    explanation = "simplify integer sum"
//                    whilePossible(evaluateSignedIntegerAddition)
//                }
//            }
//        }
//    }
//}