# Solver Engine

This project hosts the code for the solver engine.

## Overview of the structure

The solver engine project is split into modules:

- The `engine` module contains the internal representation of the
  expressions and the bits required to describe transformations on
  these, such as patterns and expression makers and also the
  transformation producers: rules and plans, the so-called methods.
    - More about the [internal representation](docs/representation.md).
    - The rule are the simplest method. It creates a transformation with
      an explanation and potentially with associated skills, but with no
      sub-steps. It transforms the input which matches its pattern into
      an equivalent expression created by its expression maker.
    - The plan is the top and mid-level method. It may have an explanation
      and skills, just like rules, but the actual work is done by one of
      the many possible [plan executors](docs/plan-executors.md).
- The `methods` module contains the concrete descriptions of the
  possible transformations. These methods are organized into categories
  and each category contains a number of rules and plans.
    - Each category has a `.category.yaml` file which contains the associated
      metadata, such as default translation for translation keys and
      description of public plans. The schema for the category files is
      available in [config/cathegorySchema.yaml](config/categorySchema.yaml).
    - A guide for adding new rules and plans is available [here](/docs/).

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
