import argparse
import csv
import json
import os
import sys
from urllib import request
from urllib.error import HTTPError

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.realpath(__file__)))
TRANSLATION_KEYS_PATH = os.path.join(PROJECT_ROOT, "methods/build/generated/ksp/main/resources/TranslationKeys.json")

with open(TRANSLATION_KEYS_PATH) as f:
    key2comment = {entry["key"]: entry.get("comment", "NO COMMENT") for entry in json.load(f)}


def merge_keys(key2comment, key2translation):
    all_keys = set(key2comment) | set(key2translation)
    for key in sorted(all_keys):
        yield key, key2comment.get(key, "REMOVED"), key2translation.get(key, "NO TRANSLATION")


def fail(msg):
    print(msg, file=sys.stderr)
    sys.exit(1)


# 1. Parse command line arguments
parser = argparse.ArgumentParser(
    description="Create a CSV document containing all current solver keys and their default translation",
)
parser.add_argument(
    '--api-token',
    default=os.getenv("GGBTRANS_API_TOKEN"),
    help='API token for ggbtrans (defaults to value of GGBTRANS_API_TOKEN)'
)
parser.add_argument(
    '--output',
    default=sys.stdout,
    type=argparse.FileType('w'),
    help='File to write CSV in (defaults to stdout)'
)
args = parser.parse_args()

if args.api_token is None:
    fail("An API token is required (either call with --api-token or set GGBTRANS_API_TOKEN")

# 2. Fetch data from ggbtrans
req = request.Request(
    "https://dev.geogebra.org/ggbtrans/props/api/solver_export",
    headers={"X-Token": args.api_token}
)
try:
    key2translation = json.load(request.urlopen(req))
except HTTPError as e:
    fail("Invalid API token" if e.code == 401 else e)

# 3. Output data in CSV format
writer = csv.writer(args.output)
writer.writerow(["Key", "Comment", "Translation"])
for row in merge_keys(key2comment, key2translation):
    writer.writerow(row)
