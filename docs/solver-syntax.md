# Solver Syntax

## General principles

The aim of the solver syntax is to be able to represent a mathematical expression as an ASCII string

- in a readable way
- with a notation that approaches mathematical notation if possible
- in such a way that the expression can be written with no ambiguity in conventional mathematical notation

Use cases for solver syntax

- tests
- the poker
- sending unambiguous requests to the API

## The syntax

### Arithmetic

The four arithmetic operations are noted with the signs

- `+`, `-` for addition and subtraction have the same precedence
- `*`, `:` for multiplication and division have the same precedence (higher than `+` and `-`)

E.g. `1 + 2 - 3 * 4 + 10 : 5`

Negation is noted with a prefix `-`: e.g. `-3`

Grouping with brackets can be done with the following

- Round brackets: `3 - (1 + 1)`
- Square brackets: `3 - [. 1 + 1 .]`
- Curly brackets: `3 - {. 1 + 1 .}`

### Variables and subscripts

Single letter variables (lower and uppercase) are written verbatim

- `x`
- `xy` means `x * y`

Variables can have a subscript which is a sequence of digits or a letter (including a greek letter)

- `x_0`
- `y_12`
- `\alpha_0`
- `\Delta_n`

Note that `x_{n+1}` is not supported

### Implicit product

In some places two expression one after another are interpreted as the "implicit product"
of these expressions

E.g.

- `3x`
- `xyz`
- `3sqrt[2]`

Implicit product has higher precedence than explicit product or division. E.g.

E.g.

- `3x : 3y` means `(3x)/(3y)`

### Fractions

A fraction is written as `[x / y]`.

### Mixed numbers

A mixed number with whole part a, numerator b and denominator c (all natural numbers) is written as `[a b / c]`.

### Roots

- square root of a: `sqrt[a]`
- n-th root of a: `root[a, n]`

### Absolute value

The absolute value is written as `abs[x]`.

### Percentages

- 10% `10%`
- 15% of 45 `15 %of 45`

### Logarithms / exponentials

- a to the power b: `[a ^ b]` <-- [TODO] should this be changed e.g. `a^[b]`
- Natural logarithm: `ln`
- Base-10 logarithm: `log`
- Logarithm in a base b: `log[b]`

### Trigonometric functions:

- `sin`, `cos`, `tan`, `arcsin`, `arccos`, `arctan`
- `sec`, `csc`, `cot`, `arcsec`, `arccsc`, `arccot`
- `sinh`, `cosh`, `tanh`, `arsinh`, `arcosh`, `artanh`
- `sech`, `csch`, `coth`, `arsech`, `arcsch`, `arcoth`

These are used as e.g. `cos x` without any curly brackets. Of course the rules of precedence mean that
we write `cos(x + 1)`. If we required square brackets (which by convention do not correspond to any conventional sign),
then we would have to write `cos[x]` and `cos[(x + 1)]` resulting in more noise and no less ambiguity.

### Mathematical constants

- Euler's constant e: `/e/`
- Circle constant pi: `/pi/`
- Imaginary unit i: `/i/`

By default the symbols `e`, `\pi` and `i` are interpreted as variables. If you mean the constants these represent,
you have to use the special syntax. Later on we will have a system that can turn `e`, `\pi`, and `i` to `/e/`,
`/pi/`, and `/i/` respectively, based on the context. The rules and plans themselves will never turn one into
the other.

### Greek letters

- `\alpha`, `\beta`, ..., `\omega`
- `\Alpha`, `\Beta`, ..., `\Omega`

### Calculus

- First derivative: `diff[sin(x) / x]`
- N-th derivative: `[diff ^ n + 2][sin(x) sin(y) / x y [x ^ n]]`
- Indefinite integral: `prim[sin x, x]`
- Definite integral: `int[0, /pi/, sin x, x]`

### Vectors

- `vec[x, y, z]`

### Matrices

- `mat[1, 0 // 0, 1]`
