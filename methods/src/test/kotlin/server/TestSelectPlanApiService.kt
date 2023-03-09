package server

import engine.context.Context
import engine.context.emptyContext
import engine.methods.Method
import engine.methods.testSelectSuccessfulPlans
import methods.methodRegistry

fun testSelectPlanApi(
    inputExpr: String,
    expectedMethodSelections: Set<Method>,
    context: Context = emptyContext,
) = testSelectSuccessfulPlans(inputExpr, expectedMethodSelections, methodRegistry, context)

fun testSelectPlanApiInX(inputExpr: String, expectedMethodSelections: Set<Method>) =
    testSelectPlanApi(inputExpr, expectedMethodSelections, Context(solutionVariable = "x"))
