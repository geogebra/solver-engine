# Expression serialization

Mathematical expression can be serialized in different formats

- `solver` - the solver format which is human-readable, unambiguous, but hard to parse for a machine, used in tests
- `latex` - this makes it easy to display the expression, but it is hard to parse and can be ambiguous
- `json` - an array based format, no longer supported
- `json2` - a structured json easiest to parse for a machine, easiest to extend in the future

## Definition of the `json2` format.

Every expression is of the form

```json
{
  "type": "ExpressionType",
  "name": "expression name",
  "decorators": ["list", "of", "decorators"]
  // Extra fields
}
```

The `name` and `decorators` fields are optional. The extra fields depend on the type of the expression (see descriptions
in sections below).

- the value of `name` is an arbitrary string that is the label given to the expression (for example it can be the label
  `(1)` for an equation)
- the items in the `decorator` array specify decorators applied to the expression. Currently these are brackets. The
  list of decorator names is:
  - `RoundBracket`: `(...)`
  - `SquareBracket`: `[...]`
  - `CurlyBracket`: `{...}`
  - `MissingBracket`: a special decorator to denote that the expression should have brackets around it when displayed
    but none of the kind above have been specified. The solver should not produce expressions with those brackets
  - `PartialBracket`: a special decorator to group together some terms in a sum or factors in a product

Different types of expression will carry different additional fields as described in the following sections

### Numbers

This is the representation of `324`

```json
{
  "type": "Integer",
  "value": "324"
}
```

This is the representation of `55.0123`

```json
{
  "type": "Decimal",
  "value": "55.0123"
}
```

This is the representation of `22.3121212121...`

```json
{
  "type": "RecurringDecimal",
  "value": "22.3[12]"
}
```

### Variables

This is the representation of `x`

```json
{
  "type": "Variable",
  "value": "x"
}
```

### Custom "names"

Sometimes we want to put some text in an expression node, most notably as a parameter to an explanation. Say for
example that there is an explanation that goes `Simplify the equation %1` and that the equation is called `(1)`, we
could give the parameter this value

```json
{
  "type": "Name",
  "value": "(1)"
}
```

### Compound expressions

Most expressions are made by composing together other expressions e.g. `1 + 2` or `(3x)^2`. Each operation has a type
and there is an `operands` field for each sub-expression. For example, this is `1 + 2`:

```json
{
  "type": "Sum",
  "operands": [
    {
      "type": "Integer",
      "value": "1"
    },
    {
      "type": "Integer",
      "value": "2"
    }
  ]
}
```

This is the list of types

The following represent mathematical expressions (which have a value)

- `Sum`: n-ary, e.g. `1 + 2 + 3`
- `Plus`: unary, e.g. `+x`
- `Minus`: unary, e.g. `-x` - note that `1 - 2` is actually encoded as `1 + -2` (a sum whose second operand is
  a `Minus`)
- `PlusMinus`:unary, e.g. `+/- 2`. It means `+2` or `-2`.
- `SmartProduct`: n-ary, e.g. `3*2*x`. Note that this is a special case, discussed below
- `DivideBy`: unary, used to denote division by its operand - only in the context of a product - e.g. `x : y` is encoded
  as a product whose second operand is a `DivideBy`.
- `Fraction`: binary e.g. `1/3`
- `MixedNumber`: ternary, used to represent e.g. `2 1/3` ("two and a third). Operands are always positive integers
- `Power`: binary, e.g. `2^n`
- `SquareRoot`: unary, e.g. `sqrt(3)`
- `Root`: binary, e.g. "4th root of n" - first operand is the radicand, second is the order of the root.
- `AbsoluteValue`: unary, e.g `|x - 1|`

The following represent mathematical statements (which can be true or false)

- `Equation`: binary, e.g. `3x + 1 = y`
- `EquationSystem`: n-ary, system of n equations
- `AddEquations`: binary, sum of equations e.g. `(2x + y = 3) + (3x - y = 2)` used e.g. when solving by elimination
- `SubtractEquations`: binary, see `AddEquations`
- `EquationUnion`: n-ary e.g. "2x = 3 OR x^2 = 1"
- `StatementWithConstraint`: binary e.g. "x^2 = 4 GIVEN x > 0" - similar to `EquationSystem` but different intent
- `LessThan`: binary, e.g `1 < 2`
- `GreaterThan`: binary, e.g. `1 > 2`
- `LessThanEqual`: binary, e.g. `x <= 3`
- `GreaterThanEqual`: binary, e.g. `x >= 3`

The following represent different types of sets

- `FiniteSet`: n-ary, e.g. the set `{1, 2, 3}`
- `CartesianProduct`: n-ary, e.g. `{1, 2} * R`
- `OpenInterval`: binary, e.g. `(3, 4)`
- `ClosedInterval`: binary, e.g. `[5, 7]`
- `OpenClosedInterval`: binary, e.g. `[-1, 7)`
- `ClosedOpenInterval`: binary, e.g. `(-inf, 0)`

The following represent solutions to a problem. They are all binary and the first operand is a variable list (the "
domain" of the solution)

- `Identity`: represents a solution set where all values are solutions
  - second operand must be a statement
  - the expression states the statement is true for all values of the variables
- `Contradiction`: same as `Identity` but the solution set is empty instead
- `ImplicitSolution`: represents a solution expressed "implicitly" by an equation which is the second operand. This can
  be used to represent e.g. the set of `x, y` such that `x^2 + y^2 = 1`
- `SetSolution`: represents a solution in the form of a set. E.g. the set of all `x` in `[1, 5)`

Finally, these are used in some of the expressions above, but are not really expressions by themselves

- `VariableList`: n-ary list of variables, used in solutions - e.g. `x, y, z`
- `Tuple`: n-ary, used in sets, e.g `(1, 2)`

## Special case of `SmartProduct`

Products have a special treatment as there are several ways you can write a product: with or without a sign, e.g.
`xyz` or `x * yz` or `x * y * z`, etc. Products have an extra field named `signs` with the same length as `operands`,
each being a boolean that indicates whether there should be a sign in front of the operand

E.g. `xyz` is

```json
{
  "type": "Product",
  "operands": [
    {
      "type": "Variable",
      "value": "x"
    },
    {
      "type": "Variable",
      "value": "y"
    },
    {
      "type": "Variable",
      "value": "z"
    }
  ],
  "signs": [false, false, false]
}
```

Whereas `x * yz` is

```json
{
  "type": "Product",
  "operands": [
    {
      "type": "Variable",
      "value": "x"
    },
    {
      "type": "Variable",
      "value": "y"
    },
    {
      "type": "Variable",
      "value": "z"
    }
  ],
  "signs": [false, true, false]
}
```

Note that the first item in `signs` must always be `false`. It could be omitted but at the cost of a bit less clarity.
