#!/usr/bin/env bash

#
# Copyright (c) 2024 GeoGebra GmbH, office@geogebra.org
#

# This script deletes docker image tags provided on stdout (one per line) from the solver-engine repository.  It can be
# used in conjunction with fetch_image_tags.sh to help clean up unused tags.

# A private access token is required in env variable PAT.  Such a token can be obtained from
#    https://git.geogebra.org/solver-team/solver-engine/-/settings/access_tokens

GITLAB_URL="https://git.geogebra.org"
PROJECT_ID=102
REPO_ID=4

if [[ $PAT == "" ]]; then
  >&2 echo "Env var PAT must be set with a valid private access token for gitlab with scopes api, write_registry"
  exit 1
fi

while read -r LINE || [[ -n $LINE ]]; do
  >&2 echo -n "Deleting tag ${LINE}... "
  >&2 curl --silent \
           --request DELETE \
           --header "PRIVATE-TOKEN: $PAT" \
           "$GITLAB_URL/api/v4/projects/$PROJECT_ID/registry/repositories/$REPO_ID/tags/${LINE}"
  >&2 echo
  sleep 0.1
done
