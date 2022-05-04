package steps

import expressions.Expression
import expressions.IntegerExpr
import kotlin.reflect.KClass

enum class SkillType(vararg val params: KClass<out Expression>) {
    NumericLCM(IntegerExpr::class, IntegerExpr::class);

    fun paramCount() : Int = params.size
}

data class NumericLCM(val x: IntegerExpr, val y: IntegerExpr): Skill