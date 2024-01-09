#!/usr/bin/env bash

#
# Copyright (c) 2024 GeoGebra GmbH, office@geogebra.org
#

# This script outputs to stdout the list of all image tags in the solver engine repository.  It can be used in
# conjunction with delete_image_tags.sh to help clean up unused tags.

# A private access token is required in env variable PAT.  Such a token can be obtained from
#    https://git.geogebra.org/solver-team/solver-engine/-/settings/access_tokens

GITLAB_URL="https://git.geogebra.org"
PROJECT_ID=102
REPO_ID=4

NEW_TAGS=$(mktemp -t new-tags)

if [[ $PAT == "" ]]; then
  >&2 echo "Env var PAT must be set with a valid private access token for gitlab with scopes read_api, read_registry"
  exit 1
fi

i=1 # For pagination
while true; do
  >&2 echo "--- Page $i"

  curl --silent \
       --header "PRIVATE-TOKEN: $PAT" \
      "$GITLAB_URL/api/v4/projects/$PROJECT_ID/registry/repositories/$REPO_ID/tags?per_page=100&page=${i}" |
    jq '.[].name' |
    sed 's:^.\(.*\).$:\1:' > "$NEW_TAGS"

  if [ -s "$NEW_TAGS" ]; then
    cat "$NEW_TAGS"
    i=$((i + 1))
  else
    break
  fi
done
