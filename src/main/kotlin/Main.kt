import expressions.*

fun main(args: Array<String>) {
    println("Hello World!")

    val three = IntegerExpr(3)
    val x = VariableExpr("x")
    val expr = BracketExpr(sumOf(x, NegExpr(three)))
    val otherExpr = BracketExpr(sumOf(x, NegExpr(three)))
    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("$expr ${expr == otherExpr} ${expr.children().toList()}")
}