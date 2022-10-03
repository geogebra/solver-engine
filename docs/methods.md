# Methods

The rule are the simplest method. It creates a transformation with
an explanation and potentially with associated skills, but with no
sub-steps. It transforms the input which matches its pattern into
an equivalent expression.
The plan is the top and mid-level method, meaning it is a combination
of rules and simpler plans. Plans have a "goal".

## Guide for adding new methods

Whenever you want to add a new rule or plan, make sure you follow
the checklist below:

1. [ ] check if a method which does the transformation you want
   already exists (or one which can be generalized to achieve it).
2. [ ] decide on which category the new method fits into.
3. [ ] choose a good descriptive name for the new method (each method
   describes an action, so the name should start with a verb).
4. [ ] add the explanation key to the explanation enum, for rules make
   the convention is that the name of the rule and the enum match. 
5. [ ] implement the method!

The conventional way to create a new instance of a `Rule` object is
to do so in a `rule` block. First you set up the pattern in successive
variable declarations, then you use the `onPattern` method of the 
RuleBuilder to describe the transformation. You can do computations inside
the code block, and in the end you have to return either `null`, if the
rule does not apply despite the pattern matching, or a `TransformationResult`
(with the resulting mapped expression, explanation, and skills) if it 
does apply.

Example:

```kotlin
val exampleRule = rule {
    val innerPattern = AnyPattern()
    val outerPattern = negOf(innerPattern)

    onPattern(outerPattern) {
       TransformationResult(
          toExpr = productOf(
             bracketOf(introduce(xp(-1))),
             move(innerPattern),
          ), 
          explanation = metadata(
            Explanation.ExampleRule,
            move(innerPattern),
          ),
          skills = listOf(
             metadata(
                Skills.ExampleSkill,
                move(outerPattern),
             )
          )
       )
    }
}
```

The above rule transforms any given expression of the form `-a` into
`(-1) * a`. The reason for extracting the declaration of the inner
pattern (instead of writing `negOf(AnyPattern())`) is so that it is 
possible to refer to the expression matched by the inner pattern 
(in our case `a`) independently.

Plans are created using the `plan` block, they may have a pattern
to check that the input expression is of the right form, a result
pattern to check that the required output form was achieved, 
explanation, and skills, just like Rules. The real work inside a plan 
is done by one of the many plan executors:
- **FirstOf**: given a list of transformation producers it
  executes the first one which applies
- **Pipeline**: given a list of transformation producers it
  executes them all in a sequence. It fails if one of the steps
  fails, unless it is marked as optional.
- **Deeply**: given a transformation producer it executes it
  at the first position it was found. Can be configured using
  the `deepFirst` parameter. E.g. for zeros in product the search
  should start at the top level (`deepFirst = false`), but for
  general arithmetic expression the evaluation should start inside
  the deepest parenthesis (`deepFirst = true`).
- **WhilePossible**: given a transformation producer it is
  executed as many times as possible on the expression. E.g.
  multiplication of integers is defined as a rule for two numbers,
  but is extended to an arbitrary number of values using a
  WhilePossible steps producer.
- **InStep**: given a list of transformation producers and a list
  of input expressions it executes the transformation producers in
  an interlaced way, e.g. first producer on first expression, then
  first producer on second expression, ..., then second producer
  on first expression, second producer on second expression etc.

Plans may declare region-specific alternatives using the `alternative`
block and the `resourceData` field.
