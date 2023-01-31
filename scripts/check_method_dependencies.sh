#!/usr/bin/env bash

WORK_DIR=$(mktemp -d)

# check if tmp dir was created
if [[ ! "$WORK_DIR" || ! -d "$WORK_DIR" ]]; then
  >&2 echo "Could not create temp dir"
  exit 1
fi

# deletes the temp directory
function cleanup {
  rm -rf "$WORK_DIR"
}

# register the cleanup function to be called on the EXIT signal
trap cleanup EXIT

DEPS=$WORK_DIR/deps
REV_DEPS=$WORK_DIR/rev_deps
CIRC_DEPS=$WORK_DIR/circ_deps

# Create a file of dependencies where each line is of the form:
# <importing methods package> <imported methods package>
grep -r '^import methods\.' methods |
  sed -n 's_methods/src/main/kotlin/methods/\([a-z]*\)/.*:import methods.\([a-z]*\).*_\1 \2_p' |
  grep -v '^\([a-z]*\) \1$' | # imports from the same package are allowed
  sort -u > "$DEPS"

# Create a file reversing the order of the dependencies
sed -n 's/\([a-z]*\) \([a-z]*\)/\2 \1/p' "$DEPS" | sort > "$REV_DEPS"

# The intersection of the files are the circular dependencies
comm -12 "$DEPS" "$REV_DEPS" > "$CIRC_DEPS"

# If there are circular dependencies, print them an return with an error
if [ -s "$CIRC_DEPS" ]; then
  >&2 echo "Circular dependencies:"
  >&2 cat "$CIRC_DEPS"
  exit 1
else
  exit 0
fi
