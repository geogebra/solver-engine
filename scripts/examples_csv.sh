#!/usr/bin/env bash

# Output a CSV of explanation keys to example inputs, generated from the tests

function abs_path {
  (cd "$(dirname '"$1"')" &>/dev/null && printf "%s/%s" "$PWD" "${1##*/}")
}

if [[ $# == 1 ]]; then
  path="$(abs_path "$1")"
elif [[ $SOLVER_EXAMPLES_FILE_PATH != "" ]]; then
  path="$(abs_path "$SOLVER_EXAMPLES_FILE_PATH")"
else
  >&2 echo Set SOLVER_EXAMPLES_FILE_PATH or path a path as an argument
fi

cd "$(dirname -- "${BASH_SOURCE[0]}")/.." || exit


./gradlew clean
SOLVER_EXAMPLES_FILE_PATH="$path" ./gradlew test
