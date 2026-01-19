# Methods

There are 3 types of methods

- rules
- plans
- task sets

The rule is the simplest method. It creates a transformation with an explanation and potentially with associated skills,
but with no sub-steps. It transforms the input which matches its pattern into an equivalent expression.

The plan is the top and mid-level method, meaning it is a sequence
of rules and simpler plans where the output of the previous method is the input of the next method. Plans have a "goal".

The task set is a method which cannot be written as a sequence of simpler methods because it does "side calculations".
For example to solve `|x + 1| = 4` we have to solve two equations (`x + 1 = 4` and `x + 1 = -4`) and then combine the
results together. In this case we use a task set with 3 tasks

1. solve `x + 1 = 4`
2. solver `x + 1 = -4`
3. combine the solutions of task 1 and task 2 together to get `x = 3, -5`

## Transformations

When a method is executed on an input, it either fails or produces a transformation

A _transformation_ turns an expression (`fromExpr`) into another (`toExpr`). There are several types of transformation:

- `rule`: this is a "leaf" transformation, it doesn't have any substeps
- `plan`: a transformation which is made of a sequence of one or more steps (transformations) applying to a
  subexpression of the `fromExpr`
- `taskset`: a transformation which is made of a number of tasks. The outcome of the last task is the `toExpr` of the
  transformation.

A _task_ only appears in a transformation of type `taskset`. It consists of the following:

- a `taskId` so that subsequent tasks can refer to this task
- a `startExpr` which is an expression built from the task set's `fromExpr` and the outcome of previous tasks in the
  task set.
- an `explanation` for the task in context of the task set
- a chain of steps (transformations) which turns the `startExpr` into something else.

Given an input, the solver produce a transformation (or a set of transformations) that represent the "solutions" of the
problem.

## Guide for adding new methods

Whenever you want to add a new rule or plan, make sure you follow
the checklist below.

1. [ ] Check if a method which does the transformation you want already exists (or one which can be generalized to
       achieve it).
2. [ ] Decide on which category the new method fits into. Categories top-level packages in the `methods` modules (for
       example `equations`, `inequalities`, etc.)
3. [ ] Choose a good descriptive name for the new method (each method
       describes an action, so the name should start with a verb).
4. [ ] Add the explanation key to the explanation enum, for rules make the convention is that the name of the rule and
       the enum match, unless there already a suitable explanation from another rule, and you are convinced that those
       explanations will never become different.
5. [ ] Implement the method! For most category packages, there is a Rules file and a Plans file. Some packages contain
       too many plans so this file is split logically into more specific files. You need to decide which one the new
       method set belongs to (or create a new file).
6. [ ] If the method has variants, then you should create a new setting in the `engine.context.Setting` enum class.
       Most settings are boolean but settings with more values can be created (see `Setting.kt`). You should then
       decide what value of the setting each preset should include. Presets are defined in the enum class
       `engine.context.Preset`.

The conventional way to create a new instance of a `Rule` object is to do so in a `rule` block. First you set up the
pattern in successive variable declarations, then you use the `onPattern` method of the RuleBuilder to describe the
transformation. You can do computations inside the code block, and in the end you have to return either `null`, if the
rule does not apply despite the pattern matching, or create a result using the `ruleResult` method (with the resulting
mapped expression, explanation, and skills) if it does apply.

### Adding a rule

To make the rule behave well when used with Graspable Maths, you can also `gmAction` parameter.

Example:

```kotlin
val evaluateSignedIntegerAddition = rule {
    // Define the patterns that are required for the rule
    val term1 = SignedIntegerPattern()
    val term2 = SignedIntegerPattern()
    val sum = sumContaining(term1, term2)

    // This registers the pattern that we want to detect and the code to execute
    onPattern(sum) {
        val explanation = when {
            !term1.isNeg() && term2.isNeg() ->
                metadata(Explanation.EvaluateIntegerSubtraction, move(term1), move(term2.unsignedPattern))
            else ->
                metadata(Explanation.EvaluateIntegerAddition, move(term1), move(term2))
        }

        // This creates a Transformation that is the outcome of the rule (here the two integers are added together)
        ruleResult(
            toExpr = sum.substitute(integerOp(term1, term2) { n1, n2 -> n1 + n2 }),
            gmAction = drag(term2, PM.Group, term1, PM.Group),
            explanation = explanation,
        )
    }
}
```

### Adding a plan

Plans are created using the `plan` block, they may have a pattern to check that the input expression is of the right
form, a result pattern to check that the required output form was achieved, explanation, and skills, just like Rules.
The real work inside a plan is done by one of the many plan executors:

Example:

```kotlin
val simplifyIntegersInProduct = plan {
    // This sets the pattern that triggers execution of the plan (here, any sum).
    pattern = optionalNegOf(productContaining())

    // This sets the explanation key to use when generating the explanation for the plan.
    explanation = Explanation.SimplifyIntegersInProduct

    // This optionally sets the parameters of the explanation.
    explanationParameters(pattern)

    // The steps block defines a pipeline of methods that are used sequentially
    steps {
        whilePossible {
            firstOf {
                option(GeneralRules.EvaluateProductDividedByZeroAsUndefined)
                option(GeneralRules.EvaluateProductContainingZero)
                option(IntegerArithmeticRules.EvaluateIntegerProductAndDivision)
                option(GeneralRules.RemoveUnitaryCoefficient)
            }
        }
    }
}
```

The example above uses `whilePossible` and `firstOf` builders, which are only two of many operations that are defined
for pipelines. To see the whole list, consult the `engine.methods.stepsproducers.PipelineBuilder` interface. There
are also many examples of plans across the different packages in the `methods` module.

The main operations are

- `apply`: compulsorily apply a method
- `optionally`: optionally apply a method
- `whilePossible`: repeatedly apply a method until it fails to apply
- `deeply`: apply a method to the first matching subexpression
- `firstOf`: defines a list of alternative methods that can be attempted, the first successful one will be applied

### Adding a task set

Using a task is necessary when computing the solution requires doing "side calculation", in other words we can't arrange
it neatly into a pipeline of sequential steps.

Example.

The example below defines a task that turns `a + b sqrt[c]` into a square of the form `(x + y sqrt[c])^ 2` if possible.
It does this by creating an equation system and solving it to find `x` and `y`, then putting the values back into
the expression to give the solution. It's clear that solving the equation system is a "task" because it's not a
transformation of the original expression.

```kotlin
val writeIntegerPlusSurdAsSquare = taskSet {
    explanation = Explanation.WriteIntegerPlusSquareRootAsSquare
    val integer = UnsignedIntegerPattern()
    val radicand = UnsignedIntegerPattern()
    val root = withOptionalIntegerCoefficient(squareRootOf(radicand))

    // a + b sqrt[c]
    pattern = commutativeSumOf(integer, root)

    tasks {
        // E.g. 11 - 6sqrt[2]

        // Task 1 - solve for x and y
        // (x + y sqrt[2])^2 = 11 - 6sqrt[2]
        // --> x^2 + 2y^2 + 2xy sqrt[2] = 11 - 6 sqrt[2]
        // --> x^2 + 2y^2 = 11 AND 2xy sqrt[2] = -6 sqrt[2]
        // --> x^2 + 2y^2 = 11 AND xy = -3
        // Now guess two numbers that multiply to -3
        // --> x = 3 AND y = -1

        val xVar = Variable("x")
        val yVar = Variable("y")

        val squareInXAndY = powerOf(sumOf(xVar, productOf(yVar, squareRootOf(get(radicand)))), Constants.Two)

        // The task(...) method creates a new task and attempts to execute it.
        val findXAndY = task(
            startExpr = equationOf(squareInXAndY, expression),
            explanation = metadata(
                Explanation.WriteEquationInXAndYAndSolveItForFactoringIntegerPlusSurd,
                squareInXAndY,
            ),
            context = context.copy(solutionVariables = listOf("x", "y")),
            stepsProducer = findXAndYSteps,
        ) ?: return@tasks null

        // Task 2 - substitute x= 3 and y = 1 into (x + y sqrt[2]) ^ 2
        // (3 - sqrt[2]) ^ 2

        // The result (final expression) of a successful task is found in task.result
        val solution = findXAndY.result
        val x = solution.firstChild.secondChild
        val y = solution.secondChild.secondChild

        val square = powerOf(sumOf(x, simplifiedProductOf(y, squareRootOf(get(radicand)))), Constants.Two)

        task(
            startExpr = square,
            explanation = metadata(Explanation.SubstituteXAndYorFactoringIntegerPlusSurd),
        )
        allTasks()
    }
}
```

There are many examples of tasks in the `methods` module, some can get quite complicated. Once way to organise a
complex task is to create a class or an object, see `methods.equationsystems.SystemSolver` for such an example.
