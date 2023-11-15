# README

This project is a simple backend that allows using the Solver API with latex or asciiMath
input instead of "Solver format". It is useful in situations where the client can't use
the Solver SDK.

Available endpoints:

- `GET /version`: returns version information
- `POST /select_plans`: same as the official /select_plans endpoint, but it allows passing
  a `latex` or `ascii` field instead of an `input` field
- `POST /ascii2latex`: turns an asciiMath expression into a latex expression

Example use:

```bash
curl http://3.80.44.86/ascii2latex -d '{ "ascii": "1/2" }' -H 'Content-Type: application/json'
{"latex":"\\frac{ 1 }{ 2 }"}
```

```bash
curl -s http://3.80.44.86/selectPlans -d '{ "ascii": "2/4" }' -H 'Content-Type: application/json' | jq | head
[
  {
    "transformation": {
      "path": ".",
      "fromExpr": "\\frac{2}{4}",
      "toExpr": "0.5",
      "pathMappings": [
        {
          "type": "Transform",
          "fromPaths": [
```

## Deployment

Currently, this backend is manually deployed on an EC2 server by Erik Weitnauer and is
reachable at the address <http://3.80.44.86>.
