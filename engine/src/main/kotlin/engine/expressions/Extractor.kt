package engine.expressions

/**
 * Interface for extracting subexpressions, used by `ApplyTo`.
 */
fun interface Extractor {
    /**
     * Extracts a `Subexpression` of a `Subexpression` and returns it, or return null if that extraction was
     * unsuccessful.
     */
    fun extract(sub: Expression): Expression?
}
