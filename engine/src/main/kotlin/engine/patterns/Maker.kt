package engine.patterns

/**
 * Can build an instance of T from a match.  Examples are ExpressionMaker and MetadataMaker.
 */
fun interface Maker<T> {
    /**
     * Returns a new instance of T build from the given match.
     */
    fun make(match: Match): T
}
