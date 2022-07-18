# Guide for adding new methods

Whenever you want to add a new rule or plan, make sure you follow
the checklist below:

1. [ ] check if a method which does the transformation you want
   already exists (or one which can be generalized to achieve it)
2. [ ] decide on which category the new method fits into
3. [ ] choose a good descriptive name for the new method (each method
   describes an action, so the name should start with a verb)
4. [ ] add the explanation key to the category file, for rules make
   sure that the names of the rule and key match
5. [ ] implement the method!

The conventional way to create a new instance of a `Rule` object is
to do so in a `run` block. First you set up the pattern in successive
variable declarations, then you return the `Rule` object created.

Example:

```kotlin
val exampleRule = run {
    val innerPattern = AnyPattern()
    val outerPattern = negOf(innerPattern)

    Rule(
        pattern = outerPattern,
        resultMaker = makeProductOf(
            bracketOf(FixedExpressionMaker(xp(-1))),
            move(innerPattern),
        ),
        explanationMaker = makeMetadata(
            Explanation.ExampleRule,
            move(innerPattern),
        ),
        skillMakers = listOf(
            makeMetadata(
                Skills.ExampleSkill,
                move(outerPattern),
            )
        )
    )
}
```

The above rule transforms any given expression of the form `-a` into
`(-1) * a`. The reason for extracting the definition of the inner
pattern is so that it is possible to refer to the expression matched
by the inner pattern (in our case `a`) independently.
