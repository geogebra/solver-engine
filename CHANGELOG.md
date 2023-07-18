# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

An entry should be added to the _[Unreleased]_ section when committing to main. When a release is made we move its
contents to a new release section (e.g. _[0.x]_).

## [1.2] - 2023-07-18

### Added

- Added support for solving modulus equations of the form |f(x)| = k and derived (PLUT-589)
- Added numerical evaluation for constant expressions if symbolic simplification fails (PLUT-597)
- Added support for solving modulus equations that reduce to the form |f(x)| = k|g(x)| (PLUT-599)
- Added new syntax's to the parser (mathematical constants, Greek letter variables,
  trigonometric functions, percentages, derivatives, integrals, vectors, matrices) (PLUT-288)
- Add rule `|-x| = |x|` for any `x` (PLUT-607)
- New test reporter to see global effect of code changes on test output (PLUT-506)
- Add equation addition and subtraction support in SDK (PLUT-561)
- Added support for solving modulus equations of the form |f(x)| = g(x) and derived (PLUT-600), including a specific
  method for US (PLUT-614)
- Added new `json2` serialisation format for math in engine (PLUT-626)
- SDK: added support for new `json2` serialisation format for math (PLUT-627)
- Added support for inequalities containing absolute values (PLUT-559)
- SDK: Add support for inequality system and different range
  (OpenRange, OpenClosedRange, etc.) objects (PLUT-559)
- SDK: added support for new `json2` serialisation format for math (PLUT-627)
- New `SolveEquationInOneVariable` plan replaces all the individual plans. Instead, the returned solution contains
  alternatives. A certain type of solution can be requested by setting the preferred strategy (PLUT-618)
- SDK: Add LaTeX to solver tree support for absolute value as `\\left|`, `\\right|` pair or pipe symbol (i.e. `|`) (
  PLUT-606)
- Added the set subtraction operator to represent solutions with holes, e.g. `R \ {1, 2}` (PLUT-639)
- Added support for solving inequations, i.e. statements with the `!=` operator, either directly or by
  transforming them to equations first and taking the complement of the solutions (PLUT-639)
- SDK: added support for rendering the set subtraction operator and rendering solutions with holes, e.g. `R \ {1, 2}`
  as `x != 1 and x != 2` (PLUT-639)
- SDK: exported another call signature for `getInnerSteps`, exported `substitute` function (SP-82)
- Added support for solving constant equations, inequations and inequalities (PLUT-611)

### Fixed

- Improve path mappings for better visualization (using colors) in GeneralRules.kt (PLUT-514)
- Improve path mappings for better visualization (using colors) in normalization rules (PLUT-520)
- Fixed the simplification of `sqrt[2] + [7 / 2] - 1` (index bug in partial expression plan) (PLUT-613)
- Improve handling negative sign with product as first & non-first term in sum (PLUT-249)
- Fixed some `gmAction` integration tests (SP-72)
- Better order of operations for EvaluateExpressionAsDecimal - multiplications and divisions are now done before
  additions (PLUT-578)
- SDK: Ignore text style commands when parsing latex (PLUT-636)
- Improve order of operations when evaluating constant expressions with absolute values (PLUT-620)
- Absolute value of non-negative value is the non-negative itself (PLUT-623)

### Changed

- SDK: Changed default API URL to production engine (PLUT-590)
- Limit precision to be between 2 and 10 decimal places (PLUT-605)
- Improved parameters for some explanations (PLUT-563)

### Removed

- The legacy Solution operator was replaced by the more descriptive SetSolution, Contradiction and
  Identity operators (PLUT-587)
- All public plans for solving equations are consolidated into one plan (`SolveEquationInOneVariable`) which uses
  alternative solutions to show different methods (PLUT-618)

## [1.1] - 2023-05-10

### Added

- Added CHANGELOG.md (this file) (PLUT-555).
- Ability to collect example inputs for a given explanation key (PLUT-569)
- All requests for a solution are logged, together with solution and timing (PLUT-548)
- Added support for the absolute value operator and rules for resolving it in simple cases (PLUT-557)
- Referring to operators and decorators in path mappings (PLUT-539)
- Now some steps may have the "InvisibleChange" tag, such as the step to turn `[1/2] + [1/3] + 7`
  into `<. [1/2] + [1/3] .> + 7` (PLUT-577)
- Expressions can now have a label (called "name" internally) - used in equation systems (PLUT-566)
- Added partial products, similar to partial sums. (PLUT-581)

### Fixed

- Fixed typo in simple solution rendering (PLUT-568)
- Fixed package CI job by setting JVM back to 17 (PLUT-570)
- Set output of `EvaluateArithmeticExpression` plan to be an integer (PLUT-550)
- Solving by completing the square works on unexpanded equations (PLUT-571)
- Fixed quadratic equations with root coefficients by adjusting brackets
  in `Expression::replaceChildren` (PLUT-572)
- Fixed quadratic equations with constant term consisting of a sum by strengthening
  `QuadraticPolynomialPattern` (PLUT-572)
- Normalize quadratic equation before solving by quadratic formula (PLUT-579)

### Changed

- Converted the poker to TypeScript (PLUT-565)
- Reworked/improved rules and plans to normalize expressions (PLUT-551)
- Use GmPathModifier.FractionBar instead of GmPathModifier.Operator to
  select fraction bars in GmActions.
- We no longer use task sets focus on adding just the two fractions in a situation like `[1/2] + 4 + [1/3]`. Instead we
  use partial sums. When writing partial sums in solver notation, they are written with the `<.` and `.>` brackets. "
  Partial sum brackets" are invisible in LaTeX and invisible to the user. (PLUT-577)
- Introduce typed expressions such as Fraction and Power (PLUT-581)
- Change the representation of products to flat instead of the explicit / implicit structure we had before. (PLUT-581)
- Handle partial products without tasks, same as partial sums (PLUT-585)

## [0.x] - 2023-04-03

This is a template for a release section.

### Added

### Fixed

### Changed

### Removed
