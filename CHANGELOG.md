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

### Fixed

- Fixed typo in simple solution rendering (PLUT-568)
- Fixed package CI job by setting JVM back to 17 (PLUT-570)
- Set output of `EvaluateArithmeticExpression` plan to be an integer (PLUT-550)
- Solving by completing the square works on unexpanded equations (PLUT-571)

### Changed

- Converted the poker to TypeScript (PLUT-565)
- Reworked/improved rules and plans to normalize expressions (PLUT-551)
- Use GmPathModifier.FractionBar instead of GmPathModifier.Operator to
  select fraction bars in GmActions.

### Removed

### [0.x] - 2023-04-03

This is a template for a release section.

### Added

### Fixed

### Changed

### Removed
