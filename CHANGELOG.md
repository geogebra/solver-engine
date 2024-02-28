# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

An entry should be added to the _[Unreleased]_ section when committing to main. When a release is made we move its
contents to a new release section (e.g. _[0.x]_).

## [Unreleased]

### Added

- Added support for solving simple exponential equations (PLUT-990)

### Fixed

- SDK: Fixed rendering of contradictions with no variables (PLUT-773)

### Changed

### Removed

## [1.4.3] 2024-02-6

### Added

- Collect like terms containing symbolic constants when rearranging equations (PLUT-980)
- Return intersection data in graphing API when the request if e.g. x + 1 = x^2 (PLUT-988)
- SDK: support for intersection object returned by graphing API (PLUT-988)

### Fixed

- Fixed problem with multivariate equations with constraints reporting no solution (PLUT-965)
- Fixed problem with multivariate equations with domain restrictions reporting no solution (PLUT-973)
- SDK: Fixed missing parentheses in products with negative factors in generated expressions (PLUT-984)
- SDK: Updated npm dependencies to remove some vulnerabilities (PLUT-991)

## [1.4.2] 2024-01-16

### Added

- Added copyright notice to engine, poker and SDK source files (PLUT-929)
- Added license headers for scripts (PLUT-960)
- Added license headers for gradle build files (PLUT-961)
- SDK: Added `Grapher` interface to SDK, with a `GgbApplerGrapher` implementation that knows how to draw a graph using a
  ggb applet (PLUT-944)

### Fixed

- SDK: Fixed error in `evalCommnandGetLabels` signature (PLUT-963)

### Changed

- Upgraded Kotlin to 1.9.22, ktlint to 1.10, detekt to 1.23.4, and a list of related dependencies. The ktlint upgrade
  means a style change to a lot of code (PLUT-952)
- Upgraded openapi-generator to version 7.2.0 (needed to upgrade to spring-boot v3) (PLUT-955)
- Upgraded spring-boot to version 3.2.1 (needed to be able to use java 21) (PLUT-956)
- Upgraded runtime JVM and JVM compilation target from 17 to 21, which is the new LTS version (PLUT-954)
- Moved `@geogebra/solver-sdk` to `/solver-sdk` (PLUT-959)

## [1.4.1] 2023-12-22

### Changed

- Normalize polynomials to a stricter standard form (fractional coefficients in front of variables) (PLUT-943)
- gmAction for adding a fraction and a non-fraction

## [1.4] 2023-12-21

### Added

- SDK: Added support for entering LaTeX systems of equations separated by '\text{AND}', ';', ',' (PLUT-717)
- Add solver syntax for inverse trig functions (e.g., `[sin^-1] x`) and power of trig functions (e.g., `[tan^2] x`)
  (PLUT-715)
- SDK: add latex rendering for inverse trig functions (e.g., `sin^-1 x`) and power of trig functions (e.g., `tan^2 x`)
  (PLUT-715)
- Added support for simplifying the sum of non-constant fractions, such as `[x + 1 / 2] + [3x / 5]` (not applied when
  solving equations) (PLUT-680)
- Optimization of deeply: added machinery for avoiding searching too deep into expressions when the plan applied
  requires a deep expression (PLUT-831)
- Added gmAction information to factoring plans (PLUT-832)
- Added simplification of fractions containing a sum with a common integer factor, such as `[2 + 4sqrt[2] / 2]`
  (PLUT-821)
- SDK: Localization of math words (e.g. 'and', 'or', 'true', 'false', ...) (PLUT-707)
- Added solver engine support for "advanced balancing" (PLUT-845)
- SDK: Allowed setting `advancedBalancing` flag in SDK (PLUT-845)
- Performance: added caching to deeply calls (PLUT-854)
- Added formulas to certain transformations (PLUT-694)
- Add rules to simplify cancellation of root with any base and absolute power (PLUT-852)
- Added an easy to extend settings system to give users granular control over the steps of the output (PLUT-796).
- Added the `/settings` and `/presets` endpoints to the API (PLUT-796).
- Added the `QuickAddLikeFractions` setting to sum like integer fractions in one step (PLUT-796).
- Added ability to simplify a square root of `a +/- b sqrt[c]` when that can be written as a square (PLUT-582)
- Added `CopySumSignsWhenDistributing` setting for gm-alignment (PLUT-798)
- Provide framework for writing new style gmActions tests (PLUT-884)
- Added the `MoveTermsOneByOne` setting for gm-alignment (PLUT-802)
- Added new gmActions tests that use the new testing framework, disabled all old gmAction tests (PLUT-888)
- Added the `MultiplyFractionsAndNotFractionsDirectly` setting for gm-alignment (PLUT-885). Multiplies
  e.g. `2 * [3 / 4]` to `[2 * 3 / 4]` in one step
- Added the `/graph` API, which returns a description of a graph that can be drawn from an input (PLUT-912).
- SDK: Added support for the `/graph` API in SDK and conversion of tree format to ggb format in SDK (PLUT-912)
- SDK: Added support for generating math problems for various topics, subtopics and difficulty levels (PLUT-911)
- Added new `EliminateNonZeroFactorByDividing` setting to eliminate non-zero factors in equations = 0 in steps (
  PLUT-942)

### Fixed

- `ComputeDomainOfAlgebraicExpression` plan isn't shown when `ComputeDomainAndSimplifyAlgebraicExpression` plan can be
  shown (e.g., for `[3 * 2 / (x - 1)(x - 2)]`) (PLUT-708)
  - SDK: Fixed LaTeX representation of equation systems (PLUT-717)
- Fixed rational equations which can be reduced to a polynomial one just by simplifying and expanding,
  e.g. `([1/x]+2)x=[2/x]x` (PLUT-703)
- Improved thread-safety of deeply optimization (PLUT-835)
- Improved handling of negatives in sums by the SDK (PLUT-832)
- Fixed bug in `productSignRequired` that led to incorrect warnings in parser for divisions (PLUT-841)
- SDK: Fixed LaTeX representation of addition/subtraction of equations in equation system (PLUT-848)
- Fix computation of lcd and addition of fractions with additive inverse denominators (PLUT-706)
- SDK: Fixed ÷ path map issue (PLUT-864)
- Multiply by denominator of variable on LHS when not a constant (PLUT-711)
- SDK: Fixed \* and ÷ path issues (PLUT-888)
- Improved handling of multiplying inverse fractions `[2/3]*[3/2]` (PLUT-894)
- Improved explanations for multiply by LCD for single denominator (PLUT-894)
- Fixed regression in solving linear equations (PLUT-923)
- Made graph API rearrange inequalities as well as equations (PLUT-926)
- Fixed several Solver <-> GM path mapping issues (PLUT-897)

### Changed

- The Spring configurations have been changed to exclude null values from the returned objects (PLUT-692)
- SDK: the Transformation type has been changed to reflect the optional fields (PLUT-692)
- Speed up method execution when it doesn't contain decimals (PLUT-836)
- Simplification of roots of integers and roots of powers of integers have been improved (PLUT-705)
- (PLUT-864) switch SDK tests to use gmath-3.0.0, which adds or improves GM alignment for
  - CancelCommonTermsOnBothSides
  - TakeRootOfBothSidesRHSIsZero
  - TakeRootOfBothSides
  - RewriteFractionOfPowersWithSameExponent
  - RewriteProductOfPowersWithSameExponent
  - FractionArithmeticRules
  - NegateBothSides
  - x^0, 1^x, 0^x
- Replaced curricula and the GM friendly flag with presets, which are a combination of various settings (PLUT-796)
- SDK: Adapted SDK and the poker to use the settings and presets returned by the engine (PLUT-796)
- Renamed the `AdvancedBalancing` flag to `BalancingMode` and changed the options to `basic`, `advanced` and
  `nextTo` (PLUT-802)
- Changed the removal of the coefficients on the left hand sides of equations and inequalities for better compatibility
  with Graspable Math (PLUT-797)
- Split GmFriendly preset into GmFriendly and GmFriendlyAdvanced (PLUT-888)
- Changed setting ReorderProductsInSteps into CommutativeReorderInSteps and made it cover step by step normalization of
  polynomials (PLUT-888)
- improved FractionArithmetic rules for signs in fractions (`-[-2/3]` and `-[2/-3]`) (PLUT-888)
- SDK: don't consider a "Rearrangement" step a trivial step
- SDK: the "simple" solution formatter now shows "no solution" or "infinitely many solutions" messages for
  contradictions and identities (PLUT-899)

### Removed

## [1.3.1] 2023-10-23

### Added

- Added a checkbox to Poker to toggle between solver and JSON math formats (PLUT-691)
- Added support for multivariate linear inequalities, such as `3a + 2b > 9` (PLUT-693)
- Added support for solving multivariate equations for one variable which is a power e.g. `m[x^2] = 3z + 1` (PLUT-674)
- Added some project settings for when using vscode, instead of ignoring those files in
  git. (PLUT-598)
- SDK: add latex rendering for greek-symbols and trigonometric functions (PLUT-714)
- SDK: add latex rendering for natural-log, log base 10 & general log functions (PLUT-805)
- SDK: add support for specials symbols i.e. `%, pi, exponentialE, iota` (PLUT-822)
- Added support for parametric quadratic equations (where coefficents are not constant) (PLUT-807)
- Added ability to simplify a square root with a square factor (e.g. `sqrt[4x - 8]`) (PLUT-808)

### Fixed

- Simplify brackets in polynomial expression deep-first (PLUT-588)
- Avoid multiplying a product by 1, instead eliminate the factor of 1 (PLUT-670). Previously this wasn't done except in
  the simplest cases.
- SDK: Fixed gm action tests by improving action handling in GM. (SP-222), (SP-239)
- SDK: Fix regression, undefined `operands` value of emptyset is parsed by latex parser without error (PLUT-695)
- SDK: Fixed the use of :op vs :op() when translating solver paths into GM terms
- SDK: Improved accuracy of type information (SP-237)
- Fixed missing product sign in expressions with missing brackets (e.g. `3*-x`) (PLUT-824)
- SDK: fixed actor selection for GM actions involving adjacent actors (PLUT-806)

### Changed

- Improved steps for expanding expressions like `(sqrt[2] + 1)(-2)` and `(x + 1)(-2)` (PLUT-478)
- SDK: Switched from allow-list to skip-list for GM action tests. (SP-222)
- Simplifying a rational equation to a polynomial equation has been rewritten to use plans instead of tasks (PLUT-701)
- Changed Poker to use Vue (SP-237) (PLUT-803)
- Converting a rational equation to a polynomial equation and solving the polynomial equation have been merged to
  a single task (PLUT-702)
- The `SolveInequationInOneVariable` plan was renamed to `SolveInequation` and now it can bring multivariate
  inequations to a simpler form (e.g. `-3ab^2 != 0` to `ab^2 != 0`) (PLUT-679)

### Removed

## [1.3] 2023-09-25

### Added

- Added support for simplifying rational expressions, without considering domain restrictions (PLUT-637)
- Added support for computing the domain of rational expressions (PLUT-638)
- Added support for variables with subscripts (PLUT-656)
- SDK: added support for displaying variables with subscripts and parsing such constructs from LaTeX (PLUT-656)
- Fallback plan for expressions which are fully simplified or quadratics with negative discriminant (PLUT-603, PLUT-624)
- SDK: added support for Void expression - this would need explicit handling in front-ends (PLUT-603)
- Added support for simplifying and expanding multivariate polynomial expressions (PLUT-651)
- Added support for simplifying multivariate algebraic expressions (PLUT-667)
- Solve rational equations implementation (PLUT-593)
- Added support for equations simplifying to undefined (PLUT-652)
- Added ExpressionList to represent a variable number of parameters in explanations (PLUT-663)
- Performance improvement via context-local caching of plans, adding guards to some methods and deduplicating the use of
  deeply (PLUT-677)
- Tools for profiling performance of queries, see [docs/profiling.md](docs/profiling.md) for details (PLUT-677)
- Equation solving has been extended to support multivariate linear equations (PLUT-672)

### Fixed

- SDK: Updated package.json for better compatibility
- Show `EvaluateExpressionAsDecimal` only when Decimal/Fraction/DivideBy is present in expression (PLUT-631)
- Add reciprocal power rule i.e. `[1/a^n] = a^-n` and collect integers with rational exponents (PLUT-625)
- Make sure leading factor of 1 is not removed when followed by division sign and relax rule for turning divisions into
  fractions (PLUT-664)
- Fix parsing the variable 'd' (PLUT-671)
- SDK: fixed missing `:group` annotations in fractions for `createPathMap` (SP-216)
- fixed gmActionInfo of several rules (e.g., select `+2` instead of `2` when dragging to add in `1+x+2`) (SP-216)
- temporary fix for PartialBrackets + SmartProduct bug (SP-216, PLUT-684)
- Simplification of -ve base root power be done by "SimplifyConstantExpression" (PLUT-682)
- Fix regression: SolveEquation plan should solve rational equations well (PLUT-689)

### Changed

- Improved factoring common factors to support powers of arbitrary expressions and sum factors with terms in a
  different order, e.g. `3 (x + 1)^3 + 6 (1 + x)^2` (PLUT-647)
- Improved the simplification of coefficients after expanding (PLUT-630)
- When a sum is substituted into a sum it is inlined instead of being wrapped in a bracket (PLUT-621)
- Changed the conversion of mixed numbers to improper fractions, the transformations are no longer executed "in step"
  on the two mixed numbers, rather in turn (PLUT-621)
- Changed polynomial simplification to normalize polynomials at the end also in GM friendly mode (PLUT-650)
- SolveDecimalLinearEquation is now only executed if the input contains a decimal and the output can be expressed
  without fractions (PLUT-660)
- Make it easy to substitute sub-step nodes into the parent step expression (PLUT-658). For details see JIRA ticket.
- SDK: Parse latex/ascii `3 x/2 x` as `3 [x/2] x`. Also, parse latex/ascii `(1+2)/x` as `[1+2 / x]` (SP-133)
- Simplification plans now only apply to expressions, not equations. The equation solving plan returns a simplified
  version of the equation if possible even when it can't solve the equation (PLUT-661)
- Simplify rational exponent of integer (e.g. `[25^-[1/2]]` to `[1/5]`) and its product with integer
  (e.g. `27*[3^-[1/3]]` to `9*3^[2/3]`) (PLUT-669)
- Improved factoring and expanding equations so examples like `([(x + 1)^2] + 1)([(x + 1)^2] + 2) + 3 = 0` can be
  solved using completing the square (PLUT-641)
- SDK: simplified logic of `solverPathToGmNodes` method (SP-216)
- SDK: simplified logic of `substituteTree` to always use the substitute's decorators (SP-216)
- Refactored and expanded the gm-action tests (SP-216)
- The `SolveEquationInOneVariable` plan has been renamed to `SolveEquation` (PLUT-672)
- SDK: made the type information of the return types of `applyPlan` and `selectPlans` more specific (PLUT-688)

### Removed

- SDK: Removed support for old Json format from the SDK (PLUT-628)
- Removed support for old Json format from the engine (PLUT-629)
- the `SolveRationalEquation` plan is no longer public (it has been rolled into `SolveEquationWithOneVariable` as part
  of PLUT-677)
- SDK: Removed the `API_APPLY_PLAN_RESPONSE` and `API_SELECT_PLANS_RESPONSE` TS types (PLUT-688)

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
- Fixed equations solvable by applying roots method followed by quadratic formula (PLUT-657)

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
