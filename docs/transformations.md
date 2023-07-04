## Transformations

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
