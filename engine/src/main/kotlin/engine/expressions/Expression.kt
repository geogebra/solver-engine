package engine.expressions

import engine.operators.LatexRenderable
import engine.operators.Operator
import engine.operators.RenderContext
import engine.operators.VariableOperator

enum class Decorator {
    RoundBracket {
        override fun decorateString(str: String) = "($str)"
        override fun decorateLatexString(str: String) = "{\\left( $str \\right)}"
    },
    SquareBracket {
        override fun decorateString(str: String) = "[. $str .]"
        override fun decorateLatexString(str: String) = "{\\left[ $str \\right]}"
    },
    CurlyBracket {
        override fun decorateString(str: String) = "{. $str .}"
        override fun decorateLatexString(str: String) = "{\\left\\{ $str \\right\\}}"
    },
    MissingBracket {
        override fun decorateString(str: String) = str
        override fun decorateLatexString(str: String) = str
    };

    abstract fun decorateString(str: String): String
    abstract fun decorateLatexString(str: String): String
}

class Expression(
    val operator: Operator,
    operands: List<Expression>,
    val decorators: List<Decorator> = emptyList()
) : LatexRenderable {
    val operands: List<Expression>

    val variables: Set<String> = when {
        operator is VariableOperator -> setOf(operator.name)
        else -> operands.flatMap { it.variables }.toSet()
    }

    init {
        this.operands = operands.mapIndexed { index, expression ->
            if (expression.hasBracket() || operator.nthChildAllowed(index, expression.operator)) expression
            else expression.decorate(Decorator.RoundBracket)
        }
    }

    override fun toString(): String {
        return decorators.fold(operator.readableString(operands)) { acc, dec -> dec.decorateString(acc) }
    }

    /**
     * Returns a LaTeX string representation of the expression.  The string should be of the form "{...}".
     * Each operand should itself be enclosed in "{...}" and there should be no other curly brackets,
     * except for the pair "{}" itself.
     *
     * This ensures that paths can be followed in the string representation by counting instances of
     * "{" and "}" and discarding "{}".
     */
    override fun toLatexString(ctx: RenderContext): String {
        return decorators.fold(operator.latexString(ctx, operands)) { acc, dec -> dec.decorateLatexString(acc) }
    }

    fun equiv(other: Expression): Boolean {
        return operator.equiv(other.operator) &&
            operands.size == other.operands.size &&
            operands.zip(other.operands).all { (op1, op2) -> op1.equiv(op2) }
    }

    fun decorate(decorator: Decorator) = Expression(operator, operands, decorators + decorator)

    fun hasBracket() = decorators.isNotEmpty()

    fun removeBrackets() = Expression(operator, operands)

    fun outerBracket() = decorators.lastOrNull()

    /**
     * The expression contains no variables of any kind
     */
    fun isConstant() = variables.isEmpty()

    /**
     * The expression does not depend on the specified symbol / variable
     */
    fun isConstantIn(symbol: String) = !variables.contains(symbol)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Expression

        if (operator != other.operator) return false
        if (decorators != other.decorators) return false
        if (operands != other.operands) return false

        return true
    }

    override fun hashCode(): Int {
        var result = operator.hashCode()
        result = 31 * result + decorators.hashCode()
        result = 31 * result + operands.hashCode()
        return result
    }
}
