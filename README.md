# Solver Engine

This project hosts the code for the solver engine.

## Overview of the structure

The solver engine project is split into modules:

- The `engine` module contains the internal representation of the
  expressions, the bits required to describe transformations on
  these, such as patterns and mapped expression, and also the
  framework for creating solutions (so-called rules and plans).
  [More about the engine](docs/engine.md).
- The `methods` module contains the concrete descriptions of the
  possible transformations. These methods are organized into categories
  and each category contains a number of rules and plans.
  [More about implementing methods](/docs/methods.md).
- The `api` module is how our system communicates with the outside
  world. It uses Spring Boot for the server and Openapi Generator
  to create the endpoints and DTOs from formal specification files.
  It also hosts the "Solver Poker" (the name coming from the iron
  rod as opposed to the card game), which is a static HTML page and
  some Javascript which connects to the API, and it is the quickest
  way to check your newly written solution.
- The `export` module is used only to upload the translation keys
  into GgbTrans. These are then translated and loaded by the poker.

## Development

### IntelliJ IDEA

There are two run configurations that can be used out of the box.

- `All Tests`: runs all the unit tests
- `API Server`: runs the API Server on local machine on port 8080

When the API server runs, you can inspect the API by navigating to http://localhost:8080
and access the _Solver Poker_ at http://localhost:8080/poker, but that will not work
until you follow the instructions in [the Typescript section of this
document](#Typescript)

### Commit hooks

There are commit hooks under revision control in the `.githooks/`
directory. To activate them in your repository you can do:

```shell
git config --local core.hooksPath .githooks/
```

Currently, there is a pre-commit hook that runs the `ktlintCheck`, `detekt`, and
Prettier tasks.

### Typescript

The project configures IntelliJ to use Prettier for formatting and .js, .html,
.md, .json, and .yaml files. It uses eslint for linting .js files.
[Volta](https://volta.sh/) is a superior way to install node.js, but using volta
isn't required. Make sure you have node.js installed and run `npm i` at the root
of this project, so that Prettier and eslint can work.

In IntelliJ, go to "Settings" > "Actions on Save" and disable "Reformat code". If you
don't do that, then IntelliJ will reformat your code using its own internal formatter,
which we don't want, because we are using prettier and ktlint instead.

To develop the poker, you can run

```
npm run poker-dev
```

Then go to http://localhost:4173/. Vite will watch the poker and solver-sdk code for
changes and automatically reflect those changes in the browser. The console output may
look funny because it is showing the output of both `tsc` and `vite` at the same time, but
that saves having to open two different terminals.

If you create a solver-poker/.env.local file that says `VITE_AUTO_SUBMISSION_MODE=true`,
then using Poker locally may be easier. See the code for details.

### Extra Help Testing

Looking at the test results for bigger tests can be difficult. To make it easier, we made
a fancy reporter for some of the bigger tests. To use it:

1. Follow the instructions in the [Typescript section of this document](#Typescript).
2. Run the kotlin tests. This can be done by either:
   - Running the `All Tests` run configuration in IntelliJ
   - Running `./gradlew methods:test --continuous` so that it will watch for changes and
     rerun only the the tests in the `methods` module, which is nice because tests outside
     that module can't use the fancy reporter (and even then it is only some of the tests
     in that module).
   - Running `./gradlew test` in the terminal
3. Run `npm run vite`
4. Go to http://localhost:4173/test-results.html

The page should automatically update when you rerun the tests. There have been issues with
that, however, requiring you to restart `npm run vite`. Please pay attention to that so
that we can figure out some steps to reproduce the issue of needing to restart to restart
`npm run vite`.

## Deployment

The project can be deployed to a Kubernetes cluster. The configured
one is run on AWS EKS and called `solver-v2`. It is powered by EKS managed
node groups.

The cluster uses the "AWS Load Balancer Controller" plugin, which
creates load balancer to route the traffic to the corresponding
namespaces.

For every git branch in the project a different namespace and hence
a different path-route on the same domain will be created.

The deployment is done in two separate stages that are defined in
the [pipeline configuration](.gitlab-ci.yml):

## Stage `publish`

The only job in this stage is `package`. It uses the Spring Boot
Gradle Plugin
to [generate a docker image](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#build-image)
and push it to the [project's container registry](https://git.geogebra.org/solver-team/solver-engine/container_registry)
.

It communicates with a docker engine through the gitlab-runner's
shared docker socket, see [doc](https://docs.gitlab.com/ee/ci/docker/using_docker_build.html#use-docker-socket-binding).
In order to choose the correct gitlab-runner we have to use the tag
`docker` in the pipeline config.

## Stage `deploy`

The first job in this stage is `deploy`. It will push the docker
image to the AWS EKS Kubernetes cluster and hence make it available
to the public. These are the actions it performs:

1. Connect to a Gitlab kubernetes agent with name `solver-v2`.
2. Install the package using `helm`.
3. Provide a path-base (branch) name in the load balancer url.

Example:
Branch name: plut-254-example
Solver URL: http://solver.geogebra.net/plut-254 (http://solver.geogebra.net/{branch-name})

- If it's a number after the first hyphen (-) just the '{string}-{number}' is put for the route-path.

Branch name: plut-string-example
Solver URL: http://solver.geogebra.net/plut-string-example (http://solver.geogebra.net/{branch-name})

- If it's a string after the first hyphen (-) the whole branch name is put for route-path.

It uses a [Gitlab cluster agent](https://docs.gitlab.com/ee/user/clusters/agent/install/)
to communicate to the cluster.

The second job in this stage is `undeploy`. It is automatically triggered when the feature branch is deleted so should
not need to be triggered manually.
