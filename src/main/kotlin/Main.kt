

interface Expression {
    fun variables(): Set<Variable> = emptySet()
    fun children(): Sequence<Expression> = emptySequence()
}

interface Literal : Expression

data class Integer(val value: Int) : Literal
data class Rational(val num: Int, val den: Int) : Literal
data class Decimal(val value: Float) : Literal

data class Variable(val name: String) : Expression {
    override fun variables(): Set<Variable> = setOf(this)
}

abstract class UnaryOp(private val expr : Expression): Expression {
    override fun children(): Sequence<Expression> = sequenceOf(expr)
}

abstract class NaryOp(private val operands: List<Expression>): Expression {
    override fun children(): Sequence<Expression> = operands.asSequence()
}

data class Neg(val expr: Expression): UnaryOp(expr)

data class Bracket(val expr: Expression) : UnaryOp(expr)

data class Sum(val terms: List<Expression>) : NaryOp(terms) {

    fun groupLiterals(): Sum {
        val literals = terms.filterIsInstance<Literal>()
        if (literals.isEmpty() || literals.size == terms.size) {
            return this
        }
        val nonLiterals = terms.filter { it !is Literal }
        return sumOf(Sum(nonLiterals), Sum(literals))
    }
}

fun sumOf(vararg terms: Expression): Sum {
    return Sum(terms.asList())
}

interface Rule {
    fun apply(expr: Expression): Expression?
}

interface Pattern {
    fun match(e : Expression): Sequence<Match>
    fun variables(): List<PatternVariable>
}

data class AssocPattern {

}

data class Path(val parent : Path?, index: Int) {
    fun child(index : Int) : Path {
        return Path()
    }
}
interface Match {
    fun expression() : Expression
    fun bindings() : List<Path>
    fun substitute(es : List<Expression>) : Expression
}

data class PatternVariable(val name : String)

object IntegerPattern : Pattern {
    fun match(e : Expression): Sequence<Match> {
        if (e is Integer) {
            return sequenceOf(e)
        }
    }
}


object groupSumLiterals : Rule {
    override fun apply(expr: Expression): Expression? {
        return when (expr) {
            is Sum -> expr
            else -> null
        }
    }
}

fun main(args: Array<String>) {
    println("Hello World!")

    val three = Integer(3)
    val x = Variable("x")
    val expr = Bracket(sumOf(x, Neg(three)))
    val otherExpr = Bracket(sumOf(x, Neg(three)))
    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("$expr ${expr == otherExpr} ${expr.children().toList()}")
}