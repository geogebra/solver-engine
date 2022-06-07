# Solver Engine

This project hosts the code for the solver engine.

## Overview of the structure

### Representation

**Operator** // TODO

**Expression** // TODO

**Pattern** // TODO

**Expression maker** // TODO

**Transformation** is the data structure which stores the 
generated solution. It consists of the original and resulting
expressions, an explanation of the change in the current step,
the skills involved and a list of substeps which are themselves
transformations.

### Transformation producers

**Rule** is the simplest transformation producer. It creates a
transformation with explanation and potentially with associated
skills, but with no substeps. It transforms the input which
matches its pattern into an equivalent expression, but it does 
not know when it should apply.

**Plan** is the top and mid-level transformation producer. 
It may have an explanation and skills, just like rules, but the
actual work is done by one of the many possible steps 
producers:
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

Rules are combined into simpler plans, which are in turn combined 
into more and more complex plans to achieve at the top level 
plans which are able to solve the required problems.