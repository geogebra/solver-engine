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

## Documentation

We use [Dokka](https://kotlinlang.org/docs/kotlin-doc.html) to generate documentation.

Simply run

```shell
./gradlew dokkaHtml
```

in command-line, which will generate html documentation in [documentation/html](documentation/html)

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

## Deployment

The project can be deployed to a Kubernetes cluster. The configured
one is run on AWS EKS and called `solver`. It is powered by Fargate
which needs to be controlled by so called "fargate profiles".

The cluster uses the "AWS Load Balancer Controller" plugin, which
creates load balancers to route the traffic to the corresponding
namespaces.

For every git branch in the project a different namespace and hence
a different route and url will be created.

The deployment is done in two separate stages that are defined in
the [pipeline configuration](.gitlab-ci.yml):

## Stage `publish`

The only job in this stage is `package`. It uses the Spring Boot 
Gradle Plugin to [generate a docker image](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#build-image)
and push it to the [project's container registry](https://git.geogebra.org/solver-team/solver-engine/container_registry).

It communicates with a docker engine through the gitlab-runner's
shared docker socket, see [doc](https://docs.gitlab.com/ee/ci/docker/using_docker_build.html#use-docker-socket-binding).
In order to choose the correct gitlab-runner we have to use the tag
`docker` in the pipeline config.

## Stage `deploy`

The first job in this stage is `deploy`. It will push the docker
image to the AWS EKS Kubernetes cluster and hence make it available
to the public. These are the actions it performs:

1. Connect to a Gitlab kubernetes agent with name `eks-solver`.
2. Make sure there is a fargate profile that supports the required 
   namespace.
3. Install the package using `helm`.
4. Provide a nice url using AWS Route53 service.

It uses a [Gitlab cluster agent](https://docs.gitlab.com/ee/user/clusters/agent/install/)
to communicate to the cluster.

The second job in this stage is `undeploy`. It can only be triggered
manually and will undo the above steps in reverse order.
