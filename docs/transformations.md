## Transformations

A _transformation_ turns an expression (`fromExpr`) into another (`toExpr`). There are several types of transformation:

- `rule`: this is a "leaf" transformation, it doesn't have any substeps
- `rearrangement`: this transformation just rearranges `fromExpr` into an equivalent expression `toExpr`, where the
  change is only structural (e.g. extracting a partial sum from a sum)
- `plan`: a transformation which is made of a sequence of one or more steps (transformations) applying to a
  subexpression of the `fromExpr`
- `taskset`: a transformation which is made of a number of tasks. The outcome of the last task is the `toExpr` of the
  transformation.

A _task_ only appears in a transformation of type `taskset`. It consists essentially of two things:

- a `taskId` so that subsequent tasks can refer to this task
- a `startExpr` which is an expression built from the task set's `fromExpr` and the outcome of previous tasks in the
  task set.
- a chain of steps (transformations) which turns the `startExpr` into something else.

Given an input, the solver produce a transformation (or a set of transformations) that represent the "solutions" of the
problem.
Here is a simplified example for solving a quadratic equation

### Example 1: solving a quadratic equation by factorising

```yaml
type: plan
explanation: solve quadratic equation by factorising
fromExpr: 'x ^ 2 = x + 6'
toExpr: 'x = -2, 3'
steps:
  - type: rule
    explanation: Make RHS equal to 0
    fromExpr: 'x ^ 2 = x + 6'
    toExpr: 'x ^ 2 - x - 6 = 0'

  - type: plan
    explanation: Factorise LHS
    toExpr: '(x - 3)(x + 2) = 0'
    steps: [...]

  - type: taskset
    explanation: Solve each factor
    fromExpr: '(x - 3)(x + 2) = 0'
    toExpr: 'x = -2, 3'
    tasks:
      - taskId: 1
        explanation: Solve first factor
        startExpr: 'x - 3 = 0'
        pathMappings: ['./0/0 -> #1/0']
        steps:
          - type: plan
            explanation: Solve linear equation
            fromExpr: 'x - 3 = 0'
            toExpr: 'x = 3'
            steps: [...]

      - taskId: 2
        explanation: Solve second factor
        startExpr: 'x + 2 = 0'
        pathMappings: ['./0/1 -> #2/0']
        steps:
          - type: plan
            explanation: Solve linear equation
            fromExpr: 'x + 2 = 0'
            toExpr: 'x = -2'
            steps: [...]

      - taskId: 3
        dependsOn: [1, 2]
        explanation: Put solutions together
        startExpr: 'x = -2, 3'
        pathMappings: ['#1/1 -> #3/1/0', '#2/1 -> #3/1/1']
```

### Example 2: converting a recurring decimal to a fraction by solving equations

```yaml
type: taskset
explanation: Convert recurring decimal to fraction
fromExpr: 1.2[3]
toExpr: 37/30
tasks:
  - taskId: 1
    explanation: Write the equation for the recurring decimal
    startExpr: x = 1.2[3]
    pathMappings: ['. -> #1/1']

  - taskId: 2
    dependsOn: [1]
    explanation: Multiply equation by 10
    startExpr: x = 1.2[3]
    pathMappings: ['#1 -> #2']
    steps:
      - toExpr: 10x = 12.[3]

  - taskId: 3
    dependsOn: [1]
    explanation: Multiply equation by 100
    startExpr: x = 1.2[3]
    pathMappings: ['#1 -> #3']
    steps:
      - toExpr: 100x = 123.[3]

  - taskId: 4
    explanation: Solve the simultaneous equations
    dependsOn: [2, 3]
    startExpr: 10x = 12.[3], 100x = 123.[3]
    pathMappings: ['#2 -> #4/0', '#3 -> #4/1']
    steps:
      - toExpr: x = 37/30

  - taskId: 5
    explanation: Substitute x back into the expression
    startExp: 37/30
    pathMappings: ['#4/1 -> #5']
```

### Example 3: solving a quadratic trigonometric equation

```yaml
type: plan
explanation: solve by substitution
fromExpr: 'cos^2 x = sin x - 1'

steps:
  - toExpr: 'sin^2 x + sin x - 2 = 0'
  - type: taskset
    explanation:
    fromExpr: 'sin^2 x + sin x - 2 = 0'
    tasks:
      - taskId: 1
        explanation: define substitution
        startExpr: 't = sin x'

      - taskId: 2
        explanation: substitute t = sinx and solve for t
        dependsOn: [1]
        startExpr: 't^2 + t - 2 = 0'
        steps:
          - fromExpr: 't^2 + t - 2 = 0'
            toExpr: 't = -1, 2'

      - taskId: 3
        explanation: Find first solution for x
        dependsOn: [2]
        startExpr: sin x = -1
        steps:
          - toExpr: x = -π/2 + 2kπ

      - taskId: 4
        explanation: Find second solution for x
        dependsOn: [2]
        startExpr: sin x = 2
        steps:
          - toExpr: x \in {}

      - taskId: 5
        explanation: Put solutions together
        dependsOn: [3, 4]
        startExpr: x = -π/2 + 2kπ
```

### Example 4: solving equation with absolute values

TODO
