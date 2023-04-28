# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

An entry should be added to the _[Unreleased]_ section when committing to main. When a release is made we move its
contents to a new release section (e.g. _[0.x]_).

### [Unreleased]

### Added

- Added CHANGELOG.md (this file) (PLUT-555).
- Ability to collect example inputs for a given explanation key (PLUT-569)
- All requests for a solution are logged, together with solution and timing (PLUT-548)
- Added support for the absolute value operator and rules for resolving it in simple cases (PLUT-557)
- Referring to operators and decorators in path mappings (PLUT-539)
- Now some steps may have the "InvisibleChange" tag, such as the step to turn `[1/2] + [1/3] + 7` into `<. [1/2] + [1/3] .> + 7` (PLUT-577)

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
- We no longer use task sets focus on adding just the two fractions in a situation like `[1/2] + 4 + [1/3]`. Instead we use partial sums. When writing partial sums in solver notation, they are written with the `<.` and `.>` brackets. "Partial sum brackets" are invisible in LaTeX and invisible to the user. (PLUT-577)

### Removed

### [0.x] - 2023-04-03

This is a template for a release section.

### Added

### Fixed

### Changed

### Removed
