# Solver Engine

This project hosts the code for the solver engine.

## Overview of the structure

- [Internal representation of the expressions and solutions](docs/representation.md)
- [Rules and how to write them](docs/rule.md)
- [Plans](docs/plan.md)

## Development

### IntelliJ IDEA

There are two run configurations that can be used out of the box.

- `All Tests`: runs all the unit tests
- `API Server`: runs the API Server on local machine on port 8080

When the API server runs, you can inspect the API by navigating to
http://localhost:8080 and access the _Solver Poker_ at
http://localhost:8080/poker.html.

### Commit hooks

There are commit hooks under revision control in the `.githooks/`
directory. To activate them in your repository you can do:

```shell
git config --local core.hooksPath .githooks/
```

Currently, there is a pre-commit hook that runs the `ktlintCheck`
and `detekt` tasks.
