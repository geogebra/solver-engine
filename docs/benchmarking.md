# Benchmarking

In the `methods` module there is a `benchmarks` source set that contains benchmarks. For now it contains a single
class called `PlansBenchmarks`. To add new benchmarks just add a method to that class.

```shell
./gradlew benchmark
```

runs all the benchmarks (currently they are all in `methods`). As you run the benchmarks you see a summary of the
results on stdout. A `benchmarks.json` file is also created in a directory
called `$PROJECT_ROOT/methods/build/reports/benchmarks/main/<timestamp>`, so if you run benchmarks several times (and
do not clear the `build` directory by e.g. running `./gradlew clean`) you have a series of benchmarks results. This
can be useful to track the impact of code changes on performance, but it's not so easy to compare the data from the
different JSON files.

To help with this, there is a Python script at `$PROJECT_ROOT/scripts/benchmarks.py` (see [Profiling](profiling.md)
for how to set up Python). Just run it and it will display a list of all the benchmark results by benchmark. E.g. this
show all benchmark results

- from today
- maximum 10 results per benchmark (the most recent ones)
- filtering out benchmarks whose name do not contain the "Equation" pattern

```sh
python scripts/benchmarks.py --today --limit 10 --filter Equation
```
