package engine.expressions

import engine.operators.NameOperator
import engine.operators.VariableListOperator
import engine.operators.VariableOperator
import engine.sign.Sign

class Variable(
    val variableName: String,
    val subscript: String? = null,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
    operator = VariableOperator(variableName, subscript),
    operands = listOf(),
    meta = meta,
) {
    override fun signOf() = Sign.UNKNOWN

    override fun fillJson(s: MutableMap<String, Any>) {
        s["type"] = "Variable"
        s["value"] = variableName
        if (subscript != null) {
            s["subscript"] = subscript
        }
    }
}

class VariableList(
    variables: List<Variable>,
    meta: NodeMeta = BasicMeta(),
) : Expression(VariableListOperator, variables, meta) {
    val variableExpressions get() = children.map { it as Variable }
}

class Name(
    val text: String,
    meta: NodeMeta = BasicMeta(),
) : Expression(NameOperator(text), listOf(), meta) {
    override fun fillJson(s: MutableMap<String, Any>) {
        s["type"] = "Name"
        s["value"] = text
    }
}
