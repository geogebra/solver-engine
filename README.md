# Solver Engine

This project hosts the code for the solver engine. The GitHub repository is a read-only mirror of the repository
where development is done, which is a private gitlab repository. Currently, only the `main` branch is mirrored.

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

### Prerequisites

- You need either IntelliJ IDEA or Java 21 installed on your development machine.
- You need node.js and npm installed. You can use [Volta](https://volta.sh/) to manage them.
- Run `npm i` to install js dependencies.

### Development quick start

The most convenient way to develop is to use IntelliJ IDEA. Configuration files for it are included in the project,
so it should more or less work out of the box.

There are a number of run configurations that are included in the project.

- `All Tests`: runs all the unit tests
- `API Server`: runs the API Server on the local machine on port 8080. You need te rebuild for changes to take effect (
  this is because building the project all the time would consume a lot of resources)
- `Poker Dev`: runs the "poker" a small html client that is useful to try out the engine, on http://localhost:4173/.
  This is configured to reload automatically when the client code changes.

When the API server runs, you can inspect the API by navigating to http://localhost:8080
and access the _Solver Poker_ at http://localhost:8080/poker, but that will not work
until you follow the instructions in [the Typescript section of this
document](#Typescript)

Alternatively you can perform the same tasks without IntelliJ, using gradle or npm

- to run all tests, run `./gradlew test` at the root of the project
- to start the api server, run `./gradlew run` at the root of the project
- to start and/or develop the poker, run `npm run poker-dev` at the root of the project. Note that this requires npm to
  be installed (see the Typescript section)

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
