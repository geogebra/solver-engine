package expressions

enum class UnaryOperator {
    Bracket,
    Plus,
    Minus,
    SquareRoot,
    NaturalLog,
}

enum class BinaryOperator {
    Fraction,
    Divide,
    Power,
    Root,
    MixedNumber,
}

enum class NaryOperator {
    Sum,
    Product,
    ImplicitProduct,
}