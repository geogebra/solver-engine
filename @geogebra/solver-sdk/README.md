# GeoGebra Solver SDK

This library provides tools and convenience functions for applications that want
to use the GeoGebra Solver API.

To use this library, you can include it as an npm package from a local path or
from the gitlab npm registry in your build pipeline. Alternatively, you can use
the `es` or `umd` bundled js file in `dist` without any build system.

While most of the functionality is meant to be used in a browser, it should be
usable in nodejs, too. Currently, when using the SDK in nodejs, you will need to
provide a polyfill for the browser's `fetch` command for the api methods to
work.

## Features

| feature          | status      | description                                                             |
| ---------------- | ----------- | ----------------------------------------------------------------------- |
| Type Definitions | Done        | Provide type definitions for all API requests and responses.            |
| API Calls        | Done        | Convenience wrappers for API calls.                                     |
| Latex to Solver  | Done [1]    | Directly convert Latex format to Solver format.                         |
| Solver to Tree   | Done [2]    | Parse the Solver format returned from the solver into a tree structure. |
| Json to Tree     | Done [3]    | Parse the Json format returned from the solver into a tree structure.   |
| Tree to Latex    | Done        | Convert ExpressionTree format to Latex string.                          |
| Path Mappings    | Partial [4] | Helpers to work with the path mappings returned by the API.             |
| Explanations     |             | Populate and localize the explanations returned by the API.             |

[1] The parser is implemented in a very compact string=>string way. It can be
used to transform user input that is in LaTeX format into the solver format
that's used in API calls.

[2] We decided against using auto-generated ANTLR files based on the .g4 grammar
definition for parsing the solver format because of large package size (>
+350kB just for the parser). Instead, we use a more compact parser. Since the backend
can now return a much easier to parse Json format, this won't usually be needed.

[3] The Json format from the solver resembles the internal solver math tree structure,
and it is easy to parse.

[4] There is a `substituteTree` helper method that can substitute the expression of a
substep into the larger expression of the parent step.

## How to use

### Code Example

```ts
import { api, latexToSolver } from '@geogebra/solver-sdk';
// ...
const solverMathString = latexToSolver('\\frac{1}{2} + 3');
const solverResult = await api.selectPlans(solverMathString);
```

### Use with a build system

When using a build system like **vite** or **webpack** during development, you can
use the npm package directly in two ways:

**Case 1:** When actively developing the Solver SDK, clone the
[Solver SDK repository](https://git.geogebra.org/solver-team/solver-sdk) into a local
folder, then reference that folder in the main application as a dependency in
`package.json`. E.g., add the following line to the dependencies:

```json
"dependencies": {
    // ...
    "@geogebra/solver-sdk": "../../solver-sdk"
}
```

Then run `npm i`, and you should be able to use imports like
`import { api } from '@geogebra/solver-sdk'`. This should also automatically load
the typescript types in your editor.

**Case 2:** When you just want to use the Solver SDK without changing it, there
is no need to clone the repository into a local folder. Instead you can run
`npm i @geogebra/solver-sdk`. For this to work, you'll have to tell `npm` how to
fetch the package from gitlab and provide credentials to read it. To do that, put
an `.npmrc` file into your repo with the following contents:

```npmrc
@geogebra:registry=https://git.geogebra.org/api/v4/projects/126/packages/npm/
//git.geogebra.org/api/v4/projects/126/packages/npm/:_authToken=${GITLAB_NPM_TOKEN}
```

For the `GITLAB_NPM_TOKEN`, you can either generate and use your own access
token on [gitlab](https://git.geogebra.org/-/profile/personal_access_tokens), or
you can simply use the following project-scoped registry read-only token:
`glpat-H-s_F3ZRKMa3itgnS3Jd`.

### Use without a build system

To use without a build system, use the files that `npm run build:web` puts into
the dist folder. See `index.html` for an example. You can also get the `/dist`
files by downloading the published package from
[gitlab](https://git.geogebra.org/solver-team/solver-sdk/-/packages/).

## Development

This project currently requires at least Node v18. If you'd like you can use
a tool like [Volta](https://volta.sh) to automatically use the correct version
of Node.

The project is set up to use prettier and eslint for linting and code
formatting. In VSCode, the code will be automatically formatted on saving.

If you use Windows, then run `git config --get core.symlinks` and make sure it
outputs `true`. If it doesn't say `true`, fix it.

The design of this library should be modular to make tree-shaking or bundling
only parts of the library possible. The goal is to be as light-weight as
possible so we don't increase loading time for applications that use this SDK.

Scripts:

- Run `npm start` to host a demo page `index.html` for manual testing. in a
  browser environment
- `npm test` will run the test suite
- `npm run test-watch-full` is like `npm test` except also reruns it when a file changes.
- `npm run test-watch` is like `npm test-watch-full` except it skips code coverage
  reporting.
- `npm run build:esm` compiles the typescripts files into `js` and `.d.ts` files
  in the `lib/esm` folder, using modern javascript modules
- `npm run build:cjs` compiles the sources into `lib/cjs` in the old commonjs
  format for backwards compatibility with older nodejs setups
- `npm run build:web` uses **vite** to compile the sources into a packaged
  library (both in esm and umd format) to be directly included in, e.g., an html
  script tag
- `npm run semantic-release` will be called by GitLab's CI on the main branch to
  automatically increment the version number.
- `npm run prettier` runs the [Prettier](https://prettier.io/) code formatter.
  (This shouldn't be necessary, most of the time, if you are using VSCode and
  your prettier VSCode extension is working)

## Testing

We use `mocha` + `chai` for the tests, and `nyc` for coverage information. You
can add test cases to `./test/`. Any file with a `.test.ts` ending will be
automatically used when running `npm test`. Coverage information will be at
`./coverage/index.html`.

There are two debug launch configurations for VSCode: "Mocha Tests" and "Mocha
Tests Current File". These will work together with setting breakpoints in the
code.

## Deployment

To develop solver-sdk, we lightly recommend opening the folder that this readme is in
inside of vscode.

We use
[semantic-release](https://git.geogebra.org/help/ci/examples/semantic-release.md)
to automatically increase the version numbers and to publish this package to the
[npm registry of this
project](https://git.geogebra.org/solver-team/solver-sdk/-/packages/) on gitlab
when pushing to the `main` branch.

Commit messages with the following prefixes will cause increments in the version number:

- `fix:` or `fix(TOPIC):` will increment the patch field
- `feat:` or `feat(TOPIC):` will increment the minor field
- `perf:` or `perf(TOPIC):` with an additional `BREAKING CHANGE:` in the commit
  message will increment the major field.

See [semantic-release](https://github.com/semantic-release/semantic-release) for
details.

We use the following versioning strategy:

- feature branches (plut-???-\*): deploys package with pre-release version
  x.y.z-plut-???.1 and @plut-??? tag.
- main development branch (main): deploys package as x.y.z with @alpha tag
- staging (staging): deploys package as x.y.z with @beta tag
- production (release): deploys package as x.y.z with @latest tag

The CI will automatically adjust the SDK to point to the right API URL depending
on which branch it is on.

For feature branches, you should only trigger an SDK package deployment if
specifically needed by a frontend for development / testing. When merging into
`main`, the squashed commit message should contain a `fix:` or `feat:` message
if and only if the SDK was changed. Merging `main` into `staging` or `release`
will automatically trigger SDK releases if no squashing is used.

### Development with respect to Solver Poker

After making changes to this project, rebuild to update the bundled version of this
project that Poker in the solver-engine project uses. There are several commands that you
could use to do this

- `npm run build`
- `npm run build:web`
- `npm run build:web -- --watch`
