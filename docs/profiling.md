# Profiling the engine

Using the profiler can be useful but it is not easy to collect data about which methods are used the most etc. There is
a custom way to collect and analyze this data, described in this document.

## Collect trace logs

Run the engine in development mode with log levels set to TRACE, i.e.

```
DEPLOYMENT_NAME=dev
LOG_LEVEL=trace
```

This will cause a log file in `$PROJECT_ROOT/logs/trace.log` to be filled with logs for each method call.

## Process the trace logs into a database and drop into a sqlite3 shell

There is a Python script that can do that at `$PROJECT_ROOT/scripts/process_trace_logs.py`. By default, it finds
the `trace.log` file and creates a sqlite database file next to it. To run it you need python 3.10+ and the `tabulate`
package. These dependencies are expressed in the `Pipfile` and `Pipfile.lock` checked into the project root, so you
should be able to set up the correct environment easily using `pipenv` (see https://pipenv.pypa.io).

Having `pipenv` set up, I get a virtualenv with the correct Python and dependencies by running from the project
root:

```
pipenv shell
```

Then I can run this command.

```
python scripts/process_trace_logs.py shell
```

This creates a database file at `$PROJECT_ROOT/logs/trace.log.db` and drops you into a sqlite3 shell (you need sqlite3
installed for the latter to work)

## Run some predifined queries

The script also allows running some predefined queries, for now there is one called `method_summary`. I ask for the
first 10 results, ordered (in descending order) by `cum_own_duration`. The available fields to order by are

- `call_count`: number of calls for this method;
- `total_own_time`: total time spent in calls for this method but not in a child method call;
- `total_time`: total time spent in calls for this method, including child method calls.

```
python scripts/process_trace_logs.py method_summary --limit 10 --order-by total_own_time                                                                                                                                                                                             solver-engine-N3UvXvGe  17:17:00

method                                            total_time    total_own_time    call_count
----------------------------------------------  ------------  ----------------  ------------
SimplifyEquation                                  1167239837         932803375            67
SimplifyPolynomialExpression                       605896794         350844847             9
ExpandPolynomialExpressionWithoutNormalization     651788917         301148590             8
FactorPolynomialInOneVariable                      504724791         267032791            14
SimplifyMonomial                                   227109438         176919117          1891
SimplifyInequation                                 197431749         129482386            12
SolveRationalEquation                             2603301250         117242409             7
ExpandSingleBracketWithIntegerCoefficient          158776958         107374106            12
SimplifyConstantExpression                         196360124         106222787             4
RewriteDivisionsAsFractions                         56298300          55200230          1157
```

We can see here that there are many calls to `SimplifyMonomial` and that they take a while.
