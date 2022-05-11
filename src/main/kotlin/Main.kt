import expressions.*
import transformations.AddLikeFractions

fun main() {
    val one = IntegerExpr(1)
    val two = IntegerExpr(2)
    val frac = fractionOf(one, two)
    val sum = sumOf(frac, frac)
    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    val step = AddLikeFractions.tryExecute(Subexpression(RootPath, sum))
    step?.prettyPrint()
}
