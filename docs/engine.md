# Engine

**Expression**s are the objects the system manipulates. They represent
mathematical constructs such as numeric values, variables, sums,
fractions, equations, etc. The `engine.expressions` package contains
the definition of the `Expression` type and a number of convenience
methods to create a variety of expressions.

**Pattern**s are used to check if an expression is of a given shape
and that it satisfies certain conditions. It is also used to grab
subexpressions to be manipulated. Think of it as a regexp, but for
tree structures. Just like for Expressions, there is a number of
convenience methods for creating patterns in `engine.patterns`.

A **Path** represents a position in an expression tree. E.g. "./0/2"
means the second child of the zeroth child of the root expression
(zero indexed). Internally they are represented by a singly linked
list.

**Mapped Expression**s represent the result of a transformation.
They contain the data of an expression together with information about
the origin of the different parts of that expression. For example,
in the transformation `1 + 2 * 3 -> 1 + 6`, the value `6` in the
result expression is obtained by combining the `2` and the `3` from the
initial expression. This can be represented in the mapped expression
with a **path mapping** `(./1/0, ./1/1) -> ./1` (`./1/0` being the path
of `2`, `./1/1` the path of `3` in the initial expression and `./1` the
path of `6` in the resulting expression).

The **Transformation** is the data structure which stores the
generated solution. It consists of the original and resulting
expressions, an explanation of the change in the current step,
the skills involved and a list of substeps which are themselves
transformations.
